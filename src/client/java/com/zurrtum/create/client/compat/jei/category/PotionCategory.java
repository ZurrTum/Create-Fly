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
import com.zurrtum.create.content.kinetics.mixer.PotionRecipe;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;

import java.util.List;

public class PotionCategory extends CreateCategory<RecipeHolder<PotionRecipe>> {
    public static List<RecipeHolder<PotionRecipe>> getRecipes(RecipeMap preparedRecipes) {
        return preparedRecipes.byType(AllRecipeTypes.POTION).stream().toList();
    }

    @Override
    @NotNull
    public IRecipeType<RecipeHolder<PotionRecipe>> getRecipeType() {
        return JeiClientPlugin.AUTOMATIC_BREWING;
    }

    @Override
    @NotNull
    public Component getTitle() {
        return CreateLang.translateDirect("recipe.automatic_brewing");
    }

    @Override
    public IDrawable getIcon() {
        return new TwoIconRenderer(AllItems.MECHANICAL_MIXER, Items.BREWING_STAND);
    }

    @Override
    public int getHeight() {
        return 103;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<PotionRecipe> entry, IFocusGroup focuses) {
        PotionRecipe recipe = entry.value();
        builder.addInputSlot(21, 51).setBackground(SLOT, -1, -1).add(recipe.ingredient());
        addFluidSlot(builder, 40, 51, recipe.fluidIngredient()).setBackground(SLOT, -1, -1);
        addFluidSlot(builder, 142, 51, recipe.result()).setBackground(SLOT, -1, -1);
    }

    @Override
    public void draw(RecipeHolder<PotionRecipe> entry, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        HeatCondition requiredHeat = HeatCondition.HEATED;
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 136, 33);
        Matrix3x2f pose = new Matrix3x2f(graphics.pose());
        AllGuiTextures.JEI_HEAT_BAR.render(graphics, 4, 81);
        AllGuiTextures.JEI_LIGHT.render(graphics, 81, 88);
        graphics.guiRenderState.submitPicturesInPictureState(new BasinBlazeBurnerRenderState(pose, 91, 69, requiredHeat.visualizeAsBlazeBurner()));
        graphics.guiRenderState.submitPicturesInPictureState(new MixingBasinRenderState(pose, 91, -5));
        graphics.drawString(
            Minecraft.getInstance().font,
            CreateLang.translateDirect(requiredHeat.getTranslationKey()),
            9,
            86,
            requiredHeat.getColor(),
            false
        );
    }
}
