package com.levandlc.util.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

/**
 * LevanDLC's own 2D rendering engine.
 *
 * <p><b>This is the single isolation layer between the mod and Minecraft.</b>
 * Every other class (the ClickGUI, HUD, etc.) draws through {@code Renderer}
 * using our own method names and our own primitives - rectangles, rounded
 * rectangles, gradients, circles, text. Nothing else imports {@code DrawContext}.
 *
 * <p>Internally every primitive is composed from the two most stable
 * {@code DrawContext} operations - {@code fill(...)} and {@code drawText(...)} -
 * so the whole UI is resilient to Minecraft render-API churn between 1.21.x
 * builds. If the engine ever changes, you fix it here, in one file.
 *
 * <p>Usage: wrap the {@code DrawContext} you receive in a screen's render method:
 * <pre>{@code
 * Renderer r = Renderer.of(drawContext);
 * r.rect(x, y, w, h, color);
 * }</pre>
 */
public final class Renderer {

    private final DrawContext ctx;

    private Renderer(DrawContext ctx) {
        this.ctx = ctx;
    }

    /** Wraps a Minecraft draw context in our renderer. */
    public static Renderer of(DrawContext ctx) {
        return new Renderer(ctx);
    }

    private static MinecraftClient mc() {
        return MinecraftClient.getInstance();
    }

    // ==================================================================
    // Rectangles
    // ==================================================================

    /** Filled rectangle at (x, y) with the given size. */
    public Renderer rect(float x, float y, float width, float height, int color) {
        ctx.fill(Math.round(x), Math.round(y), Math.round(x + width), Math.round(y + height), color);
        return this;
    }

    /** Filled rectangle between two corners. */
    public Renderer rectCorners(float x1, float y1, float x2, float y2, int color) {
        ctx.fill(Math.round(x1), Math.round(y1), Math.round(x2), Math.round(y2), color);
        return this;
    }

    /** 1px (or thicker) outline rectangle. */
    public Renderer outline(float x, float y, float width, float height, float thickness, int color) {
        int t = Math.max(1, Math.round(thickness));
        rect(x, y, width, t, color);                       // top
        rect(x, y + height - t, width, t, color);          // bottom
        rect(x, y, t, height, color);                      // left
        rect(x + width - t, y, t, height, color);          // right
        return this;
    }

    // ==================================================================
    // Rounded rectangles (scan-line corners, fill()-only)
    // ==================================================================

    public Renderer roundedRect(float x, float y, float width, float height, float radius, int color) {
        int xi = Math.round(x);
        int yi = Math.round(y);
        int w = Math.round(width);
        int h = Math.round(height);
        if (w <= 0 || h <= 0) {
            return this;
        }
        int r = Math.max(0, Math.min(Math.round(radius), Math.min(w, h) / 2));
        if (r == 0) {
            ctx.fill(xi, yi, xi + w, yi + h, color);
            return this;
        }

        ctx.fill(xi, yi + r, xi + w, yi + h - r, color); // central column

        for (int i = 0; i < r; i++) {
            double dy = r - (i + 0.5);
            int inset = (int) Math.round(r - Math.sqrt((double) r * r - dy * dy));
            ctx.fill(xi + inset, yi + i, xi + w - inset, yi + i + 1, color);                 // top cap row
            ctx.fill(xi + inset, yi + h - 1 - i, xi + w - inset, yi + h - i, color);          // bottom cap row
        }
        return this;
    }

    /** Rounded rect with a 1px outline drawn over its straight edges. */
    public Renderer roundedRectOutlined(float x, float y, float width, float height, float radius,
                                        int fillColor, int outlineColor) {
        roundedRect(x, y, width, height, radius, fillColor);
        int xi = Math.round(x);
        int yi = Math.round(y);
        int w = Math.round(width);
        int h = Math.round(height);
        int r = Math.round(radius);
        ctx.fill(xi + r, yi, xi + w - r, yi + 1, outlineColor);
        ctx.fill(xi + r, yi + h - 1, xi + w - r, yi + h, outlineColor);
        ctx.fill(xi, yi + r, xi + 1, yi + h - r, outlineColor);
        ctx.fill(xi + w - 1, yi + r, xi + w, yi + h - r, outlineColor);
        return this;
    }

    // ==================================================================
    // Gradients (per-strip interpolation)
    // ==================================================================

    public Renderer gradientV(float x, float y, float width, float height, int top, int bottom) {
        int h = Math.round(height);
        if (h <= 0) {
            return this;
        }
        int xi = Math.round(x);
        int yi = Math.round(y);
        int w = Math.round(width);
        for (int i = 0; i < h; i++) {
            float t = h == 1 ? 0f : i / (float) (h - 1);
            ctx.fill(xi, yi + i, xi + w, yi + i + 1, ColorUtil.interpolate(top, bottom, t));
        }
        return this;
    }

    public Renderer gradientH(float x, float y, float width, float height, int left, int right) {
        int w = Math.round(width);
        if (w <= 0) {
            return this;
        }
        int xi = Math.round(x);
        int yi = Math.round(y);
        int h = Math.round(height);
        for (int i = 0; i < w; i++) {
            float t = w == 1 ? 0f : i / (float) (w - 1);
            ctx.fill(xi + i, yi, xi + i + 1, yi + h, ColorUtil.interpolate(left, right, t));
        }
        return this;
    }

    // ==================================================================
    // Lines
    // ==================================================================

    public Renderer hLine(float x1, float x2, float y, int color) {
        int from = Math.round(Math.min(x1, x2));
        int to = Math.round(Math.max(x1, x2));
        ctx.fill(from, Math.round(y), to, Math.round(y) + 1, color);
        return this;
    }

    public Renderer vLine(float x, float y1, float y2, int color) {
        int from = Math.round(Math.min(y1, y2));
        int to = Math.round(Math.max(y1, y2));
        ctx.fill(Math.round(x), from, Math.round(x) + 1, to, color);
        return this;
    }

    // ==================================================================
    // Circles (scan-line)
    // ==================================================================

    public Renderer circle(float cx, float cy, float radius, int color) {
        int r = Math.round(radius);
        if (r <= 0) {
            return this;
        }
        int cxi = Math.round(cx);
        int cyi = Math.round(cy);
        for (int dy = -r; dy <= r; dy++) {
            int dx = (int) Math.round(Math.sqrt((double) r * r - (double) dy * dy));
            ctx.fill(cxi - dx, cyi + dy, cxi + dx, cyi + dy + 1, color);
        }
        return this;
    }

    public Renderer circleOutline(float cx, float cy, float radius, float thickness, int color) {
        int r = Math.round(radius);
        int th = Math.round(thickness);
        if (r <= 0 || th <= 0) {
            return this;
        }
        int cxi = Math.round(cx);
        int cyi = Math.round(cy);
        int inner = Math.max(0, r - th);
        for (int dy = -r; dy <= r; dy++) {
            int outerDx = (int) Math.round(Math.sqrt((double) r * r - (double) dy * dy));
            double innerSq = (double) inner * inner - (double) dy * dy;
            if (innerSq > 0) {
                int innerDx = (int) Math.round(Math.sqrt(innerSq));
                ctx.fill(cxi - outerDx, cyi + dy, cxi - innerDx, cyi + dy + 1, color);
                ctx.fill(cxi + innerDx, cyi + dy, cxi + outerDx, cyi + dy + 1, color);
            } else {
                ctx.fill(cxi - outerDx, cyi + dy, cxi + outerDx, cyi + dy + 1, color);
            }
        }
        return this;
    }

    // ==================================================================
    // Shadows / glow
    // ==================================================================

    /** Soft drop shadow built from concentric fading outlines (no blur shader). */
    public Renderer shadow(float x, float y, float width, float height, int spread, int baseColor) {
        if (spread <= 0) {
            return this;
        }
        int baseAlpha = ColorUtil.alpha(baseColor);
        for (int i = spread; i >= 1; i--) {
            float t = i / (float) spread;
            int alpha = Math.round(baseAlpha * (1f - t) * 0.5f);
            outline(x - i, y - i, width + i * 2, height + i * 2, 1, ColorUtil.withAlpha(baseColor, alpha));
        }
        return this;
    }

    // ==================================================================
    // Text
    // ==================================================================

    public Renderer text(String text, float x, float y, int color, boolean shadow) {
        ctx.drawText(mc().textRenderer, text, Math.round(x), Math.round(y), color, shadow);
        return this;
    }

    public Renderer textCentered(String text, float centerX, float y, int color, boolean shadow) {
        ctx.drawText(mc().textRenderer, text, Math.round(centerX - textWidth(text) / 2f),
                Math.round(y), color, shadow);
        return this;
    }

    public Renderer textRight(String text, float rightX, float y, int color, boolean shadow) {
        ctx.drawText(mc().textRenderer, text, Math.round(rightX - textWidth(text)),
                Math.round(y), color, shadow);
        return this;
    }

    public Renderer textVCentered(String text, float x, float y, float rowHeight, int color, boolean shadow) {
        float ty = y + (rowHeight - textHeight()) / 2f;
        ctx.drawText(mc().textRenderer, text, Math.round(x), Math.round(ty), color, shadow);
        return this;
    }

    /** Truncates {@code text} with an ellipsis so it fits within {@code maxWidth}. */
    public String ellipsize(String text, int maxWidth) {
        if (textWidth(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        int ellipsisW = textWidth(ellipsis);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if (textWidth(sb.toString() + text.charAt(i)) + ellipsisW > maxWidth) {
                break;
            }
            sb.append(text.charAt(i));
        }
        return sb + ellipsis;
    }

    public static int textWidth(String text) {
        return mc().textRenderer.getWidth(text);
    }

    public static int textHeight() {
        return mc().textRenderer.fontHeight;
    }

    // ==================================================================
    // Helpers
    // ==================================================================

    public static int screenWidth() {
        return mc().getWindow().getScaledWidth();
    }

    public static int screenHeight() {
        return mc().getWindow().getScaledHeight();
    }

    public static boolean hovered(double mx, double my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    /** Human-readable name for a GLFW key code, or "NONE". */
    public static String keyName(int key) {
        if (key == GLFW.GLFW_KEY_UNKNOWN) {
            return "NONE";
        }
        String name = GLFW.glfwGetKeyName(key, 0);
        if (name != null) {
            return name.toUpperCase();
        }
        return switch (key) {
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL";
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            default -> "KEY" + key;
        };
    }
}
