package com.zurrtum.create.client.compat.eiv;

import com.zurrtum.create.client.AllKeys;
import com.zurrtum.create.client.content.logistics.stockTicker.CraftableBigItemStack;
import com.zurrtum.create.client.content.logistics.stockTicker.CraftableInput;
import com.zurrtum.create.client.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.client.mixin.CraftingViewRecipeAccessor;
import com.zurrtum.create.content.logistics.packager.InventorySummary;
import de.crafty.eiv.common.api.recipe.IEivViewRecipe;
import de.crafty.eiv.common.recipe.inventory.SlotContent;
import de.crafty.eiv.common.recipe.item.FluidItem;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.zurrtum.create.Create.MOD_ID;

public class StockKeeperTransferHandler implements RecipeTransferHandler {
    @Override
    public boolean checkApplicable(Screen screen) {
        return screen instanceof StockKeeperRequestScreen;
    }

    @Override
    public boolean handle(Screen currentScreen, IEivViewRecipe current, RecipeButton button, boolean craft) {
        Identifier id = null;
        ItemStack output = null;
        for (SlotContent result : current.getResults()) {
            for (ItemStack stack : result.getValidContents()) {
                if (stack.getItem() instanceof FluidItem) {
                    return false;
                }
                if (stack.isEmpty()) {
                    continue;
                }
                id = Identifier.fromNamespaceAndPath(MOD_ID, "result_" + result.hashCode());
                output = stack;
            }
        }
        if (output == null) {
            button.setTooltip(CreateLang.translateDirect("gui.stock_keeper.recipe_result_empty"));
            return false;
        }
        StockKeeperRequestScreen screen = (StockKeeperRequestScreen) currentScreen;
        for (CraftableBigItemStack cbis : screen.recipesToOrder) {
            if (cbis.id.equals(id)) {
                button.setTooltip(CreateLang.translateDirect("gui.stock_keeper.already_ordering_recipe"));
                return false;
            }
        }
        if (screen.itemsToOrder.size() >= 9) {
            button.setTooltip(CreateLang.translateDirect("gui.stock_keeper.slots_full"));
            return false;
        }
        CraftableInput inputs;
        if (current instanceof CraftingViewRecipeAccessor accessor) {
            inputs = CraftableInput.create(true);
            HashMap<Integer, SlotContent> ingredientSlotContents = accessor.getIngredientSlotContents();
            for (int i = 0; i < 9; i++) {
                SlotContent ingredient = ingredientSlotContents.get(i);
                if (ingredient == null) {
                    continue;
                }
                List<ItemStack> contents = ingredient.getValidContents();
                int size = contents.size();
                if (size == 0) {
                    continue;
                }
                if (size == 1) {
                    ItemStack stack = contents.getFirst();
                    if (stack.getItem() instanceof FluidItem) {
                        return false;
                    }
                    inputs.add(List.of(getRawStack(stack)), i);
                    continue;
                }
                List<ItemStack> items = new ArrayList<>(size);
                for (ItemStack stack : contents) {
                    if (stack.getItem() instanceof FluidItem) {
                        return false;
                    }
                    items.add(getRawStack(stack));
                }
                inputs.add(items, i);
            }
        } else {
            inputs = CraftableInput.create(false);
            List<SlotContent> ingredients = current.getIngredients();
            for (int i = 0, ingredientSize = ingredients.size(); i < ingredientSize; i++) {
                SlotContent ingredient = ingredients.get(i);
                List<ItemStack> contents = ingredient.getValidContents();
                int size = contents.size();
                if (size == 0) {
                    continue;
                }
                if (size == 1) {
                    ItemStack stack = contents.getFirst();
                    if (stack.getItem() instanceof FluidItem) {
                        return false;
                    }
                    inputs.add(List.of(getRawStack(stack)), i);
                    continue;
                }
                List<ItemStack> items = new ArrayList<>(size);
                for (ItemStack stack : contents) {
                    if (stack.getItem() instanceof FluidItem) {
                        return false;
                    }
                    items.add(getRawStack(stack));
                }
                inputs.add(items, i);
            }
        }
        if (inputs.data().size() > 9) {
            return false;
        }
        InventorySummary summary = screen.getMenu().contentHolder.getLastClientsideStockSnapshotAsSummary();
        if (summary == null) {
            return false;
        }
        IntSet missingIndices = inputs.getMissing(summary.getStacksByCount());
        if (!missingIndices.isEmpty()) {
            button.updateMissing(missingIndices, CreateLang.translateDirect("gui.stock_keeper.not_in_stock"));
            return false;
        }
        if (craft) {
            CraftableBigItemStack cbis = new CraftableBigItemStack(id, inputs, output);
            screen.recipesToOrder.add(cbis);
            screen.searchBox.setValue("");
            screen.refreshSearchNextTick = true;
            screen.requestCraftable(cbis, AllKeys.hasShiftDown() ? cbis.stack.getMaxStackSize() : 1);
        }
        button.setSuccess();
        return true;
    }

    private static ItemStack getRawStack(ItemStack stack) {
        CustomData custom = stack.get(DataComponents.CUSTOM_DATA);
        if (custom != null && !custom.isEmpty()) {
            CompoundTag nbt = custom.copyTag();
            if (nbt.contains("eiv_recipeTag")) {
                stack = stack.copy();
                if (nbt.size() == 1) {
                    stack.remove(DataComponents.CUSTOM_DATA);
                } else {
                    nbt.remove("eiv_recipeTag");
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
                }
            }
        }
        return stack;
    }

}
