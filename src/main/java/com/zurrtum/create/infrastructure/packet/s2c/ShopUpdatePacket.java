package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public record ShopUpdatePacket(BlockPos pos) implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<ByteBuf, ShopUpdatePacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        ShopUpdatePacket::pos,
        ShopUpdatePacket::new
    );

    @Override
    public void handle(ClientGamePacketListener listener) {
        AllClientHandle.INSTANCE.onShopUpdate(listener, this);
    }

    @Override
    public PacketType<ShopUpdatePacket> type() {
        return AllPackets.SHOP_UPDATE;
    }
}
