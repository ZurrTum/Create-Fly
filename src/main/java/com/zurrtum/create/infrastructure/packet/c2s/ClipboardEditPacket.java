package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.infrastructure.component.ClipboardContent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.Nullable;

public record ClipboardEditPacket(
    int hotbarSlot, @Nullable ClipboardContent clipboardContent, @Nullable BlockPos targetedBlock
) implements Packet<ServerGamePacketListener> {
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
    public void handle(ServerGamePacketListener listener) {
        AllHandle.onClipboardEdit((ServerGamePacketListenerImpl) listener, this);
    }

    @Override
    public PacketType<ClipboardEditPacket> type() {
        return AllPackets.CLIPBOARD_EDIT;
    }
}
