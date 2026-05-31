package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import com.levandlc.module.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Working Glide: clamps downward velocity while airborne so the player descends
 * slowly (a soft, parachute-like fall). Pairs nicely with NoFall.
 *
 * <p>Uses only stable {@code getVelocity()/setVelocity(Vec3d)} and
 * {@code isOnGround()}.
 */
public class GlideModule extends Module {

    private final NumberSetting fall = addSetting(new NumberSetting("Fall Speed", 0.08, 0.02, 0.3, 0.01));

    public GlideModule() {
        super("Glide", "Descend slowly through the air", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.player.isOnGround()) {
            return;
        }
        Vec3d v = mc.player.getVelocity();
        if (v.y < 0) {
            mc.player.setVelocity(v.x, -fall.getValue(), v.z);
        }
    }
}
