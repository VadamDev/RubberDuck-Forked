package com.github.salvadormg15.rubber_duck.common;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class RubberDuckItem extends BlockItem implements ICurioItem {
	public RubberDuckItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) {
		return armorType.equals(EquipmentSlot.HEAD);
	}

	/*
	   Curios Stuff
	 */

	@Override
	public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
		return true;
	}

	@Override
	public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
		return new ICurio.SoundInfo(Registries.RUBBER_DUCK_PLACE.get(), 0.8f, 1.0f);
	}
}
