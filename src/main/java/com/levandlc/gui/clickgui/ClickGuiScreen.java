package com.levandlc.gui.clickgui;

import com.levandlc.module.Category;
import com.levandlc.util.render.ColorUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * The ClickGUI screen: a row of draggable, collapsible category {@link Panel}s.
 *
 * <p>Panels are stored statically so their positions and open/closed states
 * persist across openings (the screen instance itself is recreated each time).
 *
 * <p>Opening/closing is driven from the mod's tick handler (a toggle keybind);
 * ESC also closes via the default {@link Screen} behaviour.
 */
public class ClickGuiScreen extends Screen {

    private static final List<Panel> PANELS = new ArrayList<>();

    public ClickGuiScreen() {
        super(Text.literal("LevanDLC ClickGUI"));
        if (PANELS.isEmpty()) {
            buildPanels();
        }
    }

    private void buildPanels() {
        int x = 12;
        int y = 12;
        for (Category category : Category.values()) {
            PANELS.add(new Panel(category, x, y, 110));
            x += 122;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Dim the world behind the GUI.
        context.fill(0, 0, this.width, this.height, ColorUtil.rgba(0, 0, 0, 120));

        for (Panel panel : PANELS) {
            panel.render(context, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Panel panel : PANELS) {
            if (panel.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Panel panel : PANELS) {
            panel.mouseReleased(button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
