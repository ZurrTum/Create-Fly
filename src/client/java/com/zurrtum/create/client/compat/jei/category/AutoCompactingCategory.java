package com.zurrtum.create.client.compat.jei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.jei.CreateCategory;
import com.zurrtum.create.client.compat.jei.JeiClientPlugin;
import com.zurrtum.create.client.compat.jei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.PressBasinRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.press.MechanicalPressBlockEntity;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.*;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AutoCompactingCategory extends CreateCategory<RecipeEntry<CraftingRecipe>> {
    public static List<RecipeEntry<CraftingRecipe>> getRecipes(PreparedRecipes preparedRecipes) {
        List<RecipeEntry<CraftingRecipe>> recipes = new ArrayList<>();
        for (RecipeEntry<CraftingRecipe> entry : preparedRecipes.getAll(RecipeType.CRAFTING)) {
            CraftingRecipe recipe = entry.value();
            if (!MechanicalPressBlockEntity.canCompress(recipe) || AllRecipeTypes.shouldIgnoreInAutomation(entry)) {
                continue;
            }
            if (recipe instanceof ShapelessRecipe || recipe instanceof ShapedRecipe) {
                recipes.add(entry);
            }
        }
        return recipes;
    }

    @Override
    @NotNull
    public IRecipeType<RecipeEntry<CraftingRecipe>> getRecipeType() {
        return JeiClientPlugin.AUTOMATIC_PACKING;
    }

    @Override
    @NotNull
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.automatic_packing");
    }

    @Override
    public IDrawable getIcon() {
        return new TwoIconRenderer(AllItems.MECHANICAL_PRESS, Items.CRAFTING_TABLE);
    }

    @Override
    public int getHeight() {
        return 85;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeEntry<CraftingRecipe> entry, IFocusGroup focuses) {
        CraftingRecipe recipe = entry.value();
        ItemStack result;
        List<Ingredient> ingredients;
        if (recipe instanceof ShapedRecipe shapedRecipe) {
            result = shapedRecipe.result;
            ingredients = shapedRecipe.getIngredients().stream().filter(Optional::isPresent).map(Optional::get).toList();
        } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
            result = shapelessRecipe.result;
            ingredients = shapelessRecipe.ingredients;
        } else {
            return;
        }
        for (int i = 0, size = ingredients.size(), rows = size == 4 ? 2 : 3; i < size; i++) {
            builder.addInputSlot((rows == 2 ? 27 : 18) + (i % rows) * 19, 51 - (i / rows) * 19).setBackground(SLOT, -1, -1).add(ingredients.get(i));
        }
        builder.addOutputSlot(142, 51).setBackground(SLOT, -1, -1).add(result);
    }

    @Override
    public void draw(RecipeEntry<CraftingRecipe> entry, IRecipeSlotsView recipeSlotsView, DrawContext graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 136, 32);
        AllGuiTextures.JEI_SHADOW.render(graphics, 81, 68);
        graphics.state.addSpecialElement(new PressBasinRenderState(new Matrix3x2f(graphics.getMatrices()), 91, -5));
    }
}
