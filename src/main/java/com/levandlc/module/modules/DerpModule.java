package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import net.minecraft.client.MinecraftClient;

import java.util.Random;

/**
 * Working Derp: randomly scrambles the player's head rotation each tick - the
 * classic "derp" cosmetic effect.
 *
 * Built only on confirmed-stable setYaw/setPitch.
 */
public class DerpModule extends Module {

    private final Random random = new Random();

    public DerpModule() {
        super("Derp", "Randomly scrambles your head rotation", Category.RENDER);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return;
        }
        mc.player.setYaw(random.nextFloat() * 360f - 180f);
        mc.player.setPitch(random.nextFloat() * 180f - 90f);
    }
}
