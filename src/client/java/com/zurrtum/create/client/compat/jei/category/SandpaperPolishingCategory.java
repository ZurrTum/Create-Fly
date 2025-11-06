package com.zurrtum.create.client.compat.jei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.jei.CreateCategory;
import com.zurrtum.create.client.compat.jei.JeiClientPlugin;
import com.zurrtum.create.client.compat.jei.renderer.IconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.SandPaperRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.recipe.PreparedRecipes;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;

import java.util.List;

public class SandpaperPolishingCategory extends CreateCategory<RecipeEntry<SandPaperPolishingRecipe>> {
    public static List<RecipeEntry<SandPaperPolishingRecipe>> getRecipes(PreparedRecipes preparedRecipes) {
        return preparedRecipes.getAll(AllRecipeTypes.SANDPAPER_POLISHING).stream().toList();
    }

    @Override
    @NotNull
    public IRecipeType<RecipeEntry<SandPaperPolishingRecipe>> getRecipeType() {
        return JeiClientPlugin.SANDPAPER_POLISHING;
    }

    @Override
    @NotNull
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.sandpaper_polishing");
    }

    @Override
    public IDrawable getIcon() {
        return new IconRenderer(AllItems.SAND_PAPER);
    }

    @Override
    public int getHeight() {
        return 55;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeEntry<SandPaperPolishingRecipe> entry, IFocusGroup focuses) {
        SandPaperPolishingRecipe recipe = entry.value();
        builder.addInputSlot(27, 29).setBackground(SLOT, -1, -1).add(recipe.ingredient());
        builder.addOutputSlot(132, 29).setBackground(SLOT, -1, -1).add(recipe.result());
    }

    @Override
    public void draw(
        RecipeEntry<SandPaperPolishingRecipe> entry,
        IRecipeSlotsView recipeSlotsView,
        DrawContext graphics,
        double mouseX,
        double mouseY
    ) {
        AllGuiTextures.JEI_SHADOW.render(graphics, 61, 21);
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, 52, 32);
        recipeSlotsView.getSlotViews(RecipeIngredientRole.INPUT).getFirst().getDisplayedItemStack().ifPresent(stack -> {
            graphics.state.addSpecialElement(new SandPaperRenderState(new Matrix3x2f(graphics.getMatrices()), stack, 74, -2));
        });
    }
}
