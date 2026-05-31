package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import com.levandlc.module.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Working Dash: gives a configurable forward burst in the look direction each
 * time the sprint key is freshly pressed while on the ground.
 *
 * <p>Built only on confirmed-stable primitives ({@code getYaw}, velocity,
 * {@code isOnGround}, {@code GameOptions#sprintKey.isPressed()}).
 */
public class GodSprintModule extends Module {

    private final NumberSetting power = addSetting(new NumberSetting("Power", 1.2, 0.5, 3.0, 0.1));
    private boolean sprintHeld;

    public GodSprintModule() {
        super("Dash", "Burst forward when you start sprinting", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options == null) {
            return;
        }
        boolean sprint = mc.options.sprintKey.isPressed();
        if (sprint && !sprintHeld && mc.player.isOnGround()) {
            double yaw = Math.toRadians(mc.player.getYaw());
            double p = power.getValue();
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x - Math.sin(yaw) * p, v.y, v.z + Math.cos(yaw) * p);
        }
        sprintHeld = sprint;
    }

    @Override
    public void onDisable() {
        sprintHeld = false;
    }
}
