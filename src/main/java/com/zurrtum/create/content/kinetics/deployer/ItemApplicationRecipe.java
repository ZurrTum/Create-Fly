package com.zurrtum.create.content.kinetics.deployer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.foundation.recipe.CreateRecipe;
import com.zurrtum.create.infrastructure.component.SequencedAssemblyJunk;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public interface ItemApplicationRecipe extends CreateRecipe<ItemApplicationInput> {
    ItemStack result();

    boolean keepHeldItem();

    Ingredient target();

    Ingredient ingredient();

    @Override
    default boolean matches(ItemApplicationInput input, Level world) {
        return target().test(input.target()) && ingredient().test(input.ingredient());
    }

    @Override
    default ItemStack assemble(ItemApplicationInput input, HolderLookup.Provider registries) {
        SequencedAssemblyJunk junk = input.target().get(AllDataComponents.SEQUENCED_ASSEMBLY_JUNK);
        if (junk != null && junk.hasJunk()) {
            return junk.getJunk();
        }
        return result().copy();
    }

    record Serializer<T extends ItemApplicationRecipe>(
        MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec
    ) implements RecipeSerializer<T> {
        public Serializer(Factory<T> factory) {
            this(
                RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ItemStack.CODEC.fieldOf("result").forGetter(ItemApplicationRecipe::result),
                    Codec.BOOL.optionalFieldOf("keep_held_item", false).forGetter(ItemApplicationRecipe::keepHeldItem),
                    Ingredient.CODEC.fieldOf("target").forGetter(ItemApplicationRecipe::target),
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(ItemApplicationRecipe::ingredient)
                ).apply(instance, factory::create)), StreamCodec.composite(
                    ItemStack.STREAM_CODEC,
                    ItemApplicationRecipe::result,
                    ByteBufCodecs.BOOL,
                    ItemApplicationRecipe::keepHeldItem,
                    Ingredient.CONTENTS_STREAM_CODEC,
                    ItemApplicationRecipe::target,
                    Ingredient.CONTENTS_STREAM_CODEC,
                    ItemApplicationRecipe::ingredient,
                    factory::create
                )
            );
        }

        public interface Factory<T extends ItemApplicationRecipe> {
            T create(ItemStack result, boolean keepHeldItem, Ingredient block, Ingredient item);
        }
    }
}
