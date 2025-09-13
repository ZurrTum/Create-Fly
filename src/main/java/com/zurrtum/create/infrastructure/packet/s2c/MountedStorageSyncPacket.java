package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorage;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorage;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.HashMap;
import java.util.Map;

public record MountedStorageSyncPacket(
    int contraptionId, Map<BlockPos, MountedItemStorage> items, Map<BlockPos, MountedFluidStorage> fluids
) implements S2CPacket {
    public static final PacketCodec<RegistryByteBuf, MountedStorageSyncPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER,
        MountedStorageSyncPacket::contraptionId,
        PacketCodecs.map(HashMap::new, BlockPos.PACKET_CODEC, MountedItemStorage.STREAM_CODEC),
        MountedStorageSyncPacket::items,
        PacketCodecs.map(HashMap::new, BlockPos.PACKET_CODEC, MountedFluidStorage.STREAM_CODEC),
        MountedStorageSyncPacket::fluids,
        MountedStorageSyncPacket::new
    );

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, MountedStorageSyncPacket> callback() {
        return AllClientHandle::onMountedStorageSync;
    }

    @Override
    public PacketType<MountedStorageSyncPacket> getPacketType() {
        return AllPackets.MOUNTED_STORAGE_SYNC;
    }
}
