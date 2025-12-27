package com.zurrtum.create.foundation.blockEntity.behaviour.inventory;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.world.Container;

public class VersionedInventoryTrackerBehaviour extends BlockEntityBehaviour<SmartBlockEntity> {

    public static final BehaviourType<VersionedInventoryTrackerBehaviour> TYPE = new BehaviourType<>();

    private int ignoredId;
    private int ignoredVersion;

    public VersionedInventoryTrackerBehaviour(SmartBlockEntity be) {
        super(be);
        reset();
    }

    public boolean stillWaiting(InvManipulationBehaviour behaviour) {
        return behaviour.hasInventory() && stillWaiting(behaviour.getInventory());
    }

    public boolean stillWaiting(Container handler) {
        if (handler instanceof VersionedInventory viw)
            return viw.getId() == ignoredId && viw.getVersion() == ignoredVersion;
        return false;
    }

    public void awaitNewVersion(InvManipulationBehaviour behaviour) {
        if (behaviour.hasInventory())
            awaitNewVersion(behaviour.getInventory());
    }

    public void awaitNewVersion(Container handler) {
        if (handler instanceof VersionedInventory viw) {
            ignoredId = viw.getId();
            ignoredVersion = viw.getVersion();
        }
    }

    public void reset() {
        ignoredVersion = -1;
        ignoredId = -1;
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

}
