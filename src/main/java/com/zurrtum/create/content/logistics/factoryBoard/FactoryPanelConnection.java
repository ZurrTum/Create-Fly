package com.zurrtum.create.content.logistics.factoryBoard;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class FactoryPanelConnection {
    public static final Codec<FactoryPanelConnection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        FactoryPanelPosition.CODEC.fieldOf(
            "position").forGetter(i -> i.from),
        Codec.INT.fieldOf("amount").forGetter(i -> i.amount),
        Codec.INT.fieldOf("arrow_bending").forGetter(i -> i.arrowBendMode)
    ).apply(instance, FactoryPanelConnection::new));

    public FactoryPanelPosition from;
    public int amount;
    public List<Direction> path;
    public int arrowBendMode;
    public boolean success;

    public WeakReference<Object> cachedSource;

    private int arrowBendModeCurrentPathUses;

    public FactoryPanelConnection(FactoryPanelPosition from, int amount) {
        this(from, amount, -1);
    }

    public FactoryPanelConnection(FactoryPanelPosition from, int amount, int arrowBendMode) {
        this.from = from;
        this.amount = amount;
        this.arrowBendMode = arrowBendMode;
        path = new ArrayList<>();
        success = true;
        arrowBendModeCurrentPathUses = 0;
        cachedSource = new WeakReference<>(null);
    }

    public List<Direction> getPath(World level, BlockState state, FactoryPanelPosition to, Vec3d start) {
        if (!path.isEmpty() && arrowBendModeCurrentPathUses == arrowBendMode)
            return path;

        boolean findSuitable = arrowBendMode == -1;
        arrowBendModeCurrentPathUses = arrowBendMode;

        final Vec3d diff = calculatePathDiff(state, to);
        final float xRot = MathHelper.DEGREES_PER_RADIAN * FactoryPanelBlock.getXRot(state);
        final float yRot = MathHelper.DEGREES_PER_RADIAN * FactoryPanelBlock.getYRot(state);

        // When mode is not locked, find one that doesnt intersect with other gauges
        ModeFinder:
        for (int actualMode = 0; actualMode <= 4; actualMode++) {
            path.clear();
            if (!findSuitable && actualMode != arrowBendMode)
                continue;
            boolean desperateOption = actualMode == 4;

            BlockPos toTravelFirst = BlockPos.ORIGIN;
            BlockPos toTravelLast = BlockPos.ofFloored(diff.multiply(2).add(0.1, 0.1, 0.1));

            if (actualMode > 1) {
                boolean flipX = diff.x > 0 ^ (actualMode % 2 == 1);
                boolean flipZ = diff.z > 0 ^ (actualMode % 2 == 0);
                int ceilX = MathHelper.ceilDiv(toTravelLast.getX(), 2);
                int ceilZ = MathHelper.ceilDiv(toTravelLast.getZ(), 2);
                int floorZ = MathHelper.floorDiv(toTravelLast.getZ(), 2);
                int floorX = MathHelper.floorDiv(toTravelLast.getX(), 2);
                toTravelFirst = new BlockPos(flipX ? floorX : ceilX, 0, flipZ ? floorZ : ceilZ);
                toTravelLast = new BlockPos(!flipX ? floorX : ceilX, 0, !flipZ ? floorZ : ceilZ);
            }

            Direction lastDirection = null;
            Direction currentDirection = null;

            for (BlockPos toTravel : List.of(toTravelFirst, toTravelLast)) {
                boolean zIsFarther = Math.abs(toTravel.getZ()) > Math.abs(toTravel.getX());
                boolean zIsPreferred = desperateOption ? zIsFarther : actualMode % 2 == 1;
                List<Direction> directionOrder = zIsPreferred ? List.of(Direction.SOUTH, Direction.NORTH, Direction.WEST, Direction.EAST) : List.of(Direction.WEST,
                    Direction.EAST,
                    Direction.SOUTH,
                    Direction.NORTH
                );

                for (int i = 0; i < 100; i++) {
                    if (toTravel.equals(BlockPos.ORIGIN))
                        break;

                    for (Direction d : directionOrder) {
                        if (lastDirection != null && d == lastDirection.getOpposite())
                            continue;
                        if (currentDirection == null || toTravel.offset(d).getManhattanDistance(BlockPos.ORIGIN) < toTravel.offset(currentDirection)
                            .getManhattanDistance(BlockPos.ORIGIN))
                            currentDirection = d;
                    }

                    lastDirection = currentDirection;
                    toTravel = toTravel.offset(currentDirection);
                    path.add(currentDirection);
                }
            }

            if (findSuitable && !desperateOption) {
                BlockPos travelled = BlockPos.ORIGIN;
                for (int i = 0; i < path.size() - 1; i++) {
                    Direction d = path.get(i);
                    travelled = travelled.offset(d);
                    Vec3d testOffset = Vec3d.of(travelled).multiply(0.5);
                    testOffset = VecHelper.rotate(testOffset, 180, Axis.Y);
                    testOffset = VecHelper.rotate(testOffset, xRot + 90, Axis.X);
                    testOffset = VecHelper.rotate(testOffset, yRot, Axis.Y);
                    Vec3d v = start.add(testOffset);
                    if (!isSpaceEmpty(level, new Box(v, v).expand(1 / 128f)))
                        continue ModeFinder;
                }
            }

            break;
        }

        return path;
    }

    private static boolean isSpaceEmpty(World world, Box box) {
        for (VoxelShape voxelShape : world.getBlockCollisions(null, box)) {
            if (!voxelShape.isEmpty()) {
                return false;
            }
        }
        return world.getEntityCollisions(null, box).isEmpty();
    }

    public Vec3d calculatePathDiff(BlockState state, FactoryPanelPosition to) {
        float xRot = MathHelper.DEGREES_PER_RADIAN * FactoryPanelBlock.getXRot(state);
        float yRot = MathHelper.DEGREES_PER_RADIAN * FactoryPanelBlock.getYRot(state);
        int slotDiffx = to.slot().xOffset - from.slot().xOffset;
        int slotDiffY = to.slot().yOffset - from.slot().yOffset;

        Vec3d diff = Vec3d.of(to.pos().subtract(from.pos()));
        diff = VecHelper.rotate(diff, -yRot, Axis.Y);
        diff = VecHelper.rotate(diff, -xRot - 90, Axis.X);
        diff = VecHelper.rotate(diff, -180, Axis.Y);
        diff = diff.add(slotDiffx * .5, 0, slotDiffY * .5);
        diff = diff.multiply(1, 0, 1);
        return diff;
    }

}
