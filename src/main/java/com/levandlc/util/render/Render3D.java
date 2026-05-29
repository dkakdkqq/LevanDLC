package com.levandlc.util.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * World-space (3D) drawing helpers: boxes, lines and tracers for things like ESP.
 *
 * <p>All coordinates are given in absolute world space; this class converts them
 * to camera-relative space internally (every world render is drawn relative to the
 * camera at the origin).
 *
 * <p>Geometry is fed into Minecraft's built-in {@link RenderLayer}s through a
 * {@link VertexConsumerProvider.Immediate}, which lets the engine own the shader /
 * {@code RenderPipeline} and GL state instead of us binding them manually.
 *
 * <p><b>Version-sensitive areas are tagged {@code [1.21.11 API]}:</b>
 * <ul>
 *     <li>The {@code MatrixStack} passed in comes from the world render event,
 *         whose shape changed in 1.21.10 ("extraction vs rendering" split).</li>
 *     <li>{@link #filledLayer()} - the translucent quad layer name varies between
 *         1.21.x builds; change it in one place if it does not resolve.</li>
 * </ul>
 */
public final class Render3D {

    private Render3D() {
    }

    private static MinecraftClient mc() {
        return MinecraftClient.getInstance();
    }

    /** @return the current camera position in world space. */
    public static Vec3d cameraPos() {
        return mc().gameRenderer.getCamera().getPos();
    }

    // ------------------------------------------------------------------
    // Render layers (the single most version-sensitive spot).
    // ------------------------------------------------------------------

    /** Built-in layer for 3D lines (expects position + color + normal per vertex). */
    private static RenderLayer linesLayer() {
        return RenderLayer.getLines();
    }

    /**
     * [1.21.11 API] Translucent filled-quad layer.
     * If this does not resolve on your mappings, swap for the current equivalent
     * (e.g. a custom POSITION_COLOR layer, or {@code RenderLayer.getDebugFilledBox()}).
     */
    private static RenderLayer filledLayer() {
        return RenderLayer.getDebugQuads();
    }

    // ------------------------------------------------------------------
    // Boxes.
    // ------------------------------------------------------------------

    /** Draws the 12 edges of a world-space box as lines. */
    public static void boxOutline(MatrixStack matrices, Box box, int color) {
        VertexConsumerProvider.Immediate immediate = mc().getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer buffer = immediate.getBuffer(linesLayer());
        addBoxEdges(matrices, buffer, toCameraRelative(box), color);
        immediate.draw();
    }

    /** Draws all 6 faces of a world-space box as translucent filled quads. */
    public static void boxFilled(MatrixStack matrices, Box box, int color) {
        VertexConsumerProvider.Immediate immediate = mc().getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer buffer = immediate.getBuffer(filledLayer());
        addBoxQuads(matrices, buffer, toCameraRelative(box), color);
        immediate.draw();
    }

    /** Convenience: filled box + a (usually more opaque) outline in one call. */
    public static void box(MatrixStack matrices, Box box, int fillColor, int outlineColor) {
        boxFilled(matrices, box, fillColor);
        boxOutline(matrices, box, outlineColor);
    }

    // ------------------------------------------------------------------
    // Lines / tracers.
    // ------------------------------------------------------------------

    /** Draws a single line between two world-space points. */
    public static void line(MatrixStack matrices, Vec3d from, Vec3d to, int color) {
        Vec3d cam = cameraPos();
        VertexConsumerProvider.Immediate immediate = mc().getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer buffer = immediate.getBuffer(linesLayer());
        addLine(matrices, buffer,
                from.x - cam.x, from.y - cam.y, from.z - cam.z,
                to.x - cam.x, to.y - cam.y, to.z - cam.z, color);
        immediate.draw();
    }

    /**
     * Draws a tracer from just in front of the camera to a world-space target,
     * the classic "follow my crosshair" line.
     */
    public static void tracer(MatrixStack matrices, Vec3d target, int color) {
        Camera camera = mc().gameRenderer.getCamera();
        Vec3d cam = camera.getPos();
        // Start one block along the view direction so the line clears the near plane.
        Vec3d look = Vec3d.fromPolar(camera.getPitch(), camera.getYaw());
        Vec3d rel = target.subtract(cam);

        VertexConsumerProvider.Immediate immediate = mc().getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer buffer = immediate.getBuffer(linesLayer());
        addLine(matrices, buffer, look.x, look.y, look.z, rel.x, rel.y, rel.z, color);
        immediate.draw();
    }

    // ------------------------------------------------------------------
    // ESP conveniences.
    // ------------------------------------------------------------------

    /**
     * Draws an ESP box for an entity at its smooth, interpolated position
     * (filled + outline). Pass the frame's tick delta from your render event.
     */
    public static void entityBox(MatrixStack matrices, net.minecraft.entity.Entity entity,
                                 float tickDelta, int fillColor, int outlineColor) {
        Box box = RenderUtils.interpolatedBox(entity, tickDelta);
        box(matrices, box, fillColor, outlineColor);
    }

    /** Tracer aimed at the interpolated center of an entity. */
    public static void entityTracer(MatrixStack matrices, net.minecraft.entity.Entity entity,
                                    float tickDelta, int color) {
        tracer(matrices, RenderUtils.interpolatedBox(entity, tickDelta).getCenter(), color);
    }

    /**
     * Sets the GL line width for subsequent line draws.
     *
     * <p>[1.21.11 API] {@code RenderSystem.lineWidth} still exists, but the modern
     * line render layer often ignores it in favour of a normal-based expansion;
     * if your lines stay 1px, build a custom line layer with the desired width.
     */
    public static void lineWidth(float width) {
        com.mojang.blaze3d.systems.RenderSystem.lineWidth(width);
    }

    // ------------------------------------------------------------------
    // Geometry builders.
    // ------------------------------------------------------------------

    private static Box toCameraRelative(Box box) {
        Vec3d cam = cameraPos();
        return box.offset(-cam.x, -cam.y, -cam.z);
    }

    private static void addLine(MatrixStack matrices, VertexConsumer buffer,
                                double x1, double y1, double z1,
                                double x2, double y2, double z2, int color) {
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f position = entry.getPositionMatrix();

        float nx = (float) (x2 - x1);
        float ny = (float) (y2 - y1);
        float nz = (float) (z2 - z1);
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len != 0f) {
            nx /= len;
            ny /= len;
            nz /= len;
        }

        buffer.vertex(position, (float) x1, (float) y1, (float) z1).color(color).normal(entry, nx, ny, nz);
        buffer.vertex(position, (float) x2, (float) y2, (float) z2).color(color).normal(entry, nx, ny, nz);
    }

    private static void addBoxEdges(MatrixStack matrices, VertexConsumer buffer, Box b, int color) {
        double x1 = b.minX, y1 = b.minY, z1 = b.minZ;
        double x2 = b.maxX, y2 = b.maxY, z2 = b.maxZ;

        // Bottom face.
        addLine(matrices, buffer, x1, y1, z1, x2, y1, z1, color);
        addLine(matrices, buffer, x2, y1, z1, x2, y1, z2, color);
        addLine(matrices, buffer, x2, y1, z2, x1, y1, z2, color);
        addLine(matrices, buffer, x1, y1, z2, x1, y1, z1, color);

        // Top face.
        addLine(matrices, buffer, x1, y2, z1, x2, y2, z1, color);
        addLine(matrices, buffer, x2, y2, z1, x2, y2, z2, color);
        addLine(matrices, buffer, x2, y2, z2, x1, y2, z2, color);
        addLine(matrices, buffer, x1, y2, z2, x1, y2, z1, color);

        // Vertical edges.
        addLine(matrices, buffer, x1, y1, z1, x1, y2, z1, color);
        addLine(matrices, buffer, x2, y1, z1, x2, y2, z1, color);
        addLine(matrices, buffer, x2, y1, z2, x2, y2, z2, color);
        addLine(matrices, buffer, x1, y1, z2, x1, y2, z2, color);
    }

    private static void addBoxQuads(MatrixStack matrices, VertexConsumer buffer, Box b, int color) {
        Matrix4f m = matrices.peek().getPositionMatrix();
        float x1 = (float) b.minX, y1 = (float) b.minY, z1 = (float) b.minZ;
        float x2 = (float) b.maxX, y2 = (float) b.maxY, z2 = (float) b.maxZ;

        // Down (-Y)
        quad(buffer, m, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, color);
        // Up (+Y)
        quad(buffer, m, x1, y2, z1, x1, y2, z2, x2, y2, z2, x2, y2, z1, color);
        // North (-Z)
        quad(buffer, m, x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1, color);
        // South (+Z)
        quad(buffer, m, x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2, color);
        // West (-X)
        quad(buffer, m, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, color);
        // East (+X)
        quad(buffer, m, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, color);
    }

    private static void quad(VertexConsumer buffer, Matrix4f m,
                             float ax, float ay, float az,
                             float bx, float by, float bz,
                             float cx, float cy, float cz,
                             float dx, float dy, float dz, int color) {
        buffer.vertex(m, ax, ay, az).color(color);
        buffer.vertex(m, bx, by, bz).color(color);
        buffer.vertex(m, cx, cy, cz).color(color);
        buffer.vertex(m, dx, dy, dz).color(color);
    }
}
