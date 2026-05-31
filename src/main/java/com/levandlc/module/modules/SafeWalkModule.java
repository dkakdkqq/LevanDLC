package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;

/**
 * Working SafeWalk: automatically sneaks at block edges so you do not walk off
 * ledges, and releases the forced sneak once you are back over solid ground.
 *
 * Built only on confirmed-stable primitives (getBoundingBox, World getBlockState,
 * BlockState isAir, GameOptions sneakKey setPressed).
 */
public class SafeWalkModule extends Module {

    private boolean forced;

    public SafeWalkModule() {
        super("SafeWalk", "Sneak at block edges to avoid falling", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.options == null || !mc.player.isOnGround()) {
            return;
        }
        Box b = mc.player.getBoundingBox();
        double y = b.minY - 0.35;
        boolean ground = solid(mc, b.minX, y, b.minZ) || solid(mc, b.minX, y, b.maxZ)
                || solid(mc, b.maxX, y, b.minZ) || solid(mc, b.maxX, y, b.maxZ);

        if (!ground) {
            mc.options.sneakKey.setPressed(true);
            forced = true;
        } else if (forced) {
            mc.options.sneakKey.setPressed(false);
            forced = false;
        }
    }

    private boolean solid(MinecraftClient mc, double x, double y, double z) {
        return !mc.world.getBlockState(BlockPos.ofFloored(x, y, z)).isAir();
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options != null && forced) {
            mc.options.sneakKey.setPressed(false);
        }
        forced = false;
    }
}
