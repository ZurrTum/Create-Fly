package com.zurrtum.create.client.compat.jei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.jei.CreateCategory;
import com.zurrtum.create.client.compat.jei.JeiClientPlugin;
import com.zurrtum.create.client.compat.jei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.PressRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.press.PressingRecipe;
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

public class PressingCategory extends CreateCategory<RecipeEntry<PressingRecipe>> {
    public static List<RecipeEntry<PressingRecipe>> getRecipes(PreparedRecipes preparedRecipes) {
        return preparedRecipes.getAll(AllRecipeTypes.PRESSING).stream().toList();
    }

    @Override
    @NotNull
    public IRecipeType<RecipeEntry<PressingRecipe>> getRecipeType() {
        return JeiClientPlugin.PRESSING;
    }

    @Override
    @NotNull
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.pressing");
    }

    @Override
    public IDrawable getIcon() {
        return new TwoIconRenderer(AllItems.MECHANICAL_PRESS, AllItems.IRON_SHEET);
    }

    @Override
    public int getHeight() {
        return 70;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeEntry<PressingRecipe> entry, IFocusGroup focuses) {
        PressingRecipe recipe = entry.value();
        builder.addInputSlot(27, 51).setBackground(SLOT, -1, -1).add(recipe.ingredient());
        builder.addOutputSlot(131, 51).setBackground(SLOT, -1, -1).add(recipe.result());
    }

    @Override
    public void draw(RecipeEntry<PressingRecipe> entry, IRecipeSlotsView recipeSlotsView, DrawContext graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_SHADOW.render(graphics, 61, 41);
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, 52, 54);
        graphics.state.addSpecialElement(new PressRenderState(new Matrix3x2f(graphics.getMatrices()), 73, -16));
    }
}
