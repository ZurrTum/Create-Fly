package com.zurrtum.create.foundation.blockEntity.behaviour.inventory;

import java.util.concurrent.atomic.AtomicInteger;

public interface VersionedInventory {
    AtomicInteger idGenerator = new AtomicInteger();

    int getVersion();

    int getId();
}