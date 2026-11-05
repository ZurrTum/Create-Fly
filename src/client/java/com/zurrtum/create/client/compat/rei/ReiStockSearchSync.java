package com.zurrtum.create.client.compat.rei;

import com.zurrtum.create.client.content.logistics.stockTicker.StockSearchSync;
import me.shedaniel.rei.api.client.gui.widgets.TextField;
import org.jspecify.annotations.Nullable;

public record ReiStockSearchSync(TextField search) implements StockSearchSync {
    @Override
    public boolean slotSync() {
        return false;
    }

    @Override
    public void set(String value) {
        search.setText(value);
    }

    @Nullable
    @Override
    public String get(boolean force) {
        if (force || search.isFocused()) {
            return search.getText();
        }
        return null;
    }
}
