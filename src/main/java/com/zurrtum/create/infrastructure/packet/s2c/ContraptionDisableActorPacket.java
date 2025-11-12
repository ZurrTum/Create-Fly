package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.util.TriConsumer;

public record ContraptionDisableActorPacket(int entityId, ItemStack filter, boolean enable) implements S2CPacket {
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
    public boolean runInMain() {
        return true;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, ContraptionDisableActorPacket> callback() {
        return AllClientHandle::onContraptionDisableActor;
    }

    @Override
    public PacketType<ContraptionDisableActorPacket> type() {
        return AllPackets.CONTRAPTION_ACTOR_TOGGLE;
    }
}
