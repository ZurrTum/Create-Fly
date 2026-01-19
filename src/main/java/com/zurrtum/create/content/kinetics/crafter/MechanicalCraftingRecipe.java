package com.zurrtum.create.content.kinetics.crafter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.foundation.recipe.CreateRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public record MechanicalCraftingRecipe(
    ShapedRecipePattern raw, ItemStackTemplate result, boolean symmetrical
) implements CreateRecipe<CraftingInput> {
    @Override
    public boolean matches(CraftingInput input, Level worldIn) {
        if (symmetrical)
            return raw.matches(input);

        // From ShapedRecipe except the symmetry
        if (input.ingredientCount() != raw.ingredientCount) {
            return false;
        }
        int width = raw.width();
        if (input.width() != width) {
            return false;
        }
        int height = raw.height();
        if (input.height() != height) {
            return false;
        }
        List<Optional<Ingredient>> ingredients = raw.ingredients();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Optional<Ingredient> optional = ingredients.get(j + i * width);
                ItemStack itemStack = input.getItem(j, i);
                if (!Ingredient.testOptionalIngredient(optional, itemStack)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingRecipeInput) {
        return result.create();
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
        public static final MapCodec<ShapedRecipePattern.Data> DATA_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.strictUnboundedMap(Codec.STRING.xmap(key -> key.charAt(0), String::valueOf), Ingredient.CODEC).fieldOf("key")
                .forGetter(ShapedRecipePattern.Data::key),
            Codec.STRING.listOf().fieldOf("pattern").forGetter(ShapedRecipePattern.Data::pattern)
        ).apply(instance, ShapedRecipePattern.Data::new));
        public static final MapCodec<ShapedRecipePattern> RAW_CODEC = DATA_CODEC.flatXmap(
            ShapedRecipePattern::unpack,
            recipe -> recipe.data.map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Cannot encode unpacked recipe"))
        );
        public static final MapCodec<MechanicalCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RAW_CODEC.forGetter(MechanicalCraftingRecipe::raw),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(MechanicalCraftingRecipe::result),
            Codec.BOOL.optionalFieldOf("accept_mirrored", false).forGetter(MechanicalCraftingRecipe::symmetrical)
        ).apply(instance, MechanicalCraftingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, MechanicalCraftingRecipe> PACKET_CODEC = StreamCodec.composite(
            ShapedRecipePattern.STREAM_CODEC,
            MechanicalCraftingRecipe::raw,
            ItemStackTemplate.STREAM_CODEC,
            MechanicalCraftingRecipe::result,
            ByteBufCodecs.BOOL,
            MechanicalCraftingRecipe::symmetrical,
            MechanicalCraftingRecipe::new
        );

        @Override
        public MapCodec<MechanicalCraftingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MechanicalCraftingRecipe> streamCodec() {
            return PACKET_CODEC;
        }
    }
}
