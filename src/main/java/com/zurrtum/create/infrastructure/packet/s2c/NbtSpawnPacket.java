package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.contraptions.data.ContraptionSyncLimiting;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;
import org.jetbrains.annotations.Nullable;

public class NbtSpawnPacket extends EntitySpawnS2CPacket {
    public static final PacketCodec<RegistryByteBuf, NbtSpawnPacket> CODEC = Packet.createCodec(NbtSpawnPacket::write, NbtSpawnPacket::new);
    @Nullable
    private final NbtCompound nbt;

    public NbtSpawnPacket(Entity entity, EntityTrackerEntry entityTrackerEntry, NbtCompound nbt) {
        super(entity, entityTrackerEntry);
        this.nbt = nbt;
    }

    private NbtSpawnPacket(RegistryByteBuf buf) {
        super(buf);
        NbtElement tag = buf.readNbt(NbtSizeTracker.ofUnlimitedBytes());
        if (tag != null && !(tag instanceof NbtCompound)) {
            nbt = null;
        } else {
            nbt = (NbtCompound) tag;
        }
    }

    @Override
    public void write(RegistryByteBuf buf) {
        super.write(buf);
        ContraptionSyncLimiting.writeSafe(nbt, buf);
    }

    @Nullable
    public NbtCompound getNbt() {
        return nbt;
    }

    @Override
    @SuppressWarnings("unchecked")
    public PacketType<EntitySpawnS2CPacket> getPacketType() {
        return (PacketType<EntitySpawnS2CPacket>) (PacketType<?>) AllPackets.NBT_SPAWN;
    }
}
