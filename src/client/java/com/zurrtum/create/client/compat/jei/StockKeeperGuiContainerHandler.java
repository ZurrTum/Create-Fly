package com.zurrtum.create.client.compat.jei;

import com.zurrtum.create.client.content.logistics.stockTicker.StockKeeperRequestScreen;
import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.runtime.IClickableIngredient;

import java.util.Optional;

public class StockKeeperGuiContainerHandler implements IGuiContainerHandler<StockKeeperRequestScreen> {
    @Override
    public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(
        IClickableIngredientFactory factory,
        StockKeeperRequestScreen containerScreen,
        double mouseX,
        double mouseY
    ) {
        return containerScreen.getHoveredIngredient((int) mouseX, (int) mouseY)
            .flatMap(pair -> factory.createBuilder(pair.getFirst()).buildWithArea(pair.getSecond()));
    }
}
