package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.equipment.symmetryWand.mirror.CrossPlaneMirror;
import com.zurrtum.create.content.equipment.symmetryWand.mirror.EmptyMirror;
import com.zurrtum.create.content.equipment.symmetryWand.mirror.PlaneMirror;
import com.zurrtum.create.content.equipment.symmetryWand.mirror.TriplePlaneMirror;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public abstract class SymmetryMirror {
    public static final String EMPTY = "empty";
    public static final String PLANE = "plane";
    public static final String CROSS_PLANE = "cross_plane";
    public static final String TRIPLE_PLANE = "triple_plane";

    public static final Codec<SymmetryMirror> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.INT.fieldOf("orientation_index").forGetter(SymmetryMirror::getOrientationIndex),
        Vec3d.CODEC.fieldOf("position").forGetter(SymmetryMirror::getPosition),
        Codec.STRING.fieldOf("type").forGetter(SymmetryMirror::typeName),
        Codec.BOOL.fieldOf("enable").forGetter(m -> m.enable)
    ).apply(i, SymmetryMirror::create));

    public static final PacketCodec<PacketByteBuf, SymmetryMirror> STREAM_CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER,
        SymmetryMirror::getOrientationIndex,
        Vec3d.PACKET_CODEC,
        SymmetryMirror::getPosition,
        PacketCodecs.STRING,
        SymmetryMirror::typeName,
        PacketCodecs.BOOLEAN,
        m -> m.enable,
        SymmetryMirror::create
    );

    protected Vec3d position;
    public StringIdentifiable orientation;
    protected int orientationIndex;
    public boolean enable;

    public SymmetryMirror(Vec3d pos) {
        position = pos;
        enable = true;
        orientationIndex = 0;
    }

    private static SymmetryMirror create(Integer orientationIndex, Vec3d position, String type, Boolean enable) {
        SymmetryMirror element = switch (type) {
            case PLANE -> new PlaneMirror(position);
            case CROSS_PLANE -> new CrossPlaneMirror(position);
            case TRIPLE_PLANE -> new TriplePlaneMirror(position);
            default -> new EmptyMirror(position);
        };

        element.setOrientation(orientationIndex);
        element.enable = enable;

        return element;
    }

    public StringIdentifiable getOrientation() {
        return orientation;
    }

    public Vec3d getPosition() {
        return position;
    }

    public int getOrientationIndex() {
        return orientationIndex;
    }

    public void rotate(boolean forward) {
        orientationIndex += forward ? 1 : -1;
        setOrientation();
    }

    public void process(Map<BlockPos, Pair<Direction, BlockState>> blocks) {
        Map<BlockPos, Pair<Direction, BlockState>> result = new HashMap<>();
        for (BlockPos pos : blocks.keySet()) {
            result.putAll(process(pos, blocks.get(pos)));
        }
        blocks.putAll(result);
    }

    public void process(Set<BlockPos> positions) {
        Set<BlockPos> result = new HashSet<>();
        for (BlockPos pos : positions) {
            result.addAll(process(pos));
        }
        positions.addAll(result);
    }

    public abstract Map<BlockPos, Pair<Direction, BlockState>> process(BlockPos position, Pair<Direction, BlockState> block);

    public abstract Set<BlockPos> process(BlockPos position);

    protected abstract void setOrientation();

    public abstract void setOrientation(int index);

    public abstract String typeName();

    protected Vec3d getDiff(BlockPos position) {
        return this.position.multiply(-1).add(position.getX(), position.getY(), position.getZ());
    }

    protected BlockPos getIDiff(BlockPos position) {
        Vec3d diff = getDiff(position);
        return new BlockPos((int) diff.x, (int) diff.y, (int) diff.z);
    }

    protected BlockState flipX(BlockState in) {
        return in.mirror(BlockMirror.FRONT_BACK);
    }

    protected BlockState flipZ(BlockState in) {
        return in.mirror(BlockMirror.LEFT_RIGHT);
    }

    @SuppressWarnings("deprecation")
    protected BlockState flipD1(BlockState in) {
        return in.rotate(BlockRotation.COUNTERCLOCKWISE_90).mirror(BlockMirror.FRONT_BACK);
    }

    @SuppressWarnings("deprecation")
    protected BlockState flipD2(BlockState in) {
        return in.rotate(BlockRotation.COUNTERCLOCKWISE_90).mirror(BlockMirror.LEFT_RIGHT);
    }

    protected BlockPos flipX(BlockPos position) {
        BlockPos diff = getIDiff(position);
        return new BlockPos(position.getX() - 2 * diff.getX(), position.getY(), position.getZ());
    }

    protected BlockPos flipY(BlockPos position) {
        BlockPos diff = getIDiff(position);
        return new BlockPos(position.getX(), position.getY() - 2 * diff.getY(), position.getZ());
    }

    protected BlockPos flipZ(BlockPos position) {
        BlockPos diff = getIDiff(position);
        return new BlockPos(position.getX(), position.getY(), position.getZ() - 2 * diff.getZ());
    }

    protected BlockPos flipD2(BlockPos position) {
        BlockPos diff = getIDiff(position);
        return new BlockPos(position.getX() - diff.getX() + diff.getZ(), position.getY(), position.getZ() - diff.getZ() + diff.getX());
    }

    protected BlockPos flipD1(BlockPos position) {
        BlockPos diff = getIDiff(position);
        return new BlockPos(position.getX() - diff.getX() - diff.getZ(), position.getY(), position.getZ() - diff.getZ() - diff.getX());
    }

    protected Direction flipZ(Direction side) {
        return switch (side) {
            case UP, DOWN, EAST, WEST -> side;
            case NORTH -> Direction.SOUTH;
            case SOUTH -> Direction.NORTH;
        };
    }

    protected Direction flipX(Direction side) {
        return switch (side) {
            case UP, DOWN, NORTH, SOUTH -> side;
            case EAST -> Direction.WEST;
            case WEST -> Direction.EAST;
        };
    }

    protected Direction flipXZ(Direction side) {
        return switch (side) {
            case UP, DOWN -> side;
            case NORTH -> Direction.SOUTH;
            case SOUTH -> Direction.NORTH;
            case EAST -> Direction.WEST;
            case WEST -> Direction.EAST;
        };
    }

    protected Direction flipD1(Direction side) {
        return switch (side) {
            case UP, DOWN -> side;
            case NORTH -> Direction.EAST;
            case EAST -> Direction.NORTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.SOUTH;
        };
    }

    protected Direction flipD2(Direction side) {
        return switch (side) {
            case UP, DOWN -> side;
            case NORTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
            case SOUTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
        };
    }

    protected Direction flipD1D2(Direction side) {
        return switch (side) {
            case UP, DOWN -> side;
            case NORTH -> Direction.SOUTH;
            case SOUTH -> Direction.NORTH;
            case EAST -> Direction.WEST;
            case WEST -> Direction.EAST;
        };
    }

    protected Direction flipD1X(Direction side) {
        return switch (side) {
            case UP, DOWN -> side;
            case NORTH, EAST, SOUTH, WEST -> side.rotateYClockwise();
        };
    }

    protected Direction flipD1Z(Direction side) {
        return switch (side) {
            case UP, DOWN -> side;
            case NORTH, EAST, SOUTH, WEST -> side.rotateYCounterclockwise();
        };
    }

    protected Direction flipD1XZ(Direction side) {
        return switch (side) {
            case UP, DOWN -> side;
            case NORTH -> Direction.WEST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.EAST;
            case WEST -> Direction.NORTH;
        };
    }

    public void setPosition(Vec3d pos3d) {
        this.position = pos3d;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SymmetryMirror that))
            return false;

        return getOrientationIndex() == that.getOrientationIndex() && enable == that.enable && Objects.equals(
            getPosition(),
            that.getPosition()
        ) && Objects.equals(getOrientation(), that.getOrientation());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getPosition());
        result = 31 * result + Objects.hashCode(getOrientation());
        result = 31 * result + getOrientationIndex();
        result = 31 * result + Boolean.hashCode(enable);
        return result;
    }
}
