package com.zurrtum.create.client.flywheel.api.visualization;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface VisualManager<T> {
    int visualCount();

    void queueAdd(T var1);

    void queueRemove(T var1);

    void queueUpdate(T var1);
}
