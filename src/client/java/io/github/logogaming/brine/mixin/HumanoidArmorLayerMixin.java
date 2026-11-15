package io.github.logogaming.brine.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.logogaming.brine.item.BrineItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin<T extends LivingEntity, A extends HumanoidModel<T>> {

    @Inject(
            method = "renderArmorPiece",
            at = @At("HEAD"),
            cancellable = true
    )
    private void brine$renderCustomItemOnHead(
            PoseStack poseStack, MultiBufferSource bufferSource, T livingEntity, EquipmentSlot slot, int packedLight, A model, CallbackInfo ci
    ) {
        if (slot == EquipmentSlot.HEAD) {
            ItemStack stack = livingEntity.getItemBySlot(slot);

            if (stack.is(BrineItems.IRONDIVINGMASK)) {
                poseStack.pushPose();

                model.head.translateAndRotate(poseStack);

                poseStack.scale(0.67F, 0.67F, 0.67F);
                poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                poseStack.translate(0, model.head.y + (!livingEntity.isCrouching()?0.4:0.0),0);

                Minecraft.getInstance().getItemRenderer().renderStatic(
                        stack,
                        ItemDisplayContext.HEAD,
                        packedLight,
                        OverlayTexture.NO_OVERLAY,
                        poseStack,
                        bufferSource,
                        livingEntity.level(),
                        livingEntity.getId()
                );

                poseStack.popPose();

                ci.cancel();
            }
        }
    }
}