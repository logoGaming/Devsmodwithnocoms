package io.github.logogaming.brine.client.shader;

import com.mojang.blaze3d.pipeline.RenderTarget;
import io.github.logogaming.brine.world.BrineDimensions;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.ladysnake.satin.api.event.PostWorldRenderCallbackV3;
import org.ladysnake.satin.api.event.ShaderEffectRenderCallback;
import org.ladysnake.satin.api.experimental.ReadableDepthFramebuffer;
import org.ladysnake.satin.api.managed.ManagedShaderEffect;
import org.ladysnake.satin.api.managed.ShaderEffectManager;
import org.ladysnake.satin.api.managed.uniform.Uniform1f;
import org.ladysnake.satin.api.managed.uniform.Uniform3f;
import org.ladysnake.satin.api.managed.uniform.UniformMat4;

public final class BrineShaders {

    private static final ManagedShaderEffect EFFECT = ShaderEffectManager.getInstance()
            .manage(ResourceLocation.fromNamespaceAndPath("brine", "shaders/post/brine.json"));
    private static final UniformMat4 INV_PROJ = EFFECT.findUniformMat4("InvProjMat");
    private static final UniformMat4 CAMERA_ROT = EFFECT.findUniformMat4("CameraRot");
    private static final Uniform3f CAMERA_POS = EFFECT.findUniform3f("CameraPos");
    private static final Uniform1f TIME = EFFECT.findUniform1f("Time");
    private static final Uniform1f SURFACE_Y = EFFECT.findUniform1f("SurfaceY");
    private static final Uniform3f LIGHT_DIR = EFFECT.findUniform3f("LightDir");
    private static final Uniform1f DAYLIGHT = EFFECT.findUniform1f("Daylight");

    private static final Matrix4f PROJECTION = new Matrix4f();
    private static final Matrix4f INVERSE = new Matrix4f();
    private static final Matrix4f ROTATION = new Matrix4f();
    private static final Vector3f LIGHT = new Vector3f();
    private static boolean hasProjection;

    private BrineShaders() {
    }

    public static void register() {
        PostWorldRenderCallbackV3.EVENT.register((matrices, modelView, projection, camera, tickDelta) -> {
            PROJECTION.set(projection);
            hasProjection = true;
        });
        ShaderEffectRenderCallback.EVENT.register(BrineShaders::render);
    }

    private static void render(float tickDelta) {
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        if (level == null || !hasProjection || level.dimension() != BrineDimensions.BRINE) {
            return;
        }
        Camera camera = client.gameRenderer.getMainCamera();
        RenderTarget target = client.getMainRenderTarget();
        EFFECT.setSamplerUniform("DepthSampler", ((ReadableDepthFramebuffer) target).getStillDepthMap());
        EFFECT.setSamplerUniform("LiveDepthSampler", target.getDepthTextureId());
        float angle = level.getTimeOfDay(tickDelta) * Mth.TWO_PI;
        float height = Mth.cos(angle);
        LIGHT.set(Mth.sin(angle) * 0.7f, Math.max(Math.abs(height), 0.25f), 0.25f).normalize();
        INV_PROJ.set(PROJECTION.invert(INVERSE));
        CAMERA_ROT.set(ROTATION.rotation(camera.rotation()));
        CAMERA_POS.set((float) camera.getPosition().x, (float) camera.getPosition().y, (float) camera.getPosition().z);
        TIME.set((level.getGameTime() + tickDelta) / 20.0f);
        SURFACE_Y.set((float) BrineDimensions.SEA_LEVEL);
        LIGHT_DIR.set(LIGHT);
        DAYLIGHT.set(Math.max(0.15f, (float) Mth.smoothstep(Mth.clamp((height + 0.15f) / 0.6f, 0.0f, 1.0f))));
        EFFECT.render(tickDelta);
    }
}
