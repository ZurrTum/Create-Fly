package com.zurrtum.create.client.content.kinetics.turntable;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.kinetics.turntable.TurntableBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.PositionAndRotation;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TurntableHandler {
    public static void tick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (mc.gameMode == null || player == null) {
            return;
        }
        BlockPos pos = player.blockPosition();
        ClientLevel level = mc.level;
        if (!level.getBlockState(pos).is(AllBlocks.TURNTABLE)) {
            return;
        }
        if (!player.onGround()) {
            return;
        }
        if (mc.isPaused()) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof TurntableBlockEntity turnTable)) {
            return;
        }
        PositionAndRotation positionAndRotation = player.getClientPositionAndRotation();
        player.moveOrInterpolateTo(
            positionAndRotation.yRot() - turnTable.getSpeed() * 0.3f,
            positionAndRotation.xRot()
        );
    }
}
