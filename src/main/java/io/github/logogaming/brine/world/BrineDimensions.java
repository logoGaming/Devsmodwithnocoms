package io.github.logogaming.brine.world;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class BrineDimensions {

    public static final ResourceKey<Level> BRINE = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath("brine", "brine")
    );
    public static final int SEA_LEVEL = 110;
    private BrineDimensions() {
    }
}
