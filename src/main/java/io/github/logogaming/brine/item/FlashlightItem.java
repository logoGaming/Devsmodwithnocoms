package io.github.logogaming.brine.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public class FlashlightItem extends Item {

    public static final int MAX_CHARGE = 4800;

    private static final String ON_KEY = "On";
    private static final String CHARGE_KEY = "Charge";
    private static final int OFFHAND_SLOT = 40;
    private static final int UPDATE_INTERVAL = 20;
    private static final int RECHARGE_PER_UPDATE = 5;

    public FlashlightItem(Properties properties) {
        super(properties);
    }

    public static boolean isOn(ItemStack stack) {
        if (!(stack.getItem() instanceof FlashlightItem)) {
            return false;
        }
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().getBoolean(ON_KEY);
    }

    public static int charge(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return MAX_CHARGE;
        }
        var tag = data.copyTag();
        return tag.contains(CHARGE_KEY) ? Mth.clamp(tag.getInt(CHARGE_KEY), 0, MAX_CHARGE) : MAX_CHARGE;
    }

    public static boolean isHeldOn(Player player) {
        return isOn(player.getMainHandItem()) || isOn(player.getOffhandItem());
    }

    public static ItemStack findFlashlight(Player player) {
        if (player.getMainHandItem().getItem() instanceof FlashlightItem) {
            return player.getMainHandItem();
        }
        if (player.getOffhandItem().getItem() instanceof FlashlightItem) {
            return player.getOffhandItem();
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof FlashlightItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void write(ItemStack stack, int charge, boolean on) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putInt(CHARGE_KEY, charge);
            tag.putBoolean(ON_KEY, on);
        });
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide || level.getGameTime() % UPDATE_INTERVAL != 0) {
            return;
        }
        boolean held = isSelected || slotId == OFFHAND_SLOT;
        boolean on = isOn(stack);
        int charge = charge(stack);
        int next = charge;
        boolean nextOn = on;
        if (on && held) {
            next = Math.max(0, charge - UPDATE_INTERVAL);
            if (next == 0) {
                nextOn = false;
                level.playSound(null, entity.blockPosition(), SoundEvents.LEVER_CLICK, SoundSource.PLAYERS, 0.6f, 0.7f);
            }
        } else if (!on) {
            next = Math.min(MAX_CHARGE, charge + RECHARGE_PER_UPDATE);
        }
        if (next != charge || nextOn != on) {
            write(stack, next, nextOn);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            boolean on = isOn(stack);
            int charge = charge(stack);
            if (!on && charge <= 0) {
                level.playSound(null, player.blockPosition(), SoundEvents.LEVER_CLICK, SoundSource.PLAYERS, 0.4f, 0.6f);
            } else {
                write(stack, charge, !on);
                level.playSound(null, player.blockPosition(), SoundEvents.LEVER_CLICK, SoundSource.PLAYERS, 0.6f, !on ? 1.4f : 1.0f);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean allowComponentsUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack) {
        return false;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return charge(stack) < MAX_CHARGE;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * charge(stack) / MAX_CHARGE);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Mth.hsvToRgb(0.33f * charge(stack) / MAX_CHARGE, 0.85f, 1.0f);
    }
}
