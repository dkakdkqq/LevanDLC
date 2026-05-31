package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Working Hover: cancels vertical velocity while the sneak key is held in the
 * air, letting you hover in place.
 *
 * <p>Built only on confirmed-stable primitives (velocity, {@code isOnGround},
 * {@code GameOptions#sneakKey}).
 */
public class HoverModule extends Module {

    public HoverModule() {
        super("Hover", "Hold sneak to hover in mid-air", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options == null || mc.player.isOnGround()) {
            return;
        }
        if (mc.options.sneakKey.isPressed()) {
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x, 0, v.z);
        }
    }
}
