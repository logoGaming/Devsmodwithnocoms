package io.github.logogaming.brine.world;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;

public final class FirstJoinSpawn {
    private static final List<ServerPlayer> PENDING = new ArrayList<>();
    private FirstJoinSpawn() {
    }
    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            if (player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)) == 0) {
                PENDING.add(player);
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> PENDING.remove(handler.getPlayer()));
        ServerTickEvents.END_SERVER_TICK.register(FirstJoinSpawn::teleportPending);
    }

    private static void teleportPending(MinecraftServer server) {
        if (PENDING.isEmpty()) {
            return;
        }
        ServerLevel brine = server.getLevel(BrineDimensions.BRINE);
        List<ServerPlayer> ready = new ArrayList<>(PENDING);
        PENDING.clear();
        if (brine == null) {
            return;
        }
        for (ServerPlayer player : ready) {
            if (player.isRemoved() || player.hasDisconnected()) {
                continue;
            }
            int y = brine.getHeight(Heightmap.Types.OCEAN_FLOOR, 0, 0);
            player.teleportTo(brine, 0.5, y, 0.5, player.getYRot(), player.getXRot());
        }
    }
}
