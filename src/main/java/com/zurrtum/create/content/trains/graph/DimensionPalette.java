package com.zurrtum.create.content.trains.graph;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class DimensionPalette implements Codec<RegistryKey<World>> {
    public static final Codec<DimensionPalette> CODEC = RegistryKey.createCodec(RegistryKeys.WORLD).listOf()
        .xmap(DimensionPalette::new, DimensionPalette::getGatheredDims);
    public static final PacketCodec<ByteBuf, DimensionPalette> PACKET_CODEC = RegistryKey.createPacketCodec(RegistryKeys.WORLD)
        .collect(PacketCodecs.toList()).xmap(DimensionPalette::new, DimensionPalette::getGatheredDims);

    private final List<RegistryKey<World>> gatheredDims;

    public DimensionPalette() {
        gatheredDims = new ArrayList<>();
    }

    public DimensionPalette(List<RegistryKey<World>> gatheredDims) {
        this.gatheredDims = gatheredDims;
    }

    private List<RegistryKey<World>> getGatheredDims() {
        return gatheredDims;
    }

    public int encode(RegistryKey<World> dimension) {
        int indexOf = gatheredDims.indexOf(dimension);
        if (indexOf == -1) {
            indexOf = gatheredDims.size();
            gatheredDims.add(dimension);
        }
        return indexOf;
    }

    public RegistryKey<World> decode(int index) {
        if (gatheredDims.size() <= index || index < 0)
            return World.OVERWORLD;
        return gatheredDims.get(index);
    }

    @Override
    public <T> DataResult<Pair<RegistryKey<World>, T>> decode(DynamicOps<T> ops, T input) {
        return Codec.INT.decode(ops, input).map(p -> p.mapFirst(this::decode));
    }

    @Override
    public <T> DataResult<T> encode(RegistryKey<World> input, DynamicOps<T> ops, T prefix) {
        return Codec.INT.encode(encode(input), ops, prefix);
    }
}
