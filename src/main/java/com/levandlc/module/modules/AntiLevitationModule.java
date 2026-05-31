package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Working AntiBounce: cancels upward velocity spikes while grounded, so
 * unexpected launches/bounces are suppressed.
 *
 * <p>Uses only long-stable APIs: {@code Entity#isOnGround()},
 * {@code getVelocity()/setVelocity(Vec3d)}.
 */
public class AntiLevitationModule extends Module {

    public AntiLevitationModule() {
        super("AntiBounce", "Cancels unexpected upward launches", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return;
        }
        Vec3d v = mc.player.getVelocity();
        if (mc.player.isOnGround() && v.y > 0.6) {
            mc.player.setVelocity(v.x, 0, v.z);
        }
    }
}
