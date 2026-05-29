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
    // Screen dimensions.
    // ------------------------------------------------------------------

    public static int screenWidth() {
        return mc().getWindow().getScaledWidth();
    }

    public static int screenHeight() {
        return mc().getWindow().getScaledHeight();
    }
}
