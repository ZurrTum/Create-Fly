package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.util.TriConsumer;

public record PackageDestroyPacket(Vec3 location, ItemStack box) implements S2CPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, PackageDestroyPacket> CODEC = StreamCodec.composite(
        Vec3.STREAM_CODEC,
        PackageDestroyPacket::location,
        ItemStack.STREAM_CODEC,
        PackageDestroyPacket::box,
        PackageDestroyPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, PackageDestroyPacket> callback() {
        return AllClientHandle::onPackageDestroy;
    }

    @Override
    public PacketType<PackageDestroyPacket> type() {
        return AllPackets.PACKAGE_DESTROYED;
    }
}
