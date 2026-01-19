package com.zurrtum.create.client.compat.jei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.jei.CreateCategory;
import com.zurrtum.create.client.compat.jei.JeiClientPlugin;
import com.zurrtum.create.client.compat.jei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.SawRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.saw.CuttingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import org.joml.Matrix3x2f;

import java.util.List;

public class SawingCategory extends CreateCategory<RecipeHolder<CuttingRecipe>> {
    public static List<RecipeHolder<CuttingRecipe>> getRecipes(RecipeMap preparedRecipes) {
        return preparedRecipes.byType(AllRecipeTypes.CUTTING).stream().toList();
    }

    @Override
    public IRecipeType<RecipeHolder<CuttingRecipe>> getRecipeType() {
        return JeiClientPlugin.SAWING;
    }

    @Override
    public Component getTitle() {
        return CreateLang.translateDirect("recipe.sawing");
    }

    @Override
    public IDrawable getIcon() {
        return new TwoIconRenderer(AllItems.MECHANICAL_SAW, Items.OAK_LOG);
    }

    @Override
    public int getHeight() {
        return 70;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<CuttingRecipe> entry, IFocusGroup focuses) {
        CuttingRecipe recipe = entry.value();
        builder.addInputSlot(44, 5).setBackground(SLOT, -1, -1).add(recipe.ingredient());
        builder.addOutputSlot(118, 48).setBackground(SLOT, -1, -1).add(recipe.result());
    }

    @Override
    public void draw(RecipeHolder<CuttingRecipe> entry, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 70, 6);
        AllGuiTextures.JEI_SHADOW.render(graphics, 55, 55);
        graphics.guiRenderState.submitPicturesInPictureState(new SawRenderState(new Matrix3x2f(graphics.pose()), 64, 31));
    }
}
