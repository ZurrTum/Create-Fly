package com.zurrtum.create.content.logistics.depot;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class DepotBlockEntity extends SmartBlockEntity {

    public DepotBehaviour depotBehaviour;

    public DepotBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.DEPOT, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(depotBehaviour = new DepotBehaviour(this));
        depotBehaviour.addSubBehaviours(behaviours);
    }

    @Nullable
    public TransportedItemStack getHeldItem() {
        return depotBehaviour.heldItem;
    }

    public void setHeldItem(TransportedItemStack item) {
        depotBehaviour.setHeldItem(item);
    }

    public void removeHeldItem() {
        depotBehaviour.removeHeldItem();
    }
}
