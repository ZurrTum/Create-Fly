package com.zurrtum.create.client.compat.jei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.jei.CreateCategory;
import com.zurrtum.create.client.compat.jei.JeiClientPlugin;
import com.zurrtum.create.client.compat.jei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.PressBasinRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.mixer.CompactingRecipe;
import com.zurrtum.create.content.processing.recipe.SizedIngredient;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
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
import org.joml.Matrix3x2f;

import java.util.List;

public class CompactingCategory extends CreateCategory<RecipeHolder<CompactingRecipe>> {
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
    public int getHeight() {
        return 85;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<CompactingRecipe> entry, IFocusGroup iFocusGroup) {
        CompactingRecipe recipe = entry.value();
        List<SizedIngredient> ingredients = recipe.ingredients();
        FluidIngredient fluidIngredient = recipe.fluidIngredient();
        int ingredientSize = ingredients.size();
        int size = ingredientSize;
        if (fluidIngredient != null) {
            size++;
        }
        int xOffset = size < 3 ? 12 + (3 - size) * 19 / 2 : 12;
        int yOffset = 51;
        int i = 0;
        for (; i < ingredientSize; i++) {
            builder.addInputSlot(xOffset + (i % 3) * 19, yOffset - (i / 3) * 19).setBackground(SLOT, -1, -1)
                .addItemStacks(getStacks(ingredients.get(i)));
        }
        if (fluidIngredient != null) {
            addFluidSlot(builder, xOffset + (i % 3) * 19, yOffset - (i / 3) * 19, fluidIngredient).setBackground(SLOT, -1, -1);
        }
        builder.addOutputSlot(142, 51).setBackground(SLOT, -1, -1).add(recipe.result());
    }

    @Override
    public void draw(RecipeHolder<CompactingRecipe> entry, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 136, 32);
        AllGuiTextures.JEI_SHADOW.render(graphics, 81, 68);
        graphics.guiRenderState.submitPicturesInPictureState(new PressBasinRenderState(new Matrix3x2f(graphics.pose()), 91, -5));
    }
}
