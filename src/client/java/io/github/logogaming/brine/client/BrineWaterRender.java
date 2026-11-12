package io.github.logogaming.brine.client;

import io.github.logogaming.brine.world.BrineDimensions;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public final class BrineWaterRender implements FluidRenderHandler {

    private static final ResourceLocation STILL = ResourceLocation.fromNamespaceAndPath("brine", "block/water_still");
    private static final int WHITE = 0xFFFFFF;

    private final FluidRenderHandler vanilla;

    private BrineWaterRender(FluidRenderHandler vanilla) {
        this.vanilla = vanilla;
    }

    public static void register() {
        FluidRenderHandler vanilla = FluidRenderHandlerRegistry.INSTANCE.get(Fluids.WATER);
        FluidRenderHandlerRegistry.INSTANCE.register(Fluids.WATER, Fluids.FLOWING_WATER, new BrineWaterRender(vanilla));
    }

    private static boolean inBrine() {
        Minecraft client = Minecraft.getInstance();
        return client.level != null && client.level.dimension() == BrineDimensions.BRINE;
    }

    @Override
    public TextureAtlasSprite[] getFluidSprites(BlockAndTintGetter view, BlockPos pos, FluidState state) {
        TextureAtlasSprite[] original = vanilla.getFluidSprites(view, pos, state);
        if (!inBrine()) {
            return original;
        }
        TextureAtlasSprite custom = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(STILL);
        TextureAtlasSprite overlay = original.length > 2 ? original[2] : custom;
        return new TextureAtlasSprite[]{custom, custom, overlay};
    }

    @Override
    public int getFluidColor(BlockAndTintGetter view, BlockPos pos, FluidState state) {
        return inBrine() ? WHITE : vanilla.getFluidColor(view, pos, state);
    }
}
