package io.github.logogaming.brine.client.render.entity;

import io.github.logogaming.brine.Brine;
import io.github.logogaming.brine.client.render.entity.clam.ClamModel;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;

public class ModEntityModelLayers {
    public static final ModelLayerLocation CLAM = createMain("clam");

    private static ModelLayerLocation createMain(String name) {
        return new ModelLayerLocation(Brine.id(name), "main");
    }

    public static void registerModelLayers() {
        EntityModelLayerRegistry.registerModelLayer(ModEntityModelLayers.CLAM, ClamModel::createBodyLayer);
    }
}