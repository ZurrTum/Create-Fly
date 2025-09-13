package com.zurrtum.create.content.redstone.displayLink;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DisplayLinkContext {

    private final World level;
    private final DisplayLinkBlockEntity blockEntity;

    public Object flapDisplayContext;

    public DisplayLinkContext(World level, DisplayLinkBlockEntity blockEntity) {
        this.level = level;
        this.blockEntity = blockEntity;
    }

    public World level() {
        return level;
    }

    public DisplayLinkBlockEntity blockEntity() {
        return blockEntity;
    }

    public BlockEntity getSourceBlockEntity() {
        return level.getBlockEntity(getSourcePos());
    }

    public BlockPos getSourcePos() {
        return blockEntity.getSourcePosition();
    }

    public BlockEntity getTargetBlockEntity() {
        return level.getBlockEntity(getTargetPos());
    }

    public BlockPos getTargetPos() {
        return blockEntity.getTargetPosition();
    }

    public NbtCompound sourceConfig() {
        return blockEntity.getSourceConfig();
    }

}
