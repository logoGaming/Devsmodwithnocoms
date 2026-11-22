package io.github.logogaming.brine.world;

import io.github.logogaming.brine.Brine;
import io.github.logogaming.brine.entity.ModEntityTypes;
import io.github.logogaming.brine.entity.custom.ClamEntity;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;

public class BrineEntitySpawns {
    private static final ResourceKey<Biome> REEF = ResourceKey.create(Registries.BIOME, Brine.id("reef"));

    public static void addEntitySpawns() {
        BiomeModifications.addSpawn(
            BiomeSelectors.includeByKey(REEF),
            MobCategory.WATER_CREATURE,
            EntityType.SQUID,
            100,
            2,
            4
        );
        BiomeModifications.addSpawn(
            BiomeSelectors.includeByKey(REEF),
            MobCategory.CREATURE,
            EntityType.DROWNED,
            100,
            2,
            4
        );
        BiomeModifications.addSpawn(
            BiomeSelectors.includeByKey(REEF),
            MobCategory.CREATURE,
            EntityType.TROPICAL_FISH,
            25,
            4,
            8
        );
        BiomeModifications.addSpawn(
            BiomeSelectors.includeByKey(REEF),
            MobCategory.CREATURE,
            EntityType.COD,
            25,
            4,
            8
        );
        BiomeModifications.addSpawn(
            BiomeSelectors.includeByKey(REEF),
            MobCategory.CREATURE,
            EntityType.SALMON,
            25,
            4,
            8
        );
        BiomeModifications.addSpawn(
            BiomeSelectors.includeByKey(REEF),
            MobCategory.CREATURE,
            EntityType.GUARDIAN,
            2,
            1,
            2
        );
        BiomeModifications.addSpawn(
            BiomeSelectors.includeByKey(REEF),
            MobCategory.CREATURE,
            EntityType.ELDER_GUARDIAN,
            1,
            1,
            1
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.includeByKey(REEF),
                MobCategory.WATER_CREATURE,
                ModEntityTypes.CLAM,
                10,
                1,
                5
        );
        SpawnPlacements.register(
                ModEntityTypes.CLAM,
                SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.OCEAN_FLOOR,
                ClamEntity::checkClamSpawnRules
        );
    }
}
