package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Working SneakSpeed: removes the movement penalty applied while sneaking, so
 * you keep near-normal speed when crouched (useful for edge-safe traversal).
 *
 * <p>Uses only long-stable APIs: {@code Entity#isSneaking()},
 * {@code getVelocity()/setVelocity(Vec3d)}, {@code isOnGround()} and
 * {@code forwardSpeed/sidewaysSpeed}.
 */
public class SneakSpeedModule extends Module {

    public SneakSpeedModule() {
        super("SneakSpeed", "Move at full speed while sneaking", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || !mc.player.isSneaking() || !mc.player.isOnGround()) {
            return;
        }
        boolean moving = mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0;
        if (moving) {
            // Vanilla scales sneak movement to ~0.3; restore most of it.
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x * 2.0, v.y, v.z * 2.0);
        }
    }
}
