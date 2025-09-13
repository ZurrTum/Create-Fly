package com.zurrtum.create.foundation.blockEntity.behaviour.audio;

import com.zurrtum.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class FrogportAudioBehaviour extends BlockEntityBehaviour<FrogportBlockEntity> {
    public static final BehaviourType<FrogportAudioBehaviour> TYPE = new BehaviourType<>();

    public FrogportAudioBehaviour(FrogportBlockEntity be) {
        super(be);
    }

    @Override
    public void tick() {
    }

    public abstract void open(World level, BlockPos pos);

    public abstract void close(World level, BlockPos pos);

    public abstract void catchPackage(World level, BlockPos pos);

    public abstract void depositPackage(World level, BlockPos pos);

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }
}
