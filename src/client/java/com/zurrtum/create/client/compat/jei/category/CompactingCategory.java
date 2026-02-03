package com.zurrtum.create.client.compat.jei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.jei.JeiClientPlugin;
import com.zurrtum.create.client.compat.jei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.mixer.CompactingRecipe;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CompactingCategory extends BasinCategory<CompactingRecipe> {
    public static List<RecipeHolder<CompactingRecipe>> getRecipes(RecipeMap preparedRecipes) {
        return preparedRecipes.byType(AllRecipeTypes.COMPACTING).stream().toList();
    }

    @Override
    @NotNull
    public IRecipeType<RecipeHolder<CompactingRecipe>> getRecipeType() {
        return JeiClientPlugin.PACKING;
    }

    @Override
    @NotNull
    public Component getTitle() {
        return CreateLang.translateDirect("recipe.packing");
    }

    @Override
    public IDrawable getIcon() {
        return new TwoIconRenderer(AllItems.MECHANICAL_PRESS, AllItems.BASIN);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<CompactingRecipe> entry, IFocusGroup iFocusGroup) {
        CompactingRecipe recipe = entry.value();
        addIngredientSlots(builder, recipe);
        List<ProcessingOutput> results = recipe.results();
        int size = results.size();
        addResultSlots(builder, results, 0, size - 1, 51, size % 2 != 0);
        addHeatSlots(builder, recipe);
    }

    @Override
    public void draw(RecipeHolder<CompactingRecipe> entry, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        CompactingRecipe recipe = entry.value();
        drawBackground(recipe, graphics, recipe.results().size());
    }
}
