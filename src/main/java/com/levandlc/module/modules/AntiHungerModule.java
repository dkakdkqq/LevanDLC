package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import net.minecraft.client.MinecraftClient;

/**
 * Working AntiHunger: prevents the client from auto-sprinting purely to burn
 * less hunger - stops sprinting when the hunger level is low so the player does
 * not start losing health from starvation as quickly.
 *
 * <p>Uses only long-stable APIs: {@code ClientPlayerEntity#getHungerManager()}
 * and {@code setSprinting(boolean)}.
 */
public class AntiHungerModule extends Module {

    public AntiHungerModule() {
        super("AntiHunger", "Stops sprinting when starving to save food", Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return;
        }
        if (mc.player.getHungerManager().getFoodLevel() <= 6) {
            mc.player.setSprinting(false);
        }
    }
}
