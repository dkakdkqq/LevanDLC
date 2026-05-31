package com.levandlc.util.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * High-level 2D / HUD drawing helpers built on top of {@link DrawContext}.
 *
 * <p>Design goal: <b>maximum version resilience</b>. Every primitive here is
 * implemented using only the two most stable {@code DrawContext} operations -
 * {@link DrawContext#fill(int, int, int, int, int)} and
 * {@code drawText(...)}. Borders, gradients, lines and circles are all composed
 * from {@code fill} rectangles, so this class avoids the parts of the rendering
 * API that change between 1.21.x builds (gradient render layers, the 2D matrix
 * stack, scissor stack internals, etc.).
 */
public final class Render2D {

    private Render2D() {
    }

    private static MinecraftClient mc() {
        return MinecraftClient.getInstance();
    }

    // ------------------------------------------------------------------
    // Rectangles.
    // ------------------------------------------------------------------

    /** Filled rectangle from corner (x1,y1) to (x2,y2). */
    public static void fill(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        ctx.fill(x1, y1, x2, y2, color);
    }

    /** Filled rectangle from a top-left position with width/height. */
    public static void rect(DrawContext ctx, int x, int y, int width, int height, int color) {
        ctx.fill(x, y, x + width, y + height, color);
    }

    /** 1px outline rectangle, composed from four fills. */
    public static void outline(DrawContext ctx, int x, int y, int width, int height, int color) {
        ctx.fill(x, y, x + width, y + 1, color);                       // top
        ctx.fill(x, y + height - 1, x + width, y + height, color);     // bottom
        ctx.fill(x, y, x + 1, y + height, color);                      // left
        ctx.fill(x + width - 1, y, x + width, y + height, color);      // right
    }

    /** Filled rectangle with a separate outline color. */
    public static void rectOutlined(DrawContext ctx, int x, int y, int width, int height,
                                    int fillColor, int outlineColor) {
        rect(ctx, x, y, width, height, fillColor);
        outline(ctx, x, y, width, height, outlineColor);
    }

    // ------------------------------------------------------------------
    // Rounded rectangles (horizontal fill() strips - version-safe).
    // ------------------------------------------------------------------

    /** Filled rectangle with rounded corners of the given radius. */
    public static void roundedRect(DrawContext ctx, int x, int y, int width, int height,
                                   int radius, int color) {
        if (width <= 0 || height <= 0) {
            return;
        }
        int r = Math.max(0, Math.min(radius, Math.min(width, height) / 2));
        if (r == 0) {
            ctx.fill(x, y, x + width, y + height, color);
            return;
        }

        // Straight middle section spanning the full width.
        ctx.fill(x, y + r, x + width, y + height - r, color);

        // Rounded caps: one scan-line per pixel row of the corner radius.
        for (int i = 0; i < r; i++) {
            double dy = r - (i + 0.5);
            int inset = (int) Math.round(r - Math.sqrt(r * r - dy * dy));
            ctx.fill(x + inset, y + i, x + width - inset, y + i + 1, color);                       // top row
            ctx.fill(x + inset, y + height - 1 - i, x + width - inset, y + height - i, color);      // bottom row
        }
    }

    /** Rounded filled rectangle with a 1px outline drawn on the straight edges. */
    public static void roundedRectOutlined(DrawContext ctx, int x, int y, int width, int height,
                                           int radius, int fillColor, int outlineColor) {
        roundedRect(ctx, x, y, width, height, radius, fillColor);
        ctx.fill(x + radius, y, x + width - radius, y + 1, outlineColor);
        ctx.fill(x + radius, y + height - 1, x + width - radius, y + height, outlineColor);
        ctx.fill(x, y + radius, x + 1, y + height - radius, outlineColor);
        ctx.fill(x + width - 1, y + radius, x + width, y + height - radius, outlineColor);
    }

    // ------------------------------------------------------------------
    // Lines (composed from fills).
    // ------------------------------------------------------------------

    public static void horizontalLine(DrawContext ctx, int x1, int x2, int y, int color) {
        int from = Math.min(x1, x2);
        int to = Math.max(x1, x2);
        ctx.fill(from, y, to + 1, y + 1, color);
    }

    public static void verticalLine(DrawContext ctx, int x, int y1, int y2, int color) {
        int from = Math.min(y1, y2);
        int to = Math.max(y1, y2);
        ctx.fill(x, from, x + 1, to + 1, color);
    }

    // ------------------------------------------------------------------
    // Gradients (per-strip fill interpolation).
    // ------------------------------------------------------------------

    /** Vertical gradient (top color -> bottom color). */
    public static void gradientVertical(DrawContext ctx, int x, int y, int width, int height,
                                        int topColor, int bottomColor) {
        if (height <= 0) {
            return;
        }
        for (int i = 0; i < height; i++) {
            float t = height == 1 ? 0f : i / (float) (height - 1);
            ctx.fill(x, y + i, x + width, y + i + 1, ColorUtil.interpolate(topColor, bottomColor, t));
        }
    }

    /** Horizontal gradient (left color -> right color). */
    public static void gradientHorizontal(DrawContext ctx, int x, int y, int width, int height,
                                          int leftColor, int rightColor) {
        if (width <= 0) {
            return;
        }
        for (int i = 0; i < width; i++) {
            float t = width == 1 ? 0f : i / (float) (width - 1);
            ctx.fill(x + i, y, x + i + 1, y + height, ColorUtil.interpolate(leftColor, rightColor, t));
        }
    }

    // ------------------------------------------------------------------
    // Text.
    // ------------------------------------------------------------------

    public static void text(DrawContext ctx, String text, int x, int y, int color, boolean shadow) {
        ctx.drawText(mc().textRenderer, text, x, y, color, shadow);
    }

    /** Draws text horizontally centered on {@code centerX}. */
    public static void textCentered(DrawContext ctx, String text, int centerX, int y, int color, boolean shadow) {
        ctx.drawText(mc().textRenderer, text, centerX - textWidth(text) / 2, y, color, shadow);
    }

    /** Right-aligned text (useful for HUD array-list module names). */
    public static void textRight(DrawContext ctx, String text, int rightX, int y, int color, boolean shadow) {
        ctx.drawText(mc().textRenderer, text, rightX - textWidth(text), y, color, shadow);
    }

    /** Vertically centers text within a row of {@code rowHeight} starting at {@code y}. */
    public static void textVerticallyCentered(DrawContext ctx, String text, int x, int y, int rowHeight,
                                               int color, boolean shadow) {
        int ty = y + (rowHeight - textHeight()) / 2;
        ctx.drawText(mc().textRenderer, text, x, ty, color, shadow);
    }

    public static int textWidth(String text) {
        return mc().textRenderer.getWidth(text);
    }

    public static int textHeight() {
        return mc().textRenderer.fontHeight;
    }

    public static TextRenderer textRenderer() {
        return mc().textRenderer;
    }

    // ------------------------------------------------------------------
    // ClickGUI / HUD helpers.
    // ------------------------------------------------------------------

    /** Axis-aligned hit test - true if (mx,my) is inside the given rectangle. */
    public static boolean isHovered(double mx, double my, int x, int y, int width, int height) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }

    /**
     * Soft drop shadow behind a rectangle, faked with concentric translucent
     * frames (built from {@link #outline}) so it needs no blur shader.
     *
     * @param spread how many pixels the shadow extends outward.
     * @param color  base shadow color; its alpha is faded out across the spread.
     */
    public static void dropShadow(DrawContext ctx, int x, int y, int width, int height,
                                  int spread, int color) {
        if (spread <= 0) {
            return;
        }
        int baseAlpha = ColorUtil.alpha(color);
        for (int i = spread; i >= 1; i--) {
            float t = i / (float) spread;
            int alpha = Math.round(baseAlpha * (1f - t) * 0.5f);
            outline(ctx, x - i, y - i, width + i * 2, height + i * 2, ColorUtil.withAlpha(color, alpha));
        }
    }

    /**
     * Horizontal progress / health bar with a background track.
     *
     * @param progress 0.0-1.0 fill fraction.
     */
    public static void progressBar(DrawContext ctx, int x, int y, int width, int height,
                                   float progress, int backgroundColor, int fillColor) {
        progress = Math.max(0f, Math.min(1f, progress));
        ctx.fill(x, y, x + width, y + height, backgroundColor);
        int filled = Math.round(width * progress);
        if (filled > 0) {
            ctx.fill(x, y, x + filled, y + height, fillColor);
        }
    }

    /**
     * Health bar whose color blends green -> yellow -> red as health drops.
     *
     * @param fraction current / max health, 0.0-1.0.
     */
    public static void healthBar(DrawContext ctx, int x, int y, int width, int height,
                                 float fraction, int backgroundColor) {
        fraction = Math.max(0f, Math.min(1f, fraction));
        int color = fraction > 0.5f
                ? ColorUtil.interpolate(0xFFFFFF00, 0xFF00FF00, (fraction - 0.5f) * 2f)
                : ColorUtil.interpolate(0xFFFF0000, 0xFFFFFF00, fraction * 2f);
        progressBar(ctx, x, y, width, height, fraction, backgroundColor, color);
    }

    // ------------------------------------------------------------------
    // Circles (scan-line based, fill()-only - version-safe).
    // ------------------------------------------------------------------

    /** Filled circle centered at (cx, cy). Great for HUD indicators / dots. */
    public static void circleFilled(DrawContext ctx, int cx, int cy, int radius, int color) {
        if (radius <= 0) {
            return;
        }
        for (int dy = -radius; dy <= radius; dy++) {
            int dx = (int) Math.round(Math.sqrt((double) radius * radius - (double) dy * dy));
            ctx.fill(cx - dx, cy + dy, cx + dx, cy + dy + 1, color);
        }
    }

    /** Ring / circle outline of the given thickness. */
    public static void circleOutline(DrawContext ctx, int cx, int cy, int radius, int thickness, int color) {
        if (radius <= 0 || thickness <= 0) {
            return;
        }
        int inner = Math.max(0, radius - thickness);
        for (int dy = -radius; dy <= radius; dy++) {
            int outerDx = (int) Math.round(Math.sqrt((double) radius * radius - (double) dy * dy));
            double innerSq = (double) inner * inner - (double) dy * dy;
            if (innerSq > 0) {
                int innerDx = (int) Math.round(Math.sqrt(innerSq));
                ctx.fill(cx - outerDx, cy + dy, cx - innerDx, cy + dy + 1, color);
                ctx.fill(cx + innerDx, cy + dy, cx + outerDx, cy + dy + 1, color);
            } else {
                ctx.fill(cx - outerDx, cy + dy, cx + outerDx, cy + dy + 1, color);
            }
        }
    }

    // ------------------------------------------------------------------
    // Screen dimensions.
    // ------------------------------------------------------------------

    public static int screenWidth() {
        return mc().getWindow().getScaledWidth();
    }

    public static int screenHeight() {
        return mc().getWindow().getScaledHeight();
    }
}
