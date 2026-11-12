package io.github.logogaming.brine.oxygen;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

public final class BrineOxygen {

    private BrineOxygen() {
    }

    public static int maxTicks(Player player) {
        AttributeInstance instance = player.getAttribute(BrineAttributes.MAX_OXYGEN);
        return instance == null ? (int) BrineAttributes.DEFAULT_MAX_OXYGEN_TICKS : (int) Math.round(instance.getValue());
    }

    public static int maxSeconds(Player player) {
        return maxTicks(player) / 20;
    }

    public static void setBaseTicks(Player player, int ticks) {
        AttributeInstance instance = player.getAttribute(BrineAttributes.MAX_OXYGEN);
        if (instance != null) {
            instance.setBaseValue(ticks);
        }
    }

    public static void setBaseSeconds(Player player, int seconds) {
        setBaseTicks(player, seconds * 20);
    }

    public static void addTicks(Player player, ResourceLocation id, double ticks) {
        addModifier(player, id, ticks, AttributeModifier.Operation.ADD_VALUE);
    }

    public static void addSeconds(Player player, ResourceLocation id, double seconds) {
        addModifier(player, id, seconds * 20.0, AttributeModifier.Operation.ADD_VALUE);
    }

    public static void addMultiplier(Player player, ResourceLocation id, double multiplier) {
        addModifier(player, id, multiplier, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    public static void addModifier(Player player, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(BrineAttributes.MAX_OXYGEN);
        if (instance != null) {
            instance.removeModifier(id);
            instance.addPermanentModifier(new AttributeModifier(id, amount, operation));
        }
    }

    public static void removeModifier(Player player, ResourceLocation id) {
        AttributeInstance instance = player.getAttribute(BrineAttributes.MAX_OXYGEN);
        if (instance != null) {
            instance.removeModifier(id);
        }
    }

    public static double drainRate(Player player) {
        AttributeInstance instance = player.getAttribute(BrineAttributes.OXYGEN_DRAIN_RATE);
        return instance == null ? 1.0 : instance.getValue();
    }

    public static void setBaseDrainRate(Player player, double rate) {
        AttributeInstance instance = player.getAttribute(BrineAttributes.OXYGEN_DRAIN_RATE);
        if (instance != null) {
            instance.setBaseValue(rate);
        }
    }

    public static void addDrainMultiplier(Player player, ResourceLocation id, double multiplier) {
        AttributeInstance instance = player.getAttribute(BrineAttributes.OXYGEN_DRAIN_RATE);
        if (instance != null) {
            instance.removeModifier(id);
            instance.addPermanentModifier(new AttributeModifier(id, multiplier, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    public static void removeDrainModifier(Player player, ResourceLocation id) {
        AttributeInstance instance = player.getAttribute(BrineAttributes.OXYGEN_DRAIN_RATE);
        if (instance != null) {
            instance.removeModifier(id);
        }
    }

    public static int scaledLoss(Player player, int vanillaLoss) {
        if (vanillaLoss <= 0) {
            return 0;
        }
        double scaled = vanillaLoss * drainRate(player);
        int whole = (int) Math.floor(scaled);
        return whole + (player.getRandom().nextDouble() < scaled - whole ? 1 : 0);
    }

    public static void refill(Player player) {
        player.setAirSupply(maxTicks(player));
    }
}
