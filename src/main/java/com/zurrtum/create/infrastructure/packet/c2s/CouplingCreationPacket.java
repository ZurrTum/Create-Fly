package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;

public record CouplingCreationPacket(int id1, int id2) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, CouplingCreationPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        CouplingCreationPacket::id1,
        ByteBufCodecs.VAR_INT,
        CouplingCreationPacket::id2,
        CouplingCreationPacket::new
    );

    public CouplingCreationPacket(AbstractMinecart cart1, AbstractMinecart cart2) {
        this(cart1.getId(), cart2.getId());
    }

    @Override
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onCouplingCreation((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<CouplingCreationPacket> type() {
        return AllPackets.MINECART_COUPLING_CREATION;
    }
}
