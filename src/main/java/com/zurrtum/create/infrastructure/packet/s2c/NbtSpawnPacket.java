package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllPackets;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;

public class NbtSpawnPacket extends EntitySpawnS2CPacket {
    public static final PacketCodec<RegistryByteBuf, NbtSpawnPacket> CODEC = Packet.createCodec(NbtSpawnPacket::write, NbtSpawnPacket::new);
    private final NbtCompound nbt;

    public NbtSpawnPacket(Entity entity, EntityTrackerEntry entityTrackerEntry, NbtCompound nbt) {
        super(entity, entityTrackerEntry);
        this.nbt = nbt;
    }

    private NbtSpawnPacket(RegistryByteBuf buf) {
        super(buf);
        nbt = buf.readNbt();
    }

    @Override
    public void write(RegistryByteBuf buf) {
        super.write(buf);
        buf.writeNbt(nbt);
    }

    public NbtCompound getNbt() {
        return nbt;
    }

    @Override
    @SuppressWarnings("unchecked")
    public PacketType<EntitySpawnS2CPacket> getPacketType() {
        return (PacketType<EntitySpawnS2CPacket>) (PacketType<?>) AllPackets.NBT_SPAWN;
    }
}
