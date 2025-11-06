package com.zurrtum.create.client.compat.jei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.jei.CreateCategory;
import com.zurrtum.create.client.compat.jei.JeiClientPlugin;
import com.zurrtum.create.client.compat.jei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.BasinBlazeBurnerRenderState;
import com.zurrtum.create.client.foundation.gui.render.MixingBasinRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import com.zurrtum.create.content.processing.recipe.SizedIngredient;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.PreparedRecipes;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;

import java.util.List;

public class MixingCategory extends CreateCategory<RecipeEntry<MixingRecipe>> {
    public static List<RecipeEntry<MixingRecipe>> getRecipes(PreparedRecipes preparedRecipes) {
        return preparedRecipes.getAll(AllRecipeTypes.MIXING).stream().toList();
    }

    @Override
    @NotNull
    public IRecipeType<RecipeEntry<MixingRecipe>> getRecipeType() {
        return JeiClientPlugin.MIXING;
    }

    @Override
    @NotNull
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.mixing");
    }

    @Override
    public IDrawable getIcon() {
        return new TwoIconRenderer(AllItems.MECHANICAL_MIXER, AllItems.BASIN);
    }

    @Override
    public int getHeight() {
        return 103;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeEntry<MixingRecipe> entry, IFocusGroup focuses) {
        MixingRecipe recipe = entry.value();
        List<SizedIngredient> ingredients = recipe.ingredients();
        List<FluidIngredient> fluidIngredients = recipe.fluidIngredients();
        int i = 0;
        int ingredientSize = ingredients.size();
        int fluidIngredientSize = fluidIngredients.size();
        int size = ingredientSize + fluidIngredientSize;
        int xOffset = size < 3 ? 12 + (3 - size) * 19 / 2 : 12;
        int yOffset = 51;
        for (; i < ingredientSize; i++) {
            builder.addInputSlot(xOffset + (i % 3) * 19, yOffset - (i / 3) * 19).setBackground(SLOT, -1, -1)
                .addItemStacks(getStacks(ingredients.get(i)));
        }
        for (; i < size; i++) {
            addFluidSlot(builder, xOffset + (i % 3) * 19, yOffset - (i / 3) * 19, fluidIngredients.get(i - ingredientSize)).setBackground(
                SLOT,
                -1,
                -1
            );
        }
        ItemStack result = recipe.result();
        if (result.isEmpty()) {
            addFluidSlot(builder, 142, 51, recipe.fluidResult()).setBackground(SLOT, -1, -1);
        } else {
            builder.addOutputSlot(142, 51).setBackground(SLOT, -1, -1).add(result);
        }
        HeatCondition requiredHeat = recipe.heat();
        if (!requiredHeat.testBlazeBurner(BlazeBurnerBlock.HeatLevel.NONE)) {
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 134, 81).add(AllItems.BLAZE_BURNER);
        }
        if (!requiredHeat.testBlazeBurner(BlazeBurnerBlock.HeatLevel.KINDLED)) {
            builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 153, 81).add(AllItems.BLAZE_CAKE);
        }
    }

    @Override
    public void draw(RecipeEntry<MixingRecipe> entry, IRecipeSlotsView recipeSlotsView, DrawContext graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 136, 32);
        Matrix3x2f pose = new Matrix3x2f(graphics.getMatrices());
        HeatCondition requiredHeat = entry.value().heat();
        if (requiredHeat == HeatCondition.NONE) {
            AllGuiTextures.JEI_NO_HEAT_BAR.render(graphics, 4, 80);
            AllGuiTextures.JEI_SHADOW.render(graphics, 81, 68);
        } else {
            AllGuiTextures.JEI_HEAT_BAR.render(graphics, 4, 80);
            AllGuiTextures.JEI_LIGHT.render(graphics, 81, 88);
            graphics.state.addSpecialElement(new BasinBlazeBurnerRenderState(pose, 91, 69, requiredHeat.visualizeAsBlazeBurner()));
        }
        graphics.state.addSpecialElement(new MixingBasinRenderState(pose, 91, -5));
        graphics.drawText(
            MinecraftClient.getInstance().textRenderer,
            CreateLang.translateDirect(requiredHeat.getTranslationKey()),
            9,
            86,
            requiredHeat.getColor(),
            false
        );
    }
}
