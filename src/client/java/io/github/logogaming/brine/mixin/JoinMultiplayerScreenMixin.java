package io.github.logogaming.brine.mixin;

import com.mojang.realmsclient.RealmsMainScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JoinMultiplayerScreen.class)
public class JoinMultiplayerScreenMixin extends Screen {
    protected JoinMultiplayerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void brine$addRealmsButton(CallbackInfo ci) {
        this.addRenderableWidget(Button.builder(Component.literal("Realms"), (button) -> this.minecraft.setScreen(new RealmsMainScreen(this))).bounds(this.width / 2 - 50, 0, 40, 20).tooltip(null).build()).active = true;

    }
}
