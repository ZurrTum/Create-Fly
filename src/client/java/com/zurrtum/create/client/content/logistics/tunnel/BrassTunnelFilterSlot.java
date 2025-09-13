package com.zurrtum.create.client.content.logistics.tunnel;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.minecraft.util.math.Vec3d;

public class BrassTunnelFilterSlot extends ValueBoxTransform.Sided {

    @Override
    protected Vec3d getSouthLocation() {
        return VecHelper.voxelSpace(8, 13, 15.5f);
    }

}
