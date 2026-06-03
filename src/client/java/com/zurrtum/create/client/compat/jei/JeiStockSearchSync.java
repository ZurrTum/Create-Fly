package com.zurrtum.create.client.compat.jei;

import com.zurrtum.create.client.content.logistics.stockTicker.StockSearchSync;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientListOverlay;
import org.jspecify.annotations.Nullable;

public record JeiStockSearchSync(IIngredientFilter filter, IIngredientListOverlay overlay) implements StockSearchSync {
    @Override
    public boolean slotSync() {
        return false;
    }

    @Override
    public void set(String value) {
        filter.setFilterText(value);
    }

    @Nullable
    @Override
    public String get(boolean force) {
        if (force || overlay.hasKeyboardFocus()) {
            return filter.getFilterText();
        }
        return null;
    }
}
