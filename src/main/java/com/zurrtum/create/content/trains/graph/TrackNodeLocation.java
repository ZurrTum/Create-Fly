package com.zurrtum.create.content.trains.graph;

import com.mojang.serialization.*;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackMaterial;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.IntStream;

public class TrackNodeLocation extends Vec3i {
    private static final Codec<TrackNodeLocation> POS_CODEC = Codec.INT_STREAM.comapFlatMap(
        stream -> Util.decodeFixedLengthArray(stream, 3).map(coordinates -> new TrackNodeLocation(coordinates[0], coordinates[1], coordinates[2])),
        vec -> IntStream.of(vec.getX(), vec.getY(), vec.getZ())
    );
    public static final PacketCodec<ByteBuf, TrackNodeLocation> POS_PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.VAR_INT,
        Vec3i::getX,
        PacketCodecs.VAR_INT,
        Vec3i::getY,
        PacketCodecs.VAR_INT,
        Vec3i::getZ,
        TrackNodeLocation::new
    );
    public RegistryKey<World> dimension;
    public int yOffsetPixels;

    private TrackNodeLocation(int x, int y, int z) {
        super(x, y, z);
    }

    public TrackNodeLocation(Vec3d vec) {
        this(vec.x, vec.y, vec.z);
    }

    public TrackNodeLocation(double x, double y, double z) {
        super(MathHelper.floor(Math.round(x * 2)), MathHelper.floor(y) * 2, MathHelper.floor(Math.round(z * 2)));
    }

    public TrackNodeLocation in(World level) {
        return in(level.getRegistryKey());
    }

    public TrackNodeLocation in(RegistryKey<World> dimension) {
        this.dimension = dimension;
        return this;
    }

    private static TrackNodeLocation fromPackedPos(BlockPos bufferPos) {
        return new TrackNodeLocation(bufferPos);
    }

    private TrackNodeLocation(BlockPos readBlockPos) {
        super(readBlockPos.getX(), readBlockPos.getY(), readBlockPos.getZ());
    }

    public Vec3d getLocation() {
        return new Vec3d(getX() / 2.0, getY() / 2.0 + yOffsetPixels / 16.0, getZ() / 2.0);
    }

    public RegistryKey<World> getDimension() {
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

    public void write(WriteView view, DimensionPalette dimensions) {
        view.put("Pos", POS_CODEC, this);
        if (dimensions != null)
            view.put("D", dimensions, dimension);
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

    public static TrackNodeLocation read(ReadView view, DimensionPalette dimensions) {
        TrackNodeLocation location = view.read("Pos", POS_CODEC).orElseThrow();
        if (dimensions != null)
            location.dimension = view.read("D", dimensions).orElse(null);
        location.yOffsetPixels = view.getInt("YO", 0);
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

    public void send(PacketByteBuf buffer, DimensionPalette dimensions) {
        buffer.writeVarInt(getX());
        buffer.writeShort(getY());
        buffer.writeVarInt(getZ());
        buffer.writeVarInt(yOffsetPixels);
        buffer.writeVarInt(dimensions.encode(dimension));
    }

    public static TrackNodeLocation receive(PacketByteBuf buffer, DimensionPalette dimensions) {
        TrackNodeLocation location = new TrackNodeLocation(buffer.readVarInt(), buffer.readShort(), buffer.readVarInt());
        location.yOffsetPixels = buffer.readVarInt();
        location.dimension = dimensions.decode(buffer.readVarInt());
        return location;
    }

    public Collection<BlockPos> allAdjacent() {
        Set<BlockPos> set = new HashSet<>();
        Vec3d vec3 = getLocation().subtract(0, yOffsetPixels / 16.0, 0);
        double step = 1 / 8f;
        for (int x : Iterate.positiveAndNegative)
            for (int y : Iterate.positiveAndNegative)
                for (int z : Iterate.positiveAndNegative)
                    set.add(BlockPos.ofFloored(vec3.add(x * step, y * step, z * step)));
        return set;
    }

    public static class DiscoveredLocation extends TrackNodeLocation {

        BezierConnection turn = null;
        boolean forceNode = false;
        Vec3d direction;
        Vec3d normal;
        TrackMaterial materialA;
        TrackMaterial materialB;

        public DiscoveredLocation(World level, double x, double y, double z) {
            super(x, y, z);
            in(level);
        }

        public DiscoveredLocation(RegistryKey<World> dimension, Vec3d vec) {
            super(vec);
            in(dimension);
        }

        public DiscoveredLocation(World level, Vec3d vec) {
            this(level.getRegistryKey(), vec);
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

        public DiscoveredLocation withNormal(Vec3d normal) {
            this.normal = normal;
            return this;
        }

        public DiscoveredLocation withYOffset(int yOffsetPixels) {
            this.yOffsetPixels = yOffsetPixels;
            return this;
        }

        public DiscoveredLocation withDirection(Vec3d direction) {
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

        public boolean notInLineWith(Vec3d direction) {
            return this.direction != null && Math.max(
                direction.dotProduct(this.direction),
                direction.dotProduct(this.direction.multiply(-1))
            ) < 7 / 8f;
        }

        public Vec3d getDirection() {
            return direction;
        }

    }

}
