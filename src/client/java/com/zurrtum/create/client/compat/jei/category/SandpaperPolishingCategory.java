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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import org.joml.Matrix3x2f;

import java.util.List;

public class SandpaperPolishingCategory extends CreateCategory<RecipeHolder<SandPaperPolishingRecipe>> {
    public static List<RecipeHolder<SandPaperPolishingRecipe>> getRecipes(RecipeMap preparedRecipes) {
        return preparedRecipes.byType(AllRecipeTypes.SANDPAPER_POLISHING).stream().toList();
    }

    @Override
    public IRecipeType<RecipeHolder<SandPaperPolishingRecipe>> getRecipeType() {
        return JeiClientPlugin.SANDPAPER_POLISHING;
    }

    @Override
    public Component getTitle() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<SandPaperPolishingRecipe> entry, IFocusGroup focuses) {
        SandPaperPolishingRecipe recipe = entry.value();
        builder.addInputSlot(27, 29).setBackground(SLOT, -1, -1).add(recipe.ingredient());
        builder.addOutputSlot(132, 29).setBackground(SLOT, -1, -1).add(recipe.result());
    }

    @Override
    public void draw(
        RecipeHolder<SandPaperPolishingRecipe> entry,
        IRecipeSlotsView recipeSlotsView,
        GuiGraphics graphics,
        double mouseX,
        double mouseY
    ) {
        AllGuiTextures.JEI_SHADOW.render(graphics, 61, 21);
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, 52, 32);
        recipeSlotsView.getSlotViews(RecipeIngredientRole.INPUT).getFirst().getDisplayedItemStack().ifPresent(stack -> {
            graphics.guiRenderState.submitPicturesInPictureState(new SandPaperRenderState(new Matrix3x2f(graphics.pose()), stack, 74, -2));
        });
    }
}
