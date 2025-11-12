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
import com.zurrtum.create.content.processing.recipe.ChanceOutput;
import com.zurrtum.create.foundation.recipe.ComponentsIngredient;
import com.zurrtum.create.foundation.recipe.CreateRecipe;
import com.zurrtum.create.infrastructure.component.SequencedAssemblyJunk;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public record SequencedAssemblyRecipe(
    Ingredient ingredient, ItemStack transitionalItem, ChanceOutput result, List<ChanceOutput> junks, int loops, List<Recipe<?>> sequence
) implements CreateRecipe<SingleRecipeInput> {
    @Override
    public RecipeType<SequencedAssemblyRecipe> getType() {
        return AllRecipeTypes.SEQUENCED_ASSEMBLY;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level world) {
        return ingredient.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return result.stack().copy();
    }

    @Override
    public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return AllRecipeSerializers.SEQUENCED_ASSEMBLY;
    }

    public static class Serializer implements RecipeSerializer<SequencedAssemblyRecipe> {
        private static final Codec<List<ChanceOutput>> JUNKS_CODEC = ChanceOutput.CODEC.listOf();
        private static final Codec<List<Recipe<?>>> RECIPE_CODEC = Recipe.CODEC.listOf();
        private static final MapCodec<SequencedAssemblyRecipe> RAW_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(SequencedAssemblyRecipe::ingredient),
            ItemStack.CODEC.fieldOf("transitional_item").forGetter(SequencedAssemblyRecipe::transitionalItem),
            ChanceOutput.CODEC.fieldOf("result").forGetter(SequencedAssemblyRecipe::result),
            JUNKS_CODEC.optionalFieldOf("junks", List.of()).forGetter(SequencedAssemblyRecipe::junks),
            Codec.INT.optionalFieldOf("loops", 1).forGetter(SequencedAssemblyRecipe::loops),
            RECIPE_CODEC.fieldOf("sequence").forGetter(SequencedAssemblyRecipe::sequence)
        ).apply(instance, SequencedAssemblyRecipe::new));
        private static final String INGREDIENT_ID = "$ingredient";
        private static final String RESULT_ID = "$result";
        private static final AtomicInteger idGenerator = new AtomicInteger();
        public static final Map<ResourceLocation, Recipe<?>> GENERATE_RECIPES = new HashMap<>();
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
                    ChanceOutput result = ChanceOutput.CODEC.parse(dynamicOps, input.get("result")).getOrThrow();
                    List<ChanceOutput> junks = JUNKS_CODEC.parse(dynamicOps, input.get("junks")).result().orElse(List.of());

                    List<Component> RecipeName = new ArrayList<>(sequenceSize);
                    for (int i = 0; i < sequenceSize; i++) {
                        JsonObject object = (JsonObject) sequenceJson.get(i);
                        RecipeName.add(AllAssemblyRecipeNames.get(ops, object));
                    }

                    ItemStack transitional = transitionalItem.copy();
                    Ingredient transitionalIngredient = Ingredient.of(transitionalItem.getItem());
                    Supplier<JsonElement> transitionalJsonIngredient = () -> ComponentsIngredient.CODEC.encodeStart(
                        ops,
                        new ComponentsIngredient(transitionalIngredient, transitional.getComponentsPatch())
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
                        List<Component> lore = new ArrayList<>(6);
                        lore.add(CommonComponents.EMPTY);
                        lore.add(Component.translatable("create.recipe.sequenced_assembly").withStyle(ChatFormatting.GRAY)
                            .withStyle(style -> style.withItalic(false)));
                        lore.add(Component.translatable("create.recipe.assembly.progress", index, size).withStyle(ChatFormatting.DARK_GRAY)
                            .withStyle(style -> style.withItalic(false)));
                        lore.add(Component.translatable("create.recipe.assembly.next", RecipeName.get(index % sequenceSize))
                            .withStyle(ChatFormatting.AQUA).withStyle(style -> style.withItalic(false)));
                        for (int i = index + 1, end = Math.min(i + 2, size); i < end; i++) {
                            lore.add(Component.literal("-> ").append(RecipeName.get(i % sequenceSize)).withStyle(ChatFormatting.DARK_AQUA)
                                .withStyle(style -> style.withItalic(false)));
                        }
                        transitional.set(AllDataComponents.SEQUENCED_ASSEMBLY_PROGRESS, (float) index / size);
                        transitional.set(DataComponents.LORE, new ItemLore(lore, lore));
                        return ItemStack.CODEC.encodeStart(ops, transitional).getOrThrow();
                    };
                    Supplier<JsonElement> transitionalJsonChanceResult = () -> {
                        transitional.set(AllDataComponents.SEQUENCED_ASSEMBLY_JUNK, new SequencedAssemblyJunk(result.chance(), junks));
                        JsonElement element = transitionalJsonResult.get();
                        transitional.remove(AllDataComponents.SEQUENCED_ASSEMBLY_JUNK);
                        return element;
                    };
                    JsonElement jsonResult = ItemStack.CODEC.encodeStart(ops, result.stack()).getOrThrow();

                    ResourceLocation id = ResourceLocation.parse(AllRecipeTypes.SEQUENCED_ASSEMBLY.toString())
                        .withSuffix("_" + idGenerator.incrementAndGet() + "_" + BuiltInRegistries.ITEM.getKey(result.stack().getItem())
                            .getPath() + "_");
                    List<Recipe<?>> sequence = new ArrayList<>(size);
                    TriConsumer<Integer, JsonElement, JsonElement> recipeAdd = (i, ingredientJson, resultJson) -> {
                        JsonObject object = sequenceJsonFactory.get(i % sequenceSize).apply(ingredientJson, resultJson);
                        Recipe<?> recipe = Recipe.CODEC.parse(ops, object).getOrThrow();
                        sequence.add(recipe);
                        GENERATE_RECIPES.put(id.withSuffix(String.valueOf(sequence.size())), recipe);
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
        public static final StreamCodec<RegistryFriendlyByteBuf, SequencedAssemblyRecipe> PACKET_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            SequencedAssemblyRecipe::ingredient,
            ItemStack.STREAM_CODEC,
            SequencedAssemblyRecipe::transitionalItem,
            ChanceOutput.PACKET_CODEC,
            SequencedAssemblyRecipe::result,
            ChanceOutput.PACKET_CODEC.apply(ByteBufCodecs.list()),
            SequencedAssemblyRecipe::junks,
            ByteBufCodecs.INT,
            SequencedAssemblyRecipe::loops,
            Recipe.STREAM_CODEC.apply(ByteBufCodecs.list()),
            SequencedAssemblyRecipe::sequence,
            SequencedAssemblyRecipe::new
        );

        @Override
        public MapCodec<SequencedAssemblyRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SequencedAssemblyRecipe> streamCodec() {
            return PACKET_CODEC;
        }
    }
}
