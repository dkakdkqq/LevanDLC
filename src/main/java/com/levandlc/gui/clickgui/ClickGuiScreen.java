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
 * Celestial-style ClickGUI: a fixed centered window with a left category
 * sidebar, a search field, and a scrollable grid of module cards on the right.
 *
 * <p><b>All input is polled from GLFW</b> (mouse buttons, scroll via an accessor,
 * and typed keys) with our own edge detection - the screen never overrides the
 * version-sensitive {@code mouseClicked(Click)} / {@code keyPressed(KeyInput)}
 * methods that changed in recent 1.21.x builds. ESC still closes via vanilla.
 *
 * <p>Everything is drawn through our own {@link Renderer}; this class is the only
 * GUI piece that receives a {@code DrawContext}, and it immediately wraps it.
 */
public class ClickGuiScreen extends Screen {

    // Persisted across openings.
    private static Category selected = Category.COMBAT;
    private static String search = "";

    private final List<ModuleCard> cards = new ArrayList<>();
    private final Animation scroll = new Animation(0.0, 18.0);
    private double scrollTarget = 0;
    private double maxScroll = 0;

    // Window position (centered, computed in init()).
    private float winX;
    private float winY;

    // Input edge-detection.
    private boolean leftHeld = false;
    private final boolean[] keyHeld = new boolean[GLFW.GLFW_KEY_LAST + 1];

    // Category sidebar hover animations.
    private final Animation[] catHover = new Animation[Category.values().length];

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

    /** Rebuilds the visible card list for the selected category + search filter. */
    private void rebuildCards() {
        cards.clear();
        String q = search.toLowerCase();
        for (Module module : LevanDLC.getModuleManager().getModules(selected)) {
            if (q.isEmpty()
                    || module.getName().toLowerCase().contains(q)
                    || module.getDescription().toLowerCase().contains(q)) {
                cards.add(new ModuleCard(module));
            }
        }
        scrollTarget = 0;
    }

    // ==================================================================
    // Render
    // ==================================================================

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Renderer r = Renderer.of(context);

        pollInput(mouseX, mouseY);

        // Backdrop dim.
        r.rect(0, 0, this.width, this.height, Theme.BACKDROP);

        // Window shadow + body.
        r.shadow(winX, winY, Theme.WINDOW_W, Theme.WINDOW_H, 6, Theme.SHADOW);
        r.roundedRect(winX, winY, Theme.WINDOW_W, Theme.WINDOW_H, Theme.CORNER, Theme.WINDOW_BG);

        renderHeader(r);
        renderSidebar(r, mouseX, mouseY);
        renderContent(r, context, mouseX, mouseY);
    }

    private void renderHeader(Renderer r) {
        float cx = winX + Theme.PADDING;
        float cy = winY + 10;
        // Accent dot + title.
        r.circle(cx + 4, cy + 5, 4.5f, Theme.ACCENT);
        r.text("LevanDLC", cx + 14, cy, Theme.TEXT, true);
        r.text("v1.0", winX + Theme.WINDOW_W - 34, cy, Theme.TEXT_MUTED, false);
        r.hLine(winX, winX + Theme.WINDOW_W, winY + Theme.HEADER_H, Theme.DIVIDER);
    }

    private void renderSidebar(Renderer r, int mouseX, int mouseY) {
        float sx = winX;
        float sy = winY + Theme.HEADER_H;
        float sh = Theme.WINDOW_H - Theme.HEADER_H;
        r.rect(sx, sy, Theme.SIDEBAR_W, sh, Theme.SIDEBAR_BG);
        r.vLine(sx + Theme.SIDEBAR_W, sy, sy + sh, Theme.DIVIDER);

        Category[] cats = Category.values();
        float itemH = 30;
        float startY = sy + 10;
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

        // Hint at the bottom.
        r.text("[RShift] close", sx + 10, sy + sh - 14, Theme.TEXT_MUTED, false);
    }

    private void renderContent(Renderer r, DrawContext ctx, int mouseX, int mouseY) {
        float cx = winX + Theme.SIDEBAR_W;
        float cy = winY + Theme.HEADER_H;
        float cw = Theme.WINDOW_W - Theme.SIDEBAR_W;
        float ch = Theme.WINDOW_H - Theme.HEADER_H;
        r.rect(cx, cy, cw, ch, Theme.CONTENT_BG);

        // Search field.
        float searchX = cx + Theme.PADDING;
        float searchY = cy + Theme.PADDING;
        float searchW = cw - Theme.PADDING * 2;
        r.roundedRect(searchX, searchY, searchW, Theme.SEARCH_H, 5, Theme.SEARCH_BG);
        r.circleOutline(searchX + 11, searchY + 11, 4, 1.5f, Theme.TEXT_MUTED);
        r.text("/", searchX + 14, searchY + 11, Theme.TEXT_MUTED, false);
        String shown = search.isEmpty() ? "Search..." : search;
        int searchColor = search.isEmpty() ? Theme.TEXT_MUTED : Theme.TEXT;
        // Blinking caret when typing.
        if (!search.isEmpty() && (System.currentTimeMillis() / 500) % 2 == 0) {
            shown = shown + "_";
        }
        r.textVCentered(shown, searchX + 20, searchY, Theme.SEARCH_H, searchColor, false);

        // Cards grid area (below search).
        float gridX = cx + Theme.PADDING;
        float gridY = searchY + Theme.SEARCH_H + Theme.CARD_GAP;
        float gridW = cw - Theme.PADDING * 2;
        float gridH = cy + ch - gridY - Theme.PADDING;

        // Two-column layout.
        int cols = 2;
        float colGap = Theme.CARD_GAP;
        float cardW = (gridW - colGap * (cols - 1)) / cols;

        // Compute total content height for scrolling.
        int rows = (cards.size() + cols - 1) / cols;
        double totalH = rows * (Theme.CARD_H + Theme.CARD_GAP);
        maxScroll = Math.max(0, totalH - gridH);
        scrollTarget = Math.max(0, Math.min(scrollTarget, maxScroll));
        float sOff = (float) scroll.animateTo(scrollTarget);

        // Clip to the grid with the vanilla scissor (stable across versions).
        ctx.enableScissor(Math.round(gridX), Math.round(gridY),
                Math.round(gridX + gridW), Math.round(gridY + gridH));

        for (int i = 0; i < cards.size(); i++) {
            int col = i % cols;
            int row = i / cols;
            float bx = gridX + col * (cardW + colGap);
            float by = gridY + row * (Theme.CARD_H + Theme.CARD_GAP) - sOff;
            ModuleCard card = cards.get(i);
            card.setBounds(bx, by, cardW);
            // Skip rows fully outside the view for efficiency.
            if (by + Theme.CARD_H < gridY || by > gridY + gridH) {
                continue;
            }
            card.render(r, mouseX, mouseY);
        }

        ctx.disableScissor();

        // Scrollbar.
        if (maxScroll > 0) {
            float trackX = cx + cw - 4;
            float trackH = gridH;
            float thumbH = Math.max(20, (float) (trackH * (gridH / totalH)));
            float thumbY = gridY + (float) (sOff / maxScroll) * (trackH - thumbH);
            r.roundedRect(trackX, gridY, 3, trackH, 1.5f, Theme.DIVIDER);
            r.roundedRect(trackX, thumbY, 3, thumbH, 1.5f, Theme.ACCENT);
        }
    }

    // ==================================================================
    // Input (GLFW polled, version-safe)
    // ==================================================================

    private void pollInput(int mouseX, int mouseY) {
        long handle = MinecraftClient.getInstance().getWindow().getHandle();

        // --- Mouse (left button edge) ---
        boolean left = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        if (left && !leftHeld) {
            handleClick(mouseX, mouseY);
        }
        leftHeld = left;

        // --- Typing into the search field ---
        handleTyping(handle);

        // --- Scroll via arrow keys ---
        handleScrollKeys(handle);
    }

    private void handleClick(int mouseX, int mouseY) {
        // Sidebar category selection.
        float sx = winX;
        float sy = winY + Theme.HEADER_H;
        Category[] cats = Category.values();
        float itemH = 30;
        float startY = sy + 10;
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

        // Module cards.
        for (ModuleCard card : cards) {
            if (card.isOver(mouseX, mouseY)) {
                card.onClick();
                return;
            }
        }
    }

    /** Captures A-Z, 0-9, space and backspace for the search box via edge detection. */
    private void handleTyping(long handle) {
        boolean changed = false;

        for (int key = GLFW.GLFW_KEY_A; key <= GLFW.GLFW_KEY_Z; key++) {
            if (edge(handle, key)) {
                search += (char) ('a' + (key - GLFW.GLFW_KEY_A));
                changed = true;
            }
        }
        for (int key = GLFW.GLFW_KEY_0; key <= GLFW.GLFW_KEY_9; key++) {
            if (edge(handle, key)) {
                search += (char) ('0' + (key - GLFW.GLFW_KEY_0));
                changed = true;
            }
        }
        if (edge(handle, GLFW.GLFW_KEY_SPACE)) {
            search += " ";
            changed = true;
        }
        if (edge(handle, GLFW.GLFW_KEY_BACKSPACE) && !search.isEmpty()) {
            search = search.substring(0, search.length() - 1);
            changed = true;
        }

        if (changed) {
            rebuildCards();
        }
    }

    /** True only on the frame {@code key} transitions from up to down. */
    private boolean edge(long handle, int key) {
        boolean down = GLFW.glfwGetKey(handle, key) == GLFW.GLFW_PRESS;
        boolean was = keyHeld[key];
        keyHeld[key] = down;
        return down && !was;
    }

    // ==================================================================
    // Scroll (keyboard + scrollbar drag, all via GLFW polling so we avoid
    // overriding mouseScrolled whose signature varies between versions)
    // ==================================================================

    private void handleScrollKeys(long handle) {
        if (maxScroll <= 0) {
            return;
        }
        if (GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS) {
            scrollTarget = Math.min(maxScroll, scrollTarget + 4);
        }
        if (GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS) {
            scrollTarget = Math.max(0, scrollTarget - 4);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
