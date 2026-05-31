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
 * A module card.
 *
 * <ul>
 *     <li><b>Left click</b> - toggle the module on/off.</li>
 *     <li><b>Right click</b> - open/close the inline settings panel.</li>
 *     <li><b>Middle click</b> - enter bind mode; the next key becomes its keybind.</li>
 * </ul>
 *
 * <p>An enabled module is shown by <b>filling the whole card with the accent
 * color</b> (a gradient wash), not by a toggle pill.
 *
 * <p>Heights are dynamic: {@link #getFullHeight()} grows when the settings panel
 * is open so the parent grid can lay cards out and scroll correctly.
 */
public class ModuleCard {

    private static final float ROW_H = 14f;       // height of one setting row
    private static final float SETTINGS_PAD = 4f;

    private final Module module;

    private final Animation hover = new Animation(0.0, 14.0);
    private final Animation enable;               // 0 = off, 1 = on (fill amount)
    private final Animation expand = new Animation(0.0, 16.0);

    private boolean settingsOpen = false;
    private boolean binding = false;

    private float x;
    private float y;
    private float width;

    public ModuleCard(Module module) {
        this.module = module;
        this.enable = new Animation(module.isEnabled() ? 1.0 : 0.0, 13.0);
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
        float on = (float) enable.animateTo(module.isEnabled() ? 1.0 : 0.0);
        float e = (float) expand.animateTo(settingsOpen ? 1.0 : 0.0);

        float fullH = getFullHeight();

        // Base background (hover-lerped) for the whole, possibly expanded, card.
        int base = ColorUtil.interpolate(Theme.CARD_BG, Theme.CARD_BG_HOVER, h);
        r.roundedRect(x, y, width, fullH, Theme.CORNER, base);

        // ENABLED = fill the row with an accent gradient wash (alpha follows `on`).
        if (on > 0.01f) {
            int g1 = ColorUtil.withAlpha(Theme.accentGradientStart(), (int) (235 * on));
            int g2 = ColorUtil.withAlpha(Theme.accentGradientEnd(), (int) (235 * on));
            // Rounded clip approximated by drawing the gradient then re-rounding corners.
            r.gradientH(x, y, width, Theme.CARD_H, g1, g2);
            // Re-apply rounded corners over the gradient using the window bg.
            paintCornerNotches(r, x, y, width, Theme.CARD_H, Theme.CORNER, Theme.CONTENT_BG);
            // A subtle left accent bar stays crisp.
            r.roundedRect(x + 3, y + 5, 2, Theme.CARD_H - 10, 1f, 0x66FFFFFF);
        }

        // Text colors flip to white-ish when enabled for contrast on the fill.
        int titleColor = on > 0.5f
                ? 0xFFFFFFFF
                : ColorUtil.interpolate(Theme.TEXT, 0xFFFFFFFF, h);
        int descColor = on > 0.5f ? 0xFFE9DCFF : Theme.TEXT_MUTED;

        float textX = x + 11;
        String title = binding ? "Press a key..." : module.getName();
        r.text(title, textX, y + 5, binding ? Theme.ACCENT : titleColor, false);

        // Description.
        float gearW = 12;
        float rightReserved = gearW + 16;
        float descMaxW = width - 16 - rightReserved;
        if (!binding && !module.getDescription().isEmpty() && descMaxW > 20) {
            String desc = r.ellipsize(module.getDescription(), (int) descMaxW);
            r.text(desc, textX, y + 5 + Renderer.textHeight() + 1, descColor, false);
        }

        // Gear (settings indicator) on the right if the module has settings.
        if (module.hasSettings()) {
            float gx = x + width - gearW - 9;
            float gy = y + Theme.CARD_H / 2f;
            int gearCol = settingsOpen ? 0xFFFFFFFF : (on > 0.5f ? 0xFFE9DCFF : Theme.TEXT_DIM);
            r.circleOutline(gx + gearW / 2f, gy, gearW / 2f - 1, 1.5f, gearCol);
            r.circle(gx + gearW / 2f, gy, 1.5f, gearCol);
        }

        // Keybind chip.
        if (module.getKey() != GLFW.GLFW_KEY_UNKNOWN && !binding) {
            String key = "[" + Renderer.keyName(module.getKey()) + "]";
            int chipCol = on > 0.5f ? 0xFFE9DCFF : Theme.TEXT_MUTED;
            float chipRightX = module.hasSettings() ? x + width - gearW - 16 : x + width - 9;
            r.textRight(key, chipRightX, y + Theme.CARD_H - Renderer.textHeight() - 2, chipCol, false);
        }

        if (e > 0.01f) {
            renderSettings(r, mouseX, mouseY);
        }
    }

    /** Paints the 4 corner "notches" with bg color to fake a rounded clip over a gradient. */
    private void paintCornerNotches(Renderer r, float x, float y, float w, float h, float radius, int bg) {
        int rr = Math.round(radius);
        for (int i = 0; i < rr; i++) {
            double dy = rr - (i + 0.5);
            int inset = (int) Math.round(rr - Math.sqrt((double) rr * rr - dy * dy));
            if (inset <= 0) {
                continue;
            }
            // top-left / top-right
            r.rect(x, y + i, inset, 1, bg);
            r.rect(x + w - inset, y + i, inset, 1, bg);
            // bottom-left / bottom-right
            r.rect(x, y + h - 1 - i, inset, 1, bg);
            r.rect(x + w - inset, y + h - 1 - i, inset, 1, bg);
        }
    }

    private void renderSettings(Renderer r, int mouseX, int mouseY) {
        float sx = x + 11;
        float sy = y + Theme.CARD_H + SETTINGS_PAD;
        float sw = width - 22;

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

        float trackY = ry + ROW_H - 4;
        r.roundedRect(sx, trackY, sw, 2, 1, Theme.DIVIDER);
        float fillW = (float) (sw * ns.getFraction());
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
        String v = ms.getValue();
        int chipW = Renderer.textWidth(v) + 12;
        float chipX = sx + sw - chipW;
        r.roundedRect(chipX, ry + 1, chipW, ROW_H - 3, 3, Theme.ACCENT_SOFT);
        r.textCentered(v, chipX + chipW / 2f, ry + 3, Theme.ACCENT, false);
    }

    // ------------------------------------------------------------------
    // Input (called by the screen)
    // ------------------------------------------------------------------

    public boolean isOverRow(double mx, double my) {
        return Renderer.hovered(mx, my, x, y, width, Theme.CARD_H);
    }

    /** Left click: toggle module (or adjust a setting row if the panel is open). */
    public boolean onLeftClick(double mx, double my) {
        if (isOverRow(mx, my)) {
            module.toggle();
            return true;
        }
        return settingsOpen && onSettingsClick(mx, my, true);
    }

    /** Right click: open/close settings (or adjust a mode setting if panel is open). */
    public boolean onRightClick(double mx, double my) {
        if (isOverRow(mx, my)) {
            if (module.hasSettings()) {
                settingsOpen = !settingsOpen;
            }
            return true;
        }
        return settingsOpen && onSettingsClick(mx, my, false);
    }

    /** Middle click: enter bind mode. */
    public boolean onMiddleClick(double mx, double my) {
        if (isOverRow(mx, my)) {
            binding = true;
            return true;
        }
        return false;
    }

    private boolean onSettingsClick(double mx, double my, boolean left) {
        float sx = x + 11;
        float sy = y + Theme.CARD_H + SETTINGS_PAD;
        float sw = width - 22;
        int i = 0;
        for (Setting setting : module.getSettings()) {
            float ry = sy + i * ROW_H;
            boolean overRow = Renderer.hovered(mx, my, sx, ry, sw, ROW_H);
            if (overRow) {
                if (setting instanceof NumberSetting ns && left) {
                    ns.setFromFraction((mx - sx) / sw);
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

    public void applyBind(int key) {
        module.setKey(key == GLFW.GLFW_KEY_ESCAPE ? GLFW.GLFW_KEY_UNKNOWN : key);
        binding = false;
    }
}
