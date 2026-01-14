package com.zurrtum.create.client.content.kinetics.turntable;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.turntable.TurntableBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class TurntableHandler {
    public static void gameRenderFrame(MinecraftClient mc) {
        if (mc.interactionManager == null || mc.player == null)
            return;
        BlockPos pos = mc.player.getBlockPos();
        ClientWorld world = mc.world;
        if (!world.getBlockState(pos).isOf(AllBlocks.TURNTABLE))
            return;
        if (!mc.player.isOnGround())
            return;
        if (mc.isPaused())
            return;

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof TurntableBlockEntity turnTable))
            return;

        RenderTickCounter deltaTracker = mc.getRenderTickCounter();
        float tickSpeed = world.getTickManager().getTickRate() / 20;
        float speed = turnTable.getSpeed() * (2 / 3f) * tickSpeed * deltaTracker.getFixedDeltaTicks();

        if (speed == 0)
            return;

        Vec3d origin = VecHelper.getCenterOf(pos);
        Vec3d offset = mc.player.getEntityPos().subtract(origin);

        if (offset.length() > 1 / 4f)
            speed *= (float) MathHelper.clamp((1 / 2f - offset.length()) * 2, 0, 1);

        float yRotOffset = speed * deltaTracker.getTickProgress(false);
        mc.player.setYaw(mc.player.getYaw() - yRotOffset);
        mc.player.bodyYaw -= yRotOffset;
    }
}
