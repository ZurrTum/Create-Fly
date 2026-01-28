package com.zurrtum.create.content.kinetics.deployer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.foundation.recipe.CreateRecipe;
import com.zurrtum.create.infrastructure.component.SequencedAssemblyJunk;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

public interface ItemApplicationRecipe extends CreateRecipe<ItemApplicationInput> {
    ItemStackTemplate result();

    boolean keepHeldItem();

    Ingredient target();

    Ingredient ingredient();

    @Override
    default boolean matches(ItemApplicationInput input, Level world) {
        return target().test(input.target()) && ingredient().test(input.ingredient());
    }

    @Override
    default ItemStack assemble(ItemApplicationInput input) {
        SequencedAssemblyJunk junk = input.target().get(AllDataComponents.SEQUENCED_ASSEMBLY_JUNK);
        if (junk != null && junk.hasJunk()) {
            return junk.getJunk();
        }
        return result().create();
    }

    static <T extends ItemApplicationRecipe> MapCodec<T> createCodec(ItemApplicationRecipeFactory<T> factory) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(ItemApplicationRecipe::result),
            Codec.BOOL.optionalFieldOf("keep_held_item", false).forGetter(ItemApplicationRecipe::keepHeldItem),
            Ingredient.CODEC.fieldOf("target").forGetter(ItemApplicationRecipe::target),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(ItemApplicationRecipe::ingredient)
        ).apply(instance, factory::create));
    }

    static <T extends ItemApplicationRecipe> StreamCodec<RegistryFriendlyByteBuf, T> createStreamCodec(ItemApplicationRecipeFactory<T> factory) {
        return StreamCodec.composite(
            ItemStackTemplate.STREAM_CODEC,
            ItemApplicationRecipe::result,
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
        T create(ItemStackTemplate result, boolean keepHeldItem, Ingredient block, Ingredient item);
    }
}
