package com.zurrtum.create.content.kinetics.simpleRelays;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;

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
    protected Box createRenderBoundingBox() {
        return new Box(pos).expand(1);
    }

    @Override
    public List<BlockPos> addPropagationLocations(IRotate block, BlockState state, List<BlockPos> neighbours) {
        if (!ICogWheel.isLargeCog(state))
            return super.addPropagationLocations(block, state, neighbours);

        BlockPos.stream(new BlockPos(-1, -1, -1), new BlockPos(1, 1, 1)).forEach(offset -> {
            if (offset.getSquaredDistance(BlockPos.ZERO) == 2)
                neighbours.add(pos.add(offset));
        });
        return neighbours;
    }

    @Override
    public boolean isNoisy() {
        return false;
    }

}
