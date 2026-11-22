package io.github.logogaming.brine.client;

import io.github.logogaming.brine.client.hud.BrineHud;
import io.github.logogaming.brine.client.render.BrineRenderTypes;
import io.github.logogaming.brine.client.render.entity.clam.ClamRenderer;
import io.github.logogaming.brine.client.render.entity.ModEntityModelLayers;
import io.github.logogaming.brine.client.render.item.BrineModelPredicates;
import io.github.logogaming.brine.client.shader.BrineShaders;
import io.github.logogaming.brine.entity.ModEntityTypes;
import io.github.logogaming.brine.item.BrineItems;
import io.github.logogaming.brine.logging.BrineLogger;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;

public class BrineClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BrineConfig.load();
        BrineCommands.register();
        BrineModelPredicates.registerModelPredicates();
        BrineShaders.register();
        BrineRenderTypes.registerShaders();
        BrineHud.register();
        BrineWaterRender.register();
        WaterSplash.register();
        ModEntityModelLayers.registerModelLayers();
        EntityRendererRegistry.register(ModEntityTypes.CLAM, ClamRenderer::new);
        BrineProperties.initialize();
        BrineLogger.info("Client Loading..");
    }
}
