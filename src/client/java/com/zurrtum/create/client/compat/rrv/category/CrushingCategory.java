package com.zurrtum.create.client.compat.rrv.category;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.client.recipe.ClientRecipeManager;
import com.google.common.base.Suppliers;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.rrv.CreateCategory;
import com.zurrtum.create.client.compat.rrv.view.CrushingView;
import com.zurrtum.create.content.kinetics.crusher.CrushingRecipe;
import com.zurrtum.create.content.kinetics.millstone.MillingRecipe;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.FabricIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class CrushingCategory extends CreateCategory {
    public static final CrushingCategory INSTANCE = new CrushingCategory();

    public static void register(ClientRecipeManager recipeManager, List<ReliableClientRecipe> output) {
        Collection<RecipeHolder<CrushingRecipe>> crushingRecipes = recipeManager.getRecipesForType(AllRecipeTypes.CRUSHING);
        for (RecipeHolder<CrushingRecipe> entry : crushingRecipes) {
            output.add(new CrushingView(entry.id().identifier(), entry.value()));
        }
        Supplier<ContextMap> context = Suppliers.memoize(() -> SlotDisplayContext.fromLevel(Minecraft.getInstance().level));
        for (RecipeHolder<MillingRecipe> entry : recipeManager.getRecipesForType(AllRecipeTypes.MILLING)) {
            MillingRecipe recipe = entry.value();
            Ingredient ingredient = recipe.ingredient();
            CustomIngredient customIngredient = ((FabricIngredient) ingredient).getCustomIngredient();
            ItemStack firstInput;
            if (customIngredient == null) {
                firstInput = ingredient.values.stream().findFirst().map(item -> item.value().getDefaultInstance())
                    .orElse(ItemStack.EMPTY);
            } else {
                firstInput = customIngredient.display().resolveForFirstStack(context.get());
            }
            if (!firstInput.isEmpty() && crushingRecipes.stream()
                .anyMatch(e -> e.value().ingredient().test(firstInput))) {
                continue;
            }
            output.add(new CrushingView(entry.id().identifier(), entry.value()));
        }
    }

    public CrushingCategory() {
        super("crushing");
    }

    @Override
    public int getDisplayHeight() {
        return 98;
    }

    @Override
    public int getSlotCount() {
        return 8;
    }

    @Override
    public ItemStack getIcon() {
        return AllItems.CRUSHING_WHEEL.getDefaultInstance();
    }

    @Override
    public ItemStack getSubIcon() {
        return AllItems.CRUSHED_GOLD.getDefaultInstance();
    }

    @Override
    public List<ItemStack> getCraftReferences() {
        return List.of(AllItems.CRUSHING_WHEEL.getDefaultInstance());
    }
}
