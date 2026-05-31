package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import com.levandlc.module.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;

/**
 * Working creative-style flight: grants the fly ability and applies it each tick
 * while enabled, restoring a sane state on disable.
 *
 * <p>Uses only the long-stable {@code ClientPlayerEntity#getAbilities()} +
 * {@code PlayerAbilities} fields and {@code setFlySpeed}, so it compiles and runs
 * on 1.21.11.
 */
public class FlightModule extends Module {

    private final NumberSetting speed = addSetting(new NumberSetting("Speed", 1.0, 0.5, 5.0, 0.1));

    public FlightModule(int key) {
        super("Flight", "Creative-style free flight", Category.MOVEMENT, key);
    }

    @Override
    public void onEnable() {
        apply();
    }

    @Override
    public void onUpdate() {
        apply();
    }

    private void apply() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return;
        }
        mc.player.getAbilities().allowFlying = true;
        mc.player.getAbilities().flying = true;
        mc.player.getAbilities().setFlySpeed((float) (0.05 * speed.getValue()));
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return;
        }
        boolean creative = mc.player.isCreative();
        mc.player.getAbilities().allowFlying = creative;
        mc.player.getAbilities().flying = false;
        mc.player.getAbilities().setFlySpeed(0.05f);
    }
}
