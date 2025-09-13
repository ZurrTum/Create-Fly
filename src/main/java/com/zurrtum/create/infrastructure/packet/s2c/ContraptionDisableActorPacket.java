package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import org.apache.logging.log4j.util.TriConsumer;

public record ContraptionDisableActorPacket(int entityId, ItemStack filter, boolean enable) implements S2CPacket {
    public static final PacketCodec<RegistryByteBuf, ContraptionDisableActorPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER,
        ContraptionDisableActorPacket::entityId,
        ItemStack.OPTIONAL_PACKET_CODEC,
        ContraptionDisableActorPacket::filter,
        PacketCodecs.BOOLEAN,
        ContraptionDisableActorPacket::enable,
        ContraptionDisableActorPacket::new
    );

    @Override
    public boolean runInMain() {
        return true;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, ContraptionDisableActorPacket> callback() {
        return AllClientHandle::onContraptionDisableActor;
    }

    @Override
    public PacketType<ContraptionDisableActorPacket> getPacketType() {
        return AllPackets.CONTRAPTION_ACTOR_TOGGLE;
    }
}
