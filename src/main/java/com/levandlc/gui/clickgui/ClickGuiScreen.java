package com.levandlc.gui.clickgui;

import com.levandlc.module.Category;
import com.levandlc.util.render.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * The ClickGUI screen: a row of draggable, collapsible category {@link Panel}s.
 *
 * <p>Panels are stored statically so their positions and open/closed states
 * persist across openings (the screen instance itself is recreated each time).
 *
 * <p><b>Input handling:</b> instead of overriding the {@code Screen} mouse-event
 * methods (whose signatures changed to a {@code Click} record in recent 1.21.x
 * builds), this screen polls GLFW mouse buttons directly in {@link #render} and
 * performs its own press/release edge detection. This keeps the GUI compiling
 * across versions. ESC still closes via the default {@link Screen} behaviour.
 */
public class ClickGuiScreen extends Screen {

    private static final List<Panel> PANELS = new ArrayList<>();

    private boolean leftHeld = false;
    private boolean rightHeld = false;

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
        pollMouse(mouseX, mouseY);

        // Dim the world behind the GUI.
        context.fill(0, 0, this.width, this.height, ColorUtil.rgba(0, 0, 0, 120));

        for (Panel panel : PANELS) {
            panel.render(context, mouseX, mouseY);
        }
    }

    /** Polls GLFW mouse buttons and dispatches press/release edges to the panels. */
    private void pollMouse(int mouseX, int mouseY) {
        long handle = MinecraftClient.getInstance().getWindow().getHandle();

        boolean left = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean right = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

        if (left && !leftHeld) {
            dispatchPress(mouseX, mouseY, 0);
        } else if (!left && leftHeld) {
            dispatchRelease(0);
        }
        if (right && !rightHeld) {
            dispatchPress(mouseX, mouseY, 1);
        } else if (!right && rightHeld) {
            dispatchRelease(1);
        }

        leftHeld = left;
        rightHeld = right;
    }

    private void dispatchPress(int mouseX, int mouseY, int button) {
        for (Panel panel : PANELS) {
            if (panel.onMousePressed(mouseX, mouseY, button)) {
                return;
            }
        }
    }

    private void dispatchRelease(int button) {
        for (Panel panel : PANELS) {
            panel.onMouseReleased(button);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
