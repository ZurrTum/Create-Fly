package com.zurrtum.create.compat.eiv;

import com.mojang.serialization.Codec;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllFluids;
import com.zurrtum.create.content.fluids.potion.PotionFluidHandler;
import com.zurrtum.create.content.processing.recipe.SizedIngredient;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.foundation.fluid.FluidStackIngredient;
import com.zurrtum.create.infrastructure.component.BottleType;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import de.crafty.eiv.common.api.recipe.IEivServerRecipe;
import de.crafty.eiv.common.recipe.ItemViewRecipes;
import de.crafty.eiv.common.recipe.ServerRecipeManager;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.display.SlotDisplayContexts;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.context.ContextParameterMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class CreateDisplay implements IEivServerRecipe {
    public Codec<List<ItemStack>> STACKS_CODEC = ItemStack.CODEC.listOf();
    public Codec<List<List<ItemStack>>> STACKS_LIST_CODEC = STACKS_CODEC.listOf();

    public static RegistryOps<NbtElement> getServerOps() {
        return ServerRecipeManager.INSTANCE.getServer().getRegistryManager().getOps(NbtOps.INSTANCE);
    }

    public static RegistryOps<NbtElement> getClientOps() {
        return AllClientHandle.INSTANCE.getPlayer().getWorld().getRegistryManager().getOps(NbtOps.INSTANCE);
    }

    public static List<ItemStack> getItemStacks(FluidIngredient ingredient) {
        List<Fluid> fluids = ingredient.getMatchingFluids();
        List<ItemStack> list = new ArrayList<>(fluids.size());
        int amount = ingredient.amount();
        ComponentChanges components = null;
        if (ingredient instanceof FluidStackIngredient stackIngredient) {
            components = stackIngredient.components();
        }
        for (Fluid fluid : fluids) {
            Item item = ItemViewRecipes.INSTANCE.itemForFluid(fluid);
            if (item == Items.AIR) {
                continue;
            }
            ItemStack stack = item.getDefaultStack();
            if (components != null) {
                stack.applyUnvalidatedChanges(components);
                updatePotionName(fluid, stack);
            }
            NbtCompound tag = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
            tag.putInt("fluidAmount", amount);
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(tag));
            list.add(stack);
        }
        return list;
    }

    public static ItemStack getItemStack(FluidStack fluidStack) {
        Fluid fluid = fluidStack.getFluid();
        Item item = ItemViewRecipes.INSTANCE.itemForFluid(fluid);
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = item.getDefaultStack();
        stack.applyComponentsFrom(fluidStack.getComponents());
        updatePotionName(fluid, stack);
        NbtCompound tag = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        tag.putInt("fluidAmount", fluidStack.getAmount());
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(tag));
        return stack;
    }

    private static void updatePotionName(Fluid fluid, ItemStack stack) {
        if (fluid != AllFluids.POTION) {
            return;
        }
        PotionContentsComponent contents = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
        BottleType bottleType = stack.getOrDefault(AllDataComponents.POTION_FLUID_BOTTLE_TYPE, BottleType.REGULAR);
        Text name = contents.getName(PotionFluidHandler.itemFromBottleType(bottleType).getTranslationKey() + ".effect.");
        stack.set(DataComponentTypes.ITEM_NAME, name);
    }

    public static void addSizedIngredient(List<SizedIngredient> sizedIngredients, List<List<ItemStack>> ingredients) {
        MinecraftServer server = ServerRecipeManager.INSTANCE.getServer();
        ContextParameterMap context = new ContextParameterMap.Builder().add(SlotDisplayContexts.FUEL_REGISTRY, server.getFuelRegistry())
            .add(SlotDisplayContexts.REGISTRIES, server.getRegistryManager()).build(SlotDisplayContexts.CONTEXT_TYPE);
        for (SizedIngredient ingredient : sizedIngredients) {
            ingredients.add(getItemStacks(ingredient.getIngredient(), ingredient.getCount(), context));
        }
    }

    public static void addSizedIngredient(Object2IntMap<Ingredient> sizedIngredients, List<List<ItemStack>> ingredients) {
        MinecraftServer server = ServerRecipeManager.INSTANCE.getServer();
        ContextParameterMap context = new ContextParameterMap.Builder().add(SlotDisplayContexts.FUEL_REGISTRY, server.getFuelRegistry())
            .add(SlotDisplayContexts.REGISTRIES, server.getRegistryManager()).build(SlotDisplayContexts.CONTEXT_TYPE);
        for (Object2IntMap.Entry<Ingredient> pair : sizedIngredients.object2IntEntrySet()) {
            ingredients.add(getItemStacks(pair.getKey(), pair.getIntValue(), context));
        }
    }

    public static List<ItemStack> getItemStacks(Ingredient ingredient) {
        MinecraftServer server = ServerRecipeManager.INSTANCE.getServer();
        ContextParameterMap context = new ContextParameterMap.Builder().add(SlotDisplayContexts.FUEL_REGISTRY, server.getFuelRegistry())
            .add(SlotDisplayContexts.REGISTRIES, server.getRegistryManager()).build(SlotDisplayContexts.CONTEXT_TYPE);
        return getItemStacks(ingredient, 1, context);
    }

    private static List<ItemStack> getItemStacks(Ingredient ingredient, int count, ContextParameterMap context) {
        List<ItemStack> stacks = ingredient.toDisplay().getStacks(context);
        Optional<TagKey<Item>> value = ingredient.entries.getStorage().left();
        if (value.isPresent()) {
            String tag = value.get().id().toString();
            if (count == 1) {
                for (ItemStack stack : stacks) {
                    setEivRecipeTag(stack, tag);
                }
            } else {
                for (ItemStack stack : stacks) {
                    setEivRecipeTag(stack, tag);
                    stack.setCount(count);
                }
            }
        } else if (count != 1) {
            for (ItemStack stack : stacks) {
                stack.setCount(count);
            }
        }
        return stacks;
    }

    private static void setEivRecipeTag(ItemStack stack, String tag) {
        NbtCompound data = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        data.putString("eiv_recipeTag", tag);
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, data);
    }
}
