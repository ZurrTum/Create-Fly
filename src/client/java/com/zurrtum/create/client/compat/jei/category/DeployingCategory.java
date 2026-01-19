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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

public class DeployingCategory extends CreateCategory<RecipeHolder<? extends ItemApplicationRecipe>> {
    public static List<RecipeHolder<? extends ItemApplicationRecipe>> getRecipes(RecipeMap preparedRecipes) {
        List<RecipeHolder<? extends ItemApplicationRecipe>> recipes = new ArrayList<>();
        recipes.addAll(preparedRecipes.byType(AllRecipeTypes.DEPLOYING));
        recipes.addAll(preparedRecipes.byType(AllRecipeTypes.ITEM_APPLICATION));
        List<Holder<Item>> sandpaperList = new ArrayList<>();
        for (Holder<Item> entry : BuiltInRegistries.ITEM.getTagOrEmpty(AllItemTags.SANDPAPER)) {
            sandpaperList.add(entry);
        }
        Ingredient ingredient = Ingredient.of(HolderSet.direct(sandpaperList));
        for (RecipeHolder<SandPaperPolishingRecipe> entry : preparedRecipes.byType(AllRecipeTypes.SANDPAPER_POLISHING)) {
            SandPaperPolishingRecipe recipe = entry.value();
            recipes.add(new RecipeHolder<>(
                ResourceKey.create(Registries.RECIPE, entry.id().identifier().withSuffix("_using_deployer")),
                new DeployerApplicationRecipe(recipe.result(), true, recipe.ingredient(), ingredient)
            ));
        }
        return recipes;
    }

    @Override
    public IRecipeType<RecipeHolder<? extends ItemApplicationRecipe>> getRecipeType() {
        return JeiClientPlugin.DEPLOYING;
    }

    @Override
    public Component getTitle() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<? extends ItemApplicationRecipe> entry, IFocusGroup focuses) {
        ItemApplicationRecipe recipe = entry.value();
        builder.addInputSlot(51, 5).setBackground(SLOT, -1, -1).add(recipe.ingredient());
        builder.addInputSlot(27, 51).setBackground(SLOT, -1, -1).add(recipe.target());
        builder.addOutputSlot(132, 51).setBackground(SLOT, -1, -1).add(recipe.result());
    }

    @Override
    public void draw(
        RecipeHolder<? extends ItemApplicationRecipe> entry,
        IRecipeSlotsView recipeSlotsView,
        GuiGraphics graphics,
        double mouseX,
        double mouseY
    ) {
        AllGuiTextures.JEI_SHADOW.render(graphics, 62, 57);
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 126, 29);
        graphics.guiRenderState.submitPicturesInPictureState(new DeployerRenderState(new Matrix3x2f(graphics.pose()), 75, -10));
    }
}
