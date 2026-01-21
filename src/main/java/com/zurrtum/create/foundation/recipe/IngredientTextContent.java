package com.zurrtum.create.foundation.recipe;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.*;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class IngredientTextContent implements ComponentContents {
    private static final Codec<TagKey<Item>> TAG_CODEC = TagKey.hashedCodec(Registries.ITEM);
    private static final Codec<List<Holder<Item>>> ENTRY_CODEC = Item.CODEC.listOf();
    private static final Codec<Ingredient> INGREDIENT_CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<Ingredient, T>> decode(DynamicOps<T> ops, T input) {
            if (ops instanceof RegistryOps<T> registryOps) {
                Optional<HolderGetter<Item>> entryLookup = registryOps.getter(Registries.ITEM);
                if (entryLookup.isPresent()) {
                    DataResult<Pair<TagKey<Item>, T>> tag = TAG_CODEC.decode(ops, input);
                    if (tag.isSuccess()) {
                        Optional<HolderSet.Named<Item>> list = entryLookup.get().get(tag.getOrThrow().getFirst());
                        if (list.isPresent()) {
                            return tag.map(pair -> pair.mapFirst(i -> new Ingredient(list.get())));
                        }
                    }
                }
            }
            DataResult<Pair<Holder<Item>, T>> entry = Item.CODEC.decode(ops, input);
            if (entry.isSuccess()) {
                return entry.map(pair -> pair.mapFirst(value -> new Ingredient(HolderSet.direct(value))));
            }
            return ENTRY_CODEC.decode(ops, input).map(pair -> pair.mapFirst(value -> new Ingredient(HolderSet.direct(value))));
        }

        @Override
        public <T> DataResult<T> encode(Ingredient input, DynamicOps<T> ops, T prefix) {
            HolderSet<Item> entries = input.values;
            if (ops instanceof RegistryOps<T>) {
                Optional<TagKey<Item>> tag = entries.unwrapKey();
                if (tag.isPresent()) {
                    DataResult<T> result = TAG_CODEC.encode(tag.get(), ops, prefix);
                    if (result.isSuccess()) {
                        return result;
                    }
                }
            }
            List<Holder<Item>> list = entries.stream().toList();
            if (list.size() == 1) {
                return Item.CODEC.encode(list.getFirst(), ops, prefix);
            }
            return ENTRY_CODEC.encode(list, ops, prefix);
        }
    };
    public static final MapCodec<IngredientTextContent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        INGREDIENT_CODEC.optionalFieldOf("ingredient").forGetter(i -> Optional.ofNullable(i.ingredient)),
        ComponentSerialization.CODEC.optionalFieldOf("name").forGetter(i -> Optional.ofNullable(i.name))
    ).apply(instance, IngredientTextContent::new));

    public @Nullable Ingredient ingredient;
    public @Nullable Component name;

    @Override
    public MapCodec<? extends ComponentContents> codec() {
        return CODEC;
    }

    public IngredientTextContent(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public IngredientTextContent(Optional<Ingredient> ingredient, Optional<Component> name) {
        name.ifPresentOrElse(value -> this.name = value, () -> this.ingredient = ingredient.orElse(null));
    }

    @Override
    public <T> Optional<T> visit(FormattedText.ContentConsumer<T> visitor) {
        if (name != null) {
            return name.visit(visitor);
        }
        return findName().flatMap(text -> text.visit(visitor));
    }

    @Override
    public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> visitor, Style style) {
        if (name != null) {
            return name.visit(visitor, style);
        }
        return findName().flatMap(text -> text.visit(visitor, style));
    }

    private Optional<Component> findName() {
        if (ingredient != null && ingredient.values.isBound()) {
            Optional<Holder<Item>> first = ingredient.values.stream().findFirst();
            if (first.isPresent()) {
                name = first.get().value().getName();
                ingredient = null;
                return Optional.of(name);
            }
        }
        return Optional.empty();
    }

    public Optional<Component> getName() {
        if (name != null) {
            return Optional.of(name);
        }
        return findName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IngredientTextContent other) {
            Optional<Component> name = getName();
            Optional<Component> otherName = other.getName();
            if (name.isPresent() && otherName.isPresent()) {
                return name.get().equals(otherName.get());
            } else {
                return true;
            }
        }
        return false;
    }
}
