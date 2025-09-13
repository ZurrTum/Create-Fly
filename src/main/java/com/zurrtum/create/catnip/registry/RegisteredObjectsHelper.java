package com.zurrtum.create.catnip.registry;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleType;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class RegisteredObjectsHelper {
    public static <V> Identifier getKeyOrThrow(Registry<V> registry, V value) {
        Identifier key = registry.getId(value);
        if (key == null) {
            throw new IllegalArgumentException("Could not get key for value " + value + "!");
        }
        return key;
    }

    public static Identifier getKeyOrThrow(Block value) {
        return getKeyOrThrow(Registries.BLOCK, value);
    }

    public static Identifier getKeyOrThrow(Item value) {
        return getKeyOrThrow(Registries.ITEM, value);
    }

    public static Identifier getKeyOrThrow(Fluid value) {
        return getKeyOrThrow(Registries.FLUID, value);
    }

    public static Identifier getKeyOrThrow(EntityType<?> value) {
        return getKeyOrThrow(Registries.ENTITY_TYPE, value);
    }

    public static Identifier getKeyOrThrow(BlockEntityType<?> value) {
        return getKeyOrThrow(Registries.BLOCK_ENTITY_TYPE, value);
    }

    public static Identifier getKeyOrThrow(Potion value) {
        return getKeyOrThrow(Registries.POTION, value);
    }

    public static Identifier getKeyOrThrow(ParticleType<?> value) {
        return getKeyOrThrow(Registries.PARTICLE_TYPE, value);
    }

    public static Identifier getKeyOrThrow(RecipeSerializer<?> value) {
        return getKeyOrThrow(Registries.RECIPE_SERIALIZER, value);
    }

    public static Item getItem(Identifier location) {
        return Registries.ITEM.get(location);
    }

    public static Block getBlock(Identifier location) {
        return Registries.BLOCK.get(location);
    }

    @Nullable
    public static ItemConvertible getItemOrBlock(Identifier location) {
        Item item = getItem(location);
        if (item != Items.AIR)
            return item;

        Block block = getBlock(location);
        if (block != Blocks.AIR)
            return block;

        return null;
    }

    public static Identifier getKeyOrThrow(ItemConvertible itemLike) {
        if (itemLike instanceof Item item) {
            return getKeyOrThrow(item);
        } else if (itemLike instanceof Block block) {
            return getKeyOrThrow(block);
        }

        throw new IllegalArgumentException("Could not get key for itemLike " + itemLike + "!");
    }

}