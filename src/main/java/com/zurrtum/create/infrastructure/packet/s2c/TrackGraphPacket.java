package com.zurrtum.create.infrastructure.packet.s2c;

import java.util.UUID;

public abstract class TrackGraphPacket implements S2CPacket {
    public UUID graphId;
    public int netId;
    public boolean packetDeletesGraph;
}
