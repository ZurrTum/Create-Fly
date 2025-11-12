package com.zurrtum.create.client.content.kinetics.turntable;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.content.kinetics.turntable.TurntableBlockEntity;
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

        float speed = turnTable.getSpeed() * 3 / 10;

        if (speed == 0)
            return;

        Vec3 origin = VecHelper.getCenterOf(pos);
        Vec3 offset = mc.player.position().subtract(origin);

        if (offset.length() > 1 / 4f)
            speed *= Mth.clamp((1 / 2f - offset.length()) * 2, 0, 1);

        mc.player.setYRot(mc.player.yRotO - speed * AnimationTickHolder.getPartialTicks());
        mc.player.yBodyRot = mc.player.getYRot();
    }
}
