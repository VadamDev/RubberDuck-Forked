package com.github.salvadormg15.rubber_duck.common;

import com.github.salvadormg15.rubber_duck.RubberDuck;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class Registries {
	private Registries() {}

	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, RubberDuck.MOD_ID);
	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, RubberDuck.MOD_ID);
	private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, RubberDuck.MOD_ID);

	public static void register(IEventBus bus) {
		ITEMS.register(bus);
		BLOCKS.register(bus);
		SOUND_EVENTS.register(bus);
	}

	/*
	   Blocks
	 */

	public static final DeferredHolder<Block, RubberDuckBlock> RUBBER_DUCK_BLOCK = BLOCKS.register("rubber_duck_block", () -> new RubberDuckBlock());

	/*
	   Items
	 */

	public static final DeferredHolder<Item, RubberDuckItem> RUBBER_DUCK_ITEM = ITEMS.register("rubber_duck_item",
			() -> new RubberDuckItem(RUBBER_DUCK_BLOCK.get(), new Item.Properties().stacksTo(4).rarity(Rarity.RARE)));

	/*
	   Sound Events
	 */

	public static final DeferredHolder<SoundEvent, SoundEvent> RUBBER_DUCK_USE = SOUND_EVENTS.register("rubber_duck_use",
			() -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(RubberDuck.MOD_ID, "rubber_duck_use")));
	public static final DeferredHolder<SoundEvent, SoundEvent> RUBBER_DUCK_PLACE = SOUND_EVENTS.register("rubber_duck_place",
			() -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(RubberDuck.MOD_ID, "rubber_duck_place")));
}
