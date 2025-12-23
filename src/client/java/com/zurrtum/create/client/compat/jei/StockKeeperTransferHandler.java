package com.zurrtum.create.client.compat.jei;

import com.zurrtum.create.client.content.logistics.stockTicker.CraftableBigItemStack;
import com.zurrtum.create.client.content.logistics.stockTicker.CraftableInput;
import com.zurrtum.create.client.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.packager.InventorySummary;
import com.zurrtum.create.content.logistics.stockTicker.StockKeeperRequestMenu;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import mezz.jei.common.transfer.RecipeTransferErrorInternal;
import mezz.jei.library.transfer.RecipeTransferErrorMissingSlots;
import mezz.jei.library.transfer.RecipeTransferErrorTooltip;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StockKeeperTransferHandler implements IUniversalRecipeTransferHandler<StockKeeperRequestMenu> {
    @Override
    public Class<? extends StockKeeperRequestMenu> getContainerClass() {
        return StockKeeperRequestMenu.class;
    }

    @Override
    public Optional<MenuType<StockKeeperRequestMenu>> getMenuType() {
        return Optional.empty();
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(
        StockKeeperRequestMenu container,
        Object object,
        IRecipeSlotsView recipeSlots,
        Player player,
        boolean maxTransfer,
        boolean doTransfer
    ) {
        if (!(object instanceof RecipeHolder<?> entry)) {
            return null;
        }
        if (!(container.screenReference instanceof StockKeeperRequestScreen screen)) {
            return RecipeTransferErrorInternal.INSTANCE;
        }
        Identifier id = entry.id().identifier();
        for (CraftableBigItemStack cbis : screen.recipesToOrder) {
            if (cbis.id.equals(id)) {
                return new RecipeTransferErrorTooltip(CreateLang.translateDirect("gui.stock_keeper.already_ordering_recipe"));
            }
        }
        if (screen.itemsToOrder.size() >= 9) {
            return new RecipeTransferErrorTooltip(CreateLang.translateDirect("gui.stock_keeper.slots_full"));
        }
        List<IRecipeSlotView> inputViews = new ArrayList<>();
        List<IRecipeSlotView> outputViews = new ArrayList<>();
        for (IRecipeSlotView view : recipeSlots.getSlotViews()) {
            RecipeIngredientRole role = view.getRole();
            if (role == RecipeIngredientRole.INPUT) {
                inputViews.add(view);
            } else if (role == RecipeIngredientRole.OUTPUT) {
                outputViews.add(view);
            }
        }
        CraftableInput inputs = CraftableInput.create(entry.value() instanceof CraftingRecipe);
        for (int i = 0, size = inputViews.size(); i < size; i++) {
            List<@Nullable ITypedIngredient<?>> list = inputViews.get(i).getAllIngredientsList();
            if (list.isEmpty()) {
                continue;
            }
            List<ItemStack> items = new ArrayList<>(size);
            for (ITypedIngredient<?> ingredient : list) {
                if (ingredient == null) {
                    continue;
                }
                Optional<ItemStack> value = ingredient.getIngredient(VanillaTypes.ITEM_STACK);
                if (value.isEmpty()) {
                    return RecipeTransferErrorInternal.INSTANCE;
                }
                items.add(value.get());
            }
            inputs.add(items, i);
        }
        if (inputs.data().size() > 9) {
            return RecipeTransferErrorInternal.INSTANCE;
        }
        ItemStack output = null;
        for (IRecipeSlotView view : outputViews) {
            Optional<ItemStack> stack = view.getDisplayedItemStack();
            if (stack.isPresent()) {
                output = stack.get();
                break;
            }
        }
        if (output == null) {
            return new RecipeTransferErrorMissingSlots(CreateLang.translateDirect("gui.stock_keeper.recipe_result_empty"), outputViews);
        }
        InventorySummary summary = screen.getMenu().contentHolder.getLastClientsideStockSnapshotAsSummary();
        if (summary == null) {
            return RecipeTransferErrorInternal.INSTANCE;
        }
        IntSet missingIndices = inputs.getMissing(summary.getStacksByCount());
        if (!missingIndices.isEmpty()) {
            List<IRecipeSlotView> missingViews = new ArrayList<>();
            missingIndices.forEach(index -> missingViews.add(inputViews.get(index)));
            return new RecipeTransferErrorMissingSlots(CreateLang.translateDirect("gui.stock_keeper.not_in_stock"), missingViews);
        }
        if (doTransfer) {
            CraftableBigItemStack cbis = new CraftableBigItemStack(id, inputs, output);
            screen.recipesToOrder.add(cbis);
            screen.searchBox.setValue("");
            screen.refreshSearchNextTick = true;
            screen.requestCraftable(cbis, maxTransfer ? cbis.stack.getMaxStackSize() : 1);
        }
        return null;
    }
}