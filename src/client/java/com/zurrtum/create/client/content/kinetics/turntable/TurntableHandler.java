package com.zurrtum.create.client.content.kinetics.turntable;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.turntable.TurntableBlockEntity;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class TurntableHandler {
    public static void gameRenderFrame(Minecraft mc) {
        if (mc.gameMode == null || mc.player == null)
            return;
        BlockPos pos = mc.player.blockPosition();
        if (!mc.level.getBlockState(pos).is(AllBlocks.TURNTABLE))
            return;
        if (!mc.player.onGround())
            return;
        if (mc.isPaused())
            return;

        BlockEntity blockEntity = mc.level.getBlockEntity(pos);
        if (!(blockEntity instanceof TurntableBlockEntity turnTable))
            return;

        DeltaTracker deltaTracker = mc.getDeltaTracker();
        float tickSpeed = mc.level.tickRateManager().tickrate() / 20;
        float speed = turnTable.getSpeed() * (2 / 3f) * tickSpeed * deltaTracker.getRealtimeDeltaTicks();

        if (speed == 0)
            return;

        Vec3 origin = VecHelper.getCenterOf(pos);
        Vec3 offset = mc.player.position().subtract(origin);

        if (offset.length() > 1 / 4f)
            speed *= (float) Mth.clamp((1 / 2f - offset.length()) * 2, 0, 1);

        float yRotOffset = speed * deltaTracker.getGameTimeDeltaPartialTick(false);
        mc.player.setYRot(mc.player.getYRot() - yRotOffset);
        mc.player.yBodyRot -= yRotOffset;
    }
}
