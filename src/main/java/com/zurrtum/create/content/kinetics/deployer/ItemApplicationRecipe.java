package com.zurrtum.create.content.kinetics.deployer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.foundation.recipe.CreateRecipe;
import com.zurrtum.create.infrastructure.component.SequencedAssemblyJunk;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public interface ItemApplicationRecipe extends CreateRecipe<ItemApplicationInput> {
    ItemStack result();

    boolean keepHeldItem();

    Ingredient target();

    Ingredient ingredient();

    @Override
    default boolean matches(ItemApplicationInput input, World world) {
        return target().test(input.target()) && ingredient().test(input.ingredient());
    }

    @Override
    default ItemStack craft(ItemApplicationInput input, RegistryWrapper.WrapperLookup registries) {
        SequencedAssemblyJunk junk = input.target().get(AllDataComponents.SEQUENCED_ASSEMBLY_JUNK);
        if (junk != null && junk.hasJunk()) {
            return junk.getJunk();
        }
        return result().copy();
    }

    record Serializer<T extends ItemApplicationRecipe>(
        MapCodec<T> codec, PacketCodec<RegistryByteBuf, T> packetCodec
    ) implements RecipeSerializer<T> {
        public Serializer(Factory<T> factory) {
            this(
                RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ItemStack.CODEC.fieldOf("result").forGetter(ItemApplicationRecipe::result),
                    Codec.BOOL.optionalFieldOf("keep_held_item", false).forGetter(ItemApplicationRecipe::keepHeldItem),
                    Ingredient.CODEC.fieldOf("target").forGetter(ItemApplicationRecipe::target),
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(ItemApplicationRecipe::ingredient)
                ).apply(instance, factory::create)), PacketCodec.tuple(
                    ItemStack.PACKET_CODEC,
                    ItemApplicationRecipe::result,
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
            T create(ItemStack result, boolean keepHeldItem, Ingredient block, Ingredient item);
        }
    }
}
