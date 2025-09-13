package com.zurrtum.create.content.contraptions;

import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class TranslatingContraption extends Contraption {

    protected Set<BlockPos> cachedColliders;
    protected Direction cachedColliderDirection;

    public Set<BlockPos> getOrCreateColliders(World world, Direction movementDirection) {
        if (getBlocks() == null)
            return Collections.emptySet();
        if (cachedColliders == null || cachedColliderDirection != movementDirection) {
            cachedColliderDirection = movementDirection;
            cachedColliders = createColliders(world, movementDirection);
        }
        return cachedColliders;
    }

    public Set<BlockPos> createColliders(World world, Direction movementDirection) {
        Set<BlockPos> colliders = new HashSet<>();
        for (StructureBlockInfo info : getBlocks().values()) {
            BlockPos offsetPos = info.pos().offset(movementDirection);
            if (info.state().getCollisionShape(world, offsetPos).isEmpty())
                continue;
            if (getBlocks().containsKey(offsetPos) && !getBlocks().get(offsetPos).state().getCollisionShape(world, offsetPos).isEmpty())
                continue;
            colliders.add(info.pos());
        }
        return colliders;
    }

    @Override
    public void removeBlocksFromWorld(World world, BlockPos offset) {
        int count = blocks.size();
        super.removeBlocksFromWorld(world, offset);
        if (count != blocks.size()) {
            cachedColliders = null;
        }
    }

    @Override
    public boolean canBeStabilized(Direction facing, BlockPos localPos) {
        return AllConfigs.server().kinetics.stabiliseStableContraptions.get();
    }

}
