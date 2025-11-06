package com.zurrtum.create.client.compat.jei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.jei.CreateCategory;
import com.zurrtum.create.client.compat.jei.JeiClientPlugin;
import com.zurrtum.create.client.compat.jei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.CrushWheelRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.crusher.AbstractCrushingRecipe;
import com.zurrtum.create.content.processing.recipe.ChanceOutput;
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

import java.util.ArrayList;
import java.util.List;

public class CrushingCategory extends CreateCategory<RecipeEntry<? extends AbstractCrushingRecipe>> {
    public static List<RecipeEntry<? extends AbstractCrushingRecipe>> getRecipes(PreparedRecipes preparedRecipes) {
        List<RecipeEntry<? extends AbstractCrushingRecipe>> recipes = new ArrayList<>();
        recipes.addAll(preparedRecipes.getAll(AllRecipeTypes.CRUSHING));
        recipes.addAll(preparedRecipes.getAll(AllRecipeTypes.MILLING));
        return recipes;
    }

    @Override
    @NotNull
    public IRecipeType<RecipeEntry<? extends AbstractCrushingRecipe>> getRecipeType() {
        return JeiClientPlugin.CRUSHING;
    }

    @Override
    @NotNull
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.crushing");
    }

    @Override
    public IDrawable getIcon() {
        return new TwoIconRenderer(AllItems.CRUSHING_WHEEL, AllItems.CRUSHED_GOLD);
    }

    @Override
    public int getHeight() {
        return 100;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeEntry<? extends AbstractCrushingRecipe> entry, IFocusGroup focuses) {
        AbstractCrushingRecipe recipe = entry.value();
        builder.addInputSlot(51, 3).setBackground(SLOT, -1, -1).add(recipe.ingredient());
        List<ChanceOutput> results = recipe.results();
        for (int i = 0, size = results.size(), start = (179 - 19 * size) / 2 + 3; i < size; i++) {
            addChanceSlot(builder, start + i * 19, 83, results.get(i));
        }
    }

    @Override
    public void draw(
        RecipeEntry<? extends AbstractCrushingRecipe> entry,
        IRecipeSlotsView recipeSlotsView,
        DrawContext graphics,
        double mouseX,
        double mouseY
    ) {
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 72, 7);
        graphics.state.addSpecialElement(new CrushWheelRenderState(new Matrix3x2f(graphics.getMatrices()), 42, 24));
    }
}
