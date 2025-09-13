package com.zurrtum.create.client.content.fluids;

import com.zurrtum.create.client.flywheel.api.instance.InstanceHandle;
import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;

public class FluidInstance extends TransformedInstance {

    public float progress;
    public float vScale;
    public float v0;

    public FluidInstance(InstanceType<? extends FluidInstance> type, InstanceHandle handle) {
        super(type, handle);
    }
}
