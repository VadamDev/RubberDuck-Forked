package com.github.salvadormg15.rubber_duck.common;

import com.github.salvadormg15.rubber_duck.RubberDuck;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class Registries {
	private Registries() {}

	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RubberDuck.MOD_ID);
	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, RubberDuck.MOD_ID);
	private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, RubberDuck.MOD_ID);

	public static void register(IEventBus bus) {
		ITEMS.register(bus);
		BLOCKS.register(bus);
		SOUND_EVENTS.register(bus);
	}

	/*
	   Blocks
	 */

	public static final RegistryObject<RubberDuckBlock> RUBBER_DUCK_BLOCK = BLOCKS.register("rubber_duck_block",
			RubberDuckBlock::new);

	/*
	   Items
	 */

	public static final RegistryObject<RubberDuckItem> RUBBER_DUCK_ITEM = ITEMS.register("rubber_duck_item",
			() -> new RubberDuckItem(RUBBER_DUCK_BLOCK.get(), new Item.Properties().stacksTo(4).rarity(Rarity.RARE)));

	/*
	   Sound Events
	 */

	public static final RegistryObject<SoundEvent> RUBBER_DUCK_USE = SOUND_EVENTS.register("rubber_duck_use",
			() -> SoundEvent.createVariableRangeEvent(new ResourceLocation(RubberDuck.MOD_ID, "rubber_duck_use")));
	public static final RegistryObject<SoundEvent> RUBBER_DUCK_PLACE = SOUND_EVENTS.register("rubber_duck_place",
			() -> SoundEvent.createVariableRangeEvent(new ResourceLocation(RubberDuck.MOD_ID, "rubber_duck_place")));
}
