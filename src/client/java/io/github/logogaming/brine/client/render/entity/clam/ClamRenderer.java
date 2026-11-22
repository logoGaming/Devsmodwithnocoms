package io.github.logogaming.brine.client.render.entity.clam;

import io.github.logogaming.brine.Brine;
import io.github.logogaming.brine.client.render.entity.ModEntityModelLayers;
import io.github.logogaming.brine.entity.custom.ClamEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ClamRenderer extends MobRenderer<ClamEntity, ClamModel<ClamEntity>> {
    private static final ResourceLocation TEXTURE = Brine.id("textures/entity/clam.png");

    public ClamRenderer(EntityRendererProvider.Context context) {
        super(context, new ClamModel(context.bakeLayer(ModEntityModelLayers.CLAM)), 0.375f); // 0.375 shadow radius
    }

    @Override
    public ResourceLocation getTextureLocation(ClamEntity entity) {
        return TEXTURE;
    }
}
