package com.zurrtum.create.infrastructure.resources;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.references.BlockItemId;

public interface BlockTransformerModify extends InputModify<BlockTransformer> {
    default <T> DataResult<T> ifTrueThen(DynamicOps<T> ops, T empty, BlockItemId from, DataResult<T> source) {
        RecordBuilder<T> obj = ops.mapBuilder();
        RecordBuilder<T> ifTrue = ops.mapBuilder();
        ifTrue.add("type", ops.createString("minecraft:matching_blocks"));
        ifTrue.add("blocks", ops.createString(from.block().identifier().toString()));
        obj.add("if_true", ifTrue.build(empty));
        RecordBuilder<T> then = ops.mapBuilder();
        then.add("type", ops.createString("minecraft:copy_properties"));
        then.add("source", source);
        obj.add("then", then.build(empty));
        return obj.build(empty);
    }

    default <T> DataResult<T> transform(
        DynamicOps<T> ops,
        T empty,
        DataResult<T> rules,
        String particle,
        String sound
    ) {
        RecordBuilder<T> scrape = ops.mapBuilder();
        RecordBuilder<T> blockStateProvider = ops.mapBuilder();
        blockStateProvider.add("type", ops.createString("minecraft:rule_based"));
        blockStateProvider.add("rules", rules);
        scrape.add("block_state_provider", blockStateProvider.build(empty));
        scrape.add("item_damage_per_use", ops.createInt(1));
        scrape.add("particle", ops.createString(particle));
        scrape.add("sound", ops.createString(sound));
        return scrape.build(empty);
    }

    default <T> DataResult<T> ifTrueThenBlock(DynamicOps<T> ops, T empty, BlockItemId from, BlockItemId to) {
        RecordBuilder<T> source = ops.mapBuilder();
        source.add("id", ops.createString(to.block().identifier().toString()));
        return ifTrueThen(ops, empty, from, source.build(empty));
    }

    default <T> DataResult<T> ifTrueThenSlab(DynamicOps<T> ops, T empty, BlockItemId from, BlockItemId to) {
        RecordBuilder<T> source = ops.mapBuilder();
        source.add("id", ops.createString(to.block().identifier().toString()));
        RecordBuilder<T> properties = ops.mapBuilder();
        properties.add("type", ops.createString("bottom"));
        properties.add("waterlogged", ops.createString("false"));
        source.add("properties", properties.build(empty));
        return ifTrueThen(ops, empty, from, source.build(empty));
    }

    default <T> DataResult<T> ifTrueThenStairs(DynamicOps<T> ops, T empty, BlockItemId from, BlockItemId to) {
        RecordBuilder<T> source = ops.mapBuilder();
        source.add("id", ops.createString(to.block().identifier().toString()));
        RecordBuilder<T> properties = ops.mapBuilder();
        properties.add("facing", ops.createString("north"));
        properties.add("half", ops.createString("bottom"));
        properties.add("shape", ops.createString("straight"));
        properties.add("waterlogged", ops.createString("false"));
        source.add("properties", properties.build(empty));
        return ifTrueThen(ops, empty, from, source.build(empty));
    }
}
