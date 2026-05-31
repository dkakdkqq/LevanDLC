package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Working FastLadder: climbs ladders and vines much faster. While the player is
 * on a climbable block, vertical velocity is driven by the forward/back input.
 *
 * <p>Uses only long-stable APIs: {@code LivingEntity#isClimbing()},
 * {@code getVelocity()/setVelocity(Vec3d)} and {@code forwardSpeed}.
 */
public class FastLadderModule extends Module {

    public FastLadderModule() {
        super("FastLadder", "Climb ladders and vines faster", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || !mc.player.isClimbing()) {
            return;
        }
        Vec3d v = mc.player.getVelocity();
        double y = v.y;
        if (mc.player.forwardSpeed > 0) {
            y = 0.3;       // climb up while moving forward
        } else if (mc.player.forwardSpeed < 0) {
            y = -0.3;      // descend while moving back
        }
        mc.player.setVelocity(v.x, y, v.z);
    }
}
