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
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Items;
import net.minecraft.recipe.PreparedRecipes;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;

import java.util.List;

public class PotionCategory extends CreateCategory<RecipeEntry<PotionRecipe>> {
    public static List<RecipeEntry<PotionRecipe>> getRecipes(PreparedRecipes preparedRecipes) {
        return preparedRecipes.getAll(AllRecipeTypes.POTION).stream().toList();
    }

    @Override
    @NotNull
    public IRecipeType<RecipeEntry<PotionRecipe>> getRecipeType() {
        return JeiClientPlugin.AUTOMATIC_BREWING;
    }

    @Override
    @NotNull
    public Text getTitle() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeEntry<PotionRecipe> entry, IFocusGroup focuses) {
        PotionRecipe recipe = entry.value();
        builder.addInputSlot(21, 51).setBackground(SLOT, -1, -1).add(recipe.ingredient());
        addFluidSlot(builder, 40, 51, recipe.fluidIngredient()).setBackground(SLOT, -1, -1);
        addFluidSlot(builder, 142, 51, recipe.result()).setBackground(SLOT, -1, -1);
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 134, 81).add(AllItems.BLAZE_BURNER);
    }

    @Override
    public void draw(RecipeEntry<PotionRecipe> entry, IRecipeSlotsView recipeSlotsView, DrawContext graphics, double mouseX, double mouseY) {
        HeatCondition requiredHeat = HeatCondition.HEATED;
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 136, 32);
        Matrix3x2f pose = new Matrix3x2f(graphics.getMatrices());
        AllGuiTextures.JEI_HEAT_BAR.render(graphics, 4, 80);
        AllGuiTextures.JEI_LIGHT.render(graphics, 81, 88);
        graphics.state.addSpecialElement(new BasinBlazeBurnerRenderState(pose, 91, 69, requiredHeat.visualizeAsBlazeBurner()));
        graphics.state.addSpecialElement(new MixingBasinRenderState(pose, 91, -5));
        graphics.drawText(
            graphics.client.textRenderer,
            CreateLang.translateDirect(requiredHeat.getTranslationKey()),
            9,
            86,
            requiredHeat.getColor(),
            false
        );
    }
}
