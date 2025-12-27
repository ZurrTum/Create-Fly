package com.zurrtum.create.content.logistics.crate;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class CreativeCrateBlockEntity extends CrateBlockEntity implements Clearable {
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
    public void setChanged() {
        super.setChanged();
        inv.setChanged();
    }

    @Override
    public void clearContent() {
        filtering.setFilter(ItemStack.EMPTY);
    }
}