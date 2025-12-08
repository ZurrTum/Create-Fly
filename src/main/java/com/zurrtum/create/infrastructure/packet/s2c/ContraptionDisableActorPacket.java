package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.item.ItemStack;

public record ContraptionDisableActorPacket(int entityId, ItemStack filter, boolean enable) implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ContraptionDisableActorPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        ContraptionDisableActorPacket::entityId,
        ItemStack.OPTIONAL_STREAM_CODEC,
        ContraptionDisableActorPacket::filter,
        ByteBufCodecs.BOOL,
        ContraptionDisableActorPacket::enable,
        ContraptionDisableActorPacket::new
    );

    @Override
    public void handle(ClientGamePacketListener listener) {
        AllClientHandle.INSTANCE.onContraptionDisableActor(listener, this);
    }

    @Override
    public PacketType<ContraptionDisableActorPacket> type() {
        return AllPackets.CONTRAPTION_ACTOR_TOGGLE;
    }
}
