package com.zurrtum.create.compat.eiv;

import com.mojang.serialization.Codec;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.content.processing.recipe.SizedIngredient;
import de.crafty.eiv.common.api.recipe.IEivServerRecipe;
import de.crafty.eiv.common.recipe.ServerRecipeManager;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.display.SlotDisplayContexts;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.context.ContextParameterMap;

import java.util.List;
import java.util.Optional;

public abstract class CreateDisplay implements IEivServerRecipe {
    public Codec<List<ItemStack>> STACKS_CODEC = ItemStack.CODEC.listOf();
    public Codec<List<List<ItemStack>>> STACKS_LIST_CODEC = STACKS_CODEC.listOf();

    public static RegistryOps<NbtElement> getServerOps() {
        return ServerRecipeManager.INSTANCE.getServer().getRegistryManager().getOps(NbtOps.INSTANCE);
    }

    public static RegistryOps<NbtElement> getClientOps() {
        return AllClientHandle.INSTANCE.getPlayer().getEntityWorld().getRegistryManager().getOps(NbtOps.INSTANCE);
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
