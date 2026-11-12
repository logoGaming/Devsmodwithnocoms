package io.github.logogaming.brine.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

public final class GaugeRenderer {

    private static final float FEATHER = 0.9f;

    private GaugeRenderer() {
    }

    public static void disc(GuiGraphics graphics, float cx, float cy, float radius, int argb) {
        Matrix4f matrix = graphics.pose().last().pose();
        int segments = 48;
        begin();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(matrix, cx, cy, 0.0f).setColor(argb);
        for (int i = 0; i <= segments; i++) {
            double angle = Math.PI * 2.0 * i / segments;
            buffer.addVertex(matrix, cx + (float) Math.cos(angle) * radius, cy + (float) Math.sin(angle) * radius, 0.0f).setColor(argb);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        end();
    }

    public static void ring(GuiGraphics graphics, float cx, float cy, float inner, float outer, float fraction, int argb) {
        float clamped = Math.max(0.0f, Math.min(1.0f, fraction));
        if (clamped <= 0.0f) {
            return;
        }
        double start = -Math.PI / 2.0;
        double sweep = Math.PI * 2.0 * clamped;
        int segments = Math.max(2, (int) Math.ceil(64.0 * clamped));
        int faded = argb & 0x00FFFFFF;

        begin();
        strip(graphics, cx, cy, inner, outer, start, sweep, segments, argb, argb);
        strip(graphics, cx, cy, outer, outer + FEATHER, start, sweep, segments, argb, faded);
        strip(graphics, cx, cy, inner - FEATHER, inner, start, sweep, segments, faded, argb);
        end();
    }

    private static float[][] roundedPath(float x, float y, float w, float h, float radius) {
        int arc = 6;
        float r = Math.max(0.3f, Math.min(radius, Math.min(w, h) / 2.0f));
        float[][] path = new float[(arc + 1) * 4][];
        float[][] centers = {{x + w - r, y + r}, {x + w - r, y + h - r}, {x + r, y + h - r}, {x + r, y + r}};
        int index = 0;
        for (int corner = 0; corner < 4; corner++) {
            for (int i = 0; i <= arc; i++) {
                double angle = -Math.PI / 2.0 + Math.PI / 2.0 * (corner + (double) i / arc);
                path[index++] = new float[]{centers[corner][0] + (float) Math.cos(angle) * r, centers[corner][1] + (float) Math.sin(angle) * r};
            }
        }
        return path;
    }

    public static void roundedRect(GuiGraphics graphics, float x, float y, float w, float h, float radius, int argb) {
        Matrix4f matrix = graphics.pose().last().pose();
        float[][] path = roundedPath(x, y, w, h, radius);
        begin();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(matrix, x + w / 2.0f, y + h / 2.0f, 0.0f).setColor(argb);
        for (float[] point : path) {
            buffer.addVertex(matrix, point[0], point[1], 0.0f).setColor(argb);
        }
        buffer.addVertex(matrix, path[0][0], path[0][1], 0.0f).setColor(argb);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        end();
    }

    private static int lerpColor(int top, int bottom, float t) {
        int a = (int) (((top >>> 24) & 0xFF) + (((bottom >>> 24) & 0xFF) - ((top >>> 24) & 0xFF)) * t);
        int r = (int) (((top >> 16) & 0xFF) + (((bottom >> 16) & 0xFF) - ((top >> 16) & 0xFF)) * t);
        int g = (int) (((top >> 8) & 0xFF) + (((bottom >> 8) & 0xFF) - ((top >> 8) & 0xFF)) * t);
        int b = (int) ((top & 0xFF) + ((bottom & 0xFF) - (top & 0xFF)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static void roundedRectGradient(GuiGraphics graphics, float x, float y, float w, float h, float radius, int top, int bottom) {
        Matrix4f matrix = graphics.pose().last().pose();
        float[][] path = roundedPath(x, y, w, h, radius);
        begin();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(matrix, x + w / 2.0f, y + h / 2.0f, 0.0f).setColor(lerpColor(top, bottom, 0.5f));
        for (int i = 0; i <= path.length; i++) {
            float[] point = path[i % path.length];
            buffer.addVertex(matrix, point[0], point[1], 0.0f).setColor(lerpColor(top, bottom, (point[1] - y) / h));
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        end();
    }

    public static void roundedOutlineGradient(GuiGraphics graphics, float x, float y, float w, float h, float radius, float thickness, int top, int bottom) {
        Matrix4f matrix = graphics.pose().last().pose();
        float[][] outer = roundedPath(x, y, w, h, radius);
        float[][] inner = roundedPath(x + thickness, y + thickness, w - thickness * 2.0f, h - thickness * 2.0f, radius - thickness);
        begin();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= outer.length; i++) {
            int k = i % outer.length;
            buffer.addVertex(matrix, outer[k][0], outer[k][1], 0.0f).setColor(lerpColor(top, bottom, (outer[k][1] - y) / h));
            buffer.addVertex(matrix, inner[k][0], inner[k][1], 0.0f).setColor(lerpColor(top, bottom, (inner[k][1] - y) / h));
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        end();
    }

    public static void roundedOutline(GuiGraphics graphics, float x, float y, float w, float h, float radius, float thickness, int argb) {
        Matrix4f matrix = graphics.pose().last().pose();
        float[][] outer = roundedPath(x, y, w, h, radius);
        float[][] inner = roundedPath(x + thickness, y + thickness, w - thickness * 2.0f, h - thickness * 2.0f, radius - thickness);
        begin();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= outer.length; i++) {
            int k = i % outer.length;
            buffer.addVertex(matrix, outer[k][0], outer[k][1], 0.0f).setColor(argb);
            buffer.addVertex(matrix, inner[k][0], inner[k][1], 0.0f).setColor(argb);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        end();
    }

    private static void strip(GuiGraphics graphics, float cx, float cy, float innerRadius, float outerRadius,
                              double start, double sweep, int segments, int innerColor, int outerColor) {
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            double angle = start + sweep * i / segments;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            buffer.addVertex(matrix, cx + cos * outerRadius, cy + sin * outerRadius, 0.0f).setColor(outerColor);
            buffer.addVertex(matrix, cx + cos * innerRadius, cy + sin * innerRadius, 0.0f).setColor(innerColor);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void begin() {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
    }

    private static void end() {
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}
