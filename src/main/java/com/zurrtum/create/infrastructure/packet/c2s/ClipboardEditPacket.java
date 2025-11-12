package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.infrastructure.component.ClipboardContent;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record ClipboardEditPacket(
    int hotbarSlot, @Nullable ClipboardContent clipboardContent, @Nullable BlockPos targetedBlock
) implements C2SPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClipboardEditPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        ClipboardEditPacket::hotbarSlot,
        CatnipStreamCodecBuilders.nullable(ClipboardContent.STREAM_CODEC),
        ClipboardEditPacket::clipboardContent,
        CatnipStreamCodecBuilders.nullable(BlockPos.STREAM_CODEC),
        ClipboardEditPacket::targetedBlock,
        ClipboardEditPacket::new
    );

    @Override
    public PacketType<ClipboardEditPacket> type() {
        return AllPackets.CLIPBOARD_EDIT;
    }

    @Override
    public BiConsumer<ServerGamePacketListenerImpl, ClipboardEditPacket> callback() {
        return AllHandle::onClipboardEdit;
    }
}
