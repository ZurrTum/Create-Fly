package com.zurrtum.create.client.compat.jei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.jei.CreateCategory;
import com.zurrtum.create.client.compat.jei.JeiClientPlugin;
import com.zurrtum.create.client.compat.jei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.FanRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.fan.processing.SplashingRecipe;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
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
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;

import java.util.List;

public class FanWashingCategory extends CreateCategory<RecipeHolder<SplashingRecipe>> {
    public static List<RecipeHolder<SplashingRecipe>> getRecipes(RecipeMap preparedRecipes) {
        return preparedRecipes.byType(AllRecipeTypes.SPLASHING).stream().toList();
    }

    @Override
    @NotNull
    public IRecipeType<RecipeHolder<SplashingRecipe>> getRecipeType() {
        return JeiClientPlugin.FAN_WASHING;
    }

    @Override
    @NotNull
    public Component getTitle() {
        return CreateLang.translateDirect("recipe.fan_washing");
    }

    @Override
    public IDrawable getIcon() {
        return new TwoIconRenderer(AllItems.PROPELLER, Items.WATER_BUCKET);
    }

    @Override
    public int getHeight() {
        return 72;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<SplashingRecipe> entry, IFocusGroup focuses) {
        SplashingRecipe recipe = entry.value();
        List<ProcessingOutput> results = recipe.results();
        int outputSize = results.size();
        if (outputSize == 1) {
            builder.addInputSlot(21, 48).setBackground(SLOT, -1, -1).add(recipe.ingredient());
            addChanceSlot(builder, 141, 48, results.getFirst());
        } else {
            int xOffsetAmount = 1 - Math.min(3, outputSize);
            builder.addInputSlot(21 + xOffsetAmount * 5, 48).setBackground(SLOT, -1, -1).add(recipe.ingredient());
            for (int i = 0, left = 141 + xOffsetAmount * 9, top = outputSize <= 9 ? 48 : 57; i < outputSize; i++) {
                addChanceSlot(builder, left + (i % 3) * 19, top + (i / 3) * -19, results.get(i));
            }
        }
    }

    @Override
    public void draw(RecipeHolder<SplashingRecipe> entry, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        int xOffsetAmount = 1 - Math.min(3, entry.value().results().size());
        AllGuiTextures.JEI_SHADOW.render(graphics, 46, 27);
        AllGuiTextures.JEI_SHADOW.render(graphics, 65, 39);
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, 54 + 7 * xOffsetAmount, 51);
        graphics.guiRenderState.submitPicturesInPictureState(new FanRenderState(
            new Matrix3x2f(graphics.pose()),
            56,
            4,
            Fluids.WATER.defaultFluidState().createLegacyBlock()
        ));
    }
}
