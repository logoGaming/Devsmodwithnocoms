package io.github.logogaming.brine.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.logogaming.brine.Brine;
import io.github.logogaming.brine.item.BrineItems;
import io.github.logogaming.brine.item.FlashlightItem;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Shadow
    @Final
    private ItemModelShaper itemModelShaper;

    @Shadow
    public abstract ItemModelShaper getItemModelShaper();

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true)
    public BakedModel renderItem(BakedModel model, @Local(argsOnly = true) ItemStack stack, @Local(argsOnly = true) ItemDisplayContext renderMode) {
        if (stack.is(BrineItems.FLASHLIGHT) && (renderMode == ItemDisplayContext.GUI || renderMode == ItemDisplayContext.GROUND || renderMode == ItemDisplayContext.FIXED))
        {
            String modelName = FlashlightItem.isOn(stack) ? "flashlight_on" : "flashlight";
            return getItemModelShaper().getModelManager().getModel(ModelResourceLocation.inventory(ResourceLocation.fromNamespaceAndPath(Brine.MOD_ID, modelName)));
        }

        return model;
    }

    @ModifyVariable(method = "getModel", at = @At(value = "STORE"), ordinal = 1)
    public BakedModel brine$heldItem(BakedModel bakedModel, @Local(argsOnly = true) ItemStack stack) {
        if (stack.is(BrineItems.FLASHLIGHT)) {
            String modelName = FlashlightItem.isOn(stack) ? "flashlight_hand_on" : "flashlight_hand";
            return this.itemModelShaper.getModelManager().getModel(ModelResourceLocation.inventory(ResourceLocation.fromNamespaceAndPath(Brine.MOD_ID, modelName)));
        }

        return bakedModel;
    }
}
