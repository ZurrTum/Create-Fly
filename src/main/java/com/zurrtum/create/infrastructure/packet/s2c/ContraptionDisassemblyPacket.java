package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.contraptions.StructureTransform;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import org.apache.logging.log4j.util.TriConsumer;

public record ContraptionDisassemblyPacket(int entityId, StructureTransform transform) implements S2CPacket {
    public static final PacketCodec<RegistryByteBuf, ContraptionDisassemblyPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER,
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
    public PacketType<ContraptionDisassemblyPacket> getPacketType() {
        return AllPackets.CONTRAPTION_DISASSEMBLE;
    }
}
