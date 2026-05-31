package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Working Spider: lets the player climb walls. While horizontally colliding with
 * a block and pressing into it, the player is pushed upward.
 *
 * <p>Uses only the long-stable {@code Entity#horizontalCollision},
 * {@code getVelocity()/setVelocity(Vec3d)} and {@code forwardSpeed}, so it
 * compiles and runs on 1.21.11.
 */
public class SpiderModule extends Module {

    public SpiderModule() {
        super("Spider", "Climb walls like a spider", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return;
        }
        // Climb only when pushing into a wall.
        if (mc.player.horizontalCollision && mc.player.forwardSpeed != 0) {
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x, 0.2, v.z);
        }
    }
}
