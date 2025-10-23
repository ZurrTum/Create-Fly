package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.infrastructure.component.ClipboardContent;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public record ClipboardEditPacket(
    int hotbarSlot, @Nullable ClipboardContent clipboardContent, @Nullable BlockPos targetedBlock
) implements C2SPacket {
    public static final PacketCodec<RegistryByteBuf, ClipboardEditPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.VAR_INT,
        ClipboardEditPacket::hotbarSlot,
        CatnipStreamCodecBuilders.nullable(ClipboardContent.STREAM_CODEC),
        ClipboardEditPacket::clipboardContent,
        CatnipStreamCodecBuilders.nullable(BlockPos.PACKET_CODEC),
        ClipboardEditPacket::targetedBlock,
        ClipboardEditPacket::new
    );

    @Override
    public PacketType<ClipboardEditPacket> getPacketType() {
        return AllPackets.CLIPBOARD_EDIT;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, ClipboardEditPacket> callback() {
        return AllHandle::onClipboardEdit;
    }
}
