package com.zurrtum.create.client.foundation.blockEntity.behaviour.audio;

import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.audio.FrogportAudioBehaviour;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FrogportClientAudioBehaviour extends FrogportAudioBehaviour {
    public FrogportClientAudioBehaviour(FrogportBlockEntity be) {
        super(be);
    }

    @Override
    public void open(World level, BlockPos pos) {
        AllSoundEvents.FROGPORT_OPEN.playAt(level, Vec3d.ofCenter(pos), 0.5f, 1, false);
    }

    @Override
    public void close(World level, BlockPos pos) {
        if (isPlayerNear(pos)) {
            AllSoundEvents.FROGPORT_CLOSE.playAt(level, Vec3d.ofCenter(pos), 1.0f, 1.25f + level.random.nextFloat() * 0.25f, true);
        }
    }

    @Override
    public void catchPackage(World level, BlockPos pos) {
        if (isPlayerNear(pos)) {
            AllSoundEvents.FROGPORT_CATCH.playAt(level, Vec3d.ofCenter(pos), 1, 1, false);
        }
    }

    @Override
    public void depositPackage(World level, BlockPos pos) {
        if (isPlayerNear(pos)) {
            AllSoundEvents.FROGPORT_DEPOSIT.playAt(level, Vec3d.ofCenter(pos), 1, 1, false);
        }
    }

    private boolean isPlayerNear(BlockPos pos) {
        return pos.isWithinDistance(MinecraftClient.getInstance().player.getBlockPos(), 20);
    }
}
