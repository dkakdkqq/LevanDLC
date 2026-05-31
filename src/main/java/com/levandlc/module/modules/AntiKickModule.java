package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;

/**
 * Working AntiKick: periodically swings the hand to keep the connection
 * considered active without rotating the view.
 *
 * <p>Built only on confirmed-stable {@code PlayerEntity#swingHand(Hand)}.
 */
public class AntiKickModule extends Module {

    private int tick;

    public AntiKickModule() {
        super("AntiKick", "Periodic hand swing to avoid idle kicks", Category.UTIL);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return;
        }
        if (++tick % 60 == 0) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    @Override
    public void onDisable() {
        tick = 0;
    }
}
