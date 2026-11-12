package io.github.logogaming.brine.client;

import io.github.logogaming.brine.world.BrineDimensions;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class WaterSplash {

    public static final int MAX_RINGS = 6;
    private static final float RING_LIFETIME = 9.0f;
    private static final float NO_RING_AGE = 999.0f;
    private static final double TRACK_RANGE_SQUARED = 96.0 * 96.0;
    private static final float[] RING_X = new float[MAX_RINGS];
    private static final float[] RING_Z = new float[MAX_RINGS];
    private static final float[] RING_POWER = new float[MAX_RINGS];
    private static final long[] RING_START = new long[MAX_RINGS];
    private static final Map<Integer, Double> PREVIOUS_HEIGHT = new HashMap<>();
    private static final Set<Integer> SEEN = new HashSet<>();
    private static long exitTick = Long.MIN_VALUE;
    private static boolean primed;
    private static boolean wasSubmerged;
    private static double lastVerticalSpeed;

    static {
        Arrays.fill(RING_START, Long.MIN_VALUE);
    }

    private WaterSplash() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(WaterSplash::tick);
    }

    public static float exitAmount(ClientLevel level, float tickDelta) {
        if (exitTick == Long.MIN_VALUE) {
            return 0.0f;
        }
        float seconds = (level.getGameTime() + tickDelta - exitTick) / 20.0f;
        return Mth.clamp(1.0f - seconds / 2.0f, 0.0f, 1.0f);
    }

    public static void write(ClientLevel level, float tickDelta, float[] out) {
        for (int i = 0; i < MAX_RINGS; i++) {
            float age = NO_RING_AGE;
            float power = 0.0f;
            if (RING_START[i] != Long.MIN_VALUE) {
                float seconds = Math.max(0.0f, (level.getGameTime() + tickDelta - RING_START[i]) / 20.0f);
                if (seconds < RING_LIFETIME) {
                    age = seconds;
                    power = RING_POWER[i];
                }
            }
            out[i * 4] = RING_X[i];
            out[i * 4 + 1] = RING_Z[i];
            out[i * 4 + 2] = age;
            out[i * 4 + 3] = power;
        }
    }

    private static void addRing(ClientLevel level, double x, double z, float power) {
        int slot = 0;
        for (int i = 1; i < MAX_RINGS; i++) {
            if (RING_START[i] < RING_START[slot]) {
                slot = i;
            }
        }
        RING_X[slot] = (float) x;
        RING_Z[slot] = (float) z;
        RING_POWER[slot] = power;
        RING_START[slot] = level.getGameTime();
    }

    private static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        ClientLevel level = client.level;
        if (player == null || level == null || level.dimension() != BrineDimensions.BRINE) {
            primed = false;
            exitTick = Long.MIN_VALUE;
            Arrays.fill(RING_START, Long.MIN_VALUE);
            PREVIOUS_HEIGHT.clear();
            return;
        }
        trackPlayer(level, player);
        trackEntities(level, player);
    }

    private static void trackPlayer(ClientLevel level, LocalPlayer player) {
        boolean submerged = player.getEyeY() < BrineDimensions.SEA_LEVEL - 0.2;
        if (primed && submerged != wasSubmerged) {
            float strength = (float) Mth.clamp(Math.abs(lastVerticalSpeed) * 1.6 + 0.35, 0.35, 1.5);
            if (submerged) {
                addRing(level, player.getX(), player.getZ(), strength);
                exitTick = Long.MIN_VALUE;
            } else {
                addRing(level, player.getX(), player.getZ(), strength * 0.55f);
                exitTick = level.getGameTime();
                spawnSplashParticles(level, player.getX(), player.getZ(), strength * 0.55f);
            }
        }
        primed = true;
        wasSubmerged = submerged;
        lastVerticalSpeed = player.getDeltaMovement().y;
    }

    private static void trackEntities(ClientLevel level, LocalPlayer player) {
        SEEN.clear();
        double surface = BrineDimensions.SEA_LEVEL;
        for (Entity entity : level.entitiesForRendering()) {
            if (entity == player || entity.isSpectator() || entity.distanceToSqr(player) > TRACK_RANGE_SQUARED) {
                continue;
            }
            int id = entity.getId();
            SEEN.add(id);
            double height = entity.getY();
            Double previous = PREVIOUS_HEIGHT.put(id, height);
            if (previous == null) {
                continue;
            }
            boolean falling = previous >= surface && height < surface;
            boolean rising = previous < surface && height >= surface;
            if (!falling && !rising) {
                continue;
            }
            float size = (float) Mth.clamp(Math.sqrt(entity.getBbWidth() * entity.getBbHeight()), 0.25, 1.4);
            float speed = (float) Math.abs(height - previous);
            float power = Mth.clamp((speed * 2.0f + 0.2f) * size, 0.12f, 1.2f) * (falling ? 1.0f : 0.5f);
            addRing(level, entity.getX(), entity.getZ(), power);
        }
        PREVIOUS_HEIGHT.keySet().retainAll(SEEN);
    }

    private static void spawnSplashParticles(ClientLevel level, double x, double z, float power) {
        RandomSource random = level.getRandom();
        double surface = BrineDimensions.SEA_LEVEL;
        int splashes = (int) (power * 26.0f);
        for (int i = 0; i < splashes; i++) {
            level.addParticle(ParticleTypes.SPLASH,
                    x + (random.nextDouble() - 0.5) * 1.2, surface + 0.1, z + (random.nextDouble() - 0.5) * 1.2,
                    (random.nextDouble() - 0.5) * 0.3, 0.25 + random.nextDouble() * 0.25, (random.nextDouble() - 0.5) * 0.3);
        }
    }
}
