package io.github.logogaming.brine.oxygen;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;

public final class BrineAttributes {

    public static final double DEFAULT_MAX_OXYGEN_TICKS = 300.0;

    public static final Holder<Attribute> MAX_OXYGEN = Registry.registerForHolder(
            BuiltInRegistries.ATTRIBUTE,
            ResourceLocation.fromNamespaceAndPath("brine", "max_oxygen"),
            new RangedAttribute("attribute.name.brine.max_oxygen", DEFAULT_MAX_OXYGEN_TICKS, 20.0, 72000.0).setSyncable(true)
    );

    public static final Holder<Attribute> OXYGEN_DRAIN_RATE = Registry.registerForHolder(
            BuiltInRegistries.ATTRIBUTE,
            ResourceLocation.fromNamespaceAndPath("brine", "oxygen_drain_rate"),
            new RangedAttribute("attribute.name.brine.oxygen_drain_rate", 1.0, 0.0, 100.0).setSyncable(true)
    );

    private BrineAttributes() {
    }

    public static void register() {
        FabricDefaultAttributeRegistry.register(EntityType.PLAYER, Player.createAttributes().add(MAX_OXYGEN).add(OXYGEN_DRAIN_RATE));
    }
}
