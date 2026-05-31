package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import com.levandlc.module.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Working Strafe: while airborne and giving forward input, redirects the
 * player's horizontal momentum toward the look direction, giving smooth Quake-y
 * air control and maintaining speed.
 *
 * <p>Uses only long-stable APIs: {@code Entity#isOnGround()},
 * {@code getYaw()}, {@code getVelocity()/setVelocity(Vec3d)} and
 * {@code forwardSpeed}.
 */
public class StrafeModule extends Module {

    private final NumberSetting speed = addSetting(new NumberSetting("Speed", 0.28, 0.2, 0.6, 0.01));

    public StrafeModule() {
        super("Strafe", "Smooth air-strafe control", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.player.isOnGround()) {
            return;
        }
        if (mc.player.forwardSpeed == 0 && mc.player.sidewaysSpeed == 0) {
            return;
        }
        double yaw = Math.toRadians(mc.player.getYaw());
        double s = speed.getValue();
        double mx = -Math.sin(yaw) * s;
        double mz = Math.cos(yaw) * s;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(mx, v.y, mz);
    }
}
