package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.infrastructure.debugInfo.DebugInformation;
import com.zurrtum.create.infrastructure.debugInfo.element.DebugInfoSection;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;

import java.util.List;

public record ServerDebugInfoPacket(String serverInfo) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<ByteBuf, ServerDebugInfoPacket> CODEC = PacketCodecs.STRING.xmap(
        ServerDebugInfoPacket::new,
        ServerDebugInfoPacket::serverInfo
    );

    public ServerDebugInfoPacket(PlayerEntity target) {
        this(printServerInfo(target));
    }

    private static String printServerInfo(PlayerEntity player) {
        List<DebugInfoSection> sections = DebugInformation.getServerInfo();
        StringBuilder output = new StringBuilder();
        printInfo("Server", player, sections, output);
        return output.toString();
    }

    public static void printInfo(String side, PlayerEntity player, List<DebugInfoSection> sections, StringBuilder output) {
        output.append("<details>");
        output.append('\n');
        output.append("<summary>").append(side).append(" Info").append("</summary>");
        output.append('\n').append('\n');
        output.append("```");
        output.append('\n');

        for (int i = 0; i < sections.size(); i++) {
            if (i != 0) {
                output.append('\n');
            }
            sections.get(i).print(player, line -> output.append(line).append('\n'));
        }

        output.append("```");
        output.append('\n').append('\n');
        output.append("</details>");
        output.append('\n');
    }

    @Override
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onServerDebugInfo(this);
    }

    @Override
    public PacketType<ServerDebugInfoPacket> getPacketType() {
        return AllPackets.SERVER_DEBUG_INFO;
    }
}
