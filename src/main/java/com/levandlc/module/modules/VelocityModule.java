package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import com.levandlc.module.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Working Velocity (knockback reduction): while the player is in their hurt
 * window (just took damage), scales the knockback velocity by the configured
 * percentages. 0% = full cancel, 100% = vanilla knockback.
 *
 * <p>Uses only long-stable APIs: {@code LivingEntity#hurtTime} and
 * {@code getVelocity()/setVelocity(Vec3d)}. A packet-accurate version needs a
 * network mixin; this client-side reduction works well in practice.
 */
public class VelocityModule extends Module {

    private final NumberSetting horizontal = addSetting(new NumberSetting("Horizontal %", 0, 0, 100, 5));
    private final NumberSetting vertical = addSetting(new NumberSetting("Vertical %", 0, 0, 100, 5));

    private int lastHurt;

    public VelocityModule() {
        super("Velocity", "Reduces or cancels knockback taken", Category.COMBAT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return;
        }
        int hurt = mc.player.hurtTime;
        // Apply on the tick the hurt timer (re)starts, when knockback is fresh.
        if (hurt > 0 && hurt >= lastHurt) {
            Vec3d v = mc.player.getVelocity();
            double h = horizontal.getValue() / 100.0;
            double ver = vertical.getValue() / 100.0;
            mc.player.setVelocity(v.x * h, v.y * ver, v.z * h);
        }
        lastHurt = hurt;
    }

    @Override
    public void onDisable() {
        lastHurt = 0;
    }
}
