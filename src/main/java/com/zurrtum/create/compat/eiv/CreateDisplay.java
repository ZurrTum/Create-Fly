package com.zurrtum.create.compat.eiv;

import com.mojang.serialization.Codec;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.content.processing.recipe.SizedIngredient;
import de.crafty.eiv.common.api.recipe.IEivServerRecipe;
import de.crafty.eiv.common.recipe.ServerRecipeManager;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

import java.util.List;
import java.util.Optional;

public abstract class CreateDisplay implements IEivServerRecipe {
    public Codec<List<ItemStack>> STACKS_CODEC = ItemStack.CODEC.listOf();
    public Codec<List<List<ItemStack>>> STACKS_LIST_CODEC = STACKS_CODEC.listOf();

    public static RegistryOps<Tag> getServerOps() {
        return ServerRecipeManager.INSTANCE.getServer().registryAccess().createSerializationContext(NbtOps.INSTANCE);
    }

    public static RegistryOps<Tag> getClientOps() {
        return AllClientHandle.INSTANCE.getPlayer().level().registryAccess().createSerializationContext(NbtOps.INSTANCE);
    }

    public static void addSizedIngredient(List<SizedIngredient> sizedIngredients, List<List<ItemStack>> ingredients) {
        MinecraftServer server = ServerRecipeManager.INSTANCE.getServer();
        ContextMap context = new ContextMap.Builder().withParameter(SlotDisplayContext.FUEL_VALUES, server.fuelValues())
            .withParameter(SlotDisplayContext.REGISTRIES, server.registryAccess()).create(SlotDisplayContext.CONTEXT);
        for (SizedIngredient ingredient : sizedIngredients) {
            ingredients.add(getItemStacks(ingredient.getIngredient(), ingredient.getCount(), context));
        }
    }

    public static void addSizedIngredient(Object2IntMap<Ingredient> sizedIngredients, List<List<ItemStack>> ingredients) {
        MinecraftServer server = ServerRecipeManager.INSTANCE.getServer();
        ContextMap context = new ContextMap.Builder().withParameter(SlotDisplayContext.FUEL_VALUES, server.fuelValues())
            .withParameter(SlotDisplayContext.REGISTRIES, server.registryAccess()).create(SlotDisplayContext.CONTEXT);
        for (Object2IntMap.Entry<Ingredient> pair : sizedIngredients.object2IntEntrySet()) {
            ingredients.add(getItemStacks(pair.getKey(), pair.getIntValue(), context));
        }
    }

    public static List<ItemStack> getItemStacks(Ingredient ingredient) {
        MinecraftServer server = ServerRecipeManager.INSTANCE.getServer();
        ContextMap context = new ContextMap.Builder().withParameter(SlotDisplayContext.FUEL_VALUES, server.fuelValues())
            .withParameter(SlotDisplayContext.REGISTRIES, server.registryAccess()).create(SlotDisplayContext.CONTEXT);
        return getItemStacks(ingredient, 1, context);
    }

    private static List<ItemStack> getItemStacks(Ingredient ingredient, int count, ContextMap context) {
        List<ItemStack> stacks = ingredient.display().resolveForStacks(context);
        Optional<TagKey<Item>> value = ingredient.values.unwrap().left();
        if (value.isPresent()) {
            String tag = value.get().location().toString();
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
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        data.putString("eiv_recipeTag", tag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }
}
