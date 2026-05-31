package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import com.levandlc.module.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Working HighJump: the instant the player leaves the ground moving upward, its
 * vertical velocity is boosted, producing a noticeably higher jump.
 *
 * <p>Uses only stable {@code getVelocity()/setVelocity(Vec3d)} and
 * {@code isOnGround()}.
 */
public class HighJumpModule extends Module {

    private final NumberSetting power = addSetting(new NumberSetting("Power", 0.4, 0.1, 1.5, 0.05));
    private boolean wasOnGround = true;

    public HighJumpModule() {
        super("HighJump", "Jump higher than normal", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return;
        }
        boolean onGround = mc.player.isOnGround();
        Vec3d v = mc.player.getVelocity();
        // Just left the ground while ascending -> add a one-time boost.
        if (wasOnGround && !onGround && v.y > 0) {
            mc.player.setVelocity(v.x, v.y + power.getValue(), v.z);
        }
        wasOnGround = onGround;
    }

    @Override
    public void onDisable() {
        wasOnGround = true;
    }
}
