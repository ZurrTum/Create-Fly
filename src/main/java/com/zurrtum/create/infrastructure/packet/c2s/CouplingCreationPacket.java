package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import java.util.function.BiConsumer;

public record CouplingCreationPacket(int id1, int id2) implements C2SPacket {
    public static final PacketCodec<RegistryByteBuf, CouplingCreationPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.VAR_INT,
        CouplingCreationPacket::id1,
        PacketCodecs.VAR_INT,
        CouplingCreationPacket::id2,
        CouplingCreationPacket::new
    );

    public CouplingCreationPacket(AbstractMinecartEntity cart1, AbstractMinecartEntity cart2) {
        this(cart1.getId(), cart2.getId());
    }

    @Override
    public PacketType<CouplingCreationPacket> getPacketType() {
        return AllPackets.MINECART_COUPLING_CREATION;
    }

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, CouplingCreationPacket> callback() {
        return AllHandle::onCouplingCreation;
    }
}
