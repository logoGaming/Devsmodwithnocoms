package io.github.logogaming.brine.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class OxygenOverTimeEffect extends MobEffect {
    private final int amount;

    protected OxygenOverTimeEffect(int amount) {
        super(MobEffectCategory.BENEFICIAL, 0xffffffff);
        this.amount = amount;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % Math.max(1, 8 - amplifier) == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        livingEntity.setAirSupply(livingEntity.getAirSupply() + amplifier * amount);
        return true;
    }
}
