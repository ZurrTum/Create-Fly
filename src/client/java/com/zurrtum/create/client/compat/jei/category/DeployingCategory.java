package com.zurrtum.create.client.compat.jei.category;

import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.jei.CreateCategory;
import com.zurrtum.create.client.compat.jei.JeiClientPlugin;
import com.zurrtum.create.client.compat.jei.renderer.IconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.DeployerRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.zurrtum.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.zurrtum.create.content.kinetics.deployer.ItemApplicationRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.PreparedRecipes;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

public class DeployingCategory extends CreateCategory<RecipeEntry<? extends ItemApplicationRecipe>> {
    public static List<RecipeEntry<? extends ItemApplicationRecipe>> getRecipes(PreparedRecipes preparedRecipes) {
        List<RecipeEntry<? extends ItemApplicationRecipe>> recipes = new ArrayList<>();
        recipes.addAll(preparedRecipes.getAll(AllRecipeTypes.DEPLOYING));
        recipes.addAll(preparedRecipes.getAll(AllRecipeTypes.ITEM_APPLICATION));
        List<RegistryEntry<Item>> sandpaperList = new ArrayList<>();
        for (RegistryEntry<Item> entry : Registries.ITEM.iterateEntries(AllItemTags.SANDPAPER)) {
            sandpaperList.add(entry);
        }
        Ingredient ingredient = Ingredient.ofTag(RegistryEntryList.of(sandpaperList));
        for (RecipeEntry<SandPaperPolishingRecipe> entry : preparedRecipes.getAll(AllRecipeTypes.SANDPAPER_POLISHING)) {
            SandPaperPolishingRecipe recipe = entry.value();
            recipes.add(new RecipeEntry<>(
                RegistryKey.of(RegistryKeys.RECIPE, entry.id().getValue().withSuffixedPath("_using_deployer")),
                new DeployerApplicationRecipe(recipe.result(), true, recipe.ingredient(), ingredient)
            ));
        }
        return recipes;
    }

    @Override
    @NotNull
    public IRecipeType<RecipeEntry<? extends ItemApplicationRecipe>> getRecipeType() {
        return JeiClientPlugin.DEPLOYING;
    }

    @Override
    @NotNull
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.deploying");
    }

    @Override
    public IDrawable getIcon() {
        return new IconRenderer(AllItems.DEPLOYER);
    }

    @Override
    public int getHeight() {
        return 70;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeEntry<? extends ItemApplicationRecipe> entry, IFocusGroup focuses) {
        ItemApplicationRecipe recipe = entry.value();
        builder.addInputSlot(51, 5).setBackground(SLOT, -1, -1).add(recipe.ingredient());
        builder.addInputSlot(27, 51).setBackground(SLOT, -1, -1).add(recipe.target());
        builder.addOutputSlot(132, 51).setBackground(SLOT, -1, -1).add(recipe.result());
    }

    @Override
    public void draw(
        RecipeEntry<? extends ItemApplicationRecipe> entry,
        IRecipeSlotsView recipeSlotsView,
        DrawContext graphics,
        double mouseX,
        double mouseY
    ) {
        AllGuiTextures.JEI_SHADOW.render(graphics, 62, 57);
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 126, 29);
        graphics.state.addSpecialElement(new DeployerRenderState(new Matrix3x2f(graphics.getMatrices()), 75, -10));
    }
}
