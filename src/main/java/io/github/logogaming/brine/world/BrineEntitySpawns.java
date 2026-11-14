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

    }
}
