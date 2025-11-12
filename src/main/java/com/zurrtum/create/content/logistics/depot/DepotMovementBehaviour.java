package com.zurrtum.create.content.logistics.depot;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;

public class DepotMovementBehaviour extends MovementBehaviour {
    @Override
    public void tick(MovementContext context) {
        if (context.world.isClientSide()) {
            DepotBehaviour behaviour;
            if (context.temporaryData == null) {
                if (AllClientHandle.INSTANCE.getBlockEntityClientSide(context.contraption, context.localPos) instanceof DepotBlockEntity be) {
                    behaviour = be.depotBehaviour;
                    context.temporaryData = behaviour;
                } else {
                    return;
                }
            } else {
                behaviour = (DepotBehaviour) context.temporaryData;
            }
            TransportedItemStack heldItem = behaviour.heldItem;
            if (heldItem != null) {
                behaviour.tick(heldItem);
            }
        }
    }
}
