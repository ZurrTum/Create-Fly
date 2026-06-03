package com.zurrtum.create.client.content.logistics.stockTicker;

import org.jspecify.annotations.Nullable;

public interface StockSearchSync {
    boolean slotSync();

    void set(String value);

    @Nullable String get(boolean force);
}
