package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.infrastructure.debugInfo.DebugInformation;
import com.zurrtum.create.infrastructure.debugInfo.element.DebugInfoSection;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public record ServerDebugInfoPacket(String serverInfo) implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<ByteBuf, ServerDebugInfoPacket> CODEC = ByteBufCodecs.STRING_UTF8.map(
        ServerDebugInfoPacket::new,
        ServerDebugInfoPacket::serverInfo
    );

    public ServerDebugInfoPacket(Player target) {
        this(printServerInfo(target));
    }

    private static String printServerInfo(Player player) {
        List<DebugInfoSection> sections = DebugInformation.getServerInfo();
        StringBuilder output = new StringBuilder();
        printInfo("Server", player, sections, output);
        return output.toString();
    }

    public static void printInfo(String side, Player player, List<DebugInfoSection> sections, StringBuilder output) {
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
    public void handle(ClientGamePacketListener listener) {
        AllClientHandle.INSTANCE.onServerDebugInfo(this);
    }

    @Override
    public PacketType<ServerDebugInfoPacket> type() {
        return AllPackets.SERVER_DEBUG_INFO;
    }
}
