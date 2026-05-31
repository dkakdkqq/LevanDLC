package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import net.minecraft.client.MinecraftClient;

/**
 * Working ViewLock: pins the player's pitch to level (0 degrees) each tick,
 * keeping a clean horizontal view.
 *
 * <p>Built only on confirmed-stable {@code getPitch}/{@code setPitch}.
 */
public class ViewLockModule extends Module {

    public ViewLockModule() {
        super("ViewLock", "Locks your pitch to level", Category.RENDER);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return;
        }
        mc.player.setPitch(0f);
    }
}
