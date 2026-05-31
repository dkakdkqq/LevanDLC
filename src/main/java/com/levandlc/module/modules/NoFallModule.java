package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import net.minecraft.client.MinecraftClient;

/**
 * Working NoFall: keeps the player's accumulated fall distance at zero each tick
 * so vanilla never computes fall damage.
 *
 * <p>{@code Entity#fallDistance} has been a stable public field for many
 * versions. Defensive null-checks keep it crash-safe.
 */
public class NoFallModule extends Module {

    public NoFallModule() {
        super("NoFall", "Prevents fall damage", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return;
        }
        // Reset before the game evaluates fall damage on landing.
        if (mc.player.fallDistance > 2.0f) {
            mc.player.fallDistance = 0f;
        }
    }
}
