package com.zurrtum.create.client.flywheel.api.instance;

import com.zurrtum.create.client.flywheel.api.backend.BackendImplemented;
import com.zurrtum.create.client.flywheel.api.model.Model;

@BackendImplemented
public interface InstancerProvider {
    <I extends Instance> Instancer<I> instancer(InstanceType<I> var1, Model var2, int var3);

    default <I extends Instance> Instancer<I> instancer(InstanceType<I> type, Model model) {
        return this.<I>instancer(type, model, 0);
    }
}
