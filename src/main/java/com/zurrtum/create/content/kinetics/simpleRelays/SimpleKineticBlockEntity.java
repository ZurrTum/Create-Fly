package com.zurrtum.create.content.kinetics.simpleRelays;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class SimpleKineticBlockEntity extends KineticBlockEntity {

    public SimpleKineticBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static SimpleKineticBlockEntity small(BlockPos pos, BlockState state) {
        return new SimpleKineticBlockEntity(AllBlockEntityTypes.ENCASED_COGWHEEL, pos, state);
    }

    public static SimpleKineticBlockEntity large(BlockPos pos, BlockState state) {
        return new SimpleKineticBlockEntity(AllBlockEntityTypes.ENCASED_LARGE_COGWHEEL, pos, state);
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return new AABB(worldPosition).inflate(1);
    }

    @Override
    public List<BlockPos> addPropagationLocations(IRotate block, BlockState state, List<BlockPos> neighbours) {
        if (!ICogWheel.isLargeCog(state))
            return super.addPropagationLocations(block, state, neighbours);

        BlockPos.betweenClosedStream(new BlockPos(-1, -1, -1), new BlockPos(1, 1, 1)).forEach(offset -> {
            if (offset.distSqr(BlockPos.ZERO) == 2)
                neighbours.add(worldPosition.offset(offset));
        });
        return neighbours;
    }

    @Override
    public boolean isNoisy() {
        return false;
    }

}
