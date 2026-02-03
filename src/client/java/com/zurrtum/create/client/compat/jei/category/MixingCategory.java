package com.zurrtum.create.client.compat.jei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.jei.JeiClientPlugin;
import com.zurrtum.create.client.compat.jei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
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

public class MixingCategory extends BasinCategory<MixingRecipe> {
    public static List<RecipeHolder<MixingRecipe>> getRecipes(RecipeMap preparedRecipes) {
        return preparedRecipes.byType(AllRecipeTypes.MIXING).stream().toList();
    }

    @Override
    @NotNull
    public IRecipeType<RecipeHolder<MixingRecipe>> getRecipeType() {
        return JeiClientPlugin.MIXING;
    }

    @Override
    @NotNull
    public Component getTitle() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<MixingRecipe> entry, IFocusGroup focuses) {
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
    public void draw(RecipeHolder<MixingRecipe> entry, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        MixingRecipe recipe = entry.value();
        drawBackground(recipe, graphics, recipe.results().size() + recipe.fluidResults().size());
    }
}
