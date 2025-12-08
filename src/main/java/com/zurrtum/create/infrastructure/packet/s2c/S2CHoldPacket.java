package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

import static com.zurrtum.create.Create.MOD_ID;

public record S2CHoldPacket(
    PacketType<Packet<ClientGamePacketListener>> id, Consumer<AllClientHandle> callback
) implements Packet<ClientGamePacketListener> {
    public S2CHoldPacket(String id, Consumer<AllClientHandle> callback) {
        this(new PacketType<>(PacketFlow.CLIENTBOUND, Identifier.fromNamespaceAndPath(MOD_ID, id)), callback);
    }

    public StreamCodec<RegistryFriendlyByteBuf, Packet<ClientGamePacketListener>> codec() {
        return StreamCodec.unit(this);
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        callback.accept(AllClientHandle.INSTANCE);
    }

    @Override
    public PacketType<Packet<ClientGamePacketListener>> type() {
        return id();
    }
}
