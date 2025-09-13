package com.zurrtum.create.client.flywheel.lib.instance;

import com.zurrtum.create.client.flywheel.api.instance.InstanceHandle;
import com.zurrtum.create.client.flywheel.api.instance.InstanceType;

public class ShadowInstance extends AbstractInstance {
    public float x, y, z;
    public float entityX, entityZ;
    public float sizeX, sizeZ;
    public float alpha;
    public float radius;

    public ShadowInstance(InstanceType<? extends ShadowInstance> type, InstanceHandle handle) {
        super(type, handle);
    }
}
