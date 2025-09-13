package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.api.goggles.IHaveHoveringInformation;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.BigItemStack;
import com.zurrtum.create.content.logistics.packager.InventorySummary;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerBlockEntity.StockTickerInventory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class StockTickerTooltipBehaviour extends TooltipBehaviour<StockTickerBlockEntity> implements IHaveHoveringInformation {
    public StockTickerTooltipBehaviour(StockTickerBlockEntity be) {
        super(be);
    }

    @Override
    public boolean addToTooltip(List<Text> tooltip, boolean isPlayerSneaking) {
        StockTickerInventory receivedPayments = blockEntity.receivedPayments;
        if (receivedPayments.isEmpty())
            return false;
        if (!blockEntity.behaviour.mayAdministrate(MinecraftClient.getInstance().player))
            return false;

        CreateLang.translate("stock_ticker.contains_payments").style(Formatting.WHITE).forGoggles(tooltip);

        InventorySummary summary = new InventorySummary();
        for (int i = 0, size = receivedPayments.size(); i < size; i++)
            summary.add(receivedPayments.getStack(i));
        for (BigItemStack entry : summary.getStacksByCount())
            CreateLang.builder().text(entry.stack.getName().getString() + " x" + entry.count).style(Formatting.GREEN).forGoggles(tooltip);

        CreateLang.translate("stock_ticker.click_to_retrieve").style(Formatting.GRAY).forGoggles(tooltip);
        return true;
    }
}
