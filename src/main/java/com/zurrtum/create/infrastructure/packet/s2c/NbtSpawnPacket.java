package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllPackets;
import com.zurrtum.create.content.contraptions.data.ContraptionSyncLimiting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

public class NbtSpawnPacket extends ClientboundAddEntityPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, NbtSpawnPacket> CODEC = Packet.codec(NbtSpawnPacket::write, NbtSpawnPacket::new);
    @Nullable
    private final CompoundTag nbt;

    public NbtSpawnPacket(Entity entity, ServerEntity entityTrackerEntry, CompoundTag nbt) {
        super(entity, entityTrackerEntry);
        this.nbt = nbt;
    }

    private NbtSpawnPacket(RegistryFriendlyByteBuf buf) {
        super(buf);
        Tag tag = buf.readNbt(NbtAccounter.unlimitedHeap());
        if (tag != null && !(tag instanceof CompoundTag)) {
            nbt = null;
        } else {
            nbt = (CompoundTag) tag;
        }
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        super.write(buf);
        ContraptionSyncLimiting.writeSafe(nbt, buf);
    }

    @Nullable
    public CompoundTag getNbt() {
        return nbt;
    }

    @Override
    @SuppressWarnings("unchecked")
    public PacketType<ClientboundAddEntityPacket> type() {
        return (PacketType<ClientboundAddEntityPacket>) (PacketType<?>) AllPackets.NBT_SPAWN;
    }
}
