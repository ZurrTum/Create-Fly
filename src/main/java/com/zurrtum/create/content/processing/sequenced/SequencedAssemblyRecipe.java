package com.zurrtum.create.content.processing.sequenced;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllAssemblyRecipeNames;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllRecipeSerializers;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.recipe.ComponentsIngredient;
import com.zurrtum.create.foundation.recipe.CreateRecipe;
import com.zurrtum.create.infrastructure.component.SequencedAssemblyJunk;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public record SequencedAssemblyRecipe(
    Ingredient ingredient, ItemStack transitionalItem, ProcessingOutput result, List<ProcessingOutput> junks, int loops,
    List<Recipe<?>> sequence
) implements CreateRecipe<SingleStackRecipeInput> {
    @Override
    public RecipeType<SequencedAssemblyRecipe> getType() {
        return AllRecipeTypes.SEQUENCED_ASSEMBLY;
    }

    @Override
    public boolean matches(SingleStackRecipeInput input, World world) {
        return ingredient.test(input.item());
    }

    @Override
    public ItemStack craft(SingleStackRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        return result.create();
    }

    @Override
    public RecipeSerializer<? extends Recipe<SingleStackRecipeInput>> getSerializer() {
        return AllRecipeSerializers.SEQUENCED_ASSEMBLY;
    }

    public static class Serializer implements RecipeSerializer<SequencedAssemblyRecipe> {
        private static final Codec<List<ProcessingOutput>> JUNKS_CODEC = ProcessingOutput.CODEC.listOf();
        private static final Codec<List<Recipe<?>>> RECIPE_CODEC = Recipe.CODEC.listOf();
        private static final MapCodec<SequencedAssemblyRecipe> RAW_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(SequencedAssemblyRecipe::ingredient),
            ItemStack.CODEC.fieldOf("transitional_item").forGetter(SequencedAssemblyRecipe::transitionalItem),
            ProcessingOutput.CODEC.fieldOf("result").forGetter(SequencedAssemblyRecipe::result),
            JUNKS_CODEC.optionalFieldOf("junks", List.of()).forGetter(SequencedAssemblyRecipe::junks),
            Codec.INT.optionalFieldOf("loops", 1).forGetter(SequencedAssemblyRecipe::loops),
            RECIPE_CODEC.fieldOf("sequence").forGetter(SequencedAssemblyRecipe::sequence)
        ).apply(instance, SequencedAssemblyRecipe::new));
        private static final String INGREDIENT_ID = "$ingredient";
        private static final String RESULT_ID = "$result";
        private static final AtomicInteger idGenerator = new AtomicInteger();
        public static final Map<Identifier, Recipe<?>> GENERATE_RECIPES = new HashMap<>();
        public static final MapCodec<SequencedAssemblyRecipe> CODEC = new MapCodec<>() {
            @Override
            public <T> RecordBuilder<T> encode(SequencedAssemblyRecipe input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                prefix.add("raw", ops.createBoolean(true));
                return RAW_CODEC.encode(input, ops, prefix);
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> DataResult<SequencedAssemblyRecipe> decode(DynamicOps<T> dynamicOps, MapLike<T> input) {
                if (input.get("raw") == null) {
                    if (!(input.get("sequence") instanceof JsonArray sequenceJson)) {
                        throw new UnsupportedOperationException("ops must be a JsonOps");
                    }
                    DynamicOps<JsonElement> ops = (DynamicOps<JsonElement>) dynamicOps;
                    int loops = Optional.ofNullable(input.get("loops")).map(value -> dynamicOps.getNumberValue(value, 1).intValue()).orElse(1);
                    int sequenceSize = sequenceJson.size();
                    int size = sequenceSize * loops;
                    if (size <= 1) {
                        throw new UnsupportedOperationException("sequence must have at least two steps");
                    }

                    ItemStack transitionalItem = ItemStack.CODEC.parse(dynamicOps, input.get("transitional_item")).getOrThrow();
                    ProcessingOutput result = ProcessingOutput.CODEC.parse(dynamicOps, input.get("result")).getOrThrow();
                    List<ProcessingOutput> junks = JUNKS_CODEC.parse(dynamicOps, input.get("junks")).result().orElse(List.of());

                    List<Text> RecipeName = new ArrayList<>(sequenceSize);
                    for (int i = 0; i < sequenceSize; i++) {
                        RecipeName.add(AllAssemblyRecipeNames.get(ops, sequenceJson.get(i)));
                    }

                    ItemStack transitional = transitionalItem.copy();
                    Ingredient transitionalIngredient = Ingredient.ofItem(transitionalItem.getItem());
                    Supplier<JsonElement> transitionalJsonIngredient = () -> ComponentsIngredient.CODEC.encodeStart(
                        ops,
                        new ComponentsIngredient(transitionalIngredient, transitional.getComponentChanges())
                    ).getOrThrow();

                    List<BiFunction<JsonElement, JsonElement, JsonObject>> sequenceJsonFactory = new ArrayList<>(size);
                    for (int i = 0; i < sequenceSize; i++) {
                        JsonObject object = (JsonObject) sequenceJson.get(i);
                        Consumer<JsonElement> replaceIngredient = getReplace(object, INGREDIENT_ID);
                        Consumer<JsonElement> replaceResult = getReplace(object, RESULT_ID);
                        sequenceJsonFactory.add((ingredientJson, resultJson) -> {
                            if (replaceIngredient != null) {
                                replaceIngredient.accept(ingredientJson);
                            }
                            if (replaceResult != null) {
                                replaceResult.accept(resultJson);
                            }
                            return object;
                        });
                    }
                    MutableInt step = new MutableInt(1);
                    Supplier<JsonElement> transitionalJsonResult = () -> {
                        int index = step.getAndIncrement();
                        List<Text> lore = new ArrayList<>(6);
                        lore.add(ScreenTexts.EMPTY);
                        lore.add(Text.translatable("create.recipe.sequenced_assembly").formatted(Formatting.GRAY)
                            .styled(style -> style.withItalic(false)));
                        lore.add(Text.translatable("create.recipe.assembly.progress", index, size).formatted(Formatting.DARK_GRAY)
                            .styled(style -> style.withItalic(false)));
                        lore.add(Text.translatable("create.recipe.assembly.next", RecipeName.get(index % sequenceSize)).formatted(Formatting.AQUA)
                            .styled(style -> style.withItalic(false)));
                        for (int i = index + 1, end = Math.min(i + 2, size); i < end; i++) {
                            lore.add(Text.literal("-> ").append(RecipeName.get(i % sequenceSize)).formatted(Formatting.DARK_AQUA)
                                .styled(style -> style.withItalic(false)));
                        }
                        transitional.set(AllDataComponents.SEQUENCED_ASSEMBLY_PROGRESS, (float) index / size);
                        transitional.set(DataComponentTypes.LORE, new LoreComponent(lore, lore));
                        return ItemStack.CODEC.encodeStart(ops, transitional).getOrThrow();
                    };
                    Supplier<JsonElement> transitionalJsonChanceResult = () -> {
                        transitional.set(AllDataComponents.SEQUENCED_ASSEMBLY_JUNK, new SequencedAssemblyJunk(result.chance(), junks));
                        JsonElement element = transitionalJsonResult.get();
                        transitional.remove(AllDataComponents.SEQUENCED_ASSEMBLY_JUNK);
                        return element;
                    };
                    JsonElement jsonResult = ItemStack.CODEC.encodeStart(ops, result.create()).getOrThrow();

                    Identifier id = Identifier.of(AllRecipeTypes.SEQUENCED_ASSEMBLY.toString())
                        .withSuffixedPath("_" + idGenerator.incrementAndGet() + "_" + Registries.ITEM.getId(result.item().value())
                            .getPath() + "_");
                    List<Recipe<?>> sequence = new ArrayList<>(size);
                    TriConsumer<Integer, JsonElement, JsonElement> recipeAdd = (i, ingredientJson, resultJson) -> {
                        JsonObject object = sequenceJsonFactory.get(i % sequenceSize).apply(ingredientJson, resultJson);
                        Recipe<?> recipe = Recipe.CODEC.parse(ops, object).getOrThrow();
                        sequence.add(recipe);
                        GENERATE_RECIPES.put(id.withSuffixedPath(String.valueOf(sequence.size())), recipe);
                    };

                    JsonElement ingredientJson = (JsonElement) input.get("ingredient");
                    recipeAdd.accept(0, ingredientJson, size == 2 ? transitionalJsonChanceResult.get() : transitionalJsonResult.get());
                    for (int i = 1, end = size - 2; i < end; i++) {
                        recipeAdd.accept(i, transitionalJsonIngredient.get(), transitionalJsonResult.get());
                    }
                    if (size > 2) {
                        recipeAdd.accept(size - 2, transitionalJsonIngredient.get(), transitionalJsonChanceResult.get());
                    }
                    recipeAdd.accept(size - 1, transitionalJsonIngredient.get(), jsonResult);
                    return DataResult.success(new SequencedAssemblyRecipe(
                        Ingredient.CODEC.parse(ops, ingredientJson).getOrThrow(),
                        transitionalItem,
                        result,
                        junks,
                        loops,
                        sequence
                    ));
                } else {
                    return RAW_CODEC.decode(dynamicOps, input);
                }
            }

            private static Consumer<JsonElement> getReplace(JsonObject target, String id) {
                for (Map.Entry<String, JsonElement> entry : target.entrySet()) {
                    JsonElement value = entry.getValue();
                    Consumer<JsonElement> consumer = getReplace(value, id);
                    if (consumer != null) {
                        return consumer;
                    } else if (match(value, id)) {
                        String key = entry.getKey();
                        return data -> target.add(key, data);
                    }
                }
                return null;
            }

            private static Consumer<JsonElement> getReplace(JsonArray target, String id) {
                for (int i = 0, size = target.size(); i < size; i++) {
                    JsonElement value = target.get(i);
                    Consumer<JsonElement> consumer = getReplace(value, id);
                    if (consumer != null) {
                        return consumer;
                    } else if (match(value, id)) {
                        int index = i;
                        return data -> target.set(index, data);
                    }
                }
                return null;
            }

            private static Consumer<JsonElement> getReplace(JsonElement target, String id) {
                if (target instanceof JsonObject object) {
                    return getReplace(object, id);
                } else if (target instanceof JsonArray array) {
                    return getReplace(array, id);
                }
                return null;
            }

            private static boolean match(JsonElement target, String id) {
                return target instanceof JsonPrimitive primitive && primitive.isString() && primitive.getAsString().equals(id);
            }

            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.of(
                    ops.createString("ingredient"),
                    ops.createString("transitional_item"),
                    ops.createString("result"),
                    ops.createString("junks"),
                    ops.createString("loops"),
                    ops.createString("sequence")
                );
            }
        };
        public static final PacketCodec<RegistryByteBuf, SequencedAssemblyRecipe> PACKET_CODEC = PacketCodec.tuple(
            Ingredient.PACKET_CODEC,
            SequencedAssemblyRecipe::ingredient,
            ItemStack.PACKET_CODEC,
            SequencedAssemblyRecipe::transitionalItem,
            ProcessingOutput.STREAM_CODEC,
            SequencedAssemblyRecipe::result,
            ProcessingOutput.STREAM_CODEC.collect(PacketCodecs.toList()),
            SequencedAssemblyRecipe::junks,
            PacketCodecs.INTEGER,
            SequencedAssemblyRecipe::loops,
            Recipe.PACKET_CODEC.collect(PacketCodecs.toList()),
            SequencedAssemblyRecipe::sequence,
            SequencedAssemblyRecipe::new
        );

        @Override
        public MapCodec<SequencedAssemblyRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, SequencedAssemblyRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
