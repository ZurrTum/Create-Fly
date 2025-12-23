package com.zurrtum.create.client.compat.rei;

import com.zurrtum.create.client.content.logistics.stockTicker.CraftableBigItemStack;
import com.zurrtum.create.client.content.logistics.stockTicker.CraftableInput;
import com.zurrtum.create.client.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.packager.InventorySummary;
import it.unimi.dsi.fastutil.ints.IntSet;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.InputIngredient;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class StockKeeperTransferHandler implements TransferHandler {
    private static ItemStack findOutput(Display display) {
        for (EntryIngredient ingredient : display.getOutputEntries()) {
            if (ingredient.isEmpty()) {
                continue;
            }
            for (EntryStack<?> stack : ingredient) {
                if (stack.getType() != VanillaEntryTypes.ITEM) {
                    continue;
                }
                return stack.castValue();
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ApplicabilityResult checkApplicable(Context context) {
        if (context.getContainerScreen() instanceof StockKeeperRequestScreen && context.getDisplay().getDisplayLocation().isPresent()) {
            return ApplicabilityResult.createApplicable();
        }
        return ApplicabilityResult.createNotApplicable();
    }

    @Override
    public Result handle(Context context) {
        StockKeeperRequestScreen screen = (StockKeeperRequestScreen) context.getContainerScreen();
        Display display = context.getDisplay();
        Identifier id = display.getDisplayLocation().orElseThrow();
        for (CraftableBigItemStack cbis : screen.recipesToOrder) {
            if (cbis.id.equals(id)) {
                return Result.createFailed(CreateLang.translateDirect("gui.stock_keeper.already_ordering_recipe"));
            }
        }
        if (screen.itemsToOrder.size() >= 9) {
            return Result.createFailed(CreateLang.translateDirect("gui.stock_keeper.slots_full"));
        }
        CraftableInput inputs = CraftableInput.create(display.getCategoryIdentifier().equals(BuiltinPlugin.CRAFTING));
        List<InputIngredient<EntryStack<?>>> inputIngredients = display.getInputIngredients(context.getMenu(), context.getMinecraft().player);
        for (InputIngredient<EntryStack<?>> input : inputIngredients) {
            List<EntryStack<?>> ingredient = input.get();
            int size = ingredient.size();
            if (size == 0) {
                continue;
            }
            if (size == 1) {
                EntryStack<?> stack = ingredient.getFirst();
                if (stack.getType() != VanillaEntryTypes.ITEM) {
                    return Result.createNotApplicable();
                }
                inputs.add(List.of(stack.<ItemStack>castValue()), input.getDisplayIndex());
                continue;
            }
            List<ItemStack> items = new ArrayList<>(size);
            for (int j = 0; j < size; j++) {
                EntryStack<?> stack = ingredient.get(j);
                if (stack.getType() != VanillaEntryTypes.ITEM) {
                    return Result.createNotApplicable();
                }
                items.add(stack.castValue());
            }
            inputs.add(items, input.getDisplayIndex());
        }
        if (inputs.data().size() > 9) {
            return Result.createNotApplicable();
        }
        ItemStack output = findOutput(display);
        if (output.isEmpty()) {
            return Result.createFailed(CreateLang.translateDirect("gui.stock_keeper.recipe_result_empty"));
        }
        InventorySummary summary = screen.getMenu().contentHolder.getLastClientsideStockSnapshotAsSummary();
        if (summary == null) {
            return Result.createNotApplicable();
        }
        IntSet missingIndices = inputs.getMissing(summary.getStacksByCount());
        if (!missingIndices.isEmpty()) {
            return Result.createFailed(CreateLang.translateDirect("gui.stock_keeper.not_in_stock"))
                .renderer((graphics, mouseX, mouseY, delta, widgets, bounds, d) -> {
                    int i = 0;
                    for (Widget widget : widgets) {
                        if (widget instanceof Slot slot && slot.getNoticeMark() == Slot.INPUT) {
                            if (missingIndices.contains(i++)) {
                                Rectangle innerBounds = slot.getInnerBounds();
                                graphics.fill(innerBounds.x, innerBounds.y, innerBounds.getMaxX(), innerBounds.getMaxY(), 0x40ff0000);
                            }
                        }
                    }
                });
        }
        if (context.isActuallyCrafting()) {
            CraftableBigItemStack cbis = new CraftableBigItemStack(id, inputs, output);
            screen.recipesToOrder.add(cbis);
            screen.searchBox.setValue("");
            screen.refreshSearchNextTick = true;
            screen.requestCraftable(cbis, context.isStackedCrafting() ? cbis.stack.getMaxStackSize() : 1);
        }
        return Result.createSuccessful().blocksFurtherHandling();
    }
}
