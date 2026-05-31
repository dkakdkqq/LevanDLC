package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import com.levandlc.module.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;

/**
 * Working Step: raises the player's step height so they walk up full blocks
 * without jumping, restoring the vanilla value on disable.
 *
 * <p>Uses only the long-stable {@code Entity#setStepHeight(float)} /
 * {@code getStepHeight()}.
 */
public class StepModule extends Module {

    private final NumberSetting height = addSetting(new NumberSetting("Height", 1.0, 0.6, 2.5, 0.1));

    public StepModule() {
        super("Step", "Walk up full blocks automatically", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return;
        }
        mc.player.setStepHeight((float) height.getValue());
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.setStepHeight(0.6f);
        }
    }
}
