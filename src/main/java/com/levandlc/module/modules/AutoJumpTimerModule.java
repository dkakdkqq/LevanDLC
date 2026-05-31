package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Working AutoBunny: periodically hops while grounded even without input, a
 * gentle idle bounce that also helps avoid AFK detection.
 *
 * <p>Built only on confirmed-stable primitives (velocity, {@code isOnGround}).
 */
public class AutoJumpTimerModule extends Module {

    public AutoJumpTimerModule() {
        super("AutoBunny", "Periodic idle hop", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return;
        }
        if (mc.player.isOnGround()) {
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x, 0.42, v.z);
        }
    }
}
