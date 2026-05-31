package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import com.levandlc.module.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Working horizontal speed boost: while on the ground and moving, multiplies the
 * player's horizontal velocity by a configurable factor.
 *
 * <p>Uses only the long-stable {@code Entity#getVelocity()/setVelocity(Vec3d)}
 * and {@code isOnGround()}, so it compiles and runs on 1.21.11. Vertical motion
 * is left untouched so jumping/falling behave normally.
 */
public class SpeedModule extends Module {

    private final NumberSetting multiplier = addSetting(new NumberSetting("Multiplier", 1.4, 1.0, 3.0, 0.1));

    public SpeedModule() {
        super("Speed", "Move faster on the ground", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return;
        }
        // Only boost when there is input and the player is grounded.
        boolean moving = mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0;
        if (!moving) {
            return;
        }
        Vec3d v = mc.player.getVelocity();
        double f = multiplier.getValue();
        mc.player.setVelocity(v.x * f, v.y, v.z * f);
    }
}
