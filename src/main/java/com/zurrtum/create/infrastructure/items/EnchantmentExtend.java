package com.zurrtum.create.infrastructure.items;

import com.google.common.collect.Iterators;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.zurrtum.create.AllItemTags;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public record EnchantmentExtend(Codec<Enchantment> codec) implements Codec<Enchantment> {
    private <T> Optional<DataResult<Pair<Enchantment, T>>> apply(
        DataResult.Success<Pair<Enchantment, T>> success,
        RegistryEntryLookup<Item> registry,
        TagKey<Item> tag,
        boolean allow
    ) {
        return registry.getOptional(tag).map(named -> success.map(pair -> pair.mapFirst((enchantment -> {
            RegistryEntryList<Item> supportedItems;
            Optional<RegistryEntryList<Item>> primaryItems;
            Enchantment.Definition definition = enchantment.definition();
            if (allow) {
                supportedItems = new HolderSetAllow(definition.supportedItems(), named);
                primaryItems = definition.primaryItems().map(origin -> new HolderSetAllow(origin, named));
            } else {
                supportedItems = new HolderSetDeny(definition.supportedItems(), named);
                primaryItems = definition.primaryItems();
            }
            return new Enchantment(
                enchantment.description(), new Enchantment.Definition(
                supportedItems,
                primaryItems,
                definition.weight(),
                definition.maxLevel(),
                definition.minCost(),
                definition.maxCost(),
                definition.anvilCost(),
                definition.slots()
            ), enchantment.exclusiveSet(), enchantment.effects()
            );
        }))));
    }

    @Override
    public <T> DataResult<Pair<Enchantment, T>> decode(DynamicOps<T> ops, T input) {
        DataResult<Pair<Enchantment, T>> result = codec.decode(ops, input);
        if (result instanceof DataResult.Success<Pair<Enchantment, T>> success && ops instanceof RegistryOps<T> registryOps) {
            return registryOps.getEntryLookup(RegistryKeys.ITEM).flatMap(registry -> switch (success.value().getFirst().description()
                .getContent() instanceof TranslatableTextContent text ? text.getKey() : null) {
                case "enchantment.minecraft.knockback" ->
                    apply(success, registry, AllItemTags.ENCHANTMENT_KNOCKBACK, true);
                case "enchantment.minecraft.looting" -> apply(success, registry, AllItemTags.ENCHANTMENT_LOOTING, true);
                case "enchantment.minecraft.mending" ->
                    apply(success, registry, AllItemTags.ENCHANTMENT_DENY_MENDING, false);
                case "enchantment.minecraft.unbreaking" ->
                    apply(success, registry, AllItemTags.ENCHANTMENT_DENY_UNBREAKING, false);
                case "enchantment.minecraft.infinity" ->
                    apply(success, registry, AllItemTags.ENCHANTMENT_DENY_INFINITY, false);
                case "enchantment.minecraft.aqua_affinity" ->
                    apply(success, registry, AllItemTags.ENCHANTMENT_DENY_AQUA_AFFINITY, false);
                case null, default -> Optional.empty();
            }).orElse(success);
        }
        return result;
    }

    @Override
    public <T> DataResult<T> encode(Enchantment input, DynamicOps<T> ops, T prefix) {
        return codec.encode(input, ops, prefix);
    }

    private static abstract class HolderSetExtend implements RegistryEntryList<Item> {
        protected final RegistryEntryList<Item> origin;
        protected final RegistryEntryList<Item> tag;

        HolderSetExtend(RegistryEntryList<Item> origin, RegistryEntryList<Item> tag) {
            this.origin = origin;
            this.tag = tag;
        }

        @Override
        public boolean ownerEquals(RegistryEntryOwner<Item> owner) {
            return origin.ownerEquals(owner);
        }

        @Override
        public Optional<RegistryEntry<Item>> getRandom(Random random) {
            return origin.getRandom(random);
        }

        @Override
        public boolean isBound() {
            return origin.isBound() && tag.isBound();
        }

        @Override
        public Either<TagKey<Item>, List<RegistryEntry<Item>>> getStorage() {
            return origin.getStorage();
        }

        @Override
        public Optional<TagKey<Item>> getTagKey() {
            return origin.getTagKey();
        }
    }

    private static class HolderSetAllow extends HolderSetExtend {
        HolderSetAllow(RegistryEntryList<Item> origin, RegistryEntryList<Item> tag) {
            super(origin, tag);
        }

        @Override
        public boolean contains(RegistryEntry<Item> value) {
            return origin.contains(value) || tag.contains(value);
        }

        @Override
        public RegistryEntry<Item> get(int index) {
            int size = origin.size();
            return index < size ? origin.get(index) : tag.get(index - size);
        }

        @Override
        public Iterator<RegistryEntry<Item>> iterator() {
            return Iterators.concat(origin.iterator(), tag.iterator());
        }

        @Override
        public int size() {
            return origin.size() + tag.size();
        }

        @Override
        public Stream<RegistryEntry<Item>> stream() {
            return Stream.concat(origin.stream(), tag.stream());
        }
    }

    private static class HolderSetDeny extends HolderSetExtend {
        private @Nullable List<RegistryEntry<Item>> contents;
        private @Nullable Set<RegistryEntry<Item>> contentsSet;

        HolderSetDeny(RegistryEntryList<Item> origin, RegistryEntryList<Item> tag) {
            super(origin, tag);
        }

        @Override
        public boolean contains(RegistryEntry<Item> value) {
            if (contentsSet == null) {
                contentsSet = Set.copyOf(contents());
            }
            return contentsSet.contains(value);
        }

        private List<RegistryEntry<Item>> contents() {
            if (contents == null) {
                contents = new ArrayList<>(origin.size());
                for (RegistryEntry<Item> holder : origin) {
                    if (tag.contains(holder)) {
                        continue;
                    }
                    contents.add(holder);
                }
            }
            return contents;
        }

        @Override
        public RegistryEntry<Item> get(int index) {
            return contents().get(index);
        }

        @Override
        public Iterator<RegistryEntry<Item>> iterator() {
            return contents().iterator();
        }

        @Override
        public int size() {
            return contents().size();
        }

        @Override
        public Stream<RegistryEntry<Item>> stream() {
            return contents().stream();
        }
    }
}
