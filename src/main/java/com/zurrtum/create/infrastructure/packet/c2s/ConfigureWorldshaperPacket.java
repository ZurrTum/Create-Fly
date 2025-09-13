package com.zurrtum.create.infrastructure.packet.c2s;

import com.zurrtum.create.AllHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipLargerStreamCodecs;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import com.zurrtum.create.content.equipment.zapper.ConfigureZapperPacket;
import com.zurrtum.create.content.equipment.zapper.terrainzapper.WorldshaperItem;
import com.zurrtum.create.infrastructure.component.PlacementOptions;
import com.zurrtum.create.infrastructure.component.PlacementPatterns;
import com.zurrtum.create.infrastructure.component.TerrainBrushes;
import com.zurrtum.create.infrastructure.component.TerrainTools;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Hand;

import java.util.function.BiConsumer;

public record ConfigureWorldshaperPacket(
    Hand hand, PlacementPatterns pattern, TerrainBrushes brush, int brushParamX, int brushParamY, int brushParamZ, TerrainTools tool,
    PlacementOptions placement
) implements ConfigureZapperPacket {
    public static final PacketCodec<ByteBuf, ConfigureWorldshaperPacket> CODEC = CatnipLargerStreamCodecs.composite(
        CatnipStreamCodecs.HAND,
        packet -> packet.hand,
        PlacementPatterns.STREAM_CODEC,
        packet -> packet.pattern,
        TerrainBrushes.STREAM_CODEC,
        packet -> packet.brush,
        PacketCodecs.VAR_INT,
        packet -> packet.brushParamX,
        PacketCodecs.VAR_INT,
        packet -> packet.brushParamY,
        PacketCodecs.VAR_INT,
        packet -> packet.brushParamZ,
        TerrainTools.STREAM_CODEC,
        packet -> packet.tool,
        PlacementOptions.STREAM_CODEC,
        packet -> packet.placement,
        ConfigureWorldshaperPacket::new
    );

    @Override
    public void configureZapper(ItemStack stack) {
        WorldshaperItem.configureSettings(stack, pattern, brush, brushParamX, brushParamY, brushParamZ, tool, placement);
    }

    @Override
    public PacketType<ConfigureWorldshaperPacket> getPacketType() {
        return AllPackets.CONFIGURE_WORLDSHAPER;
    }

    @Override
    public BiConsumer<ServerPlayNetworkHandler, ConfigureWorldshaperPacket> callback() {
        return AllHandle::onConfigureWorldshaper;
    }
}
