package io.github.logogaming.brine.client.render.item;

import io.github.logogaming.brine.Brine;
import io.github.logogaming.brine.item.BrineItems;
import io.github.logogaming.brine.item.FlashlightItem;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

public class BrineModelPredicates {
    public static void registerModelPredicates() {
        ItemProperties.register(BrineItems.FLASHLIGHT, ResourceLocation.fromNamespaceAndPath(Brine.MOD_ID, "lit"),
                (stack, world, entity, seed) -> FlashlightItem.isOn(stack) ? 1.0F : 0.0F);
    }
}
