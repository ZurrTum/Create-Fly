package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.util.TriConsumer;

public record ShopUpdatePacket(BlockPos pos) implements S2CPacket {
    public static final PacketCodec<ByteBuf, ShopUpdatePacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        ShopUpdatePacket::pos,
        ShopUpdatePacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, ShopUpdatePacket> callback() {
        return AllClientHandle::onShopUpdate;
    }

    @Override
    public PacketType<ShopUpdatePacket> getPacketType() {
        return AllPackets.SHOP_UPDATE;
    }
}
