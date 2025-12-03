package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public record PotatoCannonPacket(
    Vec3d location, Vec3d motion, ItemStack item, Hand hand, float pitch, boolean self
) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, PotatoCannonPacket> CODEC = PacketCodec.tuple(
        Vec3d.PACKET_CODEC,
        PotatoCannonPacket::location,
        Vec3d.PACKET_CODEC,
        PotatoCannonPacket::motion,
        ItemStack.OPTIONAL_PACKET_CODEC,
        PotatoCannonPacket::item,
        CatnipStreamCodecs.HAND,
        PotatoCannonPacket::hand,
        PacketCodecs.FLOAT,
        PotatoCannonPacket::pitch,
        PacketCodecs.BOOLEAN,
        PotatoCannonPacket::self,
        PotatoCannonPacket::new
    );

    @Override
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onPotatoCannon(listener, this);
    }

    @Override
    public PacketType<PotatoCannonPacket> getPacketType() {
        return AllPackets.POTATO_CANNON;
    }
}
