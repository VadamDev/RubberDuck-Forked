package com.github.salvadormg15.rubber_duck.common;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class RubberDuckBlock extends DiodeBlock implements SimpleWaterloggedBlock {
	public static final MapCodec<RubberDuckBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(propertiesCodec()).apply(instance, RubberDuckBlock::new)
	);

	public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final BooleanProperty UNLOCK = BooleanProperty.create("unlock");

	private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

	public RubberDuckBlock() {
		this(Properties.of().strength(0.2f, 0.2f).pushReaction(PushReaction.DESTROY));
	}

	public RubberDuckBlock(Properties properties) {
		super(properties);

		registerDefaultState(
				stateDefinition.any()
						.setValue(POWERED, false)
						.setValue(TRIGGERED, false)
						.setValue(WATERLOGGED, false)
						.setValue(UNLOCK, false)
						.setValue(FACING, Direction.NORTH)
		);

		final VoxelShape SHAPE = Stream.of(
				Block.box(4.6, 0, 5, 11.4, 5, 11),
				Block.box(6, 4, 3, 10, 8, 7),
				Block.box(6.4, 4, 1.4, 9.6, 5, 3)
		).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

		for (Direction direction : Direction.values())
			calculateShapes(direction, SHAPE);
	}

	@Override
	protected MapCodec<? extends DiodeBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(POWERED, TRIGGERED, WATERLOGGED, UNLOCK, FACING);
	}

	/*
	   Water logged
	 */

	@Override
	protected FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
		if(!state.canSurvive(level, pos))
			return Blocks.AIR.defaultBlockState();
		else {
			if(state.getValue(WATERLOGGED))
				level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));

			return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return super.getStateForPlacement(context)
				.setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType().equals(Fluids.WATER));
	}

	/*
	   Redstone Behavior
	 */

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if(!player.getMainHandItem().isEmpty())
			return InteractionResult.PASS;

		press(state, level, pos);
		level.playSound(player, pos, Registries.RUBBER_DUCK_USE.get(), SoundSource.BLOCKS, 1.3f, 1f);

		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	public void press(BlockState state, Level level, BlockPos pos) {
		level.setBlock(
			pos,
			state
					.setValue(POWERED, Boolean.valueOf(true))
					.setValue(TRIGGERED, Boolean.valueOf(true)),
				3
		);

		updateNeighbors(level, pos);
		level.scheduleTick(pos, this, 0);
	}

	protected void updateNeighbors(Level level, BlockPos pos) {
		for (Direction direction : Direction.values()) {
			BlockEvent.NeighborNotifyEvent notifyEvent = new BlockEvent.NeighborNotifyEvent(
					level,
					pos,
					level.getBlockState(pos),
					java.util.EnumSet.of(direction),
					false
			);

			if (NeoForge.EVENT_BUS.post(notifyEvent).isCanceled())
				return;

			BlockPos blockpos = pos.relative(direction);
			level.neighborChanged(blockpos, this, pos);
			level.updateNeighborsAtExceptFromFacing(blockpos, this, direction.getOpposite());
		}
	}

	@Override
	protected void updateNeighborsInFront(Level level, BlockPos pos, BlockState state) {
		updateNeighbors(level, pos);
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!this.isLocked(level, pos, state) || state.getValue(UNLOCK)) {
			boolean unlocked = state.getValue(UNLOCK);
			boolean powered = state.getValue(POWERED);
			boolean triggered = state.getValue(TRIGGERED);

			boolean setPowered = false;
			boolean setNewState = false;
			boolean setScheduleTick = false;

			TickPriority tickPriority = TickPriority.NORMAL;

			boolean playSound = false;

			int delay = getDelay(state);

			BlockState newState = state;

			if (unlocked) {
				setNewState = true;
				newState = newState.setValue(UNLOCK, Boolean.valueOf(false));
			}

			if (powered) {
				List<? extends Entity> list = level.getEntitiesOfClass(Arrow.class, state.getShape(level, pos).bounds().move(pos));
				boolean flag = !list.isEmpty();

				if (flag != powered) {
					setPowered = true;
					setNewState = true;
					newState = newState.setValue(POWERED, Boolean.valueOf(flag));
				}

				if (flag) {
					setScheduleTick = true;
					delay = this.getPressDuration();
				}
			}

			boolean flag1 = this.shouldTurnOn(level, pos, state);

			if (!setPowered) {
				if (!powered) {
					if (triggered && !flag1) {
						setNewState = true;
						newState = newState.setValue(TRIGGERED, Boolean.valueOf(false));
					} else if (!triggered) {
						playSound = true;
						setNewState = true;
						newState = newState.setValue(TRIGGERED, Boolean.valueOf(true));

						if (!flag1) {
							setScheduleTick = true;
							tickPriority = TickPriority.VERY_HIGH;
						}
					}
				}
			} else
				setScheduleTick = true;

			if (playSound)
				level.playSound(null, pos, Registries.RUBBER_DUCK_USE.get(), SoundSource.BLOCKS, 1.3f, 1f);

			if (setNewState)
				level.setBlock(pos, newState, setPowered ? 3 : 2);

			if (setScheduleTick)
				level.scheduleTick(new BlockPos(pos), this, delay, tickPriority);
		}else {
			level.setBlock(pos, state.setValue(UNLOCK, Boolean.valueOf(true)), 2);
			level.scheduleTick(new BlockPos(pos), this, this.getPressDuration());
		}
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
		if(!level.isClientSide && !state.getValue(POWERED))
			checkPressed(state, level, pos);
	}

	private void checkPressed(BlockState state, Level level, BlockPos pos) {
		List<? extends Entity> list = level.getEntitiesOfClass(Arrow.class, state.getShape(level, pos).bounds().move(pos));
		boolean flag = !list.isEmpty();
		boolean flag1 = state.getValue(POWERED);
		BlockState newState = state;

		if (flag != flag1) {
			if (flag) {
				level.playSound(null, pos, Registries.RUBBER_DUCK_USE.get(), SoundSource.BLOCKS, 1.3f, 1f);
				newState = newState.setValue(TRIGGERED, Boolean.valueOf(true));
			}

			newState = newState.setValue(POWERED, Boolean.valueOf(flag));
			level.setBlock(pos, newState, 3);
		}

		if (flag)
			level.scheduleTick(new BlockPos(pos), this, getPressDuration());
	}

	private int getPressDuration() {
		return 20;
	}

	@Override
	public boolean isLocked(LevelReader level, BlockPos pos, BlockState state) {
		return state.getValue(POWERED);
	}

	@Override
	protected boolean shouldTurnOn(Level level, BlockPos pos, BlockState state) {
		return level.hasNeighborSignal(pos);
	}

	@Override
	protected int getDirectSignal(BlockState state, BlockGetter blockAccess, BlockPos pos, Direction side) {
		return state.getValue(POWERED) && Direction.UP == side ? 15 : 0;
	}

	@Override
	protected int getSignal(BlockState state, BlockGetter blockAccess, BlockPos pos, Direction side) {
		return state.getValue(POWERED) ? 15 : 0;
	}

	@Override
	protected int getDelay(BlockState state) {
		return 2;
	}

	/*
	   Block behavior
	 */

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (oldState.is(Blocks.AIR))
			level.playSound(null, pos, Registries.RUBBER_DUCK_PLACE.get(), SoundSource.BLOCKS, 0.8f, 1f);

		super.onPlace(state, level, pos, oldState, isMoving);
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if(newState.is(Blocks.AIR))
			level.playSound(null, pos, Registries.RUBBER_DUCK_PLACE.get(), SoundSource.BLOCKS, 0.8f, 1.2f);

		super.onRemove(state, level, pos, newState, isMoving);
	}

	/*
	   VoxelShape
	 */

	@NotNull
	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		final VoxelShape voxel = SHAPES.get(state.getValue(FACING));
		return voxel != null ? voxel : Shapes.block(); // Returns a full block if the voxelShape has an error
	}

	protected static void calculateShapes(Direction to, VoxelShape shape) {
		final VoxelShape[] buffer = new VoxelShape[] { shape, Shapes.empty() };

		int times = (to.get2DDataValue() - Direction.NORTH.get2DDataValue() + 4) % 4;
		for (int i = 0; i < times; i++) {
			buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = Shapes.or(buffer[1], Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
			buffer[0] = buffer[1];
			buffer[1] = Shapes.empty();
		}

		SHAPES.put(to, buffer[0]);
	}
}