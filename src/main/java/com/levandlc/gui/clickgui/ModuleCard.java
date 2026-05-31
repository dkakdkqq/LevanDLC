package com.levandlc.gui.clickgui;

import com.levandlc.module.Module;
import com.levandlc.util.animation.Animation;
import com.levandlc.util.render.ColorUtil;
import com.levandlc.util.render.Renderer;

/**
 * A single module card in the content grid (matching the reference screenshot):
 * a rounded panel with the module name, a short description, and an animated
 * toggle pill on the right that slides + tints purple when enabled.
 */
public class ModuleCard {

    private final Module module;

    private final Animation hover = new Animation(0.0, 14.0);
    private final Animation toggle;

    private float x;
    private float y;
    private float width;
    private final float height = Theme.CARD_H;

    public ModuleCard(Module module) {
        this.module = module;
        this.toggle = new Animation(module.isEnabled() ? 1.0 : 0.0, 14.0);
    }

    public Module getModule() {
        return module;
    }

    public void setBounds(float x, float y, float width) {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void render(Renderer r, int mouseX, int mouseY) {
        boolean isHover = Renderer.hovered(mouseX, mouseY, x, y, width, height);
        float h = (float) hover.animateTo(isHover ? 1.0 : 0.0);
        float t = (float) toggle.animateTo(module.isEnabled() ? 1.0 : 0.0);

        // Card background (lerp toward hover color).
        int bg = ColorUtil.interpolate(Theme.CARD_BG, Theme.CARD_BG_HOVER, h);
        r.roundedRect(x, y, width, height, Theme.CORNER, bg);

        // Left accent strip grows in while enabled.
        if (t > 0.01f) {
            int strip = ColorUtil.withAlpha(Theme.ACCENT, (int) (255 * t));
            r.roundedRect(x, y + 5, 3, height - 10, 1.5f, strip);
        }

        // Title.
        float textX = x + 10;
        int titleColor = ColorUtil.interpolate(Theme.TEXT, 0xFFFFFFFF, Math.max(h, t));
        r.text(module.getName(), textX, y + 6, titleColor, false);

        // Description (ellipsized to leave room for the pill).
        float pillW = 26;
        float descMaxW = width - 20 - pillW - 8;
        if (!module.getDescription().isEmpty() && descMaxW > 20) {
            String desc = r.ellipsize(module.getDescription(), (int) descMaxW);
            r.text(desc, textX, y + 6 + Renderer.textHeight() + 2, Theme.TEXT_MUTED, false);
        }

        // Toggle pill on the right.
        renderTogglePill(r, x + width - pillW - 9, y + (height - 12) / 2f, pillW, 12, t);

        // Keybind chip (small, above the pill) if bound.
        if (module.getKey() != org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN) {
            String key = Renderer.keyName(module.getKey());
            r.textRight(key, x + width - 9, y + height - Renderer.textHeight() - 3, Theme.TEXT_MUTED, false);
        }
    }

    private void renderTogglePill(Renderer r, float px, float py, float w, float h, float t) {
        // Track: grey -> purple.
        int track = ColorUtil.interpolate(Theme.DIVIDER, Theme.ACCENT, t);
        r.roundedRect(px, py, w, h, h / 2f, track);

        // Knob slides left -> right.
        float knobR = h / 2f - 1.5f;
        float knobCx = px + (h / 2f) + (w - h) * t;
        float knobCy = py + h / 2f;
        r.circle(knobCx, knobCy, knobR, 0xFFFFFFFF);
    }

    /** @return true if (mx,my) is over this card. */
    public boolean isOver(double mx, double my) {
        return Renderer.hovered(mx, my, x, y, width, height);
    }

    public void onClick() {
        module.toggle();
    }
}
