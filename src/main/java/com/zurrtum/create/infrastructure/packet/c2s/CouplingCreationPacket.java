package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;

import java.util.function.BiConsumer;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;

public record CouplingCreationPacket(int id1, int id2) implements C2SPacket {
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
    public PacketType<CouplingCreationPacket> type() {
        return AllPackets.MINECART_COUPLING_CREATION;
    }

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, CouplingCreationPacket> callback() {
        return AllHandle::onCouplingCreation;
    }
}
