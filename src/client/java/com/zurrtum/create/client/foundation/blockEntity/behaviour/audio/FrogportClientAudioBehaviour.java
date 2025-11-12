package com.zurrtum.create.client.foundation.blockEntity.behaviour.audio;

import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.audio.FrogportAudioBehaviour;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FrogportClientAudioBehaviour extends FrogportAudioBehaviour {
    public FrogportClientAudioBehaviour(FrogportBlockEntity be) {
        super(be);
    }

    @Override
    public void open(Level level, BlockPos pos) {
        AllSoundEvents.FROGPORT_OPEN.playAt(level, Vec3.atCenterOf(pos), 0.5f, 1, false);
    }

    @Override
    public void close(Level level, BlockPos pos) {
        if (isPlayerNear(pos)) {
            AllSoundEvents.FROGPORT_CLOSE.playAt(level, Vec3.atCenterOf(pos), 1.0f, 1.25f + level.random.nextFloat() * 0.25f, true);
        }
    }

    @Override
    public void catchPackage(Level level, BlockPos pos) {
        if (isPlayerNear(pos)) {
            AllSoundEvents.FROGPORT_CATCH.playAt(level, Vec3.atCenterOf(pos), 1, 1, false);
        }
    }

    @Override
    public void depositPackage(Level level, BlockPos pos) {
        if (isPlayerNear(pos)) {
            AllSoundEvents.FROGPORT_DEPOSIT.playAt(level, Vec3.atCenterOf(pos), 1, 1, false);
        }
    }

    private boolean isPlayerNear(BlockPos pos) {
        return pos.closerThan(Minecraft.getInstance().player.blockPosition(), 20);
    }
}
