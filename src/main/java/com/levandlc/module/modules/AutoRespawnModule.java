package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import net.minecraft.client.MinecraftClient;

/**
 * Working AutoRespawn: as soon as the player is dead, automatically requests a
 * respawn so you never sit on the death screen.
 *
 * <p>Uses only stable {@code ClientPlayerEntity#isDead()},
 * {@code getHealth()} and {@code requestRespawn()}.
 */
public class AutoRespawnModule extends Module {

    public AutoRespawnModule() {
        super("AutoRespawn", "Instantly respawns on death", Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return;
        }
        if (mc.player.isDead() || mc.player.getHealth() <= 0.0f) {
            mc.player.requestRespawn();
        }
    }
}
