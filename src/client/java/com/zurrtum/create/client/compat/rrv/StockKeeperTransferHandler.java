package com.zurrtum.create.client.compat.rrv;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.ReliableRecipeViewer;
import cc.cassian.rrv.common.builtin.anvil.AnvilCombiningClientRecipeType;
import cc.cassian.rrv.common.builtin.burning.BurningClientRecipeType;
import cc.cassian.rrv.common.builtin.crafting.CraftingClientRecipe;
import cc.cassian.rrv.common.builtin.entity.EntityClientRecipeType;
import cc.cassian.rrv.common.builtin.tag.item.ItemTagClientRecipeType;
import cc.cassian.rrv.common.builtin.villager.VillagerClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu.SlotFillContext;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import cc.cassian.rrv.common.recipe.item.FluidItem;
import com.zurrtum.create.client.compat.rrv.category.PotionCategory;
import com.zurrtum.create.client.compat.rrv.category.SpoutFillingCategory;
import com.zurrtum.create.client.content.logistics.stockTicker.CraftableBigItemStack;
import com.zurrtum.create.client.content.logistics.stockTicker.CraftableInput;
import com.zurrtum.create.client.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.packager.InventorySummary;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.List;

public class StockKeeperTransferHandler implements RecipeTransferHandler {
    @Override
    public boolean checkApplicable(Screen screen, ReliableClientRecipeType type) {
        if (screen instanceof StockKeeperRequestScreen) {
            if (type == ItemTagClientRecipeType.INSTANCE || type == EntityClientRecipeType.INSTANCE || type == BurningClientRecipeType.INSTANCE) {
                return false;
            }
            if (type instanceof VillagerClientRecipeType || type instanceof AnvilCombiningClientRecipeType) {
                return false;
            }
            return type != SpoutFillingCategory.INSTANCE && type != PotionCategory.INSTANCE;
        }
        return false;
    }

    @Override
    public boolean handle(Screen currentScreen, ReliableClientRecipe current, RecipeButton button, boolean craft) {
        Identifier id = current.getId();
        if (id == null) {
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
        ItemStack output = null;
        if (current instanceof CraftingClientRecipe) {
            inputs = CraftableInput.create(true);
            SlotFillContext context = new SlotFillContext();
            current.bindSlots(context);
            for (int i = 0; i < 9; i++) {
                SlotContent ingredient = context.contentBySlot(i);
                List<ItemStack> contents = ingredient.getValidContents();
                int size = contents.size();
                if (size == 0) {
                    continue;
                }
                if (size == 1) {
                    inputs.add(List.of(getRawStack(contents.getFirst())), i);
                    continue;
                }
                List<ItemStack> items = new ArrayList<>(size);
                for (ItemStack stack : contents) {
                    items.add(getRawStack(stack));
                }
                inputs.add(items, i);
            }
            List<ItemStack> items = context.contentBySlot(9).getValidContents();
            if (!items.isEmpty()) {
                output = items.getFirst();
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
            ItemStack fluid = null;
            Find:
            for (SlotContent result : current.getResults()) {
                for (ItemStack stack : result.getValidContents()) {
                    if (stack.getItem() instanceof FluidItem) {
                        if (fluid == null) {
                            fluid = stack;
                        }
                        continue;
                    }
                    if (stack.isEmpty()) {
                        continue;
                    }
                    output = stack;
                    break Find;
                }
            }
            if (output == null) {
                output = fluid;
            }
        }
        if (output == null) {
            button.setTooltip(CreateLang.translateDirect("gui.stock_keeper.recipe_result_empty"));
            return false;
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
            screen.requestCraftable(cbis, Minecraft.getInstance().hasShiftDown() ? cbis.stack.getMaxStackSize() : 1);
        }
        button.setSuccess();
        return true;
    }

    public ItemStack getRawStack(ItemStack stack) {
        CustomData custom = stack.get(DataComponents.CUSTOM_DATA);
        if (custom != null && !custom.isEmpty()) {
            CompoundTag nbt = custom.copyTag();
            List<String> list = new ArrayList<>();
            String prefix = ReliableRecipeViewer.MOD_ID + "_";
            for (String key : nbt.keySet()) {
                if (key.startsWith(prefix)) {
                    list.add(key);
                }
            }
            if (!list.isEmpty()) {
                stack = stack.copy();
                if (nbt.size() == list.size()) {
                    stack.remove(DataComponents.CUSTOM_DATA);
                } else {
                    for (String key : list) {
                        nbt.remove(key);
                    }
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
                }
            }
        }
        return stack;
    }
}
