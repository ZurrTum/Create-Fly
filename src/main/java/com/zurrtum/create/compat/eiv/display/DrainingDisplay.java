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

public class DrainingDisplay extends CreateDisplay {
    public ItemStack result;
    public FluidStack fluidResult;
    public List<ItemStack> ingredient;

    public DrainingDisplay() {
    }

    public DrainingDisplay(RecipeHolder<EmptyingRecipe> entry) {
        EmptyingRecipe recipe = entry.value();
        result = recipe.result();
        fluidResult = recipe.fluidResult();
        ingredient = getItemStacks(recipe.ingredient());
    }

    public DrainingDisplay(ItemStack result, FluidStack fluidResult, List<ItemStack> ingredient) {
        this.result = result;
        this.fluidResult = fluidResult;
        this.ingredient = ingredient;
    }

    public static void registerGenericItem(List<IEivServerRecipe> recipes) {
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            FluidState fluidState = fluid.defaultFluidState();
            if (fluid.isSource(fluidState)) {
                Item bucket = fluid.getBucket();
                if (bucket == Items.AIR) {
                    continue;
                }
                registerGenericItem(recipes, bucket.getDefaultInstance());
            }
        }
        registerGenericItem(recipes, Items.MILK_BUCKET.getDefaultInstance());
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
            recipes.add(new DrainingDisplay(capability.getContainer(), stack, List.of(item)));
        }
    }

    public static void registerPotionItem(List<IEivServerRecipe> recipes, ItemStack item) {
        recipes.add(new DrainingDisplay(Items.GLASS_BOTTLE.getDefaultInstance(), PotionFluidHandler.getFluidFromPotionItem(item), List.of(item)));
    }

    @Override
    public void writeToTag(CompoundTag tag) {
        RegistryOps<Tag> ops = getServerOps();
        tag.store("result", ItemStack.CODEC, ops, result);
        tag.store("fluidResult", FluidStack.CODEC, ops, fluidResult);
        tag.store("ingredient", STACKS_CODEC, ops, ingredient);
    }

    @Override
    public void loadFromTag(CompoundTag tag) {
        RegistryOps<Tag> ops = getClientOps();
        result = tag.read("result", ItemStack.CODEC, ops).orElseThrow();
        fluidResult = tag.read("fluidResult", FluidStack.CODEC, ops).orElseThrow();
        ingredient = tag.read("ingredient", STACKS_CODEC, ops).orElseThrow();
    }

    @Override
    public EivRecipeType<DrainingDisplay> getRecipeType() {
        return EivCommonPlugin.DRAINING;
    }
}
