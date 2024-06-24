package com.github.salvadormg15.rubber_duck.common.events;

import com.github.salvadormg15.rubber_duck.RubberDuck;
import com.github.salvadormg15.rubber_duck.common.Registries;
import com.github.salvadormg15.rubber_duck.common.config.CommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class ForgeEventHandler {
    //Entity spawn with a duck
    @SubscribeEvent
    public static void onMobFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        final var entity = event.getEntity();
        if(!entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty())
            return;

        //Checks the Mod Config
        if((entity instanceof Zombie && CommonConfig.getSpawnsOnZombies()) || (entity instanceof Skeleton && CommonConfig.getSpawnsOnSkeletons())) {
            final var chance = entity.level().getRandom().nextDouble();

            if(chance <= CommonConfig.getEntitySpawnChance())
                entity.setItemSlot(EquipmentSlot.HEAD, Registries.RUBBER_DUCK_ITEM.get().getDefaultInstance());
        }
    }

    //Drop by entity probability
    @SubscribeEvent
    public static void onDeathSpecialEvent(LivingDropsEvent event) {
        // If it's not a zombie or skeleton returns
        final var entity = event.getEntity();
        if(!(entity instanceof Zombie || entity instanceof Skeleton))
            return;

        //Has a duck equipped?
        if(getRegistryName(entity.getItemBySlot(EquipmentSlot.HEAD).getItem()).getPath().equals(getRegistryName(Registries.RUBBER_DUCK_ITEM.get()).getPath())) {
            //Removes ducks from drops
            event.getDrops().removeIf(itemEntity -> getRegistryName(itemEntity.getItem().getItem()).getPath()
                    .equals(getRegistryName(Registries.RUBBER_DUCK_ITEM.get()).getPath()));

            //Drop randomized
            final var chance = entity.level().getRandom().nextDouble();
            if(chance <= CommonConfig.getDropChance()) {
                final var pos = entity.blockPosition();

                event.getDrops().add(new ItemEntity(entity.level(), pos.getX(), pos.getY(), pos.getZ(),
                        Registries.RUBBER_DUCK_ITEM.get().getDefaultInstance()));
            }
        }
    }

    //Spawn ducks in dungeon chests
    @SubscribeEvent
    public static void onLootLoad(LootTableLoadEvent event) {
        if(!CommonConfig.getChestLootEnabled() || !event.getName().toString().startsWith("minecraft:chests"))
            return;

        event.getTable().addPool(LootPool.lootPool()
                .add(LootTableReference.lootTableReference(new ResourceLocation(RubberDuck.MOD_ID, "chests/rubber_duck"))).build());
    }

    private static ResourceLocation getRegistryName(Item item) {
        return ForgeRegistries.ITEMS.getKey(item);
    }
}
