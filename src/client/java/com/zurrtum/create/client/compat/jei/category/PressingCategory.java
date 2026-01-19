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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import org.joml.Matrix3x2f;

import java.util.List;

public class PressingCategory extends CreateCategory<RecipeHolder<PressingRecipe>> {
    public static List<RecipeHolder<PressingRecipe>> getRecipes(RecipeMap preparedRecipes) {
        return preparedRecipes.byType(AllRecipeTypes.PRESSING).stream().toList();
    }

    @Override
    public IRecipeType<RecipeHolder<PressingRecipe>> getRecipeType() {
        return JeiClientPlugin.PRESSING;
    }

    @Override
    public Component getTitle() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<PressingRecipe> entry, IFocusGroup focuses) {
        PressingRecipe recipe = entry.value();
        builder.addInputSlot(27, 51).setBackground(SLOT, -1, -1).add(recipe.ingredient());
        builder.addOutputSlot(131, 51).setBackground(SLOT, -1, -1).add(recipe.result());
    }

    @Override
    public void draw(RecipeHolder<PressingRecipe> entry, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_SHADOW.render(graphics, 61, 41);
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, 52, 54);
        graphics.guiRenderState.submitPicturesInPictureState(new PressRenderState(new Matrix3x2f(graphics.pose()), 73, -16));
    }
}
