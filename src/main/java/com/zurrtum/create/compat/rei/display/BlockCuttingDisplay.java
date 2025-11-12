package com.zurrtum.create.compat.rei.display;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.StonecutterRecipe;

import java.util.*;

import static com.zurrtum.create.Create.MOD_ID;

public record BlockCuttingDisplay(EntryIngredient input, List<EntryIngredient> outputs, Optional<Identifier> location) implements Display {
    public static final DisplaySerializer<BlockCuttingDisplay> SERIALIZER = DisplaySerializer.of(
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntryIngredient.codec().fieldOf("input").forGetter(BlockCuttingDisplay::input),
            EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(BlockCuttingDisplay::outputs),
            Identifier.CODEC.optionalFieldOf("location").forGetter(BlockCuttingDisplay::location)
        ).apply(instance, BlockCuttingDisplay::new)), StreamCodec.composite(
            EntryIngredient.streamCodec(),
            BlockCuttingDisplay::input,
            EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
            BlockCuttingDisplay::outputs,
            ByteBufCodecs.optional(Identifier.STREAM_CODEC),
            BlockCuttingDisplay::location,
            BlockCuttingDisplay::new
        )
    );

    public static void register(ServerDisplayRegistry registry) {
        Object2ObjectMap<Ingredient, Pair<EntryIngredient, List<ItemStack>>> map = new Object2ObjectOpenCustomHashMap<>(new Hash.Strategy<>() {
            public boolean equals(Ingredient ingredient, Ingredient other) {
                return Objects.equals(ingredient, other);
            }

            public int hashCode(Ingredient ingredient) {
                if (ingredient.values instanceof HolderSet.Direct<Item> direct) {
                    return direct.hashCode();
                }
                if (ingredient.values instanceof HolderSet.Named<Item> named) {
                    return named.key().location().hashCode();
                }
                return ingredient.hashCode();
            }
        });
        for (RecipeHolder<StonecutterRecipe> entry : Create.SERVER.getRecipeManager().recipes.byType(RecipeType.STONECUTTING)) {
            if (AllRecipeTypes.shouldIgnoreInAutomation(entry)) {
                continue;
            }
            StonecutterRecipe recipe = entry.value();
            Pair<EntryIngredient, List<ItemStack>> display = map.computeIfAbsent(
                recipe.input(),
                ingredient -> Pair.of(EntryIngredients.ofIngredient((Ingredient) ingredient), new ArrayList<>())
            );
            display.getSecond().add(recipe.result());
        }
        for (Pair<EntryIngredient, List<ItemStack>> pair : map.values()) {
            EntryIngredient input = pair.getFirst();
            List<ItemStack> outputs = pair.getSecond();
            Identifier itemName = BuiltInRegistries.ITEM.getKey(input.getFirst().<ItemStack>castValue().getItem());
            Optional<Identifier> location = Optional.of(Identifier.fromNamespaceAndPath(
                MOD_ID,
                "block_cutting/" + itemName.getNamespace() + "_" + itemName.getPath()
            ));
            int size = outputs.size();
            if (size <= 15) {
                List<EntryIngredient> outputIngredients = Arrays.asList(new EntryIngredient[size]);
                for (int i = 0; i < size; i++) {
                    outputIngredients.set(i, EntryIngredients.of(outputs.get(i)));
                }
                registry.add(new BlockCuttingDisplay(input, outputIngredients, location));
            } else {
                EntryIngredient.Builder[] builders = new EntryIngredient.Builder[15];
                for (int i = 0; i < 15; i++) {
                    builders[i] = EntryIngredient.builder().add(EntryStacks.of(outputs.get(i)));
                }
                for (int i = 15; i < size; i++) {
                    builders[i % 15].add(EntryStacks.of(outputs.get(i)));
                }
                List<EntryIngredient> outputIngredients = Arrays.asList(new EntryIngredient[15]);
                for (int i = 0; i < 15; i++) {
                    outputIngredients.set(i, builders[i].build());
                }
                registry.add(new BlockCuttingDisplay(input, outputIngredients, location));
            }
        }
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return List.of(input);
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return outputs;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ReiCommonPlugin.BLOCK_CUTTING;
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
