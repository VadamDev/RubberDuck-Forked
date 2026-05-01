package com.github.salvadormg15.rubber_duck.common;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class RubberDuckItem extends BlockItem implements ICurioItem {
	public RubberDuckItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public boolean canEquip(ItemStack stack, EquipmentSlot armorType, LivingEntity entity) {
		return armorType.equals(EquipmentSlot.HEAD);
	}

	@Nullable
	@Override
	public EquipmentSlot getEquipmentSlot(ItemStack stack) {
		return EquipmentSlot.HEAD;
	}

	@Override
	public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
		return true;
	}
}
