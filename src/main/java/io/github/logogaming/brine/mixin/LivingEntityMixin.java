package io.github.logogaming.brine.mixin;

import io.github.logogaming.brine.oxygen.BrineOxygen;
import io.github.logogaming.brine.world.BrineDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    public void brine$scaleOxygenDrainWithYLevel(CallbackInfo ci)
    {
        if ((Object) this instanceof Player player)
        {
            if (player.level().isClientSide)
                return;

            // Multiply oxygen drate rain by 2% the further you go below sea level
            double yPos = player.getY();
            if (yPos < BrineDimensions.SEA_LEVEL)
            {
                double depthBelowSeaLevel = BrineDimensions.SEA_LEVEL - yPos;
                double multiplier = depthBelowSeaLevel * 0.02;

                BrineOxygen.addDrainMultiplier(player, ResourceLocation.fromNamespaceAndPath("brine", "oxygen_drain_rate"), multiplier);
            }
            else
            {
                BrineOxygen.removeDrainModifier(player, ResourceLocation.fromNamespaceAndPath("brine", "oxygen_drain_rate"));
            }

            // If oxygen is less than 25%, give slowness and mining fatigue
            int air = player.getAirSupply();
            int maxAir = player.getMaxAirSupply();

            if (air > 0 && air < maxAir * 0.25)
            {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, true, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 40, 0, true, true, true));
            }
        }
    }

    @Inject(method = "decreaseAirSupply", at = @At("RETURN"), cancellable = true)
    private void brine$scaleAirLoss(int air, CallbackInfoReturnable<Integer> cir) {
        if ((Object) this instanceof Player player) {
            int vanillaLoss = air - cir.getReturnValue();
            cir.setReturnValue(air - BrineOxygen.scaledLoss(player, vanillaLoss));
        }
    }
}
