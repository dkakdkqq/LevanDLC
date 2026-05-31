package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Working NoSlow (client-side approximation): while the player is using an item
 * (eating, drinking, blocking, drawing a bow) and trying to move, the heavy
 * vanilla slowdown is counteracted by restoring horizontal velocity.
 *
 * <p>Uses only stable {@code LivingEntity#isUsingItem()},
 * {@code getVelocity()/setVelocity(Vec3d)} and {@code forwardSpeed/sidewaysSpeed}.
 * Note: a fully server-accurate NoSlow needs a movement mixin; this restores
 * most of the lost speed client-side.
 */
public class NoSlowModule extends Module {

    public NoSlowModule() {
        super("NoSlow", "Removes item-use movement slowdown", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || !mc.player.isUsingItem()) {
            return;
        }
        boolean moving = mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0;
        if (!moving) {
            return;
        }
        // Vanilla multiplies movement input by 0.2 while using an item; scale the
        // horizontal velocity back up to roughly compensate.
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x * 5.0, v.y, v.z * 5.0);
    }
}
