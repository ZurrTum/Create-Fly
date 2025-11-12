package com.zurrtum.create.content.trains.graph;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class DimensionPalette implements Codec<ResourceKey<Level>> {
    public static final Codec<DimensionPalette> CODEC = ResourceKey.codec(Registries.DIMENSION).listOf()
        .xmap(DimensionPalette::new, DimensionPalette::getGatheredDims);
    public static final StreamCodec<ByteBuf, DimensionPalette> PACKET_CODEC = ResourceKey.streamCodec(Registries.DIMENSION)
        .apply(ByteBufCodecs.list()).map(DimensionPalette::new, DimensionPalette::getGatheredDims);

    private final List<ResourceKey<Level>> gatheredDims;

    public DimensionPalette() {
        gatheredDims = new ArrayList<>();
    }

    public DimensionPalette(List<ResourceKey<Level>> gatheredDims) {
        this.gatheredDims = gatheredDims;
    }

    private List<ResourceKey<Level>> getGatheredDims() {
        return gatheredDims;
    }

    public int encode(ResourceKey<Level> dimension) {
        int indexOf = gatheredDims.indexOf(dimension);
        if (indexOf == -1) {
            indexOf = gatheredDims.size();
            gatheredDims.add(dimension);
        }
        return indexOf;
    }

    public ResourceKey<Level> decode(int index) {
        if (gatheredDims.size() <= index || index < 0)
            return Level.OVERWORLD;
        return gatheredDims.get(index);
    }

    @Override
    public <T> DataResult<Pair<ResourceKey<Level>, T>> decode(DynamicOps<T> ops, T input) {
        return Codec.INT.decode(ops, input).map(p -> p.mapFirst(this::decode));
    }

    @Override
    public <T> DataResult<T> encode(ResourceKey<Level> input, DynamicOps<T> ops, T prefix) {
        return Codec.INT.encode(encode(input), ops, prefix);
    }
}
