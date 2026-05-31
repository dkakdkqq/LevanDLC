package com.levandlc.gui.clickgui;

import com.levandlc.module.Module;
import com.levandlc.module.setting.BooleanSetting;
import com.levandlc.module.setting.ModeSetting;
import com.levandlc.module.setting.NumberSetting;
import com.levandlc.module.setting.Setting;
import com.levandlc.util.animation.Animation;
import com.levandlc.util.render.ColorUtil;
import com.levandlc.util.render.Renderer;
import org.lwjgl.glfw.GLFW;

/**
 * A module card: name, description, animated toggle pill, a gear that expands an
 * inline settings panel, a keybind chip, and a "bind mode" state in which the
 * next pressed key becomes the module's toggle keybind.
 *
 * <p>Heights are dynamic: {@link #getFullHeight()} grows when the settings panel
 * is open so the parent grid can lay cards out and scroll correctly.
 */
public class ModuleCard {

    private static final float ROW_H = 14f;       // height of one setting row
    private static final float SETTINGS_PAD = 4f;

    private final Module module;

    private final Animation hover = new Animation(0.0, 14.0);
    private final Animation toggle;
    private final Animation expand = new Animation(0.0, 16.0);

    private boolean settingsOpen = false;
    private boolean binding = false;

    private float x;
    private float y;
    private float width;

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

    public boolean isBinding() {
        return binding;
    }

    public void setBinding(boolean binding) {
        this.binding = binding;
    }

    /** Total height including the (animated) settings panel. */
    public float getFullHeight() {
        return Theme.CARD_H + (float) (settingsPanelHeight() * expand.getValue());
    }

    private double settingsPanelHeight() {
        if (!module.hasSettings()) {
            return 0;
        }
        return SETTINGS_PAD * 2 + module.getSettings().size() * ROW_H;
    }

    public void render(Renderer r, int mouseX, int mouseY) {
        boolean isHover = Renderer.hovered(mouseX, mouseY, x, y, width, Theme.CARD_H);
        float h = (float) hover.animateTo(isHover ? 1.0 : 0.0);
        float t = (float) toggle.animateTo(module.isEnabled() ? 1.0 : 0.0);
        float e = (float) expand.animateTo(settingsOpen ? 1.0 : 0.0);

        float fullH = getFullHeight();

        // Card background spanning the whole (expanded) height.
        int bg = ColorUtil.interpolate(Theme.CARD_BG, Theme.CARD_BG_HOVER, h);
        r.roundedRect(x, y, width, fullH, Theme.CORNER, bg);

        // Left accent strip while enabled.
        if (t > 0.01f) {
            int strip = ColorUtil.withAlpha(Theme.ACCENT, (int) (255 * t));
            r.roundedRect(x, y + 5, 3, Theme.CARD_H - 10, 1.5f, strip);
        }

        // Title (or "Press a key..." while binding).
        float textX = x + 10;
        int titleColor = ColorUtil.interpolate(Theme.TEXT, 0xFFFFFFFF, Math.max(h, t));
        String title = binding ? "Press a key..." : module.getName();
        r.text(title, textX, y + 5, binding ? Theme.ACCENT : titleColor, false);

        // Description line.
        float gearW = 12;
        float pillW = 22;
        float rightReserved = pillW + gearW + 14;
        float descMaxW = width - 14 - rightReserved;
        if (!binding && !module.getDescription().isEmpty() && descMaxW > 20) {
            String desc = r.ellipsize(module.getDescription(), (int) descMaxW);
            r.text(desc, textX, y + 5 + Renderer.textHeight() + 1, Theme.TEXT_MUTED, false);
        }

        // Toggle pill.
        float pillX = x + width - pillW - 9;
        float pillY = y + (Theme.CARD_H - 11) / 2f;
        renderTogglePill(r, pillX, pillY, pillW, 11, t);

        // Gear (left of the pill) if the module has settings.
        if (module.hasSettings()) {
            float gx = pillX - gearW - 4;
            float gy = y + Theme.CARD_H / 2f;
            int gearCol = settingsOpen ? Theme.ACCENT : Theme.TEXT_DIM;
            r.circleOutline(gx + gearW / 2f, gy, gearW / 2f - 1, 1.5f, gearCol);
            r.circle(gx + gearW / 2f, gy, 1.5f, gearCol);
        }

        // Keybind chip (bottom-right of the row) if bound.
        if (module.getKey() != GLFW.GLFW_KEY_UNKNOWN && !binding) {
            String key = "[" + Renderer.keyName(module.getKey()) + "]";
            r.textRight(key, x + width - 9, y + Theme.CARD_H - Renderer.textHeight() - 2, Theme.TEXT_MUTED, false);
        }

        // Settings panel (clipped by the parent grid scissor).
        if (e > 0.01f) {
            renderSettings(r, mouseX, mouseY, e);
        }
    }

    private void renderSettings(Renderer r, int mouseX, int mouseY, float e) {
        float sx = x + 10;
        float sy = y + Theme.CARD_H + SETTINGS_PAD;
        float sw = width - 20;

        // Divider above the settings.
        r.hLine(x + 8, x + width - 8, y + Theme.CARD_H, Theme.DIVIDER);

        int i = 0;
        for (Setting setting : module.getSettings()) {
            float ry = sy + i * ROW_H;
            if (setting instanceof NumberSetting ns) {
                renderNumber(r, ns, sx, ry, sw);
            } else if (setting instanceof BooleanSetting bs) {
                renderBoolean(r, bs, sx, ry, sw);
            } else if (setting instanceof ModeSetting ms) {
                renderMode(r, ms, sx, ry, sw);
            }
            i++;
        }
    }

    private void renderNumber(Renderer r, NumberSetting ns, float sx, float ry, float sw) {
        r.text(ns.getName(), sx, ry + 2, Theme.TEXT_DIM, false);
        String val = ns.isInteger()
                ? String.valueOf((int) ns.getValue())
                : String.format("%.2f", ns.getValue());
        r.textRight(val, sx + sw, ry + 2, Theme.TEXT, false);

        // Slider track + filled portion.
        float trackY = ry + ROW_H - 4;
        float trackW = sw;
        r.roundedRect(sx, trackY, trackW, 2, 1, Theme.DIVIDER);
        float fillW = (float) (trackW * ns.getFraction());
        r.roundedRect(sx, trackY, fillW, 2, 1, Theme.ACCENT);
        r.circle(sx + fillW, trackY + 1, 2.5f, 0xFFFFFFFF);
    }

    private void renderBoolean(Renderer r, BooleanSetting bs, float sx, float ry, float sw) {
        r.text(bs.getName(), sx, ry + 3, Theme.TEXT_DIM, false);
        float bw = 16, bh = 9;
        float bx = sx + sw - bw;
        float by = ry + 2;
        float t = bs.getValue() ? 1f : 0f;
        int track = ColorUtil.interpolate(Theme.DIVIDER, Theme.ACCENT, t);
        r.roundedRect(bx, by, bw, bh, bh / 2f, track);
        float knobCx = bx + bh / 2f + (bw - bh) * t;
        r.circle(knobCx, by + bh / 2f, bh / 2f - 1.5f, 0xFFFFFFFF);
    }

    private void renderMode(Renderer r, ModeSetting ms, float sx, float ry, float sw) {
        r.text(ms.getName(), sx, ry + 3, Theme.TEXT_DIM, false);
        // Value chip on the right; click cycles.
        String v = ms.getValue();
        int chipW = Renderer.textWidth(v) + 12;
        float chipX = sx + sw - chipW;
        r.roundedRect(chipX, ry + 1, chipW, ROW_H - 3, 3, Theme.ACCENT_SOFT);
        r.textCentered(v, chipX + chipW / 2f, ry + 3, Theme.ACCENT, false);
    }

    private void renderTogglePill(Renderer r, float px, float py, float w, float h, float t) {
        int track = ColorUtil.interpolate(Theme.DIVIDER, Theme.ACCENT, t);
        r.roundedRect(px, py, w, h, h / 2f, track);
        float knobR = h / 2f - 1.5f;
        float knobCx = px + (h / 2f) + (w - h) * t;
        float knobCy = py + h / 2f;
        r.circle(knobCx, knobCy, knobR, 0xFFFFFFFF);
    }

    // ------------------------------------------------------------------
    // Input (called by the screen)
    // ------------------------------------------------------------------

    /** @return true if (mx,my) is over the main card row (not the settings panel). */
    public boolean isOverRow(double mx, double my) {
        return Renderer.hovered(mx, my, x, y, width, Theme.CARD_H);
    }

    /** Handles a left click. Returns true if consumed. */
    public boolean onLeftClick(double mx, double my) {
        if (!isOverRow(mx, my)) {
            // Maybe a settings row was clicked.
            return settingsOpen && onSettingsClick(mx, my, true);
        }
        // Gear toggles the settings panel.
        if (module.hasSettings()) {
            float pillW = 22, gearW = 12;
            float pillX = x + width - pillW - 9;
            float gx = pillX - gearW - 4;
            if (Renderer.hovered(mx, my, gx, y, gearW + 4, Theme.CARD_H)) {
                settingsOpen = !settingsOpen;
                return true;
            }
        }
        // Otherwise toggle the module.
        module.toggle();
        return true;
    }

    /** Right click enters bind mode. Returns true if consumed. */
    public boolean onRightClick(double mx, double my) {
        if (isOverRow(mx, my)) {
            binding = true;
            return true;
        }
        if (settingsOpen) {
            return onSettingsClick(mx, my, false);
        }
        return false;
    }

    /** Handles a click inside the settings panel. */
    private boolean onSettingsClick(double mx, double my, boolean left) {
        float sx = x + 10;
        float sy = y + Theme.CARD_H + SETTINGS_PAD;
        float sw = width - 20;
        int i = 0;
        for (Setting setting : module.getSettings()) {
            float ry = sy + i * ROW_H;
            boolean overRow = Renderer.hovered(mx, my, sx, ry, sw, ROW_H);
            if (overRow) {
                if (setting instanceof NumberSetting ns && left) {
                    double frac = (mx - sx) / sw;
                    ns.setFromFraction(frac);
                    return true;
                } else if (setting instanceof BooleanSetting bs && left) {
                    bs.toggle();
                    return true;
                } else if (setting instanceof ModeSetting ms) {
                    if (left) {
                        ms.cycle();
                    } else {
                        ms.cycleBack();
                    }
                    return true;
                }
            }
            i++;
        }
        return false;
    }

    /** Called by the screen while binding; sets the key and exits bind mode. */
    public void applyBind(int key) {
        // ESC clears the bind.
        module.setKey(key == GLFW.GLFW_KEY_ESCAPE ? GLFW.GLFW_KEY_UNKNOWN : key);
        binding = false;
    }
}
