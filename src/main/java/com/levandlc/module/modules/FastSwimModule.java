package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import com.levandlc.module.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Working FastSwim: boosts horizontal movement while submerged in water, so you
 * swim noticeably faster.
 *
 * <p>Uses only long-stable APIs: {@code Entity#isTouchingWater()},
 * {@code getVelocity()/setVelocity(Vec3d)} and {@code forwardSpeed}.
 */
public class FastSwimModule extends Module {

    private final NumberSetting multiplier = addSetting(new NumberSetting("Multiplier", 1.5, 1.0, 3.0, 0.1));

    public FastSwimModule() {
        super("FastSwim", "Swim faster underwater", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || !mc.player.isTouchingWater()) {
            return;
        }
        if (mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0) {
            Vec3d v = mc.player.getVelocity();
            double f = multiplier.getValue();
            mc.player.setVelocity(v.x * f, v.y, v.z * f);
        }
    }
}
