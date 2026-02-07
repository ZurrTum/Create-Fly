package com.zurrtum.create.content.kinetics.deployer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.recipe.CreateRecipe;
import com.zurrtum.create.foundation.recipe.CreateRollableRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public interface ItemApplicationRecipe extends CreateRollableRecipe<ItemApplicationInput> {
    List<ProcessingOutput> results();

    boolean keepHeldItem();

    Ingredient target();

    Ingredient ingredient();

    @Override
    default boolean matches(ItemApplicationInput input, World world) {
        return target().test(input.target()) && ingredient().test(input.ingredient());
    }

    @Override
    default List<ItemStack> craft(ItemApplicationInput input, Random random) {
        ItemStack junk = CreateRecipe.getJunk(input.target());
        if (junk != null) {
            return List.of(junk);
        }
        List<ProcessingOutput> results = results();
        List<ItemStack> outputs = new ArrayList<>(results.size());
        ProcessingOutput.rollOutput(random, results, outputs::add);
        return outputs;
    }

    record Serializer<T extends ItemApplicationRecipe>(
        MapCodec<T> codec, PacketCodec<RegistryByteBuf, T> packetCodec
    ) implements RecipeSerializer<T> {
        public Serializer(Factory<T> factory) {
            this(
                RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ProcessingOutput.CODEC.listOf(1, 4).fieldOf("results").forGetter(ItemApplicationRecipe::results),
                    Codec.BOOL.optionalFieldOf("keep_held_item", false).forGetter(ItemApplicationRecipe::keepHeldItem),
                    Ingredient.CODEC.fieldOf("target").forGetter(ItemApplicationRecipe::target),
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(ItemApplicationRecipe::ingredient)
                ).apply(instance, factory::create)), PacketCodec.tuple(
                    ProcessingOutput.STREAM_CODEC.collect(PacketCodecs.toList()),
                    ItemApplicationRecipe::results,
                    PacketCodecs.BOOLEAN,
                    ItemApplicationRecipe::keepHeldItem,
                    Ingredient.PACKET_CODEC,
                    ItemApplicationRecipe::target,
                    Ingredient.PACKET_CODEC,
                    ItemApplicationRecipe::ingredient,
                    factory::create
                )
            );
        }

        public interface Factory<T extends ItemApplicationRecipe> {
            T create(List<ProcessingOutput> results, boolean keepHeldItem, Ingredient block, Ingredient item);
        }
    }
}
