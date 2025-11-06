package com.zurrtum.create.client.compat.jei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.jei.CreateCategory;
import com.zurrtum.create.client.compat.jei.JeiClientPlugin;
import com.zurrtum.create.client.compat.jei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.FanRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Items;
import net.minecraft.recipe.PreparedRecipes;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmokingRecipe;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;

import java.util.List;

public class FanSmokingCategory extends CreateCategory<RecipeEntry<SmokingRecipe>> {
    public static List<RecipeEntry<SmokingRecipe>> getRecipes(PreparedRecipes preparedRecipes) {
        return preparedRecipes.getAll(RecipeType.SMOKING).stream().filter(AllRecipeTypes.CAN_BE_AUTOMATED).toList();
    }

    @Override
    @NotNull
    public IRecipeType<RecipeEntry<SmokingRecipe>> getRecipeType() {
        return JeiClientPlugin.FAN_SMOKING;
    }

    @Override
    @NotNull
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.fan_smoking");
    }

    @Override
    public IDrawable getIcon() {
        return new TwoIconRenderer(AllItems.PROPELLER, Items.CAMPFIRE);
    }

    @Override
    public int getHeight() {
        return 77;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeEntry<SmokingRecipe> entry, IFocusGroup focuses) {
        SmokingRecipe recipe = entry.value();
        builder.addInputSlot(21, 48).setBackground(SLOT, -1, -1).add(recipe.ingredient());
        builder.addOutputSlot(141, 48).setBackground(SLOT, -1, -1).add(recipe.result());
    }

    @Override
    public void draw(RecipeEntry<SmokingRecipe> entry, IRecipeSlotsView recipeSlotsView, DrawContext graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_SHADOW.render(graphics, 46, 27);
        AllGuiTextures.JEI_LIGHT.render(graphics, 65, 39);
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, 54, 51);
        graphics.state.addSpecialElement(new FanRenderState(new Matrix3x2f(graphics.getMatrices()), 56, 4, Blocks.FIRE.getDefaultState()));
    }
}
