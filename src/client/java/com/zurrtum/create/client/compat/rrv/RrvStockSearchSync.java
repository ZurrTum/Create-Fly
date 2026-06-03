package com.zurrtum.create.client.compat.rrv;

import cc.cassian.rrv.common.overlay.itemlist.view.ItemViewOverlay;
import com.zurrtum.create.client.content.logistics.stockTicker.StockSearchSync;
import net.minecraft.client.gui.components.EditBox;
import org.jspecify.annotations.Nullable;

public record RrvStockSearchSync(ItemViewOverlay overlay) implements StockSearchSync {
    @Override
    public boolean slotSync() {
        return true;
    }

    @Override
    public void set(String value) {
        EditBox searchbar = overlay.getSearchbar();
        if (searchbar != null) {
            searchbar.setValue(value);
        }
    }

    @Nullable
    @Override
    public String get(boolean force) {
        EditBox searchbar = overlay.getSearchbar();
        if (searchbar != null && (force || searchbar.isFocused())) {
            return searchbar.getValue();
        }
        return null;
    }
}
