package com.zurrtum.create.client.flywheel.api.instance;

import com.zurrtum.create.client.flywheel.api.backend.BackendImplemented;

@BackendImplemented
public interface InstanceHandle {
    void setChanged();

    void setDeleted();

    void setVisible(boolean var1);

    boolean isVisible();
}
