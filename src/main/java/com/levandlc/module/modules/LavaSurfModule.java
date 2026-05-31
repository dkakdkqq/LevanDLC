package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Working LavaSurf: rise toward the surface while in lava and holding jump.
 *
 * Built only on confirmed-stable primitives (Entity isInLava(), velocity,
 * GameOptions jumpKey isPressed()).
 */
public class LavaSurfModule extends Module {

    public LavaSurfModule() {
        super("LavaSurf", "Rise out of lava with jump", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options == null || !mc.player.isInLava()) {
            return;
        }
        if (mc.options.jumpKey.isPressed()) {
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x, 0.25, v.z);
        }
    }
}
