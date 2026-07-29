package com.zurrtum.create.client.compat.rei;

import com.zurrtum.create.client.content.logistics.stockTicker.StockSearchSync;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.widgets.TextField;
import org.jspecify.annotations.Nullable;

public class ReiStockSearchSync implements StockSearchSync {
    @Override
    public boolean slotSync() {
        return false;
    }

    @Override
    public void set(String value) {
        TextField search = REIRuntime.getInstance().getSearchTextField();
        if (search != null) {
            search.setText(value);
        }
    }

    @Override
    public @Nullable String get(boolean force) {
        TextField search = REIRuntime.getInstance().getSearchTextField();
        if (search != null) {
            return search.getText();
        }

        return null;
    }
}
