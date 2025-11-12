package com.zurrtum.create.content.trains.station;

import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

public interface StationMapData {

    boolean create$toggleStation(LevelAccessor level, BlockPos pos, StationBlockEntity stationBlockEntity);

    void create$addStationMarker(StationMarker marker);

    Map<String, StationMarker> create$getStationMarkers();
}
