package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Working Tower: hold jump + sneak together to rise straight up in place.
 *
 * Built only on confirmed-stable primitives (velocity,
 * GameOptions jumpKey/sneakKey isPressed()).
 */
public class TowerModule extends Module {

    public TowerModule() {
        super("Tower", "Hold jump + sneak to rise straight up", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options == null) {
            return;
        }
        if (mc.options.jumpKey.isPressed() && mc.options.sneakKey.isPressed()) {
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x * 0.2, 0.42, v.z * 0.2);
        }
    }
}
