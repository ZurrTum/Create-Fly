package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record TrainPromptPacket(Text text, boolean shadow) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, TrainPromptPacket> CODEC = PacketCodec.tuple(
        TextCodecs.PACKET_CODEC,
        TrainPromptPacket::text,
        PacketCodecs.BOOLEAN,
        TrainPromptPacket::shadow,
        TrainPromptPacket::new
    );

    @Override
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onTrainPrompt(this);
    }

    @Override
    public PacketType<TrainPromptPacket> getPacketType() {
        return AllPackets.S_TRAIN_PROMPT;
    }
}
