package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record ClickToLinkData(BlockPos selectedPos, Identifier selectedDim) {
    public static final Codec<ClickToLinkData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlockPos.CODEC.fieldOf("selected_pos")
            .forGetter(ClickToLinkData::selectedPos),
        Identifier.CODEC.fieldOf("selected_dim").forGetter(ClickToLinkData::selectedDim)
    ).apply(instance, ClickToLinkData::new));

    public static final PacketCodec<PacketByteBuf, ClickToLinkData> STREAM_CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        ClickToLinkData::selectedPos,
        Identifier.PACKET_CODEC,
        ClickToLinkData::selectedDim,
        ClickToLinkData::new
    );
}