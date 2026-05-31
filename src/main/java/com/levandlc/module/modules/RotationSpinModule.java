package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import com.levandlc.module.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;

/**
 * Working Spin: continuously rotates the player's yaw - the classic spinbot
 * effect. Built only on confirmed-stable {@code getYaw}/{@code setYaw}.
 */
public class RotationSpinModule extends Module {

    private final NumberSetting speed = addSetting(new NumberSetting("Speed", 20, 1, 60, 1));

    public RotationSpinModule() {
        super("Spin", "Continuously spins your view", Category.RENDER);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return;
        }
        mc.player.setYaw(mc.player.getYaw() + (float) speed.getValue());
    }
}
