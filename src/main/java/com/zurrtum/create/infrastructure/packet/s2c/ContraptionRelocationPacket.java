package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;

public record ContraptionRelocationPacket(int entityId) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<ByteBuf, ContraptionRelocationPacket> CODEC = PacketCodecs.INTEGER.xmap(
        ContraptionRelocationPacket::new,
        ContraptionRelocationPacket::entityId
    );

    @Override
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onContraptionRelocation(this);
    }

    @Override
    public PacketType<ContraptionRelocationPacket> getPacketType() {
        return AllPackets.CONTRAPTION_RELOCATION;
    }
}
