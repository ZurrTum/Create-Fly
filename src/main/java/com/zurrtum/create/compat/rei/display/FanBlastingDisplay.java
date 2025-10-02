package com.zurrtum.create.compat.rei.display;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.Create;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.*;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

public record FanBlastingDisplay(EntryIngredient input, EntryIngredient output, Optional<Identifier> location) implements Display {
    public static final DisplaySerializer<FanBlastingDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().fieldOf("input").forGetter(FanBlastingDisplay::input),
            EntryIngredient.codec().fieldOf("output").forGetter(FanBlastingDisplay::output),
            Identifier.CODEC.optionalFieldOf("location").forGetter(FanBlastingDisplay::location)
        ).apply(instance, FanBlastingDisplay::new)), PacketCodec.tuple(
            EntryIngredient.streamCodec(),
            FanBlastingDisplay::input,
            EntryIngredient.streamCodec(),
            FanBlastingDisplay::output,
            PacketCodecs.optional(Identifier.PACKET_CODEC),
            FanBlastingDisplay::location,
            FanBlastingDisplay::new
        )
    );

    public static Display of(RecipeEntry<?> entry) {
        if (!AllRecipeTypes.CAN_BE_AUTOMATED.test(entry)) {
            return null;
        }
        SingleStackRecipe recipe = (SingleStackRecipe) entry.value();
        Ingredient ingredient = recipe.ingredient();
        Optional<ItemStack> firstInput = ingredient.entries.stream().findFirst().map(item -> item.value().getDefaultStack());
        if (firstInput.isEmpty()) {
            return null;
        }
        SingleStackRecipeInput input = new SingleStackRecipeInput(firstInput.get());
        MinecraftServer server = Create.SERVER;
        ServerWorld world = server.getWorld(World.OVERWORLD);
        ServerRecipeManager recipeManager = server.getRecipeManager();
        if (recipe instanceof SmeltingRecipe) {
            Optional<RecipeEntry<BlastingRecipe>> blastingRecipe = recipeManager.getFirstMatch(RecipeType.BLASTING, input, world)
                .filter(AllRecipeTypes.CAN_BE_AUTOMATED);
            if (blastingRecipe.isPresent()) {
                return null;
            }
        }
        Optional<RecipeEntry<SmokingRecipe>> smokingRecipe = recipeManager.getFirstMatch(RecipeType.SMOKING, input, world)
            .filter(AllRecipeTypes.CAN_BE_AUTOMATED);
        if (smokingRecipe.isPresent()) {
            return null;
        }
        return new FanBlastingDisplay(
            EntryIngredients.ofIngredient(ingredient),
            EntryIngredients.of(recipe.result()),
            Optional.of(entry.id().getValue())
        );
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return List.of(input);
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return List.of(output);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ReiCommonPlugin.FAN_BLASTING;
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return location;
    }

    @Override
    public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
