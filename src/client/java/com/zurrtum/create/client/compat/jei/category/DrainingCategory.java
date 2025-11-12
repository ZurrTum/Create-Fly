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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.zurrtum.create.Create.MOD_ID;

public class DrainingCategory extends CreateCategory<RecipeHolder<EmptyingRecipe>> {
    public static List<RecipeHolder<EmptyingRecipe>> getRecipes(RecipeMap preparedRecipes, Stream<ItemStack> itemStream) {
        List<RecipeHolder<EmptyingRecipe>> recipes = new ArrayList<>(preparedRecipes.byType(AllRecipeTypes.EMPTYING));
        MutableInt i = new MutableInt();
        itemStream.forEach(stack -> {
            if (PotionFluidHandler.isPotionItem(stack)) {
                Ingredient ingredient = stack.getComponentsPatch().isEmpty() ? Ingredient.of(stack.getItem()) : DefaultCustomIngredients.components(
                    stack);
                recipes.add(new RecipeHolder<>(
                    ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(MOD_ID, "draining_potions_" + i.getAndIncrement())),
                    new EmptyingRecipe(Items.GLASS_BOTTLE.getDefaultInstance(), PotionFluidHandler.getFluidFromPotionItem(stack), ingredient)
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
                Identifier itemName = BuiltInRegistries.ITEM.getKey(stack.getItem());
                Identifier fluidName = BuiltInRegistries.FLUID.getKey(fluid.getFluid());
                Identifier id = Identifier.fromNamespaceAndPath(
                    MOD_ID,
                    "empty_" + itemName.getNamespace() + "_" + itemName.getPath() + "_with_" + fluidName.getNamespace() + "_" + fluidName.getPath()
                );
                Ingredient ingredient = stack.getComponentsPatch().isEmpty() ? Ingredient.of(stack.getItem()) : DefaultCustomIngredients.components(
                    stack);
                recipes.add(new RecipeHolder<>(
                    ResourceKey.create(Registries.RECIPE, id),
                    new EmptyingRecipe(capability.getContainer(), fluid, ingredient)
                ));
            }
        });
        return recipes;
    }

    @Override
    @NotNull
    public IRecipeType<RecipeHolder<EmptyingRecipe>> getRecipeType() {
        return JeiClientPlugin.DRAINING;
    }

    @Override
    @NotNull
    public Component getTitle() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<EmptyingRecipe> entry, IFocusGroup focuses) {
        EmptyingRecipe recipe = entry.value();
        builder.addInputSlot(27, 8).setBackground(SLOT, -1, -1).add(recipe.ingredient());
        addFluidSlot(builder, 132, 8, recipe.fluidResult()).setBackground(SLOT, -1, -1);
        builder.addOutputSlot(132, 27).setBackground(SLOT, -1, -1).add(recipe.result());
    }

    @Override
    public void draw(RecipeHolder<EmptyingRecipe> entry, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_SHADOW.render(graphics, 62, 37);
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 73, 4);
        FluidStack stack = entry.value().fluidResult();
        graphics.guiRenderState.submitPicturesInPictureState(new DrainRenderState(
            new Matrix3x2f(graphics.pose()),
            stack.getFluid(),
            stack.getComponentChanges(),
            75,
            23
        ));
    }
}
