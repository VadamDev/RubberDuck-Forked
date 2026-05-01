package com.github.salvadormg15.rubber_duck.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class CommonConfig {
    private CommonConfig() {}

    public static final ModConfigSpec SPEC;

    // Entities
    private static final ModConfigSpec.DoubleValue ENTITY_SPAWN_WITH_CHANCE_SPEC, ENTITY_DROP_CHANCE_SPEC;
    private static final ModConfigSpec.BooleanValue SPAWN_ON_ZOMBIES, SPAWN_ON_SKELETONS;

    // Loot Chests
    private static final ModConfigSpec.BooleanValue SPAWN_IN_LOOT_CHESTS_SPEC;
    private static final ModConfigSpec.DoubleValue CHEST_LOOT_CHANCE_SPEC;

    static {
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("Spawn on mobs");
        ENTITY_SPAWN_WITH_CHANCE_SPEC = builder.comment("Chance of a rubber duck spawning on an entity's head")
                .defineInRange("SpawnWithChance", 0.02, 0.01, 1.0);
        ENTITY_DROP_CHANCE_SPEC = builder.comment("Chance of a rubber duck dropping when an entity wearing a rubber duck is killed")
                .defineInRange("DropChance", 1.0, 0.01, 1.0);
        SPAWN_ON_ZOMBIES = builder.comment("Can Zombies spawn with rubber ducks on their head?")
                .define("Zombies", true);
        SPAWN_ON_SKELETONS = builder.comment("Can Skeletons spawn with rubber ducks on their head?")
                .define("Skeletons", true);
        builder.pop();

        builder.push("Loot Chests");
        SPAWN_IN_LOOT_CHESTS_SPEC = builder.comment("Can rubber ducks be added to vanilla loot chests?")
                .define("Enabled", true);
        CHEST_LOOT_CHANCE_SPEC = builder.comment("The chance to loot a rubber duck inside vanilla loot chests")
                        .defineInRange("ChestLootChance", 0.05, 0.01, 1);
        builder.pop();

        SPEC = builder.build();
    }

    private static double spawnWithChance, dropChance;
    private static boolean spawnOnZombies, spawnOnSkeletons;

    private static boolean spawnInLootChests;
    private static double chestLootChance;

    public static void init() {
        spawnWithChance = ENTITY_SPAWN_WITH_CHANCE_SPEC.get();
        dropChance = ENTITY_DROP_CHANCE_SPEC.get();
        spawnOnZombies = SPAWN_ON_ZOMBIES.get();
        spawnOnSkeletons = SPAWN_ON_SKELETONS.get();

        spawnInLootChests = SPAWN_IN_LOOT_CHESTS_SPEC.get();
        chestLootChance = CHEST_LOOT_CHANCE_SPEC.get();
    }

    public static double getSpawnWithChance() {
        return spawnWithChance;
    }

    public static double getDropChance() {
        return dropChance;
    }

    public static boolean canSpawnOnZombies() {
        return spawnOnZombies;
    }

    public static boolean canSpawnOnSkeletons() {
        return spawnOnSkeletons;
    }

    public static boolean canSpawnInLootChests() {
        return spawnInLootChests;
    }

    public static float getChestLootChance() {
        return (float) chestLootChance;
    }
}
