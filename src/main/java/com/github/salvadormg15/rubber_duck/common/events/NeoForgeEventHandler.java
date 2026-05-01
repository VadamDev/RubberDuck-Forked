package com.github.salvadormg15.rubber_duck.common.events;

import com.github.salvadormg15.rubber_duck.common.Registries;
import com.github.salvadormg15.rubber_duck.common.config.CommonConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

public class NeoForgeEventHandler {
    // Entity spawn with a duck

    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        final Mob entity = event.getEntity();
        if(!entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty())
            return;

        // Checks the Mod Config
        if((entity instanceof Zombie && CommonConfig.canSpawnOnZombies()) || (entity instanceof Skeleton && CommonConfig.canSpawnOnSkeletons())) {
            if(entity.getRandom().nextDouble() > CommonConfig.getSpawnWithChance())
                return;

            entity.setItemSlot(EquipmentSlot.HEAD, Registries.RUBBER_DUCK_ITEM.get().getDefaultInstance());
        }
    }

    // Drop by entity probability

    @SubscribeEvent
    public static void onLivingDropsEvent(LivingDropsEvent event) {
        // If it's not a zombie or skeleton returns
        final LivingEntity entity = event.getEntity();
        if(!(entity instanceof Zombie || entity instanceof Skeleton))
            return;

        // Has a duck equipped?
        if(entity.getItemBySlot(EquipmentSlot.HEAD).is(Registries.RUBBER_DUCK_ITEM.get())) {
            // Removes ducks from drops | I highly suspect as this being useless, I've never seen a rubberduck being dropped naturally without the code below
            event.getDrops().removeIf(itemEntity -> itemEntity.getItem().is(Registries.RUBBER_DUCK_ITEM.get()));

            // Drop randomized
            boolean shouldDrop;
            final double dropChance = CommonConfig.getDropChance();
            if(dropChance == 1)
                shouldDrop = true;
            else
                shouldDrop = entity.getRandom().nextDouble() <= dropChance;

            if(shouldDrop) {
                final Vec3 pos = entity.getPosition(0);

                final ItemEntity item = new ItemEntity(entity.level(), pos.x(), pos.y(), pos.z(), Registries.RUBBER_DUCK_ITEM.get().getDefaultInstance());
                event.getDrops().add(item);
            }
        }
    }

    @SubscribeEvent
    public static void onLootTableLoadEvent(LootTableLoadEvent event) {
        if(!CommonConfig.canSpawnInLootChests())
            return;

        final ResourceLocation id = event.getName();
        if(id.getNamespace().equals("minecraft") && id.getPath().startsWith("chests/")) {
            event.getTable().addPool(LootPool.lootPool()
                    .when(LootItemRandomChanceCondition.randomChance(CommonConfig.getChestLootChance()))
                    .add(LootItem.lootTableItem(Registries.RUBBER_DUCK_ITEM.get()))
                    .build()
            );
        }
    }
}
