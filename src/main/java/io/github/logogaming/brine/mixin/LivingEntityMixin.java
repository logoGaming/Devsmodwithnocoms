package io.github.logogaming.brine.mixin;

import io.github.logogaming.brine.oxygen.BrineOxygen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "decreaseAirSupply", at = @At("RETURN"), cancellable = true)
    private void brine$scaleAirLoss(int air, CallbackInfoReturnable<Integer> cir) {
        if ((Object) this instanceof Player player) {
            int vanillaLoss = air - cir.getReturnValue();
            cir.setReturnValue(air - BrineOxygen.scaledLoss(player, vanillaLoss));
        }
    }
}
