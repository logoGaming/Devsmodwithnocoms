package io.github.logogaming.brine.effect;

import io.github.logogaming.brine.Brine;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

public class BrineEffects {
    public static final Holder<MobEffect> OXYGEN_EFFICIENCY = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Brine.id("oxygen_efficiency"),new OxygenOverTimeEffect(3));
    public static final Holder<MobEffect> OXYGEN_DRAIN = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Brine.id("oxygen_drain"),new OxygenOverTimeEffect(-2));
    public static void init() {
    }
}
