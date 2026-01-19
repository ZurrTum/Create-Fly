package com.zurrtum.create.catnip.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

public class RegisteredObjectsHelper {
    public static <V> Identifier getKeyOrThrow(Registry<V> registry, V value) {
        Identifier key = registry.getKey(value);
        if (key == null) {
            throw new IllegalArgumentException("Could not get key for value " + value + "!");
        }
        return key;
    }

    public static Identifier getKeyOrThrow(Block value) {
        return getKeyOrThrow(BuiltInRegistries.BLOCK, value);
    }

    public static Identifier getKeyOrThrow(Item value) {
        return getKeyOrThrow(BuiltInRegistries.ITEM, value);
    }

    public static Identifier getKeyOrThrow(Fluid value) {
        return getKeyOrThrow(BuiltInRegistries.FLUID, value);
    }

    public static Identifier getKeyOrThrow(EntityType<?> value) {
        return getKeyOrThrow(BuiltInRegistries.ENTITY_TYPE, value);
    }

    public static Identifier getKeyOrThrow(BlockEntityType<?> value) {
        return getKeyOrThrow(BuiltInRegistries.BLOCK_ENTITY_TYPE, value);
    }

    public static Identifier getKeyOrThrow(Potion value) {
        return getKeyOrThrow(BuiltInRegistries.POTION, value);
    }

    public static Identifier getKeyOrThrow(ParticleType<?> value) {
        return getKeyOrThrow(BuiltInRegistries.PARTICLE_TYPE, value);
    }

    public static Identifier getKeyOrThrow(RecipeSerializer<?> value) {
        return getKeyOrThrow(BuiltInRegistries.RECIPE_SERIALIZER, value);
    }

    public static Item getItem(Identifier location) {
        return BuiltInRegistries.ITEM.getValue(location);
    }

    public static Block getBlock(Identifier location) {
        return BuiltInRegistries.BLOCK.getValue(location);
    }

    @Nullable
    public static ItemLike getItemOrBlock(Identifier location) {
        Item item = getItem(location);
        if (item != Items.AIR)
            return item;

        Block block = getBlock(location);
        if (block != Blocks.AIR)
            return block;

        return null;
    }

    public static Identifier getKeyOrThrow(ItemLike itemLike) {
        if (itemLike instanceof Item item) {
            return getKeyOrThrow(item);
        } else if (itemLike instanceof Block block) {
            return getKeyOrThrow(block);
        }

        throw new IllegalArgumentException("Could not get key for itemLike " + itemLike + "!");
    }

}