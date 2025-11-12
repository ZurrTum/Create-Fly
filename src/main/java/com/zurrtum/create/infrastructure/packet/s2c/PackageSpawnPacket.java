package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.logistics.box.PackageEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.item.ItemStack;

public class PackageSpawnPacket extends ClientboundAddEntityPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, PackageSpawnPacket> CODEC = Packet.codec(
        PackageSpawnPacket::write,
        PackageSpawnPacket::new
    );
    private final ItemStack box;

    public PackageSpawnPacket(PackageEntity entity, ServerEntity entityTrackerEntry) {
        super(entity, entityTrackerEntry);
        box = entity.getBox();
    }

    private PackageSpawnPacket(RegistryFriendlyByteBuf buf) {
        super(buf);
        box = ItemStack.STREAM_CODEC.decode(buf);
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        super.write(buf);
        ItemStack.STREAM_CODEC.encode(buf, box);
    }

    public ItemStack getBox() {
        return box;
    }

    @Override
    @SuppressWarnings("unchecked")
    public PacketType<ClientboundAddEntityPacket> type() {
        return (PacketType<ClientboundAddEntityPacket>) (PacketType<?>) AllPackets.PACKAGE_SPAWN;
    }
}
