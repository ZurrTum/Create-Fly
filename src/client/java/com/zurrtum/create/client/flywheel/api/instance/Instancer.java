package com.zurrtum.create.client.flywheel.api.instance;

import com.zurrtum.create.client.flywheel.api.backend.BackendImplemented;
import org.jetbrains.annotations.Nullable;

@BackendImplemented
public interface Instancer<I extends Instance> {
    I createInstance();

    default void createInstances(I[] arr) {
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = this.createInstance();
        }

    }

    void stealInstance(@Nullable I var1);
}
