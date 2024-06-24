package com.github.salvadormg15.rubber_duck.common.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class CommonConfig {
    private CommonConfig() {}

    public static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.DoubleValue ENTITY_SPAWN_CHANCE_SPEC, ENTITY_DROP_CHANCE_SPEC;
    private static final ForgeConfigSpec.BooleanValue SPAWNABLE_ON_ZOMBIES_SPEC, SPAWNABLE_ON_SKELETONS_SPEC, CHEST_LOOT_ENABLED_SPEC;

    static {
        final var builder = new ForgeConfigSpec.Builder();

        ENTITY_SPAWN_CHANCE_SPEC = builder.comment("Spawn chance on mobs")
                .defineInRange("EntitySpawnChance", 0.02, 0.0, 1);
        ENTITY_DROP_CHANCE_SPEC = builder.comment("Drop chance when mob is killed")
                .defineInRange("DropChance", 1, 0.0, 1);

        builder.push("Spawns on this mob");
        SPAWNABLE_ON_ZOMBIES_SPEC = builder.define("Zombies", true);
        SPAWNABLE_ON_SKELETONS_SPEC = builder.define("Skeletons", true);
        builder.pop();

        CHEST_LOOT_ENABLED_SPEC = builder.comment("Loot added to Vanilla chests")
                .define("ChestLootEnabled", true);
        
        SPEC = builder.build();
    }

    private static double ENTITY_SPAWN_CHANCE;
    private static double DROP_CHANCE;
    private static boolean SPAWNS_ON_ZOMBIES;
    private static boolean SPAWNS_ON_SKELETONS;
    private static boolean CHEST_LOOT_ENABLED;

    public static void init() {
        ENTITY_SPAWN_CHANCE = CommonConfig.ENTITY_SPAWN_CHANCE_SPEC.get();
        DROP_CHANCE = CommonConfig.ENTITY_DROP_CHANCE_SPEC.get();
        SPAWNS_ON_ZOMBIES = CommonConfig.SPAWNABLE_ON_ZOMBIES_SPEC.get();
        SPAWNS_ON_SKELETONS = CommonConfig.SPAWNABLE_ON_SKELETONS_SPEC.get();
        CHEST_LOOT_ENABLED = CommonConfig.CHEST_LOOT_ENABLED_SPEC.get();
    }

    public static Double getEntitySpawnChance() {
        return ENTITY_SPAWN_CHANCE;
    }

    public static Double getDropChance() {
        return DROP_CHANCE;
    }

    public static Boolean getSpawnsOnZombies() {
        return SPAWNS_ON_ZOMBIES;
    }

    public static Boolean getSpawnsOnSkeletons() {
        return SPAWNS_ON_SKELETONS;
    }

    public static Boolean getChestLootEnabled() {
        return CHEST_LOOT_ENABLED;
    }
}
