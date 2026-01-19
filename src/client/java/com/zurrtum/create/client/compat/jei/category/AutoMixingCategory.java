package com.zurrtum.create.client.compat.jei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.jei.CreateCategory;
import com.zurrtum.create.client.compat.jei.JeiClientPlugin;
import com.zurrtum.create.client.compat.jei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.MixingBasinRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.press.MechanicalPressBlockEntity;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

public class AutoMixingCategory extends CreateCategory<RecipeHolder<ShapelessRecipe>> {
    @SuppressWarnings("unchecked")
    public static List<RecipeHolder<ShapelessRecipe>> getRecipes(RecipeMap preparedRecipes) {
        List<RecipeHolder<ShapelessRecipe>> recipes = new ArrayList<>();
        for (RecipeHolder<CraftingRecipe> entry : preparedRecipes.byType(RecipeType.CRAFTING)) {
            CraftingRecipe recipe = entry.value();
            if (!(recipe instanceof ShapelessRecipe shapelessRecipe) || MechanicalPressBlockEntity.canCompress(shapelessRecipe) || AllRecipeTypes.shouldIgnoreInAutomation(
                entry) || shapelessRecipe.ingredients.size() == 1) {
                continue;
            }
            recipes.add((RecipeHolder<ShapelessRecipe>) (Object) entry);
        }
        return recipes;
    }

    @Override
    public IRecipeType<RecipeHolder<ShapelessRecipe>> getRecipeType() {
        return JeiClientPlugin.AUTOMATIC_SHAPELESS;
    }

    @Override
    public Component getTitle() {
        return CreateLang.translateDirect("recipe.automatic_shapeless");
    }

    @Override
    public IDrawable getIcon() {
        return new TwoIconRenderer(AllItems.MECHANICAL_MIXER, Items.CRAFTING_TABLE);
    }

    @Override
    public int getHeight() {
        return 85;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<ShapelessRecipe> entry, IFocusGroup focuses) {
        ShapelessRecipe recipe = entry.value();
        List<List<ItemStack>> ingredients = condenseIngredients(recipe.ingredients);
        for (int i = 0, size = ingredients.size(), xOffset = size < 3 ? (3 - size) * 19 / 2 : 0; i < size; i++) {
            builder.addInputSlot(12 + xOffset + (i % 3) * 19, 51 - (i / 3) * 19).setBackground(SLOT, -1, -1).addItemStacks(ingredients.get(i));
        }
        builder.addOutputSlot(142, 51).setBackground(SLOT, -1, -1).add(recipe.result);
    }

    @Override
    public void draw(RecipeHolder<ShapelessRecipe> entry, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 136, 32);
        AllGuiTextures.JEI_SHADOW.render(graphics, 81, 68);
        graphics.guiRenderState.submitPicturesInPictureState(new MixingBasinRenderState(new Matrix3x2f(graphics.pose()), 91, -5));
    }
}
