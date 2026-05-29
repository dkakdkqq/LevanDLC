package com.levandlc.gui.clickgui;

import com.levandlc.module.Module;
import com.levandlc.util.animation.Animation;
import com.levandlc.util.render.ColorUtil;
import com.levandlc.util.render.Render2D;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

/**
 * A single clickable module row inside a {@link Panel}.
 *
 * <p>Has two independent animations - one for hover highlight and one for the
 * enabled state - blended every frame for a smooth feel.
 */
public class ModuleButton {

    public static final int HEIGHT = 14;

    private final Module module;
    private final Animation hover = new Animation(0.0, 18.0);
    private final Animation toggle;

    public ModuleButton(Module module) {
        this.module = module;
        this.toggle = new Animation(module.isEnabled() ? 1.0 : 0.0, 16.0);
    }

    public Module getModule() {
        return module;
    }

    public void render(DrawContext ctx, int x, int y, int width, int mouseX, int mouseY) {
        boolean hovered = Render2D.isHovered(mouseX, mouseY, x, y, width, HEIGHT);
        float h = (float) hover.animateTo(hovered ? 1.0 : 0.0);
        float t = (float) toggle.animateTo(module.isEnabled() ? 1.0 : 0.0);

        // Hover highlight, then the enabled tint on top.
        Render2D.rect(ctx, x, y, width, HEIGHT, ColorUtil.rgba(255, 255, 255, (int) (30 * h)));
        Render2D.rect(ctx, x, y, width, HEIGHT, ColorUtil.rgba(90, 140, 255, (int) (90 * t)));

        // Accent bar on the left edge while enabled.
        if (t > 0.01f) {
            Render2D.rect(ctx, x, y, 2, HEIGHT, ColorUtil.withAlpha(ColorUtil.rgb(120, 170, 255), (int) (255 * t)));
        }

        int textColor = ColorUtil.interpolate(0xFFBBBBBB, 0xFFFFFFFF, Math.max(h, t));
        Render2D.textVerticallyCentered(ctx, module.getName(), x + 7, y, HEIGHT, textColor, true);

        // Keybind hint on the right, if bound.
        if (module.getKey() != GLFW.GLFW_KEY_UNKNOWN) {
            String keyName = GLFW.glfwGetKeyName(module.getKey(), 0);
            if (keyName != null) {
                Render2D.textRight(ctx, "[" + keyName.toUpperCase() + "]",
                        x + width - 6, y + (HEIGHT - Render2D.textHeight()) / 2,
                        ColorUtil.rgb(120, 120, 135), true);
            }
        }
    }

    public void onClick(int button) {
        if (button == 0) {
            module.toggle();
        }
    }
}
