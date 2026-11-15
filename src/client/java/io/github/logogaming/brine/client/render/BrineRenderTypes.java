package io.github.logogaming.brine.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.logogaming.brine.Brine;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiFunction;

import static net.minecraft.client.renderer.RenderStateShard.*;

public class BrineRenderTypes {
    private static final BiFunction<ResourceLocation, Boolean, RenderType> TITLE_SCREEN_PLAYER;

    public static final RenderStateShard.ShaderStateShard TITLE_SCREEN_PLAYER_SHADER = new RenderStateShard.ShaderStateShard(BrineRenderTypes::getTitleScreenPlayerShader);

    public static RenderType titleScreenPlayer(ResourceLocation location, boolean outline) {
        return TITLE_SCREEN_PLAYER.apply(location, outline);
    }

    public static RenderType titleScreenPlayer(ResourceLocation location) {
        return titleScreenPlayer(location, true);
    }

    static {
        TITLE_SCREEN_PLAYER = Util.memoize((resourceLocation, boolean_) -> {
            RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
                    .setShaderState(TITLE_SCREEN_PLAYER_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .createCompositeState(boolean_);
            return RenderType.create("title_screen_player", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, true, compositeState);
        });
    }

    private static ShaderInstance titleScreenPlayerShader;

    public static void registerShaders() {
        CoreShaderRegistrationCallback.EVENT.register(context -> {
            context.register(
                    ResourceLocation.fromNamespaceAndPath(Brine.MOD_ID, "title_screen_player"),
                    DefaultVertexFormat.NEW_ENTITY,
                    shaderInstance -> titleScreenPlayerShader = shaderInstance
            );
        });
    }

    public static ShaderInstance getTitleScreenPlayerShader() {
        return titleScreenPlayerShader;
    }
}
