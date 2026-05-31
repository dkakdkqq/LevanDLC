package com.levandlc.gui.hud;

import com.levandlc.LevanDLC;
import com.levandlc.gui.clickgui.Theme;
import com.levandlc.module.Module;
import com.levandlc.util.render.ColorUtil;
import com.levandlc.util.render.Renderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * In-game HUD overlay, drawn entirely through our own {@link Renderer}.
 *
 * <p>Shows a watermark and a live, color-cycling array-list of the currently
 * enabled modules (classic cheat-client style). It only renders when the
 * {@code HUD} module is toggled on, so the HUD module in the ClickGUI is itself
 * fully functional.
 *
 * <p>This is the single HUD entry point; it is registered from {@code LevanDLC}
 * via Fabric's HUD render event with an inferred-type lambda, so it never
 * hard-references a version-volatile render type.
 */
public final class Hud {

    private Hud() {
    }

    public static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return;
        }
        Module hud = LevanDLC.getModuleManager().getModule("HUD");
        if (hud == null || !hud.isEnabled()) {
            return;
        }

        Renderer r = Renderer.of(ctx);
        renderWatermark(r);
        renderArrayList(r);
    }

    private static void renderWatermark(Renderer r) {
        String title = "LevanDLC";
        int pad = 4;
        int w = Renderer.textWidth(title) + pad * 2 + 8;
        int h = Renderer.textHeight() + pad * 2;
        r.roundedRect(4, 4, w, h, Theme.CORNER, Theme.WINDOW_BG);
        r.circle(4 + pad + 3, 4 + h / 2f, 3, Theme.ACCENT);
        r.text(title, 4 + pad + 9, 4 + pad, Theme.TEXT, true);
    }

    private static void renderArrayList(Renderer r) {
        List<Module> enabled = new ArrayList<>();
        for (Module m : LevanDLC.getModuleManager().getModules()) {
            if (m.isEnabled() && !m.getName().equals("HUD")) {
                enabled.add(m);
            }
        }
        // Longest names on top - the classic array-list staircase.
        enabled.sort(Comparator.comparingInt((Module m) -> Renderer.textWidth(m.getName())).reversed());

        int screenW = Renderer.screenWidth();
        int rowH = Renderer.textHeight() + 3;
        int y = 4;
        int i = 0;
        for (Module m : enabled) {
            String name = m.getName();
            int tw = Renderer.textWidth(name);
            int x = screenW - tw - 6;
            int color = ColorUtil.rainbow(2400, i * 180, 0.8f, 1.0f);

            r.rect(x - 4, y, tw + 8, rowH, ColorUtil.rgba(15, 13, 20, 170));
            r.text(name, x, y + 2, color, true);
            r.rect(screenW - 2, y, 2, rowH, color);
            y += rowH;
            i++;
        }
    }
}
