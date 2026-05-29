package com.levandlc.util.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * Shared render math used by both {@link Render2D} and {@link Render3D}:
 * interpolation, entity smoothing, and world-to-screen projection.
 *
 * <p>The projection helper takes the model-view and projection matrices as
 * arguments rather than fetching them itself, so the math here stays valid no
 * matter how matrix access changes between versions - capture them from your
 * world render event and pass them in.
 */
public final class RenderUtils {

    private RenderUtils() {
    }

    private static MinecraftClient mc() {
        return MinecraftClient.getInstance();
    }

    // ------------------------------------------------------------------
    // Interpolation.
    // ------------------------------------------------------------------

    public static double lerp(double start, double end, double t) {
        return start + (end - start) * t;
    }

    public static float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }

    /**
     * Smooth, render-tick-accurate position of an entity (kills the jitter you
     * get from using {@code entity.getPos()} directly while rendering).
     */
    public static Vec3d interpolatedPos(Entity entity, float tickDelta) {
        return new Vec3d(
                MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX()),
                MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY()),
                MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ()));
    }

    /**
     * Entity bounding box offset to its interpolated position - ideal as input to
     * {@link Render3D#boxOutline} / {@link Render3D#boxFilled}.
     */
    public static Box interpolatedBox(Entity entity, float tickDelta) {
        Vec3d pos = interpolatedPos(entity, tickDelta);
        Box box = entity.getBoundingBox();
        return box.offset(
                pos.x - entity.getX(),
                pos.y - entity.getY(),
                pos.z - entity.getZ());
    }

    // ------------------------------------------------------------------
    // World -> screen projection.
    // ------------------------------------------------------------------

    /**
     * Projects a world-space point onto the screen (scaled GUI coordinates).
     *
     * @param worldPos   the point in world space.
     * @param modelView  the camera model-view matrix for this frame.
     * @param projection the projection matrix for this frame.
     * @return {@code (screenX, screenY, depth)} or {@code null} if the point is
     *         behind the camera and should not be drawn.
     */
    public static Vec3d worldToScreen(Vec3d worldPos, Matrix4f modelView, Matrix4f projection) {
        Vec3d cam = mc().gameRenderer.getCamera().getPos();

        Vector4f clip = new Vector4f(
                (float) (worldPos.x - cam.x),
                (float) (worldPos.y - cam.y),
                (float) (worldPos.z - cam.z),
                1.0f);

        // clip = projection * modelView * pos
        clip.mul(modelView);
        clip.mul(projection);

        if (clip.w <= 0.0f) {
            return null; // Behind the camera.
        }

        // Perspective divide -> normalized device coordinates (-1 .. 1).
        float ndcX = clip.x / clip.w;
        float ndcY = clip.y / clip.w;

        int width = mc().getWindow().getScaledWidth();
        int height = mc().getWindow().getScaledHeight();

        float screenX = (ndcX * 0.5f + 0.5f) * width;
        float screenY = (1.0f - (ndcY * 0.5f + 0.5f)) * height; // Flip Y for screen space.

        return new Vec3d(screenX, screenY, clip.z / clip.w);
    }

    /** @return true if a projected point (from {@link #worldToScreen}) is within the screen bounds. */
    public static boolean isOnScreen(Vec3d screenPos) {
        if (screenPos == null) {
            return false;
        }
        return screenPos.x >= 0 && screenPos.x <= mc().getWindow().getScaledWidth()
                && screenPos.y >= 0 && screenPos.y <= mc().getWindow().getScaledHeight();
    }

    // ------------------------------------------------------------------
    // Misc.
    // ------------------------------------------------------------------

    /** Distance from the camera to a world-space point. */
    public static double distanceToCamera(Vec3d worldPos) {
        return mc().gameRenderer.getCamera().getPos().distanceTo(worldPos);
    }

    public static float tickDelta() {
        // [1.21.11 API] RenderTickCounter is the modern source of the frame delta.
        return mc().getRenderTickCounter().getTickProgress(true);
    }
}
