package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;

/**
 * Working Anti-AFK: periodically swings the arm and nudges the view so the
 * server does not flag the client as idle.
 *
 * <p>Uses only the long-stable {@code PlayerEntity#swingHand(Hand)} and
 * {@code Entity#setYaw(float)} / {@code getYaw()}.
 */
public class AntiAFKModule extends Module {

    private int tick;

    public AntiAFKModule() {
        super("AntiAFK", "Avoids being kicked for inactivity", Category.UTIL);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return;
        }
        // Roughly every 2 seconds (40 ticks).
        if (++tick % 40 == 0) {
            mc.player.swingHand(Hand.MAIN_HAND);
            // Small reversible head turn.
            float dir = (tick % 80 == 0) ? 5f : -5f;
            mc.player.setYaw(mc.player.getYaw() + dir);
        }
    }

    @Override
    public void onDisable() {
        tick = 0;
    }
}
