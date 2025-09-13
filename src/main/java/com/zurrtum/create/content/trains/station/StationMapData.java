package com.zurrtum.create.content.trains.station;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

import java.util.Map;

public interface StationMapData {

    boolean create$toggleStation(WorldAccess level, BlockPos pos, StationBlockEntity stationBlockEntity);

    void create$addStationMarker(StationMarker marker);

    Map<String, StationMarker> create$getStationMarkers();
}
