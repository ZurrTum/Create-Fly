package com.zurrtum.create.client.flywheel.api.instance;

public interface Instance {
    InstanceType<?> type();

    InstanceHandle handle();

    default void setChanged() {
        this.handle().setChanged();
    }

    default void delete() {
        this.handle().setDeleted();
    }

    default void setVisible(boolean visible) {
        this.handle().setVisible(visible);
    }
}
