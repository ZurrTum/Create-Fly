package com.zurrtum.create.client.flywheel.backend.engine;

import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import com.zurrtum.create.client.flywheel.backend.engine.embed.Environment;

public record GroupKey<I extends Instance>(InstanceType<I> instanceType, Environment environment) {
}
