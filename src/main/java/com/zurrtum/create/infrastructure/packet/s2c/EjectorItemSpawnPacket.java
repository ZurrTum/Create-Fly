package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.logistics.depot.EjectorBlockEntity;
import com.zurrtum.create.content.logistics.depot.EjectorItemEntity;
import com.zurrtum.create.content.logistics.depot.EntityLauncher;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import org.jspecify.annotations.Nullable;

public class EjectorItemSpawnPacket extends ClientboundAddEntityPacket {
    private final boolean alive;
    private final int progress;
    private final boolean hasLauncher;
    private final @Nullable EntityLauncher launcher;
    private final @Nullable Direction direction;
    public static final StreamCodec<RegistryFriendlyByteBuf, EjectorItemSpawnPacket> CODEC = Packet.codec(
        EjectorItemSpawnPacket::write,
        EjectorItemSpawnPacket::new
    );

    public EjectorItemSpawnPacket(EjectorItemEntity entity, ServerEntity entityTrackerEntry) {
        super(entity, entityTrackerEntry);
        alive = entity.isAlive();
        hasLauncher = !alive && !(entity.level().getBlockEntity(entity.blockPosition()) instanceof EjectorBlockEntity);
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

    private EjectorItemSpawnPacket(RegistryFriendlyByteBuf buf) {
        super(buf);
        alive = buf.readBoolean();
        progress = buf.readInt();
        if (!alive) {
            hasLauncher = buf.readBoolean();
            if (hasLauncher) {
                launcher = EntityLauncher.PACKET_CODEC.decode(buf);
                direction = Direction.STREAM_CODEC.decode(buf);
                return;
            }
        } else {
            hasLauncher = false;
        }
        launcher = null;
        direction = null;
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        super.write(buf);
        buf.writeBoolean(alive);
        buf.writeInt(progress);
        if (!alive) {
            buf.writeBoolean(hasLauncher);
            if (hasLauncher) {
                EntityLauncher.PACKET_CODEC.encode(buf, launcher);
                Direction.STREAM_CODEC.encode(buf, direction);
            }
        }
    }

    public boolean getAlive() {
        return alive;
    }

    public int getProgress() {
        return progress;
    }

    @Nullable
    public EntityLauncher getLauncher() {
        return launcher;
    }

    @Nullable
    public Direction getDirection() {
        return direction;
    }

    public boolean hasLauncher() {
        return hasLauncher;
    }

    @Override
    @SuppressWarnings("unchecked")
    public PacketType<ClientboundAddEntityPacket> type() {
        return (PacketType<ClientboundAddEntityPacket>) (PacketType<?>) AllPackets.EJECTOR_ITEM_SPAWN;
    }
}
