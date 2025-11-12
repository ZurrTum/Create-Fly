package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import org.apache.logging.log4j.util.TriConsumer;

public record TrainPromptPacket(Component text, boolean shadow) implements S2CPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, TrainPromptPacket> CODEC = StreamCodec.composite(
        ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC,
        TrainPromptPacket::text,
        ByteBufCodecs.BOOL,
        TrainPromptPacket::shadow,
        TrainPromptPacket::new
    );

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, TrainPromptPacket> callback() {
        return AllClientHandle::onTrainPrompt;
    }

    @Override
    public PacketType<TrainPromptPacket> type() {
        return AllPackets.S_TRAIN_PROMPT;
    }
}
