package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllPackets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public class SuperGlueSpawnPacket extends ClientboundAddEntityPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, SuperGlueSpawnPacket> CODEC = Packet.codec(
        SuperGlueSpawnPacket::write,
        SuperGlueSpawnPacket::new
    );
    private final AABB box;

    public SuperGlueSpawnPacket(Entity entity, ServerEntity entityTrackerEntry) {
        super(entity, entityTrackerEntry);
        box = entity.getBoundingBox();
    }

    private SuperGlueSpawnPacket(RegistryFriendlyByteBuf buf) {
        super(buf);
        box = new AABB(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble()).move(
            getX(),
            getY(),
            getZ()
        );
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        super.write(buf);
        AABB box = this.box.move(-getX(), -getY(), -getZ());
        buf.writeDouble(box.minX);
        buf.writeDouble(box.minY);
        buf.writeDouble(box.minZ);
        buf.writeDouble(box.maxX);
        buf.writeDouble(box.maxY);
        buf.writeDouble(box.maxZ);
    }

    public AABB getBox() {
        return box;
    }

    @Override
    @SuppressWarnings("unchecked")
    public PacketType<ClientboundAddEntityPacket> type() {
        return (PacketType<ClientboundAddEntityPacket>) (PacketType<?>) AllPackets.SUPER_GLUE_SPAWN;
    }
}
