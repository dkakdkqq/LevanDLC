package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import net.minecraft.client.MinecraftClient;

/**
 * A genuinely-working module: forces the player to sprint every tick while
 * enabled. Uses only the long-stable {@code Entity#setSprinting(boolean)} and
 * {@code ClientPlayerEntity#forwardSpeed}, so it compiles and runs on 1.21.11.
 *
 * <p>This is the reference pattern for turning any {@link SimpleModule} into a
 * real feature: subclass {@link Module}, override {@link #onUpdate()}, and act on
 * {@code MinecraftClient.getInstance()}.
 */
public class SprintModule extends Module {

    public SprintModule(int key) {
        super("Sprint", "Always sprint while moving forward", Category.MOVEMENT, key);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return;
        }
        // Only sprint when actually moving forward and able to.
        if (mc.player.forwardSpeed > 0 && !mc.player.horizontalCollision && !mc.player.isUsingItem()) {
            mc.player.setSprinting(true);
        }
    }
}
