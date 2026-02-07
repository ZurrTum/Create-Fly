package com.zurrtum.create.client.compat.jei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.jei.JeiClientPlugin;
import com.zurrtum.create.client.compat.jei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.render.PressBasinRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.mixer.CompactingRecipe;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.recipe.PreparedRecipes;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;

import java.util.List;

public class CompactingCategory extends BasinCategory<CompactingRecipe> {
    public static List<RecipeEntry<CompactingRecipe>> getRecipes(PreparedRecipes preparedRecipes) {
        return preparedRecipes.getAll(AllRecipeTypes.COMPACTING).stream().toList();
    }

    @Override
    @NotNull
    public IRecipeType<RecipeEntry<CompactingRecipe>> getRecipeType() {
        return JeiClientPlugin.PACKING;
    }

    @Override
    @NotNull
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.packing");
    }

    @Override
    public IDrawable getIcon() {
        return new TwoIconRenderer(AllItems.MECHANICAL_PRESS, AllItems.BASIN);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeEntry<CompactingRecipe> entry, IFocusGroup iFocusGroup) {
        CompactingRecipe recipe = entry.value();
        addIngredientSlots(builder, recipe);
        List<ProcessingOutput> results = recipe.results();
        int size = results.size();
        addResultSlots(builder, results, 0, size - 1, 51, size % 2 != 0);
        addHeatSlots(builder, recipe);
    }

    @Override
    public void draw(RecipeEntry<CompactingRecipe> entry, IRecipeSlotsView recipeSlotsView, DrawContext graphics, double mouseX, double mouseY) {
        CompactingRecipe recipe = entry.value();
        drawBackground(recipe, graphics, recipe.results().size());
        graphics.state.addSpecialElement(new PressBasinRenderState(new Matrix3x2f(graphics.getMatrices()), 91, -5));
    }
}
