package com.zurrtum.create.client.compat.jei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.jei.CreateCategory;
import com.zurrtum.create.client.compat.jei.JeiClientPlugin;
import com.zurrtum.create.client.compat.jei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.MillstoneRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.millstone.MillingRecipe;
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

public class MillingCategory extends CreateCategory<RecipeEntry<MillingRecipe>> {
    public static List<RecipeEntry<MillingRecipe>> getRecipes(PreparedRecipes preparedRecipes) {
        return preparedRecipes.getAll(AllRecipeTypes.MILLING).stream().toList();
    }

    @Override
    @NotNull
    public IRecipeType<RecipeEntry<MillingRecipe>> getRecipeType() {
        return JeiClientPlugin.MILLING;
    }

    @Override
    @NotNull
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.milling");
    }

    @Override
    public IDrawable getIcon() {
        return new TwoIconRenderer(AllItems.MILLSTONE, AllItems.WHEAT_FLOUR);
    }

    @Override
    public int getHeight() {
        return 53;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeEntry<MillingRecipe> entry, IFocusGroup focuses) {
        MillingRecipe recipe = entry.value();
        builder.addInputSlot(15, 9).setBackground(SLOT, -1, -1).add(recipe.ingredient());
        List<ProcessingOutput> results = recipe.results();
        int size = results.size();
        if (size == 1) {
            addChanceSlot(builder, 139, 27, results.getFirst());
        } else {
            for (int i = 0; i < size; i++) {
                addChanceSlot(builder, i % 2 == 0 ? 133 : 152, 27 + (i / 2) * -19, results.get(i));
            }
        }
    }

    @Override
    public void draw(RecipeEntry<MillingRecipe> entry, IRecipeSlotsView recipeSlotsView, DrawContext graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_ARROW.render(graphics, 85, 32);
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 43, 4);
        AllGuiTextures.JEI_SHADOW.render(graphics, 32, 40);
        graphics.state.addSpecialElement(new MillstoneRenderState(new Matrix3x2f(graphics.getMatrices()), 42, 19));
    }
}
