package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.BiConsumer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;

import static com.zurrtum.create.Create.MOD_ID;

@SuppressWarnings("unchecked")
public record S2CHoldPacket<T extends ClientGamePacketListener>(
    PacketType<Packet<ClientGamePacketListener>> id, BiConsumer<AllClientHandle<T>, T> consumer
) implements S2CPacket {
    public S2CHoldPacket(String id, BiConsumer<AllClientHandle<T>, T> callback) {
        this(new PacketType<>(PacketFlow.CLIENTBOUND, ResourceLocation.fromNamespaceAndPath(MOD_ID, id)), callback);
    }

    public StreamCodec<RegistryFriendlyByteBuf, Packet<ClientGamePacketListener>> codec() {
        return StreamCodec.unit(this);
    }

    @Override
    public PacketType<Packet<ClientGamePacketListener>> type() {
        return id();
    }

    @Override
    public TriConsumer<AllClientHandle<T>, T, S2CHoldPacket<T>> callback() {
        return (instance, listener, packet) -> consumer.accept(instance, listener);
    }
}
