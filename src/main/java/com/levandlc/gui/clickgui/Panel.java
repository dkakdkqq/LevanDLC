package com.levandlc.gui.clickgui;

import com.levandlc.LevanDLC;
import com.levandlc.module.Category;
import com.levandlc.module.Module;
import com.levandlc.util.animation.Animation;
import com.levandlc.util.render.ColorUtil;
import com.levandlc.util.render.Render2D;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

/**
 * A draggable, collapsible category panel.
 *
 * <ul>
 *     <li>Left-click + drag the header to move the panel.</li>
 *     <li>Right-click the header to expand / collapse (animated).</li>
 *     <li>Left-click a module row to toggle it.</li>
 * </ul>
 *
 * <p>Click handling is driven by {@link ClickGuiScreen} polling GLFW mouse
 * state (edge-detected), so this class does not depend on the version-sensitive
 * {@code Screen} mouse-event signatures.
 */
public class Panel {

    public static final int HEADER_HEIGHT = 16;

    private final Category category;
    private final int width;
    private int x;
    private int y;

    private boolean open = true;
    private boolean dragging = false;
    private int grabX;
    private int grabY;

    private final Animation expand = new Animation(1.0, 16.0);
    private final List<ModuleButton> buttons = new ArrayList<>();

    public Panel(Category category, int x, int y, int width) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = width;
        for (Module module : LevanDLC.getModuleManager().getModules(category)) {
            buttons.add(new ModuleButton(module));
        }
    }

    public void render(DrawContext ctx, int mouseX, int mouseY) {
        if (dragging) {
            x = mouseX - grabX;
            y = mouseY - grabY;
        }

        float progress = (float) expand.animateTo(open ? 1.0 : 0.0);
        int fullBody = buttons.size() * ModuleButton.HEIGHT;
        int bodyHeight = Math.round(fullBody * progress);

        // Soft shadow behind the whole panel.
        Render2D.dropShadow(ctx, x, y, width, HEADER_HEIGHT + bodyHeight, 4,
                ColorUtil.rgba(0, 0, 0, 150));

        // Body (drawn first so the header overlaps its top edge).
        if (bodyHeight > 0) {
            int bodyTop = y + HEADER_HEIGHT;
            Render2D.rect(ctx, x, bodyTop, width, bodyHeight, ColorUtil.rgba(18, 18, 24, 235));

            // Render rows that fit within the currently revealed height.
            int rowY = bodyTop;
            for (ModuleButton button : buttons) {
                if (rowY + ModuleButton.HEIGHT > bodyTop + bodyHeight) {
                    break;
                }
                button.render(ctx, x, rowY, width, mouseX, mouseY);
                rowY += ModuleButton.HEIGHT;
            }
        }

        // Header.
        Render2D.roundedRect(ctx, x, y, width, HEADER_HEIGHT, 4, ColorUtil.rgb(28, 28, 38));
        Render2D.textVerticallyCentered(ctx, category.getDisplayName(), x + 7, y, HEADER_HEIGHT,
                0xFFFFFFFF, true);

        String indicator = open ? "-" : "+";
        Render2D.textRight(ctx, indicator, x + width - 7,
                y + (HEADER_HEIGHT - Render2D.textHeight()) / 2, ColorUtil.rgb(176, 176, 192), true);
    }

    /**
     * Handles a mouse press at (mouseX, mouseY).
     *
     * @param button 0 = left, 1 = right.
     * @return true if this panel consumed the click.
     */
    public boolean onMousePressed(double mouseX, double mouseY, int button) {
        if (Render2D.isHovered(mouseX, mouseY, x, y, width, HEADER_HEIGHT)) {
            if (button == 0) {
                dragging = true;
                grabX = (int) mouseX - x;
                grabY = (int) mouseY - y;
            } else if (button == 1) {
                open = !open;
            }
            return true;
        }

        // Only forward to rows once the panel is meaningfully open.
        if (open && expand.getValuef() > 0.5f) {
            int rowY = y + HEADER_HEIGHT;
            for (ModuleButton moduleButton : buttons) {
                if (Render2D.isHovered(mouseX, mouseY, x, rowY, width, ModuleButton.HEIGHT)) {
                    moduleButton.onClick(button);
                    return true;
                }
                rowY += ModuleButton.HEIGHT;
            }
        }
        return false;
    }

    public void onMouseReleased(int button) {
        if (button == 0) {
            dragging = false;
        }
    }
}
