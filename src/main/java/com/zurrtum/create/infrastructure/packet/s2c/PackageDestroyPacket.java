package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public record PackageDestroyPacket(Vec3 location, ItemStack box) implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, PackageDestroyPacket> CODEC = StreamCodec.composite(
        Vec3.STREAM_CODEC,
        PackageDestroyPacket::location,
        ItemStack.STREAM_CODEC,
        PackageDestroyPacket::box,
        PackageDestroyPacket::new
    );

    @Override
    public void handle(ClientGamePacketListener listener) {
        AllClientHandle.INSTANCE.onPackageDestroy(listener, this);
    }

    @Override
    public PacketType<PackageDestroyPacket> type() {
        return AllPackets.PACKAGE_DESTROYED;
    }
}
