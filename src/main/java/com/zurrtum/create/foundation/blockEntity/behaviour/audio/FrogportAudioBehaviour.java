package com.zurrtum.create.foundation.blockEntity.behaviour.audio;

import com.zurrtum.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public abstract class FrogportAudioBehaviour extends BlockEntityBehaviour<FrogportBlockEntity> {
    public static final BehaviourType<FrogportAudioBehaviour> TYPE = new BehaviourType<>();

    public FrogportAudioBehaviour(FrogportBlockEntity be) {
        super(be);
    }

    @Override
    public void tick() {
    }

    public abstract void open(Level level, BlockPos pos);

    public abstract void close(Level level, BlockPos pos);

    public abstract void catchPackage(Level level, BlockPos pos);

    public abstract void depositPackage(Level level, BlockPos pos);

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }
}
