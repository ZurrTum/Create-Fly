package com.zurrtum.create.client.compat.jei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.jei.CreateCategory;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.BasinBlazeBurnerRenderState;
import com.zurrtum.create.client.foundation.gui.render.MixingBasinRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.processing.basin.BasinRecipe;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.content.processing.recipe.SizedIngredient;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.joml.Matrix3x2f;

import java.util.List;

public abstract class BasinCategory<T extends BasinRecipe> extends CreateCategory<RecipeHolder<T>> {
    @Override
    public int getHeight() {
        return 103;
    }

    public static void addIngredientSlots(IRecipeLayoutBuilder builder, BasinRecipe recipe) {
        List<SizedIngredient> ingredients = recipe.ingredients();
        List<FluidIngredient> fluidIngredients = recipe.fluidIngredients();
        int size = ingredients.size() + fluidIngredients.size();
        int xOffset = size < 3 ? 12 + (3 - size) * 19 / 2 : 12;
        int yOffset = size <= 9 ? 51 : 60;
        int i = 0;
        for (SizedIngredient ingredient : ingredients) {
            builder.addInputSlot(xOffset + (i % 3) * 19, yOffset - (i / 3) * 19).setBackground(SLOT, -1, -1).addItemStacks(getStacks(ingredient));
            i++;
        }
        for (FluidIngredient fluidIngredient : fluidIngredients) {
            addFluidSlot(builder, xOffset + (i % 3) * 19, yOffset - (i / 3) * 19, fluidIngredient).setBackground(SLOT, -1, -1);
            i++;
        }
    }

    public static void addResultSlots(IRecipeLayoutBuilder builder, List<ProcessingOutput> results, int i, int end, int y, boolean isOddSize) {
        int xPosition, yPosition;
        for (ProcessingOutput result : results) {
            if (isOddSize && i == end) {
                xPosition = 142;
            } else {
                xPosition = i % 2 == 0 ? 132 : 151;
            }
            yPosition = -19 * (i / 2) + y;
            addChanceSlot(builder, xPosition, yPosition, result);
            i++;
        }
    }

    public static void addFluidResultSlots(IRecipeLayoutBuilder builder, List<FluidStack> fluidResults, int i, int end, int y, boolean isOddSize) {
        int xPosition, yPosition;
        for (FluidStack fluidResult : fluidResults) {
            if (isOddSize && i == end) {
                xPosition = 142;
            } else {
                xPosition = i % 2 == 0 ? 132 : 151;
            }
            yPosition = -19 * (i / 2) + y;
            addFluidSlot(builder, xPosition, yPosition, fluidResult).setBackground(SLOT, -1, -1);
            i++;
        }
    }

    public static void addHeatSlots(IRecipeLayoutBuilder builder, BasinRecipe recipe) {
        HeatCondition requiredHeat = recipe.heat();
        if (!requiredHeat.testBlazeBurner(HeatLevel.NONE)) {
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 134, 81).add(AllItems.BLAZE_BURNER);
        }
        if (!requiredHeat.testBlazeBurner(HeatLevel.KINDLED)) {
            builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 153, 81).add(AllItems.BLAZE_CAKE);
        }
    }

    public static void drawBackground(BasinRecipe recipe, GuiGraphics graphics, int size) {
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 136, (size <= 4 ? 32 : 41) - (size - 1) / 2 * 19);
        Matrix3x2f pose = new Matrix3x2f(graphics.pose());
        HeatCondition requiredHeat = recipe.heat();
        if (requiredHeat == HeatCondition.NONE) {
            AllGuiTextures.JEI_NO_HEAT_BAR.render(graphics, 4, 80);
            AllGuiTextures.JEI_SHADOW.render(graphics, 81, 68);
        } else {
            AllGuiTextures.JEI_HEAT_BAR.render(graphics, 4, 80);
            AllGuiTextures.JEI_LIGHT.render(graphics, 81, 88);
            graphics.guiRenderState.submitPicturesInPictureState(new BasinBlazeBurnerRenderState(
                pose,
                91,
                69,
                requiredHeat.visualizeAsBlazeBurner()
            ));
        }
        graphics.guiRenderState.submitPicturesInPictureState(new MixingBasinRenderState(pose, 91, -5));
        graphics.drawString(
            graphics.minecraft.font,
            CreateLang.translateDirect(requiredHeat.getTranslationKey()),
            9,
            86,
            requiredHeat.getColor(),
            false
        );
    }
}
