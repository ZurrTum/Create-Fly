package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import org.apache.logging.log4j.util.TriConsumer;

public record TrainPromptPacket(Text text, boolean shadow) implements S2CPacket {
    public static final PacketCodec<RegistryByteBuf, TrainPromptPacket> CODEC = PacketCodec.tuple(
        TextCodecs.PACKET_CODEC,
        TrainPromptPacket::text,
        PacketCodecs.BOOLEAN,
        TrainPromptPacket::shadow,
        TrainPromptPacket::new
    );

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, TrainPromptPacket> callback() {
        return AllClientHandle::onTrainPrompt;
    }

    @Override
    public PacketType<TrainPromptPacket> getPacketType() {
        return AllPackets.S_TRAIN_PROMPT;
    }
}
