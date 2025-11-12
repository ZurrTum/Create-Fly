package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorage;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorage;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;

public record MountedStorageSyncPacket(
    int contraptionId, Map<BlockPos, MountedItemStorage> items, Map<BlockPos, MountedFluidStorage> fluids
) implements S2CPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, MountedStorageSyncPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        MountedStorageSyncPacket::contraptionId,
        ByteBufCodecs.map(HashMap::new, BlockPos.STREAM_CODEC, MountedItemStorage.STREAM_CODEC),
        MountedStorageSyncPacket::items,
        ByteBufCodecs.map(HashMap::new, BlockPos.STREAM_CODEC, MountedFluidStorage.STREAM_CODEC),
        MountedStorageSyncPacket::fluids,
        MountedStorageSyncPacket::new
    );

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, MountedStorageSyncPacket> callback() {
        return AllClientHandle::onMountedStorageSync;
    }

    @Override
    public PacketType<MountedStorageSyncPacket> type() {
        return AllPackets.MOUNTED_STORAGE_SYNC;
    }
}
