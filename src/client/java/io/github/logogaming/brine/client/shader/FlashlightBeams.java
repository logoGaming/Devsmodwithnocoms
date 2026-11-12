package io.github.logogaming.brine.client.shader;

import io.github.logogaming.brine.item.FlashlightItem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class FlashlightBeams {

    public static final int MAX_BEAMS = 4;

    private static final double RANGE_SQUARED = 64.0 * 64.0;

    public record Beam(Vector3f position, Vector3f direction, double distanceSquared) {
    }

    private FlashlightBeams() {
    }

    public static List<Beam> collect(Minecraft client, Camera camera, float tickDelta) {
        ClientLevel level = client.level;
        List<Beam> beams = new ArrayList<>();
        if (level == null) {
            return beams;
        }
        for (Player player : level.players()) {
            if (!FlashlightItem.isHeldOn(player)) {
                continue;
            }
            Beam beam = beamFor(client, camera, player, tickDelta);
            if (beam.distanceSquared() <= RANGE_SQUARED) {
                beams.add(beam);
            }
        }
        beams.sort(Comparator.comparingDouble(Beam::distanceSquared));
        return beams.size() > MAX_BEAMS ? beams.subList(0, MAX_BEAMS) : beams;
    }

    private static Beam beamFor(Minecraft client, Camera camera, Player player, float tickDelta) {
        Vec3 cameraPos = camera.getPosition();
        Vector3f position;
        Vector3f direction;
        if (player == client.player && !camera.isDetached()) {
            direction = new Vector3f(camera.getLookVector());
            position = new Vector3f((float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z)
                    .fma(-0.35f, camera.getLeftVector())
                    .fma(-0.3f, camera.getUpVector());
        } else {
            Vec3 eye = player.getEyePosition(tickDelta);
            Vec3 look = player.getViewVector(tickDelta);
            direction = new Vector3f((float) look.x, (float) look.y, (float) look.z);
            Vector3f right = new Vector3f(direction).cross(0.0f, 1.0f, 0.0f).normalize();
            position = new Vector3f((float) eye.x, (float) eye.y - 0.25f, (float) eye.z).fma(0.3f, right);
        }
        double dx = position.x - cameraPos.x;
        double dy = position.y - cameraPos.y;
        double dz = position.z - cameraPos.z;
        return new Beam(position, direction.normalize(), dx * dx + dy * dy + dz * dz);
    }
}
