package com.zurrtum.create.content.logistics.depot;

import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;

public class DepotMovementBehaviour extends MovementBehaviour {
    @Override
    public void tick(MovementContext context) {
        DepotBehaviour behaviour;
        if (context.temporaryData == null) {
            DepotBlockEntity depotBlockEntity = (DepotBlockEntity) context.contraption.presentBlockEntities.get(context.localPos);
            if (depotBlockEntity == null) {
                return;
            }
            behaviour = depotBlockEntity.depotBehaviour;
            context.temporaryData = behaviour;
        } else {
            behaviour = (DepotBehaviour) context.temporaryData;
        }
        TransportedItemStack heldItem = behaviour.heldItem;
        if (heldItem != null) {
            behaviour.tick(heldItem);
        }
    }
}
