package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import com.levandlc.module.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Working MultiJump: lets the player jump a configurable number of extra times
 * while in mid-air. Each press of the jump key (edge-detected) in the air
 * applies a fresh upward velocity until the air-jump budget is spent; the budget
 * refills on landing.
 *
 * <p>Uses only long-stable APIs: {@code KeyBinding#isPressed()},
 * {@code Entity#isOnGround()} and {@code getVelocity()/setVelocity(Vec3d)}.
 */
public class MultiJumpModule extends Module {

    private final NumberSetting extraJumps = addSetting(new NumberSetting("Extra Jumps", 1, 1, 5, 1));

    private int jumpsUsed;
    private boolean jumpHeld;

    public MultiJumpModule() {
        super("MultiJump", "Jump additional times in mid-air", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options == null) {
            return;
        }
        if (mc.player.isOnGround()) {
            jumpsUsed = 0;
        }
        boolean jumpDown = mc.options.jumpKey.isPressed();
        if (jumpDown && !jumpHeld && !mc.player.isOnGround() && jumpsUsed < extraJumps.getValue()) {
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x, 0.42, v.z);
            jumpsUsed++;
        }
        jumpHeld = jumpDown;
    }

    @Override
    public void onDisable() {
        jumpsUsed = 0;
        jumpHeld = false;
    }
}
