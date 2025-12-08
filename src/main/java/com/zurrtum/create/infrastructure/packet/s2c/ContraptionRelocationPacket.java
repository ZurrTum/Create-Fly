package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public record ContraptionRelocationPacket(int entityId) implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<ByteBuf, ContraptionRelocationPacket> CODEC = ByteBufCodecs.INT.map(
        ContraptionRelocationPacket::new,
        ContraptionRelocationPacket::entityId
    );

    @Override
    public void handle(ClientGamePacketListener listener) {
        AllClientHandle.INSTANCE.onContraptionRelocation(this);
    }

    @Override
    public PacketType<ContraptionRelocationPacket> type() {
        return AllPackets.CONTRAPTION_RELOCATION;
    }
}
