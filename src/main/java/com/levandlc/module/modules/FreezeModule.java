package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import net.minecraft.client.MinecraftClient;

/**
 * Working Freeze: zeroes the player's velocity every tick, locking them in place
 * until disabled.
 *
 * <p>Uses only the long-stable {@code Entity#setVelocity(double,double,double)}.
 */
public class FreezeModule extends Module {

    public FreezeModule() {
        super("Freeze", "Locks you in place", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return;
        }
        mc.player.setVelocity(0, 0, 0);
    }
}
