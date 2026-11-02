package com.zurrtum.create.infrastructure.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface EnchantmentModify extends InputModify<Enchantment> {
    default Enchantment mapItemSets(Enchantment enchantment, Function<HolderSet<Item>, HolderSet<Item>> factory) {
        Enchantment.EnchantmentDefinition definition = enchantment.definition();
        return new Enchantment(
            enchantment.description(), new Enchantment.EnchantmentDefinition(
            factory.apply(definition.supportedItems()),
            definition.primaryItems().map(factory),
            definition.weight(),
            definition.maxLevel(),
            definition.minCost(),
            definition.maxCost(),
            definition.anvilCost(),
            definition.slots()
        ), enchantment.exclusiveSet(), enchantment.effects()
        );
    }

    default <T> DataResult<Pair<Enchantment, T>> mapItemSetsByTag(
        RegistryOps<T> registryOps,
        DataResult.Success<Pair<Enchantment, T>> success,
        BiFunction<HolderSet<Item>, HolderSet<Item>, HolderSet<Item>> factory,
        TagKey<Item> tag
    ) {
        return registryOps.getter(Registries.ITEM).flatMap(registry -> registry.get(tag)
            .map(named -> success.map(pair -> pair.mapFirst(enchantment -> mapItemSets(
                enchantment,
                origin -> factory.apply(origin, named)
            ))))).orElse(success);
    }

    static EnchantmentModify define(
        BiFunction<HolderSet<Item>, HolderSet<Item>, HolderSet<Item>> factory,
        TagKey<Item> tag
    ) {
        return new EnchantmentModify() {
            @Override
            public <T> DataResult<Pair<Enchantment, T>> decode(Codec<Enchantment> codec, DynamicOps<T> ops, T input) {
                DataResult<Pair<Enchantment, T>> result = codec.decode(ops, input);
                if (!(ops instanceof RegistryOps<T> registryOps) || !(result instanceof DataResult.Success<Pair<Enchantment, T>> success)) {
                    return result;
                }
                return mapItemSetsByTag(registryOps, success, factory, tag);
            }
        };
    }

    static EnchantmentModify deny(TagKey<Item> tag) {
        return define(HolderSetDeny::new, tag);
    }

    static EnchantmentModify allow(TagKey<Item> tag) {
        return define(HolderSetAllow::new, tag);
    }
}
