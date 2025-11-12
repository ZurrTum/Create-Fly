package com.zurrtum.create.content.logistics.itemHatch;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ItemHatchBlockEntity extends SmartBlockEntity {

    public ServerFilteringBehaviour filtering;

    public ItemHatchBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.ITEM_HATCH, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(filtering = new ServerFilteringBehaviour(this));
    }

}
