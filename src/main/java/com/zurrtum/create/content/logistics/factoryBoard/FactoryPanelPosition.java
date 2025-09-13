package com.zurrtum.create.content.logistics.factoryBoard;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.math.BlockPos;

public record FactoryPanelPosition(BlockPos pos, PanelSlot slot) {
    public static final Codec<FactoryPanelPosition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlockPos.CODEC.fieldOf("pos")
            .forGetter(FactoryPanelPosition::pos), PanelSlot.CODEC.fieldOf("slot").forGetter(FactoryPanelPosition::slot)
    ).apply(instance, FactoryPanelPosition::new));

    public static final PacketCodec<ByteBuf, FactoryPanelPosition> PACKET_CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        FactoryPanelPosition::pos,
        PanelSlot.STREAM_CODEC,
        FactoryPanelPosition::slot,
        FactoryPanelPosition::new
    );
}
