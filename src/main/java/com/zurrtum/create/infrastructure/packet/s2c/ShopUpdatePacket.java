package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.BlockPos;

public record ShopUpdatePacket(BlockPos pos) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<ByteBuf, ShopUpdatePacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        ShopUpdatePacket::pos,
        ShopUpdatePacket::new
    );

    @Override
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onShopUpdate(listener, this);
    }

    @Override
    public PacketType<ShopUpdatePacket> getPacketType() {
        return AllPackets.SHOP_UPDATE;
    }
}
