package io.github.logogaming.brine.client;

import io.github.logogaming.brine.item.BrineItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;

public class BrineProperties {
    public static void initialize() {
        ItemProperties.register(BrineItems.BARNACLE_ROD, ResourceLocation.withDefaultNamespace("cast"),
                (stack, level, entity, seed) -> {
                    if (!(entity instanceof Player player)) return 0.0F;
                    boolean main = player.getMainHandItem() == stack;
                    boolean off = player.getOffhandItem() == stack;
                    if (player.getMainHandItem().getItem() instanceof FishingRodItem) off = false;
                    return (main || off) && player.fishing != null ? 1.0F : 0.0F;
                });

        ItemProperties.register(BrineItems.IRON_ROD, ResourceLocation.withDefaultNamespace("cast"),
                (stack, level, entity, seed) -> {
                    if (!(entity instanceof Player player)) return 0.0F;
                    boolean main = player.getMainHandItem() == stack;
                    boolean off = player.getOffhandItem() == stack;
                    if (player.getMainHandItem().getItem() instanceof FishingRodItem) off = false;
                    return (main || off) && player.fishing != null ? 1.0F : 0.0F;
                });
    }
}
