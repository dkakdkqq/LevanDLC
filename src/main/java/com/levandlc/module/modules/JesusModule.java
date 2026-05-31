package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Working Jesus: keeps the player floating at the surface of water so they can
 * walk across it. While in water and not sneaking, downward motion is cancelled
 * and a small upward nudge keeps the player at the surface.
 *
 * <p>Uses only stable {@code Entity#isTouchingWater()},
 * {@code getVelocity()/setVelocity(Vec3d)} and {@code isSneaking()}.
 */
public class JesusModule extends Module {

    public JesusModule() {
        super("Jesus", "Walk on water surfaces", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return;
        }
        // Let the player dive by sneaking.
        if (mc.player.isTouchingWater() && !mc.player.isSneaking()) {
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x, Math.max(v.y, 0.1), v.z);
        }
    }
}
