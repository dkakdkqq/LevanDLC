package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Working WaterSurf: while in water and holding the jump key, gives a strong
 * upward boost so you rise quickly to the surface (auto-swim-up).
 *
 * <p>Uses only long-stable APIs: {@code Entity#isTouchingWater()},
 * {@code GameOptions#jumpKey}, {@code getVelocity()/setVelocity(Vec3d)}.
 */
public class WaterSurfModule extends Module {

    public WaterSurfModule() {
        super("WaterSurf", "Rise quickly in water with jump", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options == null || !mc.player.isTouchingWater()) {
            return;
        }
        if (mc.options.jumpKey.isPressed()) {
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x, 0.25, v.z);
        }
    }
}
