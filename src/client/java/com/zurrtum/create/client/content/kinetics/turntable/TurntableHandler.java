package com.zurrtum.create.client.content.kinetics.turntable;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.content.kinetics.turntable.TurntableBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class TurntableHandler {
    public static void gameRenderFrame(MinecraftClient mc) {
        if (mc.interactionManager == null || mc.player == null)
            return;
        BlockPos pos = mc.player.getBlockPos();
        if (!mc.world.getBlockState(pos).isOf(AllBlocks.TURNTABLE))
            return;
        if (!mc.player.isOnGround())
            return;
        if (mc.isPaused())
            return;

        BlockEntity blockEntity = mc.world.getBlockEntity(pos);
        if (!(blockEntity instanceof TurntableBlockEntity turnTable))
            return;

        float speed = turnTable.getSpeed() * 3 / 10;

        if (speed == 0)
            return;

        Vec3d origin = VecHelper.getCenterOf(pos);
        Vec3d offset = mc.player.getPos().subtract(origin);

        if (offset.length() > 1 / 4f)
            speed *= MathHelper.clamp((1 / 2f - offset.length()) * 2, 0, 1);

        mc.player.setYaw(mc.player.lastYaw - speed * AnimationTickHolder.getPartialTicks());
        mc.player.bodyYaw = mc.player.getYaw();
    }
}
