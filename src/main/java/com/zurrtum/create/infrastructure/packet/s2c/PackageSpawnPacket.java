package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.logistics.box.PackageEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;

public class PackageSpawnPacket extends EntitySpawnS2CPacket {
    public static final PacketCodec<RegistryByteBuf, PackageSpawnPacket> CODEC = Packet.createCodec(
        PackageSpawnPacket::write,
        PackageSpawnPacket::new
    );
    private final ItemStack box;

    public PackageSpawnPacket(PackageEntity entity, EntityTrackerEntry entityTrackerEntry) {
        super(entity, entityTrackerEntry);
        box = entity.getBox();
    }

    private PackageSpawnPacket(RegistryByteBuf buf) {
        super(buf);
        box = ItemStack.PACKET_CODEC.decode(buf);
    }

    @Override
    public void write(RegistryByteBuf buf) {
        super.write(buf);
        ItemStack.PACKET_CODEC.encode(buf, box);
    }

    public ItemStack getBox() {
        return box;
    }

    @Override
    @SuppressWarnings("unchecked")
    public PacketType<EntitySpawnS2CPacket> getPacketType() {
        return (PacketType<EntitySpawnS2CPacket>) (PacketType<?>) AllPackets.PACKAGE_SPAWN;
    }
}
