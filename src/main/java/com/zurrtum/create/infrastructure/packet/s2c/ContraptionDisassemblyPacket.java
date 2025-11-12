package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.contraptions.StructureTransform;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import org.apache.logging.log4j.util.TriConsumer;

public record ContraptionDisassemblyPacket(int entityId, StructureTransform transform) implements S2CPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ContraptionDisassemblyPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        ContraptionDisassemblyPacket::entityId,
        StructureTransform.STREAM_CODEC,
        ContraptionDisassemblyPacket::transform,
        ContraptionDisassemblyPacket::new
    );

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, ContraptionDisassemblyPacket> callback() {
        return AllClientHandle::onContraptionDisassembly;
    }

    @Override
    public PacketType<ContraptionDisassemblyPacket> type() {
        return AllPackets.CONTRAPTION_DISASSEMBLE;
    }
}
