package com.zurrtum.create.content.kinetics.deployer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.recipe.CreateRecipe;
import com.zurrtum.create.foundation.recipe.CreateRollableRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public interface ItemApplicationRecipe extends CreateRollableRecipe<ItemApplicationInput> {
    List<ProcessingOutput> results();

    boolean keepHeldItem();

    Ingredient target();

    Ingredient ingredient();

    @Override
    default boolean matches(ItemApplicationInput input, Level world) {
        return target().test(input.target()) && ingredient().test(input.ingredient());
    }

    @Override
    default List<ItemStack> assemble(ItemApplicationInput input, RandomSource random) {
        ItemStack junk = CreateRecipe.getJunk(input.target());
        if (junk != null) {
            return List.of(junk);
        }
        List<ProcessingOutput> results = results();
        List<ItemStack> outputs = new ArrayList<>(results.size());
        ProcessingOutput.rollOutput(random, results, outputs::add);
        return outputs;
    }

    static <T extends ItemApplicationRecipe> MapCodec<T> createCodec(ItemApplicationRecipeFactory<T> factory) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
            ProcessingOutput.CODEC.listOf(1, 4).fieldOf("results").forGetter(ItemApplicationRecipe::results),
            Codec.BOOL.optionalFieldOf("keep_held_item", false).forGetter(ItemApplicationRecipe::keepHeldItem),
            Ingredient.CODEC.fieldOf("target").forGetter(ItemApplicationRecipe::target),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(ItemApplicationRecipe::ingredient)
        ).apply(instance, factory::create));
    }

    static <T extends ItemApplicationRecipe> StreamCodec<RegistryFriendlyByteBuf, T> createStreamCodec(ItemApplicationRecipeFactory<T> factory) {
        return StreamCodec.composite(
            ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list()),
            ItemApplicationRecipe::results,
            ByteBufCodecs.BOOL,
            ItemApplicationRecipe::keepHeldItem,
            Ingredient.CONTENTS_STREAM_CODEC,
            ItemApplicationRecipe::target,
            Ingredient.CONTENTS_STREAM_CODEC,
            ItemApplicationRecipe::ingredient,
            factory::create
        );
    }

    interface ItemApplicationRecipeFactory<T extends ItemApplicationRecipe> {
        T create(List<ProcessingOutput> results, boolean keepHeldItem, Ingredient block, Ingredient item);
    }
}
