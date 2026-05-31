package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Working FastFall: accelerates the player's descent while airborne and already
 * falling, so you hit the ground quicker (combine with NoFall to avoid damage).
 *
 * <p>Uses only stable {@code getVelocity()/setVelocity(Vec3d)} +
 * {@code isOnGround()}.
 */
public class FastFallModule extends Module {

    public FastFallModule() {
        super("FastFall", "Fall to the ground faster", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.player.isOnGround()) {
            return;
        }
        Vec3d v = mc.player.getVelocity();
        if (v.y < 0) {
            mc.player.setVelocity(v.x, v.y - 0.25, v.z);
        }
    }
}
