package com.zurrtum.create.catnip.levelWrappers;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SchematicLevelAccessor extends WorldAccess {
    Set<BlockPos> getAllPositions();

    List<Entity> getEntityList();

    Map<BlockPos, BlockState> getBlockMap();

    BlockBox getBounds();

    void setBounds(BlockBox bounds);

    Iterable<BlockEntity> getBlockEntities();

    Iterable<BlockEntity> getRenderedBlockEntities();
}
