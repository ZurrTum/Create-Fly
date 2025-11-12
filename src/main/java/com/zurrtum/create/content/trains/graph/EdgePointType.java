package com.zurrtum.create.content.trains.graph;

import com.mojang.serialization.Codec;
import com.zurrtum.create.content.trains.observer.TrackObserver;
import com.zurrtum.create.content.trains.signal.SignalBoundary;
import com.zurrtum.create.content.trains.signal.TrackEdgePoint;
import com.zurrtum.create.content.trains.station.GlobalStation;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import static com.zurrtum.create.Create.MOD_ID;

public class EdgePointType<T extends TrackEdgePoint> {
    public static final Map<ResourceLocation, EdgePointType<?>> TYPES = new HashMap<>();
    public static final Codec<EdgePointType<?>> CODEC = ResourceLocation.CODEC.xmap(TYPES::get, EdgePointType::getId);
    private final ResourceLocation id;
    private final Supplier<T> factory;

    public static final EdgePointType<SignalBoundary> SIGNAL = register(ResourceLocation.fromNamespaceAndPath(MOD_ID, "signal"), SignalBoundary::new);
    public static final EdgePointType<GlobalStation> STATION = register(ResourceLocation.fromNamespaceAndPath(MOD_ID, "station"), GlobalStation::new);
    public static final EdgePointType<TrackObserver> OBSERVER = register(ResourceLocation.fromNamespaceAndPath(MOD_ID, "observer"), TrackObserver::new);

    public static <T extends TrackEdgePoint> EdgePointType<T> register(ResourceLocation id, Supplier<T> factory) {
        EdgePointType<T> type = new EdgePointType<>(id, factory);
        TYPES.put(id, type);
        return type;
    }

    public EdgePointType(ResourceLocation id, Supplier<T> factory) {
        this.id = id;
        this.factory = factory;
    }

    public T create() {
        T t = factory.get();
        t.setType(this);
        return t;
    }

    public ResourceLocation getId() {
        return id;
    }

    public static TrackEdgePoint read(FriendlyByteBuf buffer, DimensionPalette dimensions) {
        ResourceLocation type = buffer.readResourceLocation();
        EdgePointType<?> edgePointType = TYPES.get(type);
        TrackEdgePoint point = edgePointType.create();
        point.read(buffer, dimensions);
        return point;
    }

}
