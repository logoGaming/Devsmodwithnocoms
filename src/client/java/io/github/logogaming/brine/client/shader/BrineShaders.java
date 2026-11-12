package io.github.logogaming.brine.client.shader;

import com.mojang.blaze3d.pipeline.RenderTarget;
import io.github.logogaming.brine.client.WaterSplash;
import io.github.logogaming.brine.world.BrineDimensions;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
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
import org.ladysnake.satin.api.managed.uniform.Uniform4f;
import org.ladysnake.satin.api.managed.uniform.UniformMat4;

import java.util.List;

public final class BrineShaders {

    private static final ManagedShaderEffect EFFECT = ShaderEffectManager.getInstance()
            .manage(ResourceLocation.fromNamespaceAndPath("brine", "shaders/post/brine.json"));
    private static final UniformMat4 INV_PROJ = EFFECT.findUniformMat4("InvProjMat");
    private static final UniformMat4 SCREEN_PROJ = EFFECT.findUniformMat4("ScreenProj");
    private static final UniformMat4 CAMERA_ROT = EFFECT.findUniformMat4("CameraRot");
    private static final Uniform3f CAMERA_POS = EFFECT.findUniform3f("CameraPos");
    private static final Uniform1f TIME = EFFECT.findUniform1f("BrineTime");
    private static final Uniform1f SURFACE_Y = EFFECT.findUniform1f("SurfaceY");
    private static final Uniform3f LIGHT_DIR = EFFECT.findUniform3f("LightDir");
    private static final Uniform3f SUN_DIR = EFFECT.findUniform3f("SunDir");
    private static final Uniform1f DAYLIGHT = EFFECT.findUniform1f("Daylight");
    private static final Uniform3f SPLASH_POS = EFFECT.findUniform3f("SplashPos");
    private static final Uniform1f SPLASH_AGE = EFFECT.findUniform1f("SplashAge");
    private static final Uniform1f SPLASH_POWER = EFFECT.findUniform1f("SplashPower");
    private static final Uniform1f SPLASH_ENTER = EFFECT.findUniform1f("SplashEnter");
    private static final Uniform1f EXIT_WATER = EFFECT.findUniform1f("ExitWater");
    private static final Uniform4f[] BEAM_POS = new Uniform4f[FlashlightBeams.MAX_BEAMS];
    private static final Uniform3f[] BEAM_DIR = new Uniform3f[FlashlightBeams.MAX_BEAMS];

    private static final Matrix4f PROJECTION = new Matrix4f();
    private static final Matrix4f INVERSE = new Matrix4f();
    private static final Matrix4f ROTATION = new Matrix4f();
    private static final Vector3f LIGHT = new Vector3f();
    private static final Vector3f SUN = new Vector3f();
    private static boolean hasProjection;

    static {
        for (int i = 0; i < FlashlightBeams.MAX_BEAMS; i++) {
            BEAM_POS[i] = EFFECT.findUniform4f("BeamPos" + i);
            BEAM_DIR[i] = EFFECT.findUniform3f("BeamDir" + i);
        }
    }

    private BrineShaders() {
    }

    public static void register() {
        DimensionRenderingRegistry.registerCloudRenderer(BrineDimensions.BRINE, context -> {
        });
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
        SUN.set(Mth.sin(angle) * 0.9f, height, 0.3f).normalize();
        LIGHT.set(SUN.x * 0.8f, Math.max(Math.abs(height), 0.3f), SUN.z).normalize();

        INV_PROJ.set(PROJECTION.invert(INVERSE));
        SCREEN_PROJ.set(PROJECTION);
        CAMERA_ROT.set(ROTATION.rotation(camera.rotation()));
        CAMERA_POS.set((float) camera.getPosition().x, (float) camera.getPosition().y, (float) camera.getPosition().z);
        TIME.set((level.getGameTime() + tickDelta) / 20.0f);
        SURFACE_Y.set((float) BrineDimensions.SEA_LEVEL);
        LIGHT_DIR.set(LIGHT);
        SUN_DIR.set(SUN);
        DAYLIGHT.set(Math.max(0.0f, (float) Mth.smoothstep(Mth.clamp((height + 0.1f) / 0.55f, 0.0f, 1.0f))));

        SPLASH_POS.set((float) WaterSplash.originX(), (float) BrineDimensions.SEA_LEVEL, (float) WaterSplash.originZ());
        SPLASH_AGE.set(WaterSplash.age(level, tickDelta));
        SPLASH_POWER.set(WaterSplash.power());
        SPLASH_ENTER.set(WaterSplash.entering() ? 1.0f : 0.0f);
        EXIT_WATER.set(WaterSplash.exitAmount(level, tickDelta));

        List<FlashlightBeams.Beam> beams = FlashlightBeams.collect(client, camera, tickDelta);
        for (int i = 0; i < FlashlightBeams.MAX_BEAMS; i++) {
            if (i < beams.size()) {
                FlashlightBeams.Beam beam = beams.get(i);
                BEAM_POS[i].set(beam.position().x, beam.position().y, beam.position().z, 1.0f);
                BEAM_DIR[i].set(beam.direction());
            } else {
                BEAM_POS[i].set(0.0f, 0.0f, 0.0f, 0.0f);
                BEAM_DIR[i].set(0.0f, 0.0f, -1.0f);
            }
        }
        EFFECT.render(tickDelta);
    }
}
