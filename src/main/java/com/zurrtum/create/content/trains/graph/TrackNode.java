package com.zurrtum.create.content.trains.graph;

import net.minecraft.util.math.Vec3d;

public class TrackNode {

    int netId;
    Vec3d normal;
    TrackNodeLocation location;

    public TrackNode(TrackNodeLocation location, int netId, Vec3d normal) {
        this.location = location;
        this.netId = netId;
        this.normal = normal;
    }

    public TrackNodeLocation getLocation() {
        return location;
    }

    public int getNetId() {
        return netId;
    }

    public Vec3d getNormal() {
        return normal;
    }

}
