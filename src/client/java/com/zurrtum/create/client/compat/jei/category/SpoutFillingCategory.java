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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.zurrtum.create.Create.MOD_ID;

public class SpoutFillingCategory extends CreateCategory<RecipeHolder<FillingRecipe>> {
    public static final int MAX = 3;
    public static AtomicInteger idGenerator = new AtomicInteger();

    public static List<RecipeHolder<FillingRecipe>> getRecipes(
        RecipeMap preparedRecipes,
        Stream<ItemStack> itemStream,
        Stream<IJeiFluidIngredient> fluidStream
    ) {
        List<RecipeHolder<FillingRecipe>> recipes = new ArrayList<>(preparedRecipes.byType(AllRecipeTypes.FILLING));
        List<FluidStack> fluids = fluidStream.map(ingredient -> {
            FluidVariant variant = ingredient.getFluidVariant();
            return new FluidStack(variant.getFluid(), ingredient.getAmount(), variant.getComponents());
        }).toList();
        MutableInt i = new MutableInt();
        itemStream.forEach(stack -> {
            if (PotionFluidHandler.isPotionItem(stack)) {
                PotionContents potion = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                BottleType bottleType = PotionFluidHandler.bottleTypeFromItem(stack.getItem());
                recipes.add(new RecipeHolder<>(
                    ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(MOD_ID, "filling_potions_" + i.getAndIncrement())),
                    new FillingRecipe(
                        stack,
                        Ingredient.of(Items.GLASS_BOTTLE),
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
                        if (!result.is(item)) {
                            Identifier itemName = BuiltInRegistries.ITEM.getKey(item);
                            Identifier fluidName = BuiltInRegistries.FLUID.getKey(fluid.getFluid());
                            Identifier id = Identifier.fromNamespaceAndPath(
                                MOD_ID,
                                "fill_" + itemName.getNamespace() + "_" + itemName.getPath() + "_with_" + fluidName.getNamespace() + "_" + fluidName.getPath()
                            );
                            Ingredient ingredient = stack.getComponentsPatch()
                                .isEmpty() ? Ingredient.of(stack.getItem()) : DefaultCustomIngredients.components(stack);
                            recipes.add(new RecipeHolder<>(
                                ResourceKey.create(Registries.RECIPE, id),
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
    public IRecipeType<RecipeHolder<FillingRecipe>> getRecipeType() {
        return JeiClientPlugin.SPOUT_FILLING;
    }

    @Override
    public Component getTitle() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<FillingRecipe> entry, IFocusGroup focuses) {
        FillingRecipe recipe = entry.value();
        builder.addInputSlot(27, 51).setBackground(SLOT, -1, -1).add(recipe.ingredient());
        addFluidSlot(builder, 27, 32, recipe.fluidIngredient()).setBackground(SLOT, -1, -1).setSlotName("fluid");
        builder.addOutputSlot(132, 51).setBackground(SLOT, -1, -1).add(recipe.result());
    }

    @Override
    public void draw(RecipeHolder<FillingRecipe> entry, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_SHADOW.render(graphics, 62, 57);
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 126, 29);
        recipeSlotsView.findSlotByName("fluid").flatMap(view -> view.getDisplayedIngredient(FabricTypes.FLUID_STACK)).ifPresent(fluidIngredient -> {
            FluidVariant fluidVariant = fluidIngredient.getFluidVariant();
            int i = idGenerator.getAndIncrement();
            if (i >= MAX) {
                idGenerator.set(0);
            }
            graphics.guiRenderState.submitPicturesInPictureState(new SpoutRenderState(
                i,
                new Matrix3x2f(graphics.pose()),
                fluidVariant.getFluid(),
                fluidVariant.getComponentsPatch(),
                75,
                1,
                0
            ));
        });
    }
}
