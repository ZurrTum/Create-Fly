package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.logistics.depot.EjectorBlockEntity;
import com.zurrtum.create.content.logistics.depot.EjectorItemEntity;
import com.zurrtum.create.content.logistics.depot.EntityLauncher;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.util.math.Direction;

public class EjectorItemSpawnPacket extends EntitySpawnS2CPacket {
    private final boolean alive;
    private final int progress;
    private final boolean hasLauncher;
    private final EntityLauncher launcher;
    private final Direction direction;
    public static final PacketCodec<RegistryByteBuf, EjectorItemSpawnPacket> CODEC = Packet.createCodec(
        EjectorItemSpawnPacket::write,
        EjectorItemSpawnPacket::new
    );

    public EjectorItemSpawnPacket(EjectorItemEntity entity, EntityTrackerEntry entityTrackerEntry) {
        super(entity, entityTrackerEntry);
        alive = entity.isAlive();
        hasLauncher = !alive && !(entity.getEntityWorld().getBlockEntity(entity.getBlockPos()) instanceof EjectorBlockEntity);
        if (hasLauncher) {
            progress = entity.progress;
            launcher = entity.launcher;
            direction = entity.direction;
        } else {
            progress = 0;
            launcher = null;
            direction = null;
        }
    }

    private EjectorItemSpawnPacket(RegistryByteBuf buf) {
        super(buf);
        alive = buf.readBoolean();
        progress = buf.readInt();
        if (!alive) {
            hasLauncher = buf.readBoolean();
            if (hasLauncher) {
                launcher = EntityLauncher.PACKET_CODEC.decode(buf);
                direction = Direction.PACKET_CODEC.decode(buf);
                return;
            }
        } else {
            hasLauncher = false;
        }
        launcher = null;
        direction = null;
    }

    @Override
    public void write(RegistryByteBuf buf) {
        super.write(buf);
        buf.writeBoolean(alive);
        buf.writeInt(progress);
        if (!alive) {
            buf.writeBoolean(hasLauncher);
            if (hasLauncher) {
                EntityLauncher.PACKET_CODEC.encode(buf, launcher);
                Direction.PACKET_CODEC.encode(buf, direction);
            }
        }
    }

    public boolean getAlive() {
        return alive;
    }

    public int getProgress() {
        return progress;
    }

    public EntityLauncher getLauncher() {
        return launcher;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean hasLauncher() {
        return hasLauncher;
    }

    @Override
    @SuppressWarnings("unchecked")
    public PacketType<EntitySpawnS2CPacket> getPacketType() {
        return (PacketType<EntitySpawnS2CPacket>) (PacketType<?>) AllPackets.EJECTOR_ITEM_SPAWN;
    }
}
