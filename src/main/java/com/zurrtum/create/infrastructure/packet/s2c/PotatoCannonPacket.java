package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.util.TriConsumer;

public record PotatoCannonPacket(
    Vec3d location, Vec3d motion, ItemStack item, Hand hand, float pitch, boolean self
) implements ShootGadgetPacket {
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
    public boolean runInMain() {
        return true;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, PotatoCannonPacket> callback() {
        return AllClientHandle::onPotatoCannon;
    }

    @Override
    public PacketType<PotatoCannonPacket> getPacketType() {
        return AllPackets.POTATO_CANNON;
    }
}
