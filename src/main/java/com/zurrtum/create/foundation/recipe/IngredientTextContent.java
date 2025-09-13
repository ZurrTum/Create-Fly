package com.zurrtum.create.foundation.recipe;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.*;

import java.util.List;
import java.util.Optional;

public class IngredientTextContent implements TextContent {
    private static final Codec<TagKey<Item>> TAG_CODEC = TagKey.codec(RegistryKeys.ITEM);
    private static final Codec<List<RegistryEntry<Item>>> ENTRY_CODEC = Item.ENTRY_CODEC.listOf();
    private static final Codec<Ingredient> INGREDIENT_CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<Ingredient, T>> decode(DynamicOps<T> ops, T input) {
            if (ops instanceof RegistryOps<T> registryOps) {
                Optional<RegistryEntryLookup<Item>> entryLookup = registryOps.getEntryLookup(RegistryKeys.ITEM);
                if (entryLookup.isPresent()) {
                    DataResult<Pair<TagKey<Item>, T>> tag = TAG_CODEC.decode(ops, input);
                    if (tag.isSuccess()) {
                        Optional<RegistryEntryList.Named<Item>> list = entryLookup.get().getOptional(tag.getOrThrow().getFirst());
                        if (list.isPresent()) {
                            return tag.map(pair -> pair.mapFirst(i -> new Ingredient(list.get())));
                        }
                    }
                }
            }
            DataResult<Pair<RegistryEntry<Item>, T>> entry = Item.ENTRY_CODEC.decode(ops, input);
            if (entry.isSuccess()) {
                return entry.map(pair -> pair.mapFirst(value -> new Ingredient(RegistryEntryList.of(value))));
            }
            return ENTRY_CODEC.decode(ops, input).map(pair -> pair.mapFirst(value -> new Ingredient(RegistryEntryList.of(value))));
        }

        @Override
        public <T> DataResult<T> encode(Ingredient input, DynamicOps<T> ops, T prefix) {
            RegistryEntryList<Item> entries = input.entries;
            if (ops instanceof RegistryOps<T>) {
                Optional<TagKey<Item>> tag = entries.getTagKey();
                if (tag.isPresent()) {
                    DataResult<T> result = TAG_CODEC.encode(tag.get(), ops, prefix);
                    if (result.isSuccess()) {
                        return result;
                    }
                }
            }
            List<RegistryEntry<Item>> list = entries.stream().toList();
            if (list.size() == 1) {
                return Item.ENTRY_CODEC.encode(list.getFirst(), ops, prefix);
            }
            return ENTRY_CODEC.encode(list, ops, prefix);
        }
    };
    public static final MapCodec<IngredientTextContent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        INGREDIENT_CODEC.optionalFieldOf("ingredient").forGetter(i -> Optional.ofNullable(i.ingredient)),
        TextCodecs.CODEC.optionalFieldOf("name").forGetter(i -> Optional.ofNullable(i.name))
    ).apply(instance, IngredientTextContent::new));

    public Ingredient ingredient;
    public Text name;
    public static final Type<IngredientTextContent> TYPE = new Type<>(CODEC, "translatable");

    @Override
    public Type<?> getType() {
        return TYPE;
    }

    public IngredientTextContent(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public IngredientTextContent(Optional<Ingredient> ingredient, Optional<Text> name) {
        name.ifPresentOrElse(value -> this.name = value, () -> this.ingredient = ingredient.orElse(null));
    }

    @Override
    public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
        if (name != null) {
            return name.visit(visitor);
        }
        return findName().flatMap(text -> text.visit(visitor));
    }

    @Override
    public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> visitor, Style style) {
        if (name != null) {
            return name.visit(visitor, style);
        }
        return findName().flatMap(text -> text.visit(visitor, style));
    }

    private Optional<Text> findName() {
        if (ingredient != null && ingredient.entries.isBound()) {
            Optional<RegistryEntry<Item>> first = ingredient.entries.stream().findFirst();
            if (first.isPresent()) {
                name = first.get().value().getName();
                ingredient = null;
                return Optional.of(name);
            }
        }
        return Optional.empty();
    }
}
