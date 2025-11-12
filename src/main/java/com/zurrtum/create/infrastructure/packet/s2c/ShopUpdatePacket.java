package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import org.apache.logging.log4j.util.TriConsumer;

public record ShopUpdatePacket(BlockPos pos) implements S2CPacket {
    public static final StreamCodec<ByteBuf, ShopUpdatePacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
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
    public PacketType<ShopUpdatePacket> type() {
        return AllPackets.SHOP_UPDATE;
    }
}
