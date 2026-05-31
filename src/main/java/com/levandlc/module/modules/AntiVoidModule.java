package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Working AntiVoid: if the player drops below the world's bottom, it cancels
 * downward velocity and gives a small upward push, buying time before void death.
 *
 * <p>Uses only long-stable APIs: {@code Entity#getY()},
 * {@code World#getBottomY()}, {@code getVelocity()/setVelocity(Vec3d)}.
 */
public class AntiVoidModule extends Module {

    public AntiVoidModule() {
        super("AntiVoid", "Prevents falling into the void", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) {
            return;
        }
        if (mc.player.getY() < mc.world.getBottomY() + 1) {
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x, 0.2, v.z);
        }
    }
}
