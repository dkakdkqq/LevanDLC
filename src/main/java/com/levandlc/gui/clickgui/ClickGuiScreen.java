package com.levandlc.gui.clickgui;

import com.levandlc.LevanDLC;
import com.levandlc.module.Category;
import com.levandlc.module.Module;
import com.levandlc.util.animation.Animation;
import com.levandlc.util.render.ColorUtil;
import com.levandlc.util.render.Renderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Celestial-style ClickGUI: a centered window with a left category sidebar and a
 * scrollable column of module cards on the right. No search bar.
 *
 * <p>Features: smooth mouse-wheel + scrollbar-drag scrolling, per-module keybinds
 * (right-click a card, then press a key), and inline settings panels (gear icon)
 * with sliders / toggles / mode-cyclers.
 *
 * <p>Input is GLFW-polled with our own edge detection for clicks and binding, so
 * we never override the version-sensitive {@code mouseClicked(Click)} /
 * {@code keyPressed(KeyInput)} methods. Scrolling uses the stable
 * {@code mouseScrolled(double,double,double,double)} override.
 *
 * <p>Everything draws through our own {@link Renderer}; this is the only GUI
 * piece that touches {@code DrawContext}.
 */
public class ClickGuiScreen extends Screen {

    private static Category selected = Category.COMBAT;

    private final List<ModuleCard> cards = new ArrayList<>();
    private final Animation scroll = new Animation(0.0, 18.0);
    private double scrollTarget = 0;
    private double maxScroll = 0;

    private float winX;
    private float winY;

    // Input edge-detection.
    private boolean leftHeld = false;
    private boolean rightHeld = false;
    private final boolean[] keyHeld = new boolean[GLFW.GLFW_KEY_LAST + 1];

    private final Animation[] catHover = new Animation[Category.values().length];

    // Geometry of the scrollable content region (set during render).
    private float gridX, gridY, gridW, gridH;

    public ClickGuiScreen() {
        super(Text.literal("LevanDLC"));
        for (int i = 0; i < catHover.length; i++) {
            catHover[i] = new Animation(0.0, 14.0);
        }
    }

    @Override
    protected void init() {
        winX = (this.width - Theme.WINDOW_W) / 2f;
        winY = (this.height - Theme.WINDOW_H) / 2f;
        rebuildCards();
    }

    private void rebuildCards() {
        cards.clear();
        for (Module module : LevanDLC.getModuleManager().getModules(selected)) {
            cards.add(new ModuleCard(module));
        }
        scrollTarget = 0;
    }

    /** @return the card currently in bind mode, or null. */
    private ModuleCard bindingCard() {
        for (ModuleCard c : cards) {
            if (c.isBinding()) {
                return c;
            }
        }
        return null;
    }

    // ==================================================================
    // Render
    // ==================================================================

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Renderer r = Renderer.of(context);

        pollInput(mouseX, mouseY);

        r.rect(0, 0, this.width, this.height, Theme.BACKDROP);
        r.shadow(winX, winY, Theme.WINDOW_W, Theme.WINDOW_H, 6, Theme.SHADOW);
        r.roundedRect(winX, winY, Theme.WINDOW_W, Theme.WINDOW_H, Theme.CORNER, Theme.WINDOW_BG);

        renderHeader(r);
        renderSidebar(r, mouseX, mouseY);
        renderContent(r, context, mouseX, mouseY);
    }

    private void renderHeader(Renderer r) {
        float cx = winX + Theme.PADDING;
        float cy = winY + 11;
        r.circle(cx + 4, cy + 4, 4.5f, Theme.ACCENT);
        r.text("LevanDLC", cx + 14, cy, Theme.TEXT, true);
        r.text(selected.getDisplayName(), winX + Theme.SIDEBAR_W + Theme.PADDING, cy, Theme.TEXT_DIM, false);
        r.textRight("v1.0", winX + Theme.WINDOW_W - 10, cy, Theme.TEXT_MUTED, false);
        r.hLine(winX, winX + Theme.WINDOW_W, winY + Theme.HEADER_H, Theme.DIVIDER);
    }

    private void renderSidebar(Renderer r, int mouseX, int mouseY) {
        float sx = winX;
        float sy = winY + Theme.HEADER_H;
        float sh = Theme.WINDOW_H - Theme.HEADER_H;
        r.rect(sx, sy, Theme.SIDEBAR_W, sh, Theme.SIDEBAR_BG);
        r.vLine(sx + Theme.SIDEBAR_W, sy, sy + sh, Theme.DIVIDER);

        Category[] cats = Category.values();
        float itemH = 28;
        // Categories sit a bit lower in the column (per request).
        float startY = sy + Theme.CATEGORY_TOP_OFFSET;
        for (int i = 0; i < cats.length; i++) {
            float iy = startY + i * itemH;
            boolean hov = Renderer.hovered(mouseX, mouseY, sx + 6, iy, Theme.SIDEBAR_W - 12, itemH - 4);
            boolean active = cats[i] == selected;
            float h = (float) catHover[i].animateTo(hov || active ? 1.0 : 0.0);

            if (active) {
                r.roundedRect(sx + 6, iy, Theme.SIDEBAR_W - 12, itemH - 4, 5, Theme.ACCENT_SOFT);
                r.roundedRect(sx + 6, iy + 4, 3, itemH - 12, 1.5f, Theme.ACCENT);
            }
            int col = ColorUtil.interpolate(Theme.TEXT_DIM, Theme.TEXT, h);
            r.textVCentered(cats[i].getDisplayName(), sx + 16, iy, itemH - 4, active ? Theme.TEXT : col, false);
        }

        r.text("[RShift] close", sx + 10, sy + sh - 14, Theme.TEXT_MUTED, false);
    }

    private void renderContent(Renderer r, DrawContext ctx, int mouseX, int mouseY) {
        float cx = winX + Theme.SIDEBAR_W;
        float cy = winY + Theme.HEADER_H;
        float cw = Theme.WINDOW_W - Theme.SIDEBAR_W;
        float ch = Theme.WINDOW_H - Theme.HEADER_H;
        r.rect(cx, cy, cw, ch, Theme.CONTENT_BG);

        gridX = cx + Theme.PADDING;
        gridY = cy + Theme.PADDING;
        gridW = cw - Theme.PADDING * 2 - 5; // leave room for scrollbar
        gridH = ch - Theme.PADDING * 2;

        // Lay cards out vertically, honouring each card's (dynamic) full height.
        double totalH = 0;
        for (ModuleCard card : cards) {
            totalH += card.getFullHeight() + Theme.CARD_GAP;
        }
        maxScroll = Math.max(0, totalH - gridH);
        scrollTarget = Math.max(0, Math.min(scrollTarget, maxScroll));
        float sOff = (float) scroll.animateTo(scrollTarget);

        ctx.enableScissor(Math.round(gridX), Math.round(gridY),
                Math.round(gridX + gridW), Math.round(gridY + gridH));

        float cursorY = gridY - sOff;
        for (ModuleCard card : cards) {
            float cardH = card.getFullHeight();
            card.setBounds(gridX, cursorY, gridW);
            if (cursorY + cardH >= gridY && cursorY <= gridY + gridH) {
                card.render(r, mouseX, mouseY);
            }
            cursorY += cardH + Theme.CARD_GAP;
        }

        ctx.disableScissor();

        // Scrollbar.
        if (maxScroll > 0) {
            float trackX = cx + cw - 4;
            float thumbH = Math.max(20, (float) (gridH * (gridH / totalH)));
            float thumbY = gridY + (float) (sOff / maxScroll) * (gridH - thumbH);
            r.roundedRect(trackX, gridY, 3, gridH, 1.5f, Theme.DIVIDER);
            r.roundedRect(trackX, thumbY, 3, thumbH, 1.5f, Theme.ACCENT);
        }
    }

    // ==================================================================
    // Input
    // ==================================================================

    private void pollInput(int mouseX, int mouseY) {
        long handle = MinecraftClient.getInstance().getWindow().getHandle();

        // If a card is waiting for a bind, capture the next key press.
        ModuleCard binding = bindingCard();
        if (binding != null) {
            int pressed = firstPressedKey(handle);
            if (pressed != GLFW.GLFW_KEY_UNKNOWN) {
                binding.applyBind(pressed);
            }
            // Swallow clicks while binding.
            leftHeld = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
            rightHeld = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
            return;
        }

        boolean left = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        if (left && !leftHeld) {
            handleClick(mouseX, mouseY, true);
        }
        leftHeld = left;

        boolean right = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        if (right && !rightHeld) {
            handleClick(mouseX, mouseY, false);
        }
        rightHeld = right;
    }

    private void handleClick(int mouseX, int mouseY, boolean left) {
        // Sidebar category selection (left click only).
        if (left) {
            float sx = winX;
            float sy = winY + Theme.HEADER_H;
            Category[] cats = Category.values();
            float itemH = 28;
            float startY = sy + Theme.CATEGORY_TOP_OFFSET;
            for (int i = 0; i < cats.length; i++) {
                float iy = startY + i * itemH;
                if (Renderer.hovered(mouseX, mouseY, sx + 6, iy, Theme.SIDEBAR_W - 12, itemH - 4)) {
                    if (selected != cats[i]) {
                        selected = cats[i];
                        rebuildCards();
                    }
                    return;
                }
            }
        }

        // Cards (only within the content viewport).
        if (mouseX < gridX || mouseX > gridX + gridW || mouseY < gridY || mouseY > gridY + gridH) {
            return;
        }
        for (ModuleCard card : cards) {
            boolean consumed = left ? card.onLeftClick(mouseX, mouseY) : card.onRightClick(mouseX, mouseY);
            if (consumed) {
                return;
            }
        }
    }

    /** Returns the first key currently pressed (edge), else UNKNOWN. */
    private int firstPressedKey(long handle) {
        for (int key = 32; key <= GLFW.GLFW_KEY_LAST; key++) {
            boolean down = GLFW.glfwGetKey(handle, key) == GLFW.GLFW_PRESS;
            boolean was = keyHeld[key];
            keyHeld[key] = down;
            if (down && !was) {
                return key;
            }
        }
        return GLFW.GLFW_KEY_UNKNOWN;
    }

    // ==================================================================
    // Scroll
    // ==================================================================

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollTarget = Math.max(0, Math.min(scrollTarget - verticalAmount * 26, maxScroll));
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
