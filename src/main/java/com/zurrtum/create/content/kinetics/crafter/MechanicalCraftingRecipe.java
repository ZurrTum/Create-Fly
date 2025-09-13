package com.zurrtum.create.content.kinetics.crafter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.foundation.recipe.CreateRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

public record MechanicalCraftingRecipe(RawShapedRecipe raw, ItemStack result, boolean symmetrical) implements CreateRecipe<CraftingRecipeInput> {
    @Override
    public boolean matches(CraftingRecipeInput input, World worldIn) {
        if (symmetrical)
            return raw.matches(input);

        // From ShapedRecipe except the symmetry
        if (input.getStackCount() != raw.ingredientCount) {
            return false;
        }
        int width = raw.getWidth();
        if (input.getWidth() != width) {
            return false;
        }
        int height = raw.getHeight();
        if (input.getHeight() != height) {
            return false;
        }
        List<Optional<Ingredient>> ingredients = raw.getIngredients();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Optional<Ingredient> optional = ingredients.get(j + i * width);
                ItemStack itemStack = input.getStackInSlot(j, i);
                if (!Ingredient.matches(optional, itemStack)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput craftingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) {
        return result.copy();
    }

    @Override
    public RecipeType<MechanicalCraftingRecipe> getType() {
        return AllRecipeTypes.MECHANICAL_CRAFTING;
    }

    @Override
    public RecipeSerializer<MechanicalCraftingRecipe> getSerializer() {
        return AllRecipeSerializers.MECHANICAL_CRAFTING;
    }

    public static class Serializer implements RecipeSerializer<MechanicalCraftingRecipe> {
        public static final MapCodec<RawShapedRecipe.Data> DATA_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codecs.strictUnboundedMap(Codec.STRING.xmap(key -> key.charAt(0), String::valueOf), Ingredient.CODEC).fieldOf("key")
                .forGetter(RawShapedRecipe.Data::key), Codec.STRING.listOf().fieldOf("pattern").forGetter(RawShapedRecipe.Data::pattern)
        ).apply(instance, RawShapedRecipe.Data::new));
        public static final MapCodec<RawShapedRecipe> RAW_CODEC = DATA_CODEC.flatXmap(
            RawShapedRecipe::fromData,
            recipe -> recipe.data.map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Cannot encode unpacked recipe"))
        );
        public static final MapCodec<MechanicalCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RAW_CODEC.forGetter(MechanicalCraftingRecipe::raw),
            ItemStack.CODEC.fieldOf("result").forGetter(MechanicalCraftingRecipe::result),
            Codec.BOOL.optionalFieldOf("accept_mirrored", false).forGetter(MechanicalCraftingRecipe::symmetrical)
        ).apply(instance, MechanicalCraftingRecipe::new));

        public static final PacketCodec<RegistryByteBuf, MechanicalCraftingRecipe> PACKET_CODEC = PacketCodec.tuple(
            RawShapedRecipe.PACKET_CODEC,
            MechanicalCraftingRecipe::raw,
            ItemStack.PACKET_CODEC,
            MechanicalCraftingRecipe::result,
            PacketCodecs.BOOLEAN,
            MechanicalCraftingRecipe::symmetrical,
            MechanicalCraftingRecipe::new
        );

        @Override
        public MapCodec<MechanicalCraftingRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, MechanicalCraftingRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
