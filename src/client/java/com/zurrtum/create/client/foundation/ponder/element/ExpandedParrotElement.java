package com.zurrtum.create.client.foundation.ponder.element;

import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.client.ponder.api.element.ParrotPose;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.element.ParrotElementImpl;
import net.minecraft.util.math.Vec3d;

import java.util.function.Supplier;

public class ExpandedParrotElement extends ParrotElementImpl {

    protected boolean deferConductor = false;

    protected ExpandedParrotElement(Vec3d location, Supplier<? extends ParrotPose> pose) {
        super(location, pose);
    }

    @Override
    public void reset(PonderScene scene) {
        super.reset(scene);
        AllSynchedDatas.PARROT_TRAIN_HAT.set(entity, false);
        deferConductor = false;
    }

    @Override
    public void tick(PonderScene scene) {
        boolean wasNull = entity == null;
        super.tick(scene);
        if (wasNull) {
            if (deferConductor) {
                setConductor(true);
            }
            deferConductor = false;
        }
    }

    public void setConductor(boolean isConductor) {
        if (entity == null) {
            deferConductor = isConductor;
            return;
        }
        if (isConductor)
            AllSynchedDatas.PARROT_TRAIN_HAT.set(entity, true);
        else
            AllSynchedDatas.PARROT_TRAIN_HAT.set(entity, false);
    }
}