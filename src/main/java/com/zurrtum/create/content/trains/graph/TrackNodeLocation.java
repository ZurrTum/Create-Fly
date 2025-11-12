package com.zurrtum.create.content.trains.graph;

import com.mojang.serialization.*;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackMaterial;
import io.netty.buffer.ByteBuf;

import java.util.*;
import java.util.stream.IntStream;

import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class TrackNodeLocation extends Vec3i {
    private static final Codec<TrackNodeLocation> POS_CODEC = Codec.INT_STREAM.comapFlatMap(
        stream -> Util.fixedSize(stream, 3)
            .map(coordinates -> new TrackNodeLocation(coordinates[0], coordinates[1], coordinates[2])),
        vec -> IntStream.of(vec.getX(), vec.getY(), vec.getZ())
    );
    public static final StreamCodec<ByteBuf, TrackNodeLocation> POS_PACKET_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        Vec3i::getX,
        ByteBufCodecs.VAR_INT,
        Vec3i::getY,
        ByteBufCodecs.VAR_INT,
        Vec3i::getZ,
        TrackNodeLocation::new
    );
    public ResourceKey<Level> dimension;
    public int yOffsetPixels;

    private TrackNodeLocation(int x, int y, int z) {
        super(x, y, z);
    }

    public TrackNodeLocation(Vec3 vec) {
        this(vec.x, vec.y, vec.z);
    }

    public TrackNodeLocation(double x, double y, double z) {
        super(Mth.floor((double) Math.round(x * 2)), Mth.floor(y) * 2, Mth.floor((double) Math.round(z * 2)));
    }

    public TrackNodeLocation in(Level level) {
        return in(level.dimension());
    }

    public TrackNodeLocation in(ResourceKey<Level> dimension) {
        this.dimension = dimension;
        return this;
    }

    private static TrackNodeLocation fromPackedPos(BlockPos bufferPos) {
        return new TrackNodeLocation(bufferPos);
    }

    private TrackNodeLocation(BlockPos readBlockPos) {
        super(readBlockPos.getX(), readBlockPos.getY(), readBlockPos.getZ());
    }

    public Vec3 getLocation() {
        return new Vec3(getX() / 2.0, getY() / 2.0 + yOffsetPixels / 16.0, getZ() / 2.0);
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    @Override
    public boolean equals(Object pOther) {
        return equalsIgnoreDim(pOther) && pOther instanceof TrackNodeLocation tnl && Objects.equals(tnl.dimension, dimension);
    }

    public boolean equalsIgnoreDim(Object pOther) {
        return super.equals(pOther) && pOther instanceof TrackNodeLocation tnl && tnl.yOffsetPixels == yOffsetPixels;
    }

    @Override
    public int hashCode() {
        return (getY() + ((getZ() + yOffsetPixels * 31) * 31 + dimension.hashCode()) * 31) * 31 + getX();
    }

    public void write(ValueOutput view, DimensionPalette dimensions) {
        view.store("Pos", POS_CODEC, this);
        if (dimensions != null)
            view.store("D", dimensions, dimension);
        if (yOffsetPixels != 0)
            view.putInt("YO", yOffsetPixels);
    }

    public static <T> DataResult<T> encode(final TrackNodeLocation input, final DynamicOps<T> ops, final T empty, DimensionPalette dimensions) {
        RecordBuilder<T> builder = ops.mapBuilder();
        builder.add("Pos", input, POS_CODEC);
        if (dimensions != null)
            builder.add("D", input.dimension, dimensions);
        if (input.yOffsetPixels != 0)
            builder.add("YO", ops.createInt(input.yOffsetPixels));
        return builder.build(empty);
    }

    public static TrackNodeLocation read(ValueInput view, DimensionPalette dimensions) {
        TrackNodeLocation location = view.read("Pos", POS_CODEC).orElseThrow();
        if (dimensions != null)
            location.dimension = view.read("D", dimensions).orElse(null);
        location.yOffsetPixels = view.getIntOr("YO", 0);
        return location;
    }

    public static <T> TrackNodeLocation decode(final DynamicOps<T> ops, final T input, DimensionPalette dimensions) {
        MapLike<T> map = ops.getMap(input).getOrThrow();
        TrackNodeLocation location = POS_CODEC.decode(ops, map.get("Pos")).getOrThrow().getFirst();
        if (dimensions != null)
            location.dimension = dimensions.parse(ops, map.get("D")).result().orElse(null);
        location.yOffsetPixels = Optional.ofNullable(map.get("YO")).map(value -> ops.getNumberValue(value).getOrThrow().intValue()).orElse(0);
        return location;
    }

    public void send(FriendlyByteBuf buffer, DimensionPalette dimensions) {
        buffer.writeVarInt(getX());
        buffer.writeShort(getY());
        buffer.writeVarInt(getZ());
        buffer.writeVarInt(yOffsetPixels);
        buffer.writeVarInt(dimensions.encode(dimension));
    }

    public static TrackNodeLocation receive(FriendlyByteBuf buffer, DimensionPalette dimensions) {
        TrackNodeLocation location = new TrackNodeLocation(buffer.readVarInt(), buffer.readShort(), buffer.readVarInt());
        location.yOffsetPixels = buffer.readVarInt();
        location.dimension = dimensions.decode(buffer.readVarInt());
        return location;
    }

    public Collection<BlockPos> allAdjacent() {
        Set<BlockPos> set = new HashSet<>();
        Vec3 vec3 = getLocation().subtract(0, yOffsetPixels / 16.0, 0);
        double step = 1 / 8f;
        for (int x : Iterate.positiveAndNegative)
            for (int y : Iterate.positiveAndNegative)
                for (int z : Iterate.positiveAndNegative)
                    set.add(BlockPos.containing(vec3.add(x * step, y * step, z * step)));
        return set;
    }

    public static class DiscoveredLocation extends TrackNodeLocation {

        BezierConnection turn = null;
        boolean forceNode = false;
        Vec3 direction;
        Vec3 normal;
        TrackMaterial materialA;
        TrackMaterial materialB;

        public DiscoveredLocation(Level level, double x, double y, double z) {
            super(x, y, z);
            in(level);
        }

        public DiscoveredLocation(ResourceKey<Level> dimension, Vec3 vec) {
            super(vec);
            in(dimension);
        }

        public DiscoveredLocation(Level level, Vec3 vec) {
            this(level.dimension(), vec);
        }

        public DiscoveredLocation materialA(TrackMaterial material) {
            this.materialA = material;
            return this;
        }

        public DiscoveredLocation materialB(TrackMaterial material) {
            this.materialB = material;
            return this;
        }

        public DiscoveredLocation materials(TrackMaterial materialA, TrackMaterial materialB) {
            this.materialA = materialA;
            this.materialB = materialB;
            return this;
        }

        public DiscoveredLocation viaTurn(BezierConnection turn) {
            this.turn = turn;
            if (turn != null)
                forceNode();
            return this;
        }

        public DiscoveredLocation forceNode() {
            forceNode = true;
            return this;
        }

        public DiscoveredLocation withNormal(Vec3 normal) {
            this.normal = normal;
            return this;
        }

        public DiscoveredLocation withYOffset(int yOffsetPixels) {
            this.yOffsetPixels = yOffsetPixels;
            return this;
        }

        public DiscoveredLocation withDirection(Vec3 direction) {
            this.direction = direction == null ? null : direction.normalize();
            return this;
        }

        public boolean connectedViaTurn() {
            return turn != null;
        }

        public BezierConnection getTurn() {
            return turn;
        }

        public boolean shouldForceNode() {
            return forceNode;
        }

        public boolean differentMaterials() {
            return materialA != materialB;
        }

        public boolean notInLineWith(Vec3 direction) {
            return this.direction != null && Math.max(direction.dot(this.direction), direction.dot(this.direction.scale(-1))) < 7 / 8f;
        }

        public Vec3 getDirection() {
            return direction;
        }

    }

}
