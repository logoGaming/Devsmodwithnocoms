package io.github.logogaming.brine.mixin;

import io.github.logogaming.brine.oxygen.BrineOxygen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "getMaxAirSupply", at = @At("HEAD"), cancellable = true)
    private void brine$maxAirFromAttribute(CallbackInfoReturnable<Integer> cir) {
        if ((Object) this instanceof Player player) {
            cir.setReturnValue(BrineOxygen.maxTicks(player));
        }
    }
}
