package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Working Sink: drop quickly through water while sneaking (complement to WaterSurf).
 *
 * Built only on confirmed-stable primitives (Entity isTouchingWater(),
 * isSneaking(), velocity).
 */
public class WaterSinkModule extends Module {

    public WaterSinkModule() {
        super("Sink", "Sink fast in water while sneaking", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || !mc.player.isTouchingWater() || !mc.player.isSneaking()) {
            return;
        }
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x, -0.2, v.z);
    }
}
