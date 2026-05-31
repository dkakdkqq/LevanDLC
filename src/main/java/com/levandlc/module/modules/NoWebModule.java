package com.levandlc.module.modules;

import com.levandlc.module.Category;
import com.levandlc.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Vec3d;

/**
 * Working NoWeb: counteracts the strong slowdown applied while standing in
 * cobwebs by restoring horizontal velocity when the player is inside one.
 *
 * <p>Uses only long-stable APIs: {@code Entity#getBlockStateAtPos()} /
 * {@code World#getBlockState} via the player's block position,
 * {@code getVelocity()/setVelocity(Vec3d)}.
 */
public class NoWebModule extends Module {

    public NoWebModule() {
        super("NoWeb", "Move normally through cobwebs", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) {
            return;
        }
        boolean inWeb = mc.world.getBlockState(mc.player.getBlockPos()).isOf(Blocks.COBWEB);
        if (inWeb && (mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0)) {
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x * 5.0, v.y, v.z * 5.0);
        }
    }
}
