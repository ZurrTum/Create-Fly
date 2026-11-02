package com.zurrtum.create.infrastructure.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;
import com.zurrtum.create.AllBlockItemIds;
import net.minecraft.core.component.BlockTransformer;

public class AxeModify implements BlockTransformerModify {
    @Override
    public <T> DataResult<Pair<BlockTransformer, T>> decode(
        Codec<BlockTransformer> codec,
        DynamicOps<T> ops,
        T prefix
    ) {
        T empty = ops.empty();
        ListBuilder<T> list = ops.listBuilder();
        ListBuilder<T> scrape = ops.listBuilder();
        AllBlockItemIds.COPPER_SHINGLES.weathering()
            .progressMapping((to, from) -> scrape.add(ifTrueThenBlock(ops, empty, from, to)));
        AllBlockItemIds.COPPER_SHINGLE_SLAB.weathering()
            .progressMapping((to, from) -> scrape.add(ifTrueThenSlab(ops, empty, from, to)));
        AllBlockItemIds.COPPER_SHINGLE_STAIRS.weathering()
            .progressMapping((to, from) -> scrape.add(ifTrueThenStairs(ops, empty, from, to)));
        AllBlockItemIds.COPPER_TILES.weathering()
            .progressMapping((to, from) -> scrape.add(ifTrueThenBlock(ops, empty, from, to)));
        AllBlockItemIds.COPPER_TILE_SLAB.weathering()
            .progressMapping((to, from) -> scrape.add(ifTrueThenSlab(ops, empty, from, to)));
        AllBlockItemIds.COPPER_TILE_STAIRS.weathering()
            .progressMapping((to, from) -> scrape.add(ifTrueThenStairs(ops, empty, from, to)));
        list.add(transform(ops, empty, scrape.build(empty), "scrape", "minecraft:item.axe.scrape"));
        ListBuilder<T> waxOff = ops.listBuilder();
        AllBlockItemIds.COPPER_SHINGLES.zipUnwaxedWaxed((to, from) -> waxOff.add(ifTrueThenBlock(
            ops,
            empty,
            from,
            to
        )));
        AllBlockItemIds.COPPER_SHINGLE_SLAB.zipUnwaxedWaxed((to, from) -> waxOff.add(ifTrueThenSlab(
            ops,
            empty,
            from,
            to
        )));
        AllBlockItemIds.COPPER_SHINGLE_STAIRS.zipUnwaxedWaxed((to, from) -> waxOff.add(ifTrueThenStairs(
            ops,
            empty,
            from,
            to
        )));
        AllBlockItemIds.COPPER_TILES.zipUnwaxedWaxed((to, from) -> waxOff.add(ifTrueThenBlock(ops, empty, from, to)));
        AllBlockItemIds.COPPER_TILE_SLAB.zipUnwaxedWaxed((to, from) -> waxOff.add(ifTrueThenSlab(
            ops,
            empty,
            from,
            to
        )));
        AllBlockItemIds.COPPER_TILE_STAIRS.zipUnwaxedWaxed((to, from) -> waxOff.add(ifTrueThenStairs(
            ops,
            empty,
            from,
            to
        )));
        list.add(transform(ops, empty, waxOff.build(empty), "wax_off", "minecraft:item.axe.wax_off"));
        T input = list.build(prefix).result().orElse(prefix);
        return codec.decode(ops, input);
    }
}
