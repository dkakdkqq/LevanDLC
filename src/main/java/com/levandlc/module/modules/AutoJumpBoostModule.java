package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import com.levandlc.module.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Working JumpBoost: adds extra upward velocity the moment the player jumps off
 * the ground, for a snappier higher hop (lighter than HighJump).
 *
 * <p>Built only on confirmed-stable primitives (velocity, {@code isOnGround},
 * {@code GameOptions#jumpKey}).
 */
public class AutoJumpBoostModule extends Module {

    private final NumberSetting boost = addSetting(new NumberSetting("Boost", 0.15, 0.05, 0.6, 0.05));

    public AutoJumpBoostModule() {
        super("JumpBoost", "Adds extra height to each jump", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options == null) {
            return;
        }
        if (mc.options.jumpKey.isPressed() && mc.player.isOnGround()) {
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x, v.y + boost.getValue(), v.z);
        }
    }
}
