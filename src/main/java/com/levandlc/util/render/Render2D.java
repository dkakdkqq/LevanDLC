package com.levandlc.util.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * High-level 2D / HUD drawing helpers built on top of {@link DrawContext}.
 *
 * <p>{@code DrawContext}'s primitive operations ({@code fill}, {@code drawBorder},
 * {@code drawText}, scissor) are stable across the 1.21.x line, so this class is
 * the most version-resilient way to draw on screen.
 *
 * <p><b>Version-sensitive areas are tagged with {@code [1.21.11 API]}.</b> The two
 * things that changed during 1.21.x are:
 * <ul>
 *     <li>2D transforms moved from {@code MatrixStack} to {@code Matrix3x2fStack}
 *         (JOML) around 1.21.6 - see {@link #pushMatrix(DrawContext)} below.</li>
 *     <li>{@code drawTexture} now requires a {@code RenderPipeline}/{@code RenderLayer}
 *         function as its first argument.</li>
 * </ul>
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

    /** 1px outline rectangle. */
    public static void outline(DrawContext ctx, int x, int y, int width, int height, int color) {
        ctx.drawBorder(x, y, width, height, color);
    }

    /** Filled rectangle with a separate outline color. */
    public static void rectOutlined(DrawContext ctx, int x, int y, int width, int height,
                                    int fillColor, int outlineColor) {
        rect(ctx, x, y, width, height, fillColor);
        outline(ctx, x, y, width, height, outlineColor);
    }

    // ------------------------------------------------------------------
    // Rounded rectangles.
    //
    // Built purely from horizontal fill() strips, so they need no custom
    // geometry / shaders and work on every Minecraft version.
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

            // Top row.
            ctx.fill(x + inset, y + i, x + width - inset, y + i + 1, color);
            // Bottom row (mirrored).
            ctx.fill(x + inset, y + height - 1 - i, x + width - inset, y + height - i, color);
        }
    }

    /** Rounded filled rectangle with a separate rounded "outline" drawn on top. */
    public static void roundedRectOutlined(DrawContext ctx, int x, int y, int width, int height,
                                           int radius, int fillColor, int outlineColor) {
        roundedRect(ctx, x, y, width, height, radius, fillColor);
        // Cheap outline: a slightly larger rounded rect behind would be ideal, but
        // overlaying a 1px frame on the straight edges reads well for GUI panels.
        ctx.fill(x + radius, y, x + width - radius, y + 1, outlineColor);
        ctx.fill(x + radius, y + height - 1, x + width - radius, y + height, outlineColor);
        ctx.fill(x, y + radius, x + 1, y + height - radius, outlineColor);
        ctx.fill(x + width - 1, y + radius, x + width, y + height - radius, outlineColor);
    }

    // ------------------------------------------------------------------
    // Lines.
    // ------------------------------------------------------------------

    public static void horizontalLine(DrawContext ctx, int x1, int x2, int y, int color) {
        ctx.drawHorizontalLine(x1, x2, y, color);
    }

    public static void verticalLine(DrawContext ctx, int x, int y1, int y2, int color) {
        ctx.drawVerticalLine(x, y1, y2, color);
    }

    // ------------------------------------------------------------------
    // Gradients.
    // ------------------------------------------------------------------

    /** Vertical gradient (top color -> bottom color). */
    public static void gradientVertical(DrawContext ctx, int x, int y, int width, int height,
                                        int topColor, int bottomColor) {
        ctx.fillGradient(x, y, x + width, y + height, topColor, bottomColor);
    }

    /**
     * Horizontal gradient (left color -> right color).
     *
     * <p>Implemented as per-column strips using {@code fill} so it works on every
     * version without depending on the gradient render layer overloads.
     */
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
    // Scissor (clipping).
    // ------------------------------------------------------------------

    /** Restricts subsequent drawing to the given rectangle. Pair with {@link #unscissor(DrawContext)}. */
    public static void scissor(DrawContext ctx, int x, int y, int width, int height) {
        ctx.enableScissor(x, y, x + width, y + height);
    }

    public static void unscissor(DrawContext ctx) {
        ctx.disableScissor();
    }

    // ------------------------------------------------------------------
    // Transforms.
    // ------------------------------------------------------------------
    //
    // [1.21.11 API] As of ~1.21.6, DrawContext#getMatrices() returns a JOML
    // Matrix3x2fStack (2D) instead of the old MatrixStack. The calls below use
    // that newer API. If you target an earlier 1.21.x build, swap to:
    //     ctx.getMatrices().push();  /  pop();  /  translate(x, y, z);  /  scale(x, y, z);
    // ------------------------------------------------------------------

    public static void pushMatrix(DrawContext ctx) {
        ctx.getMatrices().pushMatrix();
    }

    public static void popMatrix(DrawContext ctx) {
        ctx.getMatrices().popMatrix();
    }

    public static void translate(DrawContext ctx, float x, float y) {
        ctx.getMatrices().translate(x, y);
    }

    public static void scale(DrawContext ctx, float x, float y) {
        ctx.getMatrices().scale(x, y);
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
     * frames so it needs no blur shader (works on every version).
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
            float t = i / (float) spread;            // 1 at the outer edge, ~0 near the box
            int alpha = Math.round(baseAlpha * (1f - t) * 0.5f);
            int ring = ColorUtil.withAlpha(color, alpha);
            ctx.drawBorder(x - i, y - i, width + i * 2, height + i * 2, ring);
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
                ? ColorUtil.interpolate(0xFFFFFF00, 0xFF00FF00, (fraction - 0.5f) * 2f)  // yellow -> green
                : ColorUtil.interpolate(0xFFFF0000, 0xFFFFFF00, fraction * 2f);          // red -> yellow
        progressBar(ctx, x, y, width, height, fraction, backgroundColor, color);
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
