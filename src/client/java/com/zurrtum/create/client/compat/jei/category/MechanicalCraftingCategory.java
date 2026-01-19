package com.zurrtum.create.client.compat.jei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.jei.CreateCategory;
import com.zurrtum.create.client.compat.jei.JeiClientPlugin;
import com.zurrtum.create.client.compat.jei.renderer.IconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.CrafterRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.joml.Matrix3x2f;

import java.util.List;
import java.util.Optional;

public class MechanicalCraftingCategory extends CreateCategory<RecipeHolder<MechanicalCraftingRecipe>> {
    public static List<RecipeHolder<MechanicalCraftingRecipe>> getRecipes(RecipeMap preparedRecipes) {
        return preparedRecipes.byType(AllRecipeTypes.MECHANICAL_CRAFTING).stream().toList();
    }

    @Override
    public IRecipeType<RecipeHolder<MechanicalCraftingRecipe>> getRecipeType() {
        return JeiClientPlugin.MECHANICAL_CRAFTING;
    }

    @Override
    public Component getTitle() {
        return CreateLang.translateDirect("recipe.mechanical_crafting");
    }

    @Override
    public IDrawable getIcon() {
        return new IconRenderer(AllItems.MECHANICAL_CRAFTER);
    }

    @Override
    public int getHeight() {
        return 107;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<MechanicalCraftingRecipe> entry, IFocusGroup focuses) {
        MechanicalCraftingRecipe recipe = entry.value();
        ShapedRecipePattern raw = recipe.raw();
        int width = raw.width();
        int height = raw.height();
        List<Optional<Ingredient>> layout = raw.ingredients();
        int left = 7;
        if (width < 5) {
            left += (19 * (5 - width)) / 2;
        }
        int top = 7;
        if (height < 5) {
            top += (19 * (5 - height)) / 2;
        }
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                Optional<Ingredient> ingredient = layout.get(h * width + w);
                if (ingredient.isEmpty()) {
                    continue;
                }
                builder.addInputSlot(left + 16 * w + (w * 3), top + 16 * h + (h * 3)).setBackground(SLOT, -1, -1).add(ingredient.get());
            }
        }
        builder.addOutputSlot(133, 80).setBackground(SLOT, -1, -1).add(recipe.result());
    }

    @Override
    public void draw(
        RecipeHolder<MechanicalCraftingRecipe> entry,
        IRecipeSlotsView recipeSlotsView,
        GuiGraphics graphics,
        double mouseX,
        double mouseY
    ) {
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 128, 59);
        AllGuiTextures.JEI_SHADOW.render(graphics, 113, 38);
        graphics.guiRenderState.submitPicturesInPictureState(new CrafterRenderState(new Matrix3x2f(graphics.pose()), 124, 18));
        int size = recipeSlotsView.getSlotViews(RecipeIngredientRole.INPUT).size();
        graphics.drawString(Minecraft.getInstance().font, String.valueOf(size), 142, 39, 0xFFFFFFFF, true);
    }
}
