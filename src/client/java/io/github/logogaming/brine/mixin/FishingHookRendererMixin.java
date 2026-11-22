package io.github.logogaming.brine.mixin;

import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FishingHookRenderer.class)
public class FishingHookRendererMixin {
    @Redirect(
            method = "getPlayerHandPos",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
    )
    private boolean brine$treatCustomRodsAsRods(ItemStack stack, Item item) {
        return stack.getItem() instanceof FishingRodItem || stack.is(item);
    }
}
