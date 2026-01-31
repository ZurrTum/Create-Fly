package com.zurrtum.create.content.processing.recipe;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SizedIngredient {
    public static Codec<List<SizedIngredient>> LIST_CODEC = Ingredient.CODEC.listOf().xmap(SizedIngredient::of, SizedIngredient::unpack);
    public static StreamCodec<RegistryFriendlyByteBuf, SizedIngredient> PACKET_CODEC = StreamCodec.composite(
        Ingredient.CONTENTS_STREAM_CODEC,
        i -> i.ingredient,
        ByteBufCodecs.INT,
        i -> i.count,
        SizedIngredient::new
    );

    private final Ingredient ingredient;
    private int count;

    public SizedIngredient(Ingredient ingredient, int count) {
        this.ingredient = ingredient;
        this.count = count;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public int getCount() {
        return count;
    }

    public boolean test(ItemStack stack) {
        return ingredient.test(stack);
    }

    public static List<SizedIngredient> of(ShapelessRecipe recipe) {
        return of(recipe.ingredients);
    }

    public static List<SizedIngredient> of(List<Ingredient> ingredients) {
        int size = ingredients.size();
        if (size == 0) {
            return List.of();
        }
        List<SizedIngredient> result = new ArrayList<>();
        result.add(new SizedIngredient(ingredients.getFirst(), 1));
        if (size == 1) {
            return result;
        }
        Find:
        for (int i = 1; i < size; i++) {
            Ingredient ingredient = ingredients.get(i);
            for (SizedIngredient sizedIngredient : result) {
                if (sizedIngredient.ingredient.equals(ingredient)) {
                    sizedIngredient.count++;
                    continue Find;
                }
            }
            result.add(new SizedIngredient(ingredient, 1));
        }
        return result;
    }

    public static List<SizedIngredient> of(ShapedRecipe recipe) {
        List<Optional<Ingredient>> ingredients = recipe.getIngredients();
        List<SizedIngredient> result = new ArrayList<>();
        if (ingredients.isEmpty()) {
            return result;
        }
        Find:
        for (Optional<Ingredient> placement : ingredients) {
            if (placement.isEmpty()) {
                continue;
            }
            Ingredient ingredient = placement.get();
            for (SizedIngredient sizedIngredient : result) {
                if (sizedIngredient.ingredient.equals(ingredient)) {
                    sizedIngredient.count++;
                    continue Find;
                }
            }
            result.add(new SizedIngredient(ingredient, 1));
        }
        return result;
    }

    public static List<Ingredient> unpack(List<SizedIngredient> ingredients) {
        List<Ingredient> result = new ArrayList<>();
        for (SizedIngredient sizedIngredient : ingredients) {
            for (int i = 0; i < sizedIngredient.count; i++) {
                result.add(sizedIngredient.ingredient);
            }
        }
        return result;
    }
}
