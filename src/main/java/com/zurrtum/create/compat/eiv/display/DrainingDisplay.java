package com.zurrtum.create.compat.eiv.display;

import com.zurrtum.create.compat.eiv.CreateDisplay;
import com.zurrtum.create.compat.eiv.EivCommonPlugin;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.content.fluids.transfer.EmptyingRecipe;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidItemInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import de.crafty.eiv.common.api.recipe.EivRecipeType;
import de.crafty.eiv.common.api.recipe.IEivServerRecipe;
import de.crafty.eiv.common.api.recipe.ItemView;
import de.crafty.eiv.common.api.recipe.ItemView.StackSensitive;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryOps;

import java.util.HashMap;
import java.util.List;

public class DrainingDisplay extends CreateDisplay {
    public ItemStack result;
    public ItemStack fluidResult;
    public List<ItemStack> ingredient;

    public DrainingDisplay() {
    }

    public DrainingDisplay(RecipeEntry<EmptyingRecipe> entry) {
        EmptyingRecipe recipe = entry.value();
        result = recipe.result();
        fluidResult = getItemStack(recipe.fluidResult());
        ingredient = getItemStacks(recipe.ingredient());
    }

    public DrainingDisplay(ItemStack result, ItemStack fluidResult, List<ItemStack> ingredient) {
        this.result = result;
        this.fluidResult = fluidResult;
        this.ingredient = ingredient;
    }

    public static void registerGenericItem(List<IEivServerRecipe> recipes) {
        for (Fluid fluid : Registries.FLUID) {
            FluidState fluidState = fluid.getDefaultState();
            if (fluid.isStill(fluidState)) {
                Item bucket = fluid.getBucketItem();
                if (bucket == Items.AIR) {
                    continue;
                }
                registerGenericItem(recipes, bucket.getDefaultStack());
            }
        }
        registerGenericItem(recipes, Items.MILK_BUCKET.getDefaultStack());
        HashMap<Item, List<StackSensitive>> map = ItemView.getStackSensitive();
        for (StackSensitive stackSensitive : map.get(Items.POTION)) {
            registerPotionItem(recipes, stackSensitive.stack());
        }
        for (StackSensitive stackSensitive : map.get(Items.SPLASH_POTION)) {
            registerPotionItem(recipes, stackSensitive.stack());
        }
        for (StackSensitive stackSensitive : map.get(Items.LINGERING_POTION)) {
            registerPotionItem(recipes, stackSensitive.stack());
        }
    }

    public static void registerGenericItem(List<IEivServerRecipe> recipes, ItemStack item) {
        try (FluidItemInventory capability = FluidHelper.getFluidInventory(item)) {
            if (capability == null) {
                return;
            }
            FluidStack stack = capability.extractAny(BucketFluidInventory.CAPACITY);
            if (stack.isEmpty()) {
                return;
            }
            recipes.add(new DrainingDisplay(capability.getContainer(), getItemStack(stack), List.of(item)));
        }
    }

    public static void registerPotionItem(List<IEivServerRecipe> recipes, ItemStack item) {
        recipes.add(new DrainingDisplay(
            Items.GLASS_BOTTLE.getDefaultStack(),
            getItemStack(PotionFluidHandler.getFluidFromPotionItem(item)),
            List.of(item)
        ));
    }

    @Override
    public void writeToTag(NbtCompound tag) {
        RegistryOps<NbtElement> ops = getServerOps();
        tag.put("result", ItemStack.CODEC, ops, result);
        tag.put("fluidResult", ItemStack.CODEC, ops, fluidResult);
        tag.put("ingredient", STACKS_CODEC, ops, ingredient);
    }

    @Override
    public void loadFromTag(NbtCompound tag) {
        RegistryOps<NbtElement> ops = getClientOps();
        result = tag.get("result", ItemStack.CODEC, ops).orElseThrow();
        fluidResult = tag.get("fluidResult", ItemStack.CODEC, ops).orElseThrow();
        ingredient = tag.get("ingredient", STACKS_CODEC, ops).orElseThrow();
    }

    @Override
    public EivRecipeType<DrainingDisplay> getRecipeType() {
        return EivCommonPlugin.DRAINING;
    }
}
