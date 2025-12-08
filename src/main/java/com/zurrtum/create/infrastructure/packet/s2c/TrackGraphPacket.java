package com.zurrtum.create.infrastructure.packet.s2c;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

import java.util.UUID;

public abstract class TrackGraphPacket implements Packet<ClientGamePacketListener> {
    public UUID graphId;
    public int netId;
    public boolean packetDeletesGraph;
}
