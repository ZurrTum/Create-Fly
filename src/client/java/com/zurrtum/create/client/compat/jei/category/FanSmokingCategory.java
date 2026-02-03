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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;

import java.util.List;

public class FanSmokingCategory extends CreateCategory<RecipeHolder<SmokingRecipe>> {
    public static List<RecipeHolder<SmokingRecipe>> getRecipes(RecipeMap preparedRecipes) {
        return preparedRecipes.byType(RecipeType.SMOKING).stream().filter(AllRecipeTypes.CAN_BE_AUTOMATED).toList();
    }

    @Override
    @NotNull
    public IRecipeType<RecipeHolder<SmokingRecipe>> getRecipeType() {
        return JeiClientPlugin.FAN_SMOKING;
    }

    @Override
    @NotNull
    public Component getTitle() {
        return CreateLang.translateDirect("recipe.fan_smoking");
    }

    @Override
    public IDrawable getIcon() {
        return new TwoIconRenderer(AllItems.PROPELLER, Items.CAMPFIRE);
    }

    @Override
    public int getHeight() {
        return 72;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<SmokingRecipe> entry, IFocusGroup focuses) {
        SmokingRecipe recipe = entry.value();
        builder.addInputSlot(21, 48).setBackground(SLOT, -1, -1).add(recipe.input());
        builder.addOutputSlot(141, 48).setBackground(SLOT, -1, -1).add(recipe.result());
    }

    @Override
    public void draw(RecipeHolder<SmokingRecipe> entry, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_SHADOW.render(graphics, 46, 27);
        AllGuiTextures.JEI_LIGHT.render(graphics, 65, 39);
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, 54, 51);
        graphics.guiRenderState.submitPicturesInPictureState(new FanRenderState(
            new Matrix3x2f(graphics.pose()),
            56,
            4,
            Blocks.FIRE.defaultBlockState()
        ));
    }
}
