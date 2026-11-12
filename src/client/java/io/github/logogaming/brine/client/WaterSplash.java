package io.github.logogaming.brine.client;

import io.github.logogaming.brine.world.BrineDimensions;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class WaterSplash {

    private static final float NO_SPLASH_AGE = 999.0f;

    private static double originX;
    private static double originZ;
    private static float power;
    private static long startTick = Long.MIN_VALUE;
    private static boolean primed;
    private static boolean wasSubmerged;
    private static double lastVerticalSpeed;

    private WaterSplash() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(WaterSplash::tick);
    }

    public static float power() {
        return power;
    }

    public static double originX() {
        return originX;
    }

    public static double originZ() {
        return originZ;
    }

    public static float age(ClientLevel level, float tickDelta) {
        if (startTick == Long.MIN_VALUE) {
            return NO_SPLASH_AGE;
        }
        return Math.max(0.0f, (level.getGameTime() + tickDelta - startTick) / 20.0f);
    }

    private static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        ClientLevel level = client.level;
        if (player == null || level == null || level.dimension() != BrineDimensions.BRINE) {
            primed = false;
            startTick = Long.MIN_VALUE;
            return;
        }
        boolean submerged = player.getEyeY() < BrineDimensions.SEA_LEVEL - 0.2;
        if (primed && submerged != wasSubmerged) {
            trigger(level, player, submerged);
        }
        primed = true;
        wasSubmerged = submerged;
        lastVerticalSpeed = player.getDeltaMovement().y;
    }

    private static void trigger(ClientLevel level, LocalPlayer player, boolean entering) {
        float strength = (float) Mth.clamp(Math.abs(lastVerticalSpeed) * 1.6 + 0.35, 0.35, 1.5);
        power = entering ? strength : strength * 0.55f;
        originX = player.getX();
        originZ = player.getZ();
        startTick = level.getGameTime();

        RandomSource random = level.getRandom();
        double surface = BrineDimensions.SEA_LEVEL;
        int splashes = (int) (power * 26.0f);
        for (int i = 0; i < splashes; i++) {
            level.addParticle(ParticleTypes.SPLASH,
                    originX + (random.nextDouble() - 0.5) * 1.2, surface + 0.1, originZ + (random.nextDouble() - 0.5) * 1.2,
                    (random.nextDouble() - 0.5) * 0.3, 0.25 + random.nextDouble() * 0.25, (random.nextDouble() - 0.5) * 0.3);
        }
        if (entering) {
            int bubbles = (int) (power * 24.0f);
            for (int i = 0; i < bubbles; i++) {
                level.addParticle(ParticleTypes.BUBBLE,
                        originX + (random.nextDouble() - 0.5) * 1.4, surface - 0.3 - random.nextDouble() * 1.8, originZ + (random.nextDouble() - 0.5) * 1.4,
                        (random.nextDouble() - 0.5) * 0.1, 0.05 + random.nextDouble() * 0.12, (random.nextDouble() - 0.5) * 0.1);
            }
        }
    }
}
