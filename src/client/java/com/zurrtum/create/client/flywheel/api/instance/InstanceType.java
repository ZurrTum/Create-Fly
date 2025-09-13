package com.zurrtum.create.client.flywheel.api.instance;

import com.zurrtum.create.client.flywheel.api.layout.Layout;
import net.minecraft.util.Identifier;

public interface InstanceType<I extends Instance> {
    I create(InstanceHandle var1);

    Layout layout();

    InstanceWriter<I> writer();

    Identifier vertexShader();

    Identifier cullShader();
}
