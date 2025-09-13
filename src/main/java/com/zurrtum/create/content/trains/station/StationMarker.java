package com.zurrtum.create.content.trains.station;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllMapDecorationTypes;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.item.map.MapDecoration;
import net.minecraft.item.map.MapState;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class StationMarker {
    public static final Codec<StationMarker> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlockPos.CODEC.fieldOf("source").forGetter(StationMarker::getSource),
        BlockPos.CODEC.fieldOf("target").forGetter(StationMarker::getTarget),
        TextCodecs.CODEC.fieldOf("name").forGetter(StationMarker::getName)
    ).apply(instance, StationMarker::new));
    public static final Codec<List<StationMarker>> LIST_CODEC = CODEC.listOf();

    private final BlockPos source;
    private final BlockPos target;
    private final Text name;
    private final String id;

    public StationMarker(BlockPos source, BlockPos target, Text name) {
        this.source = source;
        this.target = target;
        this.name = name;
        id = "create:station-" + target.getX() + "," + target.getY() + "," + target.getZ();
    }

    public static StationMarker fromWorld(BlockView level, BlockPos pos) {
        Optional<StationBlockEntity> stationOption = level.getBlockEntity(pos, AllBlockEntityTypes.TRACK_STATION);

        if (stationOption.isEmpty() || stationOption.get().getStation() == null)
            return null;

        String name = stationOption.get().getStation().name;
        return new StationMarker(
            pos,
            BlockEntityBehaviour.get(stationOption.get(), TrackTargetingBehaviour.TYPE).getPositionForMapMarker(),
            Text.literal(name)
        );
    }

    public BlockPos getSource() {
        return source;
    }

    public BlockPos getTarget() {
        return target;
    }

    public Text getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        StationMarker that = (StationMarker) o;

        if (!target.equals(that.target))
            return false;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, name);
    }

    public static MapDecoration createStationDecoration(byte x, byte y, Optional<Text> name) {
        return new MapDecoration(AllMapDecorationTypes.STATION_MAP_DECORATION, x, y, (byte) 0, name);
    }

    public record WrapperCodec(Codec<MapState> codec) implements Codec<MapState> {
        private static final String STATION_MARKERS_KEY = "create:stations";
        private static WrapperCodec CODEC;

        public static WrapperCodec get(Codec<MapState> codec) {
            if (CODEC == null) {
                CODEC = new WrapperCodec(codec);
            }
            return CODEC;
        }

        @Override
        public <T> DataResult<Pair<MapState, T>> decode(DynamicOps<T> ops, T input) {
            return codec.decode(ops, input).map(pair -> {
                LIST_CODEC.parse(ops, ops.getMap(input).getOrThrow().get(STATION_MARKERS_KEY))
                    .ifSuccess(list -> list.forEach(((StationMapData) pair.getFirst())::create$addStationMarker));
                return pair;
            });
        }

        @Override
        public <T> DataResult<T> encode(MapState input, DynamicOps<T> ops, T prefix) {
            return codec.encode(input, ops, prefix).flatMap(result -> {
                RecordBuilder<T> map = ops.mapBuilder();
                map.add(STATION_MARKERS_KEY, ((StationMapData) input).create$getStationMarkers().values().stream().toList(), LIST_CODEC);
                return map.build(result);
            });
        }
    }
}
