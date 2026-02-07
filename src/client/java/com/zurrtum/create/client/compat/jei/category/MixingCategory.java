package com.zurrtum.create.client.compat.jei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.jei.JeiClientPlugin;
import com.zurrtum.create.client.compat.jei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.render.MixingBasinRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
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

public class MixingCategory extends BasinCategory<MixingRecipe> {
    public static List<RecipeEntry<MixingRecipe>> getRecipes(PreparedRecipes preparedRecipes) {
        return preparedRecipes.getAll(AllRecipeTypes.MIXING).stream().toList();
    }

    @Override
    @NotNull
    public IRecipeType<RecipeEntry<MixingRecipe>> getRecipeType() {
        return JeiClientPlugin.MIXING;
    }

    @Override
    @NotNull
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.mixing");
    }

    @Override
    public IDrawable getIcon() {
        return new TwoIconRenderer(AllItems.MECHANICAL_MIXER, AllItems.BASIN);
    }

    @Override
    public int getHeight() {
        return 103;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeEntry<MixingRecipe> entry, IFocusGroup focuses) {
        MixingRecipe recipe = entry.value();
        addIngredientSlots(builder, recipe);
        List<ProcessingOutput> results = recipe.results();
        List<FluidStack> fluidResults = recipe.fluidResults();
        int resultSize = results.size();
        int size = resultSize + fluidResults.size();
        boolean isOddSize = size % 2 != 0;
        int end = size - 1;
        int y = size <= 4 ? 51 : 60;
        addResultSlots(builder, results, 0, end, y, isOddSize);
        addFluidResultSlots(builder, fluidResults, resultSize, end, y, isOddSize);
        addHeatSlots(builder, recipe);
    }

    @Override
    public void draw(RecipeEntry<MixingRecipe> entry, IRecipeSlotsView recipeSlotsView, DrawContext graphics, double mouseX, double mouseY) {
        MixingRecipe recipe = entry.value();
        drawBackground(recipe, graphics, recipe.results().size() + recipe.fluidResults().size());
        graphics.state.addSpecialElement(new MixingBasinRenderState(new Matrix3x2f(graphics.getMatrices()), 91, -5));
    }
}
