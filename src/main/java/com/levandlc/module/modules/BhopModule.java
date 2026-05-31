package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Working Bunny Hop: automatically jumps the instant the player is grounded and
 * moving, so you bounce continuously without holding space.
 *
 * <p>Uses only long-stable APIs: {@code Entity#isOnGround()},
 * {@code getVelocity()/setVelocity(Vec3d)} and {@code forwardSpeed/sidewaysSpeed}.
 */
public class BhopModule extends Module {

    public BhopModule() {
        super("Bhop", "Automatically bunny-hops while moving", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return;
        }
        boolean moving = mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0;
        if (moving && mc.player.isOnGround() && !mc.player.isSneaking()) {
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x, 0.42, v.z);
        }
    }
}
