package com.zurrtum.create.infrastructure.items;

import com.google.common.collect.Iterators;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DataResult.Success;
import com.mojang.serialization.DynamicOps;
import com.zurrtum.create.AllItemTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantment.EnchantmentDefinition;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public record EnchantmentExtend(Codec<Enchantment> codec) implements Codec<Enchantment> {
    private <T> Optional<DataResult<Pair<Enchantment, T>>> apply(
        Success<Pair<Enchantment, T>> success,
        HolderGetter<Item> registry,
        TagKey<Item> tag,
        boolean allow
    ) {
        return registry.get(tag).map(named -> success.map(pair -> pair.mapFirst((enchantment -> {
            HolderSet<Item> supportedItems;
            Optional<HolderSet<Item>> primaryItems;
            EnchantmentDefinition definition = enchantment.definition();
            if (allow) {
                supportedItems = new HolderSetAllow(definition.supportedItems(), named);
                primaryItems = definition.primaryItems().map(origin -> new HolderSetAllow(origin, named));
            } else {
                supportedItems = new HolderSetDeny(definition.supportedItems(), named);
                primaryItems = definition.primaryItems();
            }
            return new Enchantment(
                enchantment.description(), new EnchantmentDefinition(
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
        if (result instanceof Success<Pair<Enchantment, T>> success && ops instanceof RegistryOps<T> registryOps) {
            return registryOps.getter(Registries.ITEM).flatMap(registry -> switch (success.value().getFirst().description()
                .getContents() instanceof TranslatableContents text ? text.getKey() : null) {
                case "enchantment.minecraft.knockback" -> apply(success, registry, AllItemTags.ENCHANTMENT_KNOCKBACK, true);
                case "enchantment.minecraft.looting" -> apply(success, registry, AllItemTags.ENCHANTMENT_LOOTING, true);
                case "enchantment.minecraft.mending" -> apply(success, registry, AllItemTags.ENCHANTMENT_DENY_MENDING, false);
                case "enchantment.minecraft.unbreaking" -> apply(success, registry, AllItemTags.ENCHANTMENT_DENY_UNBREAKING, false);
                case "enchantment.minecraft.infinity" -> apply(success, registry, AllItemTags.ENCHANTMENT_DENY_INFINITY, false);
                case "enchantment.minecraft.aqua_affinity" -> apply(success, registry, AllItemTags.ENCHANTMENT_DENY_AQUA_AFFINITY, false);
                case null, default -> Optional.empty();
            }).orElse(success);
        }
        return result;
    }

    @Override
    public <T> DataResult<T> encode(Enchantment input, DynamicOps<T> ops, T prefix) {
        return codec.encode(input, ops, prefix);
    }

    private static abstract class HolderSetExtend implements HolderSet<Item> {
        protected final HolderSet<Item> origin;
        protected final HolderSet<Item> tag;

        HolderSetExtend(HolderSet<Item> origin, HolderSet<Item> tag) {
            this.origin = origin;
            this.tag = tag;
        }

        @Override
        public boolean canSerializeIn(HolderOwner<Item> owner) {
            return origin.canSerializeIn(owner);
        }

        @Override
        public Optional<Holder<Item>> getRandomElement(RandomSource random) {
            return origin.getRandomElement(random);
        }

        @Override
        public boolean isBound() {
            return origin.isBound() && tag.isBound();
        }

        @Override
        public Either<TagKey<Item>, List<Holder<Item>>> unwrap() {
            return origin.unwrap();
        }

        @Override
        public Optional<TagKey<Item>> unwrapKey() {
            return origin.unwrapKey();
        }
    }

    private static class HolderSetAllow extends HolderSetExtend {
        HolderSetAllow(HolderSet<Item> origin, HolderSet<Item> tag) {
            super(origin, tag);
        }

        @Override
        public boolean contains(Holder<Item> value) {
            return origin.contains(value) || tag.contains(value);
        }

        @Override
        public Holder<Item> get(int index) {
            int size = origin.size();
            return index < size ? origin.get(index) : tag.get(index - size);
        }

        @Override
        public Iterator<Holder<Item>> iterator() {
            return Iterators.concat(origin.iterator(), tag.iterator());
        }

        @Override
        public int size() {
            return origin.size() + tag.size();
        }

        @Override
        public Stream<Holder<Item>> stream() {
            return Stream.concat(origin.stream(), tag.stream());
        }
    }

    private static class HolderSetDeny extends HolderSetExtend {
        private @Nullable List<Holder<Item>> contents;
        private @Nullable Set<Holder<Item>> contentsSet;

        HolderSetDeny(HolderSet<Item> origin, HolderSet<Item> tag) {
            super(origin, tag);
        }

        @Override
        public boolean contains(Holder<Item> value) {
            if (contentsSet == null) {
                contentsSet = Set.copyOf(contents());
            }
            return contentsSet.contains(value);
        }

        private List<Holder<Item>> contents() {
            if (contents == null) {
                contents = new ArrayList<>(origin.size());
                for (Holder<Item> holder : origin) {
                    if (tag.contains(holder)) {
                        continue;
                    }
                    contents.add(holder);
                }
            }
            return contents;
        }

        @Override
        public Holder<Item> get(int index) {
            return contents().get(index);
        }

        @Override
        public Iterator<Holder<Item>> iterator() {
            return contents().iterator();
        }

        @Override
        public int size() {
            return contents().size();
        }

        @Override
        public Stream<Holder<Item>> stream() {
            return contents().stream();
        }
    }
}
