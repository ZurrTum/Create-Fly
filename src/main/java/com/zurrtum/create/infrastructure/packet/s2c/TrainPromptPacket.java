package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public record TrainPromptPacket(Component text, boolean shadow) implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, TrainPromptPacket> CODEC = StreamCodec.composite(
        ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC,
        TrainPromptPacket::text,
        ByteBufCodecs.BOOL,
        TrainPromptPacket::shadow,
        TrainPromptPacket::new
    );

    @Override
    public void handle(ClientGamePacketListener listener) {
        AllClientHandle.INSTANCE.onTrainPrompt(this);
    }

    @Override
    public PacketType<TrainPromptPacket> type() {
        return AllPackets.S_TRAIN_PROMPT;
    }
}
