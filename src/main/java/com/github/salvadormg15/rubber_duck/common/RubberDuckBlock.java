package com.github.salvadormg15.rubber_duck.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class RubberDuckBlock extends DiodeBlock implements SimpleWaterloggedBlock {
	public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final BooleanProperty UNLOCK = BooleanProperty.create("unlock");

	public RubberDuckBlock() {
		super(Properties.of().strength(0.2f, 0.2f).pushReaction(PushReaction.DESTROY));

		registerDefaultState(
				stateDefinition.any()
						.setValue(POWERED, false)
						.setValue(TRIGGERED, false)
						.setValue(WATERLOGGED, false)
						.setValue(UNLOCK, false)
						.setValue(FACING, Direction.NORTH)
		);

		runCalculations(SHAPE);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(POWERED, TRIGGERED, WATERLOGGED, UNLOCK, FACING);
	}

	/*
	   Water logged
	 */

	@Override
	public FluidState getFluidState(BlockState p_56131_) {
		return p_56131_.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(p_56131_);
	}

	@Override
	public BlockState updateShape(BlockState p_56113_, Direction p_56114_, BlockState p_56115_, LevelAccessor p_56116_, BlockPos p_56117_, BlockPos p_56118_) {
		if(!p_56113_.canSurvive(p_56116_, p_56117_))
			return Blocks.AIR.defaultBlockState();
		else {
			if(p_56113_.getValue(WATERLOGGED))
				p_56116_.scheduleTick(p_56117_, Fluids.WATER, Fluids.WATER.getTickDelay(p_56116_));

			return super.updateShape(p_56113_, p_56114_, p_56115_, p_56116_, p_56117_, p_56118_);
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext p_52501_) {
		return super.getStateForPlacement(p_52501_)
				.setValue(WATERLOGGED, p_52501_.getLevel().getFluidState(p_52501_.getClickedPos()).getType().equals(Fluids.WATER));
	}

	/*
	   Redstone Behavior
	 */

	@Override
	public InteractionResult use(BlockState p_225533_1_, Level p_225533_2_, BlockPos p_225533_3_, Player p_225533_4_, InteractionHand p_225533_5_, BlockHitResult p_225533_6_) {
		this.press(p_225533_1_, p_225533_2_, p_225533_3_);
		p_225533_2_.playSound(p_225533_4_, p_225533_3_, Registries.RUBBER_DUCK_USE.get(), SoundSource.BLOCKS, 1.3f, 1f);
		return InteractionResult.sidedSuccess(p_225533_2_.isClientSide);
	}

	public void press(BlockState p_226910_1_, Level p_226910_2_, BlockPos p_226910_3_) {
		p_226910_2_.setBlock(
			p_226910_3_,
			p_226910_1_
				.setValue(POWERED, Boolean.valueOf(true))
				.setValue(TRIGGERED, Boolean.valueOf(true)),
			3);
		this.updateNeighbors(p_226910_2_, p_226910_3_, p_226910_1_);
		p_226910_2_.scheduleTick(p_226910_3_, this, 0);
	}

	protected void updateNeighbors(Level p_176400_1_, BlockPos p_176400_2_, BlockState p_176400_3_) {
		for (Direction direction : Direction.values()) {
			if (net.minecraftforge.event.ForgeEventFactory.onNeighborNotify(p_176400_1_, p_176400_2_, p_176400_1_.getBlockState(p_176400_2_), java.util.EnumSet.of(direction), false).isCanceled())
				return;
			BlockPos blockpos = p_176400_2_.relative(direction);
			p_176400_1_.neighborChanged(blockpos, this, p_176400_2_);
			p_176400_1_.updateNeighborsAtExceptFromFacing(blockpos, this, direction.getOpposite());
		}
	}

	@Override
	protected void updateNeighborsInFront(Level p_176400_1_, BlockPos p_176400_2_, BlockState p_176400_3_) {
		this.updateNeighbors(p_176400_1_, p_176400_2_, p_176400_3_);
	}

	@Override
	public void tick(BlockState p_225534_1_, ServerLevel p_225534_2_, BlockPos p_225534_3_, RandomSource p_221068_) {
		if (!this.isLocked(p_225534_2_, p_225534_3_, p_225534_1_) || p_225534_1_.getValue(UNLOCK)) {
			boolean unlocked = p_225534_1_.getValue(UNLOCK);
			boolean powered = p_225534_1_.getValue(POWERED);
			boolean triggered = p_225534_1_.getValue(TRIGGERED);

			boolean setPowered = false;
			boolean setNewState = false;
			boolean setScheduleTick = false;

			TickPriority tickPriority = TickPriority.NORMAL;

			boolean playSound = false;

			int delay = getDelay(p_225534_1_);

			BlockState newState = p_225534_1_;

			if (unlocked) {
				setNewState = true;
				newState = newState.setValue(UNLOCK, Boolean.valueOf(false));
			}

			if (powered) {
				List<? extends Entity> list = p_225534_2_.getEntitiesOfClass(Arrow.class, p_225534_1_.getShape(p_225534_2_, p_225534_3_).bounds().move(p_225534_3_));
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

			boolean flag1 = this.shouldTurnOn(p_225534_2_, p_225534_3_, p_225534_1_);

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
			} else {
				setScheduleTick = true;
			}

			if (playSound) {
				p_225534_2_.playSound(null, p_225534_3_, Registries.RUBBER_DUCK_USE.get(), SoundSource.BLOCKS, 1.3f, 1f);
			}

			if (setNewState) {
				p_225534_2_.setBlock(p_225534_3_, newState, setPowered ? 3 : 2);
			}

			if (setScheduleTick) {
				p_225534_2_.scheduleTick(new BlockPos(p_225534_3_), this, delay, tickPriority);
			}
		} else {
			p_225534_2_.setBlock(p_225534_3_, p_225534_1_.setValue(UNLOCK, Boolean.valueOf(true)), 2);
			p_225534_2_.scheduleTick(new BlockPos(p_225534_3_), this, this.getPressDuration());
		}
	}

	@Override
	public void entityInside(BlockState p_196262_1_, Level p_196262_2_, BlockPos p_196262_3_, Entity p_196262_4_) {
		if (!p_196262_2_.isClientSide && !p_196262_1_.getValue(POWERED)) {
			this.checkPressed(p_196262_1_, p_196262_2_, p_196262_3_);
		}
	}

	private void checkPressed(BlockState p_185616_1_, Level p_185616_2_, BlockPos p_185616_3_) {
		List<? extends Entity> list = p_185616_2_.getEntitiesOfClass(Arrow.class, p_185616_1_.getShape(p_185616_2_, p_185616_3_).bounds().move(p_185616_3_));
		boolean flag = !list.isEmpty();
		boolean flag1 = p_185616_1_.getValue(POWERED);
		BlockState newState = p_185616_1_;

		if (flag != flag1) {
			if (flag) {
				p_185616_2_.playSound(null, p_185616_3_, Registries.RUBBER_DUCK_USE.get(), SoundSource.BLOCKS, 1.3f, 1f);
				newState = newState.setValue(TRIGGERED, Boolean.valueOf(true));
			}

			newState = newState.setValue(POWERED, Boolean.valueOf(flag));
			p_185616_2_.setBlock(p_185616_3_, newState, 3);
		}

		if (flag) {
			p_185616_2_.scheduleTick(new BlockPos(p_185616_3_), this, this.getPressDuration());
		}

	}

	private int getPressDuration() {
		return 20;
	}

	@Override
	public boolean isLocked(LevelReader p_176405_1_, BlockPos p_176405_2_, BlockState p_176405_3_) {
		return p_176405_3_.getValue(POWERED);
	}

	@Override
	protected void checkTickOnNeighbor(Level p_176398_1_, BlockPos p_176398_2_, BlockState p_176398_3_) {
		if (!this.isLocked(p_176398_1_, p_176398_2_, p_176398_3_)) {
			boolean flag = p_176398_3_.getValue(TRIGGERED);
			boolean flag1 = this.shouldTurnOn(p_176398_1_, p_176398_2_, p_176398_3_);
			if (flag != flag1 && !p_176398_1_.getBlockTicks().willTickThisTick(p_176398_2_, this)) {
				TickPriority tickpriority = TickPriority.HIGH;
				if (this.shouldPrioritize(p_176398_1_, p_176398_2_, p_176398_3_)) {
					tickpriority = TickPriority.EXTREMELY_HIGH;
				} else if (flag) {
					tickpriority = TickPriority.VERY_HIGH;
				}

				p_176398_1_.scheduleTick(p_176398_2_, this, this.getDelay(p_176398_3_), tickpriority);
			}
		}
	}

	@Override
	protected boolean shouldTurnOn(Level p_176404_1_, BlockPos p_176404_2_, BlockState p_176404_3_) {
		return p_176404_1_.hasNeighborSignal(p_176404_2_);
	}

	@Override
	public int getDirectSignal(BlockState p_176211_1_, BlockGetter p_176211_2_, BlockPos p_176211_3_, Direction p_176211_4_) {
		return p_176211_1_.getValue(POWERED) && Direction.UP == p_176211_4_ ? 15 : 0;
	}

	@Override
	public int getSignal(BlockState p_180656_1_, BlockGetter p_180656_2_, BlockPos p_180656_3_, Direction p_180656_4_) {
		return p_180656_1_.getValue(POWERED) ? 15 : 0;
	}

	@Override
	protected int getDelay(BlockState p_196346_1_) {
		return 2;
	}

//	Block behavior
	@Override
	public void onPlace(BlockState p_220082_1_, Level p_220082_2_, BlockPos p_220082_3_, BlockState p_220082_4_, boolean p_220082_5_) {
		if (p_220082_4_.is(Blocks.AIR)) {
			p_220082_2_.playSound(null, p_220082_3_, Registries.RUBBER_DUCK_PLACE.get(), SoundSource.BLOCKS, 0.8f, 1f);
		}
		super.onPlace(p_220082_1_, p_220082_2_, p_220082_3_, p_220082_4_, p_220082_5_);
	}

	@Override
	public void onRemove(BlockState p_196243_1_, Level p_196243_2_, BlockPos p_196243_3_, BlockState p_196243_4_, boolean p_196243_5_) {
		if (p_196243_4_.is(Blocks.AIR)) {
			p_196243_2_.playSound(null, p_196243_3_, Registries.RUBBER_DUCK_PLACE.get(), SoundSource.BLOCKS, 0.8f, 1.2f);
		}
		super.onRemove(p_196243_1_, p_196243_2_, p_196243_3_, p_196243_4_, p_196243_5_);
	}

//	Voxelshape stuff
	VoxelShape SHAPE = Stream.of(
			Block.box(4.6, 0, 5, 11.4, 5, 11),
			Block.box(6, 4, 3, 10, 8, 7),
			Block.box(6.4, 4, 1.4, 9.6, 5, 3)
	).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
	private static Map<Direction, VoxelShape> SHAPES = new HashMap<Direction, VoxelShape>();

	protected void runCalculations(VoxelShape shape) {
		for (Direction direction : Direction.values()) {
			calculateShapes(direction, shape);
		}
	}
	@Override
	public VoxelShape getShape(BlockState p_220053_1_, BlockGetter p_220053_2_, BlockPos p_220053_3_,
							   CollisionContext p_220053_4_) {
		VoxelShape voxel = SHAPES.get(p_220053_1_.getValue(FACING));
		return voxel != null ? voxel : Shapes.block();	//Returns a full block if the voxelShape has an error
	}

	protected static void calculateShapes(Direction to, VoxelShape shape) {
		VoxelShape[] buffer = new VoxelShape[] { shape, Shapes.empty() };

		int times = (to.get2DDataValue() - Direction.NORTH.get2DDataValue() + 4) % 4;
		for (int i = 0; i < times; i++) {
			buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = Shapes.or(buffer[1],
					Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
			buffer[0] = buffer[1];
			buffer[1] = Shapes.empty();
		}
		SHAPES.put(to, buffer[0]);
	}
}