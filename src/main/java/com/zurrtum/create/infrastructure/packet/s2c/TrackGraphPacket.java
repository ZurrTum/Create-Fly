package com.zurrtum.create.infrastructure.packet.s2c;

import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;

import java.util.UUID;

public abstract class TrackGraphPacket implements Packet<ClientPlayPacketListener> {
    public UUID graphId;
    public int netId;
    public boolean packetDeletesGraph;
}
