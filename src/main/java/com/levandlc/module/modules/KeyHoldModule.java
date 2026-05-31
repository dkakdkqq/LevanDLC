package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

import java.util.function.Function;

/**
 * A working module that virtually holds one of the player's movement keys down
 * every tick while enabled (e.g. Sneak, AutoWalk).
 *
 * <p>Uses {@code GameOptions} keybindings + {@code KeyBinding#setPressed}, which
 * have been stable for many versions, so it compiles and runs on 1.21.11.
 */
public class KeyHoldModule extends Module {

    private final transient Function<MinecraftClient, KeyBinding> keyPicker;

    public KeyHoldModule(String name, String description, Category category, int key,
                         Function<MinecraftClient, KeyBinding> keyPicker) {
        super(name, description, category, key);
        this.keyPicker = keyPicker;
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options == null) {
            return;
        }
        keyPicker.apply(mc).setPressed(true);
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options != null) {
            keyPicker.apply(mc).setPressed(false);
        }
    }
}
