package com.zurrtum.create.client.compat.jei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.jei.CreateCategory;
import com.zurrtum.create.client.compat.jei.JeiClientPlugin;
import com.zurrtum.create.client.compat.jei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.DrainRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.content.fluids.transfer.EmptyingRecipe;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidItemInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeType;
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.PreparedRecipes;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.zurrtum.create.Create.MOD_ID;

public class DrainingCategory extends CreateCategory<RecipeEntry<EmptyingRecipe>> {
    public static List<RecipeEntry<EmptyingRecipe>> getRecipes(PreparedRecipes preparedRecipes, Stream<ItemStack> itemStream) {
        List<RecipeEntry<EmptyingRecipe>> recipes = new ArrayList<>(preparedRecipes.getAll(AllRecipeTypes.EMPTYING));
        MutableInt i = new MutableInt();
        itemStream.forEach(stack -> {
            if (PotionFluidHandler.isPotionItem(stack)) {
                Ingredient ingredient = stack.getComponentChanges()
                    .isEmpty() ? Ingredient.ofItem(stack.getItem()) : DefaultCustomIngredients.components(stack);
                recipes.add(new RecipeEntry<>(
                    RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(MOD_ID, "draining_potions_" + i.getAndIncrement())),
                    new EmptyingRecipe(Items.GLASS_BOTTLE.getDefaultStack(), PotionFluidHandler.getFluidFromPotionItem(stack), ingredient)
                ));
                return;
            }
            try (FluidItemInventory capability = FluidHelper.getFluidInventory(stack.copy())) {
                if (capability == null) {
                    return;
                }
                FluidStack fluid = capability.extractAny(BucketFluidInventory.CAPACITY);
                if (fluid.isEmpty()) {
                    return;
                }
                Identifier itemName = Registries.ITEM.getId(stack.getItem());
                Identifier fluidName = Registries.FLUID.getId(fluid.getFluid());
                Identifier id = Identifier.of(
                    MOD_ID,
                    "empty_" + itemName.getNamespace() + "_" + itemName.getPath() + "_with_" + fluidName.getNamespace() + "_" + fluidName.getPath()
                );
                Ingredient ingredient = stack.getComponentChanges()
                    .isEmpty() ? Ingredient.ofItem(stack.getItem()) : DefaultCustomIngredients.components(stack);
                recipes.add(new RecipeEntry<>(
                    RegistryKey.of(RegistryKeys.RECIPE, id),
                    new EmptyingRecipe(capability.getContainer(), fluid, ingredient)
                ));
            }
        });
        return recipes;
    }

    @Override
    @NotNull
    public IRecipeType<RecipeEntry<EmptyingRecipe>> getRecipeType() {
        return JeiClientPlugin.DRAINING;
    }

    @Override
    @NotNull
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.draining");
    }

    @Override
    public IDrawable getIcon() {
        return new TwoIconRenderer(AllItems.ITEM_DRAIN, Items.WATER_BUCKET);
    }

    @Override
    public int getHeight() {
        return 50;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeEntry<EmptyingRecipe> entry, IFocusGroup focuses) {
        EmptyingRecipe recipe = entry.value();
        builder.addInputSlot(27, 8).setBackground(SLOT, -1, -1).add(recipe.ingredient());
        addFluidSlot(builder, 132, 8, recipe.fluidResult()).setBackground(SLOT, -1, -1);
        builder.addOutputSlot(132, 27).setBackground(SLOT, -1, -1).add(recipe.result());
    }

    @Override
    public void draw(RecipeEntry<EmptyingRecipe> entry, IRecipeSlotsView recipeSlotsView, DrawContext graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_SHADOW.render(graphics, 62, 37);
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 73, 4);
        FluidStack stack = entry.value().fluidResult();
        graphics.state.addSpecialElement(new DrainRenderState(
            new Matrix3x2f(graphics.getMatrices()),
            stack.getFluid(),
            stack.getComponentChanges(),
            75,
            23
        ));
    }
}
