package io.github.logogaming.brine.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.logogaming.brine.world.BrineDimensions;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class BrineHud {

    private static final ResourceLocation HEART = ResourceLocation.withDefaultNamespace("hud/heart/full");
    private static final ResourceLocation ARMOR = ResourceLocation.withDefaultNamespace("hud/armor_full");
    private static final ResourceLocation FOOD = ResourceLocation.withDefaultNamespace("hud/food_full");
    private static final ResourceLocation AIR = ResourceLocation.withDefaultNamespace("hud/air");

    private static final float HEALTH_RADIUS = 10.0f;
    private static final float OXYGEN_RADIUS = 9.0f;
    private static final float HUNGER_RADIUS = 7.5f;
    private static final float ARMOR_RADIUS = 7.0f;
    private static final float MARGIN = 3.0f;
    private static final long NUMBER_HOLD_MILLIS = 2200L;
    private static final long NUMBER_FADE_MILLIS = 500L;

    private static final int TRACK = 0x40C8E8F0;
    private static final int BACKDROP = 0x4A0C1E2A;
    private static final int GLASS_TOP = 0x2EDFF6FF;
    private static final int GLASS_BOTTOM = 0x4A0A2230;
    private static final int GLASS_TOP_SELECTED = 0x48EAFBFF;
    private static final int GLASS_BOTTOM_SELECTED = 0x5A0E3A4C;
    private static final int RIM_TOP = 0xC8FFFFFF;
    private static final int RIM_BOTTOM = 0x44A6D2E0;
    private static final int RIM_TOP_SELECTED = 0xFFFFF0B8;
    private static final int RIM_BOTTOM_SELECTED = 0x99E8C45A;
    private static final int TAB = 0xE04FA8D8;
    private static final int TAB_SELECTED = 0xFFE8C45A;

    private static float shownHealth = -1.0f;
    private static float shownArmor = -1.0f;
    private static float shownAir = -1.0f;
    private static float shownFood = -1.0f;
    private static float shownDrowning;
    private static int lastHealth = -1;
    private static long healthChangedAt = Long.MIN_VALUE;

    private BrineHud() {
    }

    public static boolean active() {
        Minecraft client = Minecraft.getInstance();
        return client.level != null && client.player != null && client.level.dimension() == BrineDimensions.BRINE;
    }

    public static float drowning() {
        return shownDrowning;
    }

    public static void register() {
        HudRenderCallback.EVENT.register(BrineHud::render);
        ClientTickEvents.END_CLIENT_TICK.register(BrineHud::tick);
    }

    private static void tick(Minecraft client) {
        float target = 0.0f;
        if (active() && client.player.isAlive()) {
            Player player = client.player;
            float fraction = Mth.clamp(player.getAirSupply(), 0, player.getMaxAirSupply()) / (float) Math.max(1, player.getMaxAirSupply());
            target = (float) Mth.smoothstep(Mth.clamp((0.4f - fraction) / 0.4f, 0.0f, 1.0f));
        }
        shownDrowning += (target - shownDrowning) * 0.25f;
        if (Math.abs(target - shownDrowning) < 0.002f) {
            shownDrowning = target;
        }
    }

    private static float approach(float shown, float target) {
        if (shown < 0.0f) {
            return target;
        }
        return shown + (target - shown) * 0.2f;
    }

    private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (!active() || client.options.hideGui) {
            lastHealth = -1;
            return;
        }
        Player player = client.player;
        long now = Util.getMillis();

        float maxHealth = Math.max(1.0f, player.getMaxHealth());
        int health = (int) Math.ceil(player.getHealth());
        if (lastHealth != -1 && health != lastHealth) {
            healthChangedAt = now;
        }
        lastHealth = health;

        shownHealth = approach(shownHealth, player.getHealth() / maxHealth);
        shownArmor = approach(shownArmor, Math.min(player.getArmorValue(), 20) / 20.0f);
        int maxAir = Math.max(1, player.getMaxAirSupply());
        shownAir = approach(shownAir, Mth.clamp(player.getAirSupply(), 0, maxAir) / (float) maxAir);
        int foodLevel = player.getFoodData().getFoodLevel();
        shownFood = approach(shownFood, foodLevel / 20.0f);

        float bottomRowY = graphics.guiHeight() - MARGIN - HEALTH_RADIUS;
        float topRowY = bottomRowY - 19.0f;
        float oxygenX = MARGIN + OXYGEN_RADIUS;
        float healthX = oxygenX + OXYGEN_RADIUS + 2.0f + HEALTH_RADIUS;
        float hungerX = healthX + HEALTH_RADIUS + 2.0f + HUNGER_RADIUS;
        float armorX = MARGIN + ARMOR_RADIUS;

        long sinceChange = now - healthChangedAt;
        float numberAlpha = healthChangedAt == Long.MIN_VALUE ? 0.0f
                : sinceChange < NUMBER_HOLD_MILLIS ? 1.0f
                : Math.max(0.0f, 1.0f - (sinceChange - NUMBER_HOLD_MILLIS) / (float) NUMBER_FADE_MILLIS);
        boolean healthFlash = sinceChange < 350L;
        int healthColor = healthFlash ? 0xFFFFFFFF : mix(0xFFE0383B, 0xFFE8605F, shownHealth);
        gauge(graphics, healthX, bottomRowY, HEALTH_RADIUS, shownHealth, healthColor);
        icon(graphics, HEART, healthX, bottomRowY, 9, 1.0f - numberAlpha);
        if (numberAlpha > 0.02f) {
            number(graphics, client, healthX, bottomRowY, Integer.toString(health), 0xFFFFFFFF, numberAlpha, 1.0f);
        }

        int airSeconds = (int) Math.ceil(Mth.clamp(player.getAirSupply(), 0, maxAir) / 20.0);
        int airColor = shownAir < 0.25f ? mix(0xFFE0383B, 0xFF4FD6E6, shownAir / 0.25f) : 0xFF4FD6E6;
        gauge(graphics, oxygenX, bottomRowY, OXYGEN_RADIUS, shownAir, airColor);
        number(graphics, client, oxygenX, bottomRowY - 2.6f, "O₂", 0xFFB8F2F8, 1.0f, 0.55f);
        number(graphics, client, oxygenX, bottomRowY + 2.4f, Integer.toString(airSeconds), 0xFFFFFFFF, 1.0f, 0.8f);

        boolean starving = foodLevel <= 6;
        float hungerPulse = starving ? 0.6f + 0.4f * (float) Math.sin(now / 200.0) : 1.0f;
        gauge(graphics, hungerX, bottomRowY, HUNGER_RADIUS, shownFood, withAlpha(0xFFF29A2E, hungerPulse));
        icon(graphics, FOOD, hungerX, bottomRowY, 8, 1.0f);

        gauge(graphics, armorX, topRowY, ARMOR_RADIUS, shownArmor, player.getArmorValue() > 0 ? 0xFF9CC2E8 : 0x00000000);
        icon(graphics, ARMOR, armorX, topRowY, 7, player.getArmorValue() > 0 ? 1.0f : 0.5f);

        hotbar(graphics, client, player);
    }

    private static void hotbar(GuiGraphics graphics, Minecraft client, Player player) {
        float slotSize = 20.0f;
        float pitch = 24.0f;
        float left = (graphics.guiWidth() - (pitch * 9.0f - 4.0f)) / 2.0f;
        float baseY = graphics.guiHeight() - 28.0f;

        for (int i = 0; i < 9; i++) {
            boolean selected = i == player.getInventory().selected;
            ItemStack stack = player.getInventory().items.get(i);
            float x = left + i * pitch;
            float y = baseY - (selected ? 2.0f : 0.0f);
            drawSlot(graphics, x, y, slotSize, selected, stack);
            drawStack(graphics, client, stack, x, y);
        }

        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty()) {
            boolean leftHanded = player.getMainArm() == HumanoidArm.LEFT;
            float x = leftHanded ? left + pitch * 9.0f : left - pitch - 4.0f;
            drawSlot(graphics, x, baseY, slotSize, false, offhand);
            drawStack(graphics, client, offhand, x, baseY);
        }
    }

    private static void drawSlot(GuiGraphics graphics, float x, float y, float size, boolean selected, ItemStack stack) {
        float tabWidth = 12.0f;
        float tabX = x + (size - tabWidth) / 2.0f;
        float tabY = y - 5.0f;
        int base = selected ? TAB_SELECTED : TAB;
        if (!stack.isEmpty() && stack.isBarVisible()) {
            GaugeRenderer.roundedRect(graphics, tabX, tabY, tabWidth, 2.6f, 1.2f, (base & 0x00FFFFFF) | 0x55000000);
            float width = Math.max(1.8f, tabWidth * stack.getBarWidth() / 13.0f);
            GaugeRenderer.roundedRect(graphics, tabX, tabY, width, 2.6f, 1.2f, 0xFF000000 | stack.getBarColor());
        } else {
            GaugeRenderer.roundedRect(graphics, tabX, tabY, tabWidth, 2.6f, 1.2f, base);
        }
        if (selected) {
            GaugeRenderer.roundedOutline(graphics, x - 1.5f, y - 1.5f, size + 3.0f, size + 3.0f, 6.0f, 1.5f, 0x2EF2E2A6);
        }
        GaugeRenderer.roundedRectGradient(graphics, x, y, size, size, 5.0f,
                selected ? GLASS_TOP_SELECTED : GLASS_TOP, selected ? GLASS_BOTTOM_SELECTED : GLASS_BOTTOM);
        GaugeRenderer.roundedOutlineGradient(graphics, x, y, size, size, 5.0f, selected ? 1.4f : 0.9f,
                selected ? RIM_TOP_SELECTED : RIM_TOP, selected ? RIM_BOTTOM_SELECTED : RIM_BOTTOM);
        GaugeRenderer.roundedOutlineGradient(graphics, x + 1.0f, y + 1.0f, size - 2.0f, size - 2.0f, 4.0f, 0.6f, 0x44FFFFFF, 0x00FFFFFF);
    }

    private static void drawStack(GuiGraphics graphics, Minecraft client, ItemStack stack, float x, float y) {
        if (stack.isEmpty()) {
            return;
        }
        int ix = Math.round(x) + 2;
        int iy = Math.round(y) + 2;
        graphics.renderItem(stack, ix, iy);
        if (stack.getCount() > 1) {
            String count = Integer.toString(stack.getCount());
            graphics.pose().pushPose();
            graphics.pose().translate(0.0f, 0.0f, 200.0f);
            graphics.drawString(client.font, count, ix + 17 - client.font.width(count), iy + 9, 0xFFFFFFFF, true);
            graphics.pose().popPose();
        }
    }

    private static void gauge(GuiGraphics graphics, float cx, float cy, float radius, float fraction, int color) {
        float width = Math.max(1.8f, radius * 0.24f);
        GaugeRenderer.disc(graphics, cx, cy, radius, BACKDROP);
        GaugeRenderer.ring(graphics, cx, cy, radius - width, radius, 1.0f, TRACK);
        if ((color >>> 24) != 0) {
            GaugeRenderer.ring(graphics, cx, cy, radius - width, radius, fraction, color);
        }
    }

    private static void icon(GuiGraphics graphics, ResourceLocation sprite, float cx, float cy, int size, float alpha) {
        if (alpha <= 0.02f) {
            return;
        }
        RenderSystem.enableBlend();
        graphics.setColor(1.0f, 1.0f, 1.0f, alpha);
        graphics.blitSprite(sprite, Math.round(cx - size / 2.0f), Math.round(cy - size / 2.0f), size, size);
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    private static void number(GuiGraphics graphics, Minecraft client, float cx, float cy, String text, int color, float alpha, float scale) {
        int alphaByte = Mth.clamp((int) (alpha * 255.0f), 4, 255);
        int argb = (alphaByte << 24) | (color & 0x00FFFFFF);
        float width = client.font.width(text) * scale;
        graphics.pose().pushPose();
        graphics.pose().translate(cx - width / 2.0f, cy - 3.5f * scale, 0.0f);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.drawString(client.font, text, 0, 0, argb, true);
        graphics.pose().popPose();
    }

    private static int withAlpha(int color, float alpha) {
        int a = Mth.clamp((int) (((color >>> 24) & 0xFF) * alpha), 0, 255);
        return (a << 24) | (color & 0x00FFFFFF);
    }

    private static int mix(int low, int high, float t) {
        float f = Mth.clamp(t, 0.0f, 1.0f);
        int r = (int) Mth.lerp(f, (low >> 16) & 0xFF, (high >> 16) & 0xFF);
        int g = (int) Mth.lerp(f, (low >> 8) & 0xFF, (high >> 8) & 0xFF);
        int b = (int) Mth.lerp(f, low & 0xFF, high & 0xFF);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
