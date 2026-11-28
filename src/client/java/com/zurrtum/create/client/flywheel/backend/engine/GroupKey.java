package com.zurrtum.create.client.flywheel.backend.engine;

import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import com.zurrtum.create.client.flywheel.backend.engine.embed.Environment;
import org.jetbrains.annotations.UnknownNullability;

public class GroupKey {
    private @UnknownNullability InstanceType<?> instanceType;
    private @UnknownNullability Environment environment;
    private boolean readonly;

    public GroupKey(InstanceType<?> instanceType, Environment environment) {
        this.instanceType = instanceType;
        this.environment = environment;
    }

    public GroupKey() {
        instanceType = null;
        environment = null;
    }

    public GroupKey mutable() {
        readonly = false;
        return this;
    }

    public void readonly() {
        readonly = true;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public GroupKey update(InstanceType<?> instanceType, Environment environment) {
        this.instanceType = instanceType;
        this.environment = environment;
        return this;
    }

    public InstanceType<?> instanceType() {
        return instanceType;
    }

    public Environment environment() {
        return environment;
    }

    @Override
    public int hashCode() {
        return 31 * System.identityHashCode(instanceType) + System.identityHashCode(environment);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GroupKey key && key.instanceType == instanceType && key.environment == environment;
    }
}
