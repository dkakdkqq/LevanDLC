package com.levandlc.util.render;

import java.awt.Color;

/**
 * Color helpers built around the packed ARGB {@code int} format used everywhere
 * in Minecraft's rendering pipeline ({@code 0xAARRGGBB}).
 *
 * <p>This class is completely independent of the Minecraft API, so it is safe
 * across every game version.
 */
public final class ColorUtil {

    private ColorUtil() {
    }

    // ------------------------------------------------------------------
    // Packing.
    // ------------------------------------------------------------------

    /** Packs the given 0-255 channels into an {@code 0xAARRGGBB} int. */
    public static int argb(int a, int r, int g, int b) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    /** Packs an opaque color from 0-255 channels. */
    public static int rgb(int r, int g, int b) {
        return argb(255, r, g, b);
    }

    /** Packs from 0-255 channels with explicit alpha (argument order r,g,b,a). */
    public static int rgba(int r, int g, int b, int a) {
        return argb(a, r, g, b);
    }

    /** Packs from normalized 0.0-1.0 float channels. */
    public static int rgba(float r, float g, float b, float a) {
        return argb(clamp255(a * 255f), clamp255(r * 255f), clamp255(g * 255f), clamp255(b * 255f));
    }

    // ------------------------------------------------------------------
    // Unpacking.
    // ------------------------------------------------------------------

    public static int alpha(int color) {
        return (color >> 24) & 0xFF;
    }

    public static int red(int color) {
        return (color >> 16) & 0xFF;
    }

    public static int green(int color) {
        return (color >> 8) & 0xFF;
    }

    public static int blue(int color) {
        return color & 0xFF;
    }

    /** @return {r, g, b, a} normalized to 0.0-1.0, ready for vertex {@code color()} calls. */
    public static float[] toFloats(int color) {
        return new float[] {
                red(color) / 255f,
                green(color) / 255f,
                blue(color) / 255f,
                alpha(color) / 255f
        };
    }

    // ------------------------------------------------------------------
    // Manipulation.
    // ------------------------------------------------------------------

    /** Returns the color with its alpha replaced (0-255). */
    public static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    /** Returns the color with its alpha replaced (0.0-1.0). */
    public static int withAlpha(int color, float alpha) {
        return withAlpha(color, clamp255(alpha * 255f));
    }

    /** Multiplies the existing alpha by {@code factor} (0.0-1.0). */
    public static int multiplyAlpha(int color, float factor) {
        return withAlpha(color, clamp255(alpha(color) * factor));
    }

    /**
     * Linearly interpolates between two ARGB colors.
     *
     * @param t blend factor, clamped to [0, 1] (0 = from, 1 = to).
     */
    public static int interpolate(int from, int to, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int a = Math.round(alpha(from) + (alpha(to) - alpha(from)) * t);
        int r = Math.round(red(from) + (red(to) - red(from)) * t);
        int g = Math.round(green(from) + (green(to) - green(from)) * t);
        int b = Math.round(blue(from) + (blue(to) - blue(from)) * t);
        return argb(a, r, g, b);
    }

    // ------------------------------------------------------------------
    // HSB / rainbow.
    // ------------------------------------------------------------------

    /** Builds an opaque color from hue/saturation/brightness (each 0.0-1.0). */
    public static int hsb(float hue, float saturation, float brightness) {
        return 0xFF000000 | (Color.HSBtoRGB(hue, saturation, brightness) & 0x00FFFFFF);
    }

    /**
     * Time-based rainbow color.
     *
     * @param periodMs   how many milliseconds one full hue cycle takes.
     * @param offset     per-element phase offset in milliseconds (e.g. index * 100)
     *                   so neighbouring elements get slightly different hues.
     * @param saturation 0.0-1.0
     * @param brightness 0.0-1.0
     */
    public static int rainbow(int periodMs, int offset, float saturation, float brightness) {
        if (periodMs <= 0) {
            periodMs = 1;
        }
        float hue = ((System.currentTimeMillis() + offset) % periodMs) / (float) periodMs;
        return hsb(hue, saturation, brightness);
    }

    /** Convenience rainbow: 2s period, full saturation/brightness. */
    public static int rainbow(int offset) {
        return rainbow(2000, offset, 1f, 1f);
    }

    private static int clamp255(float value) {
        return (int) Math.max(0f, Math.min(255f, value));
    }
}
