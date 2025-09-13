package com.zurrtum.create.content.logistics.crate;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class CreativeCrateBlockEntity extends CrateBlockEntity {
    ServerFilteringBehaviour filtering;
    public BottomlessItemHandler inv;

    public CreativeCrateBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.CREATIVE_CRATE, pos, state);
        inv = new BottomlessItemHandler(filtering::getFilter);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(filtering = new ServerFilteringBehaviour(this));
    }

    @Override
    public void markDirty() {
        super.markDirty();
        inv.markDirty();
    }
}