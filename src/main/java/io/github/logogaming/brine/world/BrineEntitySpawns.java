package io.github.logogaming.brine.world;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;

public class BrineEntitySpawns
{
    private static final ResourceKey<Biome> REEF = ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("brine", "reef"));

    public static void addEntitySpawns()
    {
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(REEF), MobCategory.CREATURE, EntityType.GLOW_SQUID, 25, 1, 10);
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(REEF), MobCategory.CREATURE, EntityType.DROWNED, 10, 1, 3);
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(REEF), MobCategory.CREATURE, EntityType.GUARDIAN, 5, 1, 1);
    }
}
