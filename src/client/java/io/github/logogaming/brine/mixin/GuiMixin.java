package io.github.logogaming.brine.mixin;

import io.github.logogaming.brine.client.hud.BrineHud;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Inject(method = "renderHearts", at = @At("HEAD"), cancellable = true)
    private void brine$hideHearts(GuiGraphics graphics, Player player, int x, int y, int height, int offset,
                                  float maxHealth, int health, int displayHealth, int absorption, boolean highlight,
                                  CallbackInfo ci) {
        if (BrineHud.active()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private static void brine$hideArmor(GuiGraphics graphics, Player player, int y, int rows, int height, int x,
                                        CallbackInfo ci) {
        if (BrineHud.active()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    private void brine$hideFood(GuiGraphics graphics, Player player, int y, int x, CallbackInfo ci) {
        if (BrineHud.active()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void brine$hideExperienceBar(GuiGraphics graphics, int x, CallbackInfo ci) {
        if (BrineHud.active()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderExperienceLevel", at = @At("HEAD"), cancellable = true)
    private void brine$hideExperienceLevel(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (BrineHud.active()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderItemHotbar", at = @At("HEAD"), cancellable = true)
    private void brine$hideVanillaHotbar(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (BrineHud.active()) {
            ci.cancel();
        }
    }

    @Redirect(method = "renderPlayerHealth", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"))
    private void brine$hideAir(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height) {
        if (!BrineHud.active()) {
            graphics.blitSprite(sprite, x, y, width, height);
        }
    }
}
