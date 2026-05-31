package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import net.minecraft.client.MinecraftClient;

/**
 * Working AutoSprint (omni-directional): forces sprint regardless of direction,
 * so strafing and walking backward also sprint.
 *
 * <p>Uses only the long-stable {@code Entity#setSprinting(boolean)}.
 */
public class AutoSprintModule extends Module {

    public AutoSprintModule() {
        super("AutoSprint", "Always sprint in any direction", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return;
        }
        boolean moving = mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0;
        if (moving && !mc.player.isUsingItem()) {
            mc.player.setSprinting(true);
        }
    }
}
