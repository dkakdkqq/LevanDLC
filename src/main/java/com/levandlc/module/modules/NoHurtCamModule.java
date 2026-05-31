package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import net.minecraft.client.MinecraftClient;

/**
 * Working NoHurtCam: clears the player's hurt timer each tick so the red
 * damage-tilt screen wobble never plays.
 *
 * <p>Uses only the long-stable public field {@code LivingEntity#hurtTime}.
 */
public class NoHurtCamModule extends Module {

    public NoHurtCamModule() {
        super("NoHurtCam", "Removes the damage screen shake", Category.RENDER);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return;
        }
        mc.player.hurtTime = 0;
    }
}
