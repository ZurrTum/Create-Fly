package com.zurrtum.create.client.compat.jei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.jei.CreateCategory;
import com.zurrtum.create.client.compat.jei.JeiClientPlugin;
import com.zurrtum.create.client.compat.jei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.SpoutRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.content.fluids.transfer.FillingRecipe;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.foundation.fluid.FluidStackIngredient;
import com.zurrtum.create.infrastructure.component.BottleType;
import com.zurrtum.create.infrastructure.fluids.BottleFluidInventory;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidItemInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeType;
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.zurrtum.create.Create.MOD_ID;

public class SpoutFillingCategory extends CreateCategory<RecipeEntry<FillingRecipe>> {
    public static final int MAX = 3;
    public static AtomicInteger idGenerator = new AtomicInteger();

    public static List<RecipeEntry<FillingRecipe>> getRecipes(
        PreparedRecipes preparedRecipes,
        Stream<ItemStack> itemStream,
        Stream<IJeiFluidIngredient> fluidStream
    ) {
        List<RecipeEntry<FillingRecipe>> recipes = new ArrayList<>(preparedRecipes.getAll(AllRecipeTypes.FILLING));
        List<FluidStack> fluids = fluidStream.map(ingredient -> {
            FluidVariant variant = ingredient.getFluidVariant();
            return new FluidStack(variant.getFluid(), ingredient.getAmount(), variant.getComponents());
        }).toList();
        MutableInt i = new MutableInt();
        itemStream.forEach(stack -> {
            if (PotionFluidHandler.isPotionItem(stack)) {
                PotionContentsComponent potion = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
                BottleType bottleType = PotionFluidHandler.bottleTypeFromItem(stack.getItem());
                recipes.add(new RecipeEntry<>(
                    RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(MOD_ID, "filling_potions_" + i.getAndIncrement())), new FillingRecipe(
                    stack,
                    Ingredient.ofItem(Items.GLASS_BOTTLE),
                    PotionFluidHandler.getFluidIngredientFromPotion(potion, bottleType, BottleFluidInventory.CAPACITY)
                )
                ));
                return;
            }
            try (FluidItemInventory capability = FluidHelper.getFluidInventory(stack.copy())) {
                if (capability == null) {
                    return;
                }
                int size = capability.size();
                FluidStack existingFluid = size == 1 ? capability.getStack(0) : FluidStack.EMPTY;
                for (FluidStack fluid : fluids) {
                    if (size == 1 && !existingFluid.isEmpty() && !FluidStack.areFluidsAndComponentsEqual(existingFluid, fluid)) {
                        continue;
                    }
                    int insert = capability.insert(fluid, BucketFluidInventory.CAPACITY);
                    if (insert == 0) {
                        continue;
                    }
                    ItemStack result = capability.getContainer();
                    if (!result.isEmpty()) {
                        Item item = stack.getItem();
                        if (!result.isOf(item)) {
                            Identifier itemName = Registries.ITEM.getId(item);
                            Identifier fluidName = Registries.FLUID.getId(fluid.getFluid());
                            Identifier id = Identifier.of(
                                MOD_ID,
                                "fill_" + itemName.getNamespace() + "_" + itemName.getPath() + "_with_" + fluidName.getNamespace() + "_" + fluidName.getPath()
                            );
                            Ingredient ingredient = stack.getComponentChanges()
                                .isEmpty() ? Ingredient.ofItem(stack.getItem()) : DefaultCustomIngredients.components(stack);
                            recipes.add(new RecipeEntry<>(
                                RegistryKey.of(RegistryKeys.RECIPE, id),
                                new FillingRecipe(result, ingredient, new FluidStackIngredient(fluid.getFluid(), fluid.getComponentChanges(), insert))
                            ));
                        }
                    }
                    capability.extract(fluid, insert);
                }
            }
        });
        return recipes;
    }

    @Override
    @NotNull
    public IRecipeType<RecipeEntry<FillingRecipe>> getRecipeType() {
        return JeiClientPlugin.SPOUT_FILLING;
    }

    @Override
    @NotNull
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.spout_filling");
    }

    @Override
    public IDrawable getIcon() {
        return new TwoIconRenderer(AllItems.SPOUT, Items.WATER_BUCKET);
    }

    @Override
    public int getHeight() {
        return 70;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeEntry<FillingRecipe> entry, IFocusGroup focuses) {
        FillingRecipe recipe = entry.value();
        builder.addInputSlot(27, 51).setBackground(SLOT, -1, -1).add(recipe.ingredient());
        addFluidSlot(builder, 27, 32, recipe.fluidIngredient()).setBackground(SLOT, -1, -1).setSlotName("fluid");
        builder.addOutputSlot(132, 51).setBackground(SLOT, -1, -1).add(recipe.result());
    }

    @Override
    public void draw(RecipeEntry<FillingRecipe> entry, IRecipeSlotsView recipeSlotsView, DrawContext graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_SHADOW.render(graphics, 62, 57);
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 126, 29);
        recipeSlotsView.findSlotByName("fluid").flatMap(view -> view.getDisplayedIngredient(FabricTypes.FLUID_STACK)).ifPresent(fluidIngredient -> {
            FluidVariant fluidVariant = fluidIngredient.getFluidVariant();
            int i = idGenerator.getAndIncrement();
            if (i >= MAX) {
                idGenerator.set(0);
            }
            graphics.state.addSpecialElement(new SpoutRenderState(
                i,
                new Matrix3x2f(graphics.getMatrices()),
                fluidVariant.getFluid(),
                fluidVariant.getComponents(),
                75,
                1,
                0
            ));
        });
    }
}
