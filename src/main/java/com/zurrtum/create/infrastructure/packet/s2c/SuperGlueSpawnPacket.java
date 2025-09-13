package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllPackets;
import net.minecraft.entity.Entity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.util.math.Box;

public class SuperGlueSpawnPacket extends EntitySpawnS2CPacket {
    public static final PacketCodec<RegistryByteBuf, SuperGlueSpawnPacket> CODEC = Packet.createCodec(
        SuperGlueSpawnPacket::write,
        SuperGlueSpawnPacket::new
    );
    private final Box box;

    public SuperGlueSpawnPacket(Entity entity, EntityTrackerEntry entityTrackerEntry) {
        super(entity, entityTrackerEntry);
        box = entity.getBoundingBox();
    }

    private SuperGlueSpawnPacket(RegistryByteBuf buf) {
        super(buf);
        box = new Box(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble()).offset(
            getX(),
            getY(),
            getZ()
        );
    }

    @Override
    public void write(RegistryByteBuf buf) {
        super.write(buf);
        Box box = this.box.offset(-getX(), -getY(), -getZ());
        buf.writeDouble(box.minX);
        buf.writeDouble(box.minY);
        buf.writeDouble(box.minZ);
        buf.writeDouble(box.maxX);
        buf.writeDouble(box.maxY);
        buf.writeDouble(box.maxZ);
    }

    public Box getBox() {
        return box;
    }

    @Override
    @SuppressWarnings("unchecked")
    public PacketType<EntitySpawnS2CPacket> getPacketType() {
        return (PacketType<EntitySpawnS2CPacket>) (PacketType<?>) AllPackets.SUPER_GLUE_SPAWN;
    }
}
