package com.zurrtum.create.client.flywheel.lib.model.baked;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.function.ToIntFunction;

public class SinglePosVirtualBlockGetter extends VirtualBlockGetter {
    protected BlockPos pos = BlockPos.ORIGIN;
    protected BlockState blockState = Blocks.AIR.getDefaultState();
    @Nullable
    protected BlockEntity blockEntity;

    public SinglePosVirtualBlockGetter(ToIntFunction<BlockPos> blockLightFunc, ToIntFunction<BlockPos> skyLightFunc) {
        super(blockLightFunc, skyLightFunc);
    }

    public static SinglePosVirtualBlockGetter createFullDark() {
        return new SinglePosVirtualBlockGetter(p -> 0, p -> 0);
    }

    public static SinglePosVirtualBlockGetter createFullBright() {
        return new SinglePosVirtualBlockGetter(p -> 15, p -> 15);
    }

    public SinglePosVirtualBlockGetter pos(BlockPos pos) {
        this.pos = pos;
        return this;
    }

    public SinglePosVirtualBlockGetter blockState(BlockState state) {
        blockState = state;
        return this;
    }

    public SinglePosVirtualBlockGetter blockEntity(@Nullable BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        return this;
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos) {
        if (pos.equals(this.pos)) {
            return blockEntity;
        }

        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (pos.equals(this.pos)) {
            return blockState;
        }

        return Blocks.AIR.getDefaultState();
    }

    @Override
    public int getHeight() {
        return 1;
    }

    @Override
    public int getBottomY() {
        return pos.getY();
    }
}
