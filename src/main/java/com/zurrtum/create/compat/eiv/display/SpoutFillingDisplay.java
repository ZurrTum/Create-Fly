package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.content.fluids.transfer.FillingRecipe;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.foundation.fluid.FluidStackIngredient;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidItemInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import de.crafty.eiv.common.api.recipe.IEivServerRecipe;
import de.crafty.eiv.common.api.recipe.ItemView;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import java.util.HashMap;
import java.util.List;

public class SpoutFillingDisplay extends CreateDisplay {
    public ItemStack result;
    public FluidIngredient fluidIngredient;
    public List<ItemStack> ingredient;

    public SpoutFillingDisplay() {
    }

    public SpoutFillingDisplay(RecipeHolder<FillingRecipe> entry) {
        FillingRecipe recipe = entry.value();
        result = recipe.result();
        fluidIngredient = recipe.fluidIngredient();
        ingredient = getItemStacks(recipe.ingredient());
    }

    public SpoutFillingDisplay(ItemStack result, FluidIngredient fluidIngredient, List<ItemStack> ingredient) {
        this.result = result;
        this.fluidIngredient = fluidIngredient;
        this.ingredient = ingredient;
    }

    public static void registerGenericItem(List<IEivServerRecipe> recipes) {
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            FluidState fluidState = fluid.defaultFluidState();
            if (fluid.isSource(fluidState)) {
                registerGenericItem(recipes, fluid);
            }
        }
        HashMap<Item, List<ItemView.StackSensitive>> map = ItemView.getStackSensitive();
        for (ItemView.StackSensitive stackSensitive : map.get(Items.POTION)) {
            registerPotionItem(recipes, stackSensitive.stack());
        }
        for (ItemView.StackSensitive stackSensitive : map.get(Items.SPLASH_POTION)) {
            registerPotionItem(recipes, stackSensitive.stack());
        }
        for (ItemView.StackSensitive stackSensitive : map.get(Items.LINGERING_POTION)) {
            registerPotionItem(recipes, stackSensitive.stack());
        }
    }

    public static void registerGenericItem(List<IEivServerRecipe> recipes, Fluid fluid) {
        ItemStack item = Items.BUCKET.getDefaultInstance();
        try (FluidItemInventory capability = FluidHelper.getFluidInventory(item)) {
            int insert = capability.insert(new FluidStack(fluid, BucketFluidInventory.CAPACITY));
            if (insert == 0) {
                return;
            }
            ItemStack result = capability.getContainer();
            if (result.isEmpty()) {
                return;
            }
            if (result.is(Items.BUCKET)) {
                return;
            }
            FluidIngredient ingredient = new FluidStackIngredient(fluid, DataComponentPatch.EMPTY, BucketFluidInventory.CAPACITY);
            recipes.add(new SpoutFillingDisplay(result, ingredient, List.of(item)));
        }
    }

    public static void registerPotionItem(List<IEivServerRecipe> recipes, ItemStack stack) {
        FluidStack fluidStack = PotionFluidHandler.getFluidFromPotionItem(stack);
        recipes.add(new SpoutFillingDisplay(
            stack,
            new FluidStackIngredient(fluidStack.getFluid(), fluidStack.getComponentChanges(), fluidStack.getAmount()),
            List.of(Items.GLASS_BOTTLE.getDefaultInstance())
        ));
    }

    @Override
    public void writeToTag(CompoundTag tag) {
        RegistryOps<Tag> ops = getServerOps();
        tag.store("result", ItemStack.CODEC, ops, result);
        tag.store("fluidIngredient", FluidIngredient.CODEC, ops, fluidIngredient);
        tag.store("ingredient", STACKS_CODEC, ops, ingredient);
    }

    @Override
    public void loadFromTag(CompoundTag tag) {
        RegistryOps<Tag> ops = getClientOps();
        result = tag.read("result", ItemStack.CODEC, ops).orElseThrow();
        fluidIngredient = tag.read("fluidIngredient", FluidIngredient.CODEC, ops).orElseThrow();
        ingredient = tag.read("ingredient", STACKS_CODEC, ops).orElseThrow();
    }

    @Override
    public EivRecipeType<SpoutFillingDisplay> getRecipeType() {
        return EivCommonPlugin.SPOUT_FILLING;
    }
}
