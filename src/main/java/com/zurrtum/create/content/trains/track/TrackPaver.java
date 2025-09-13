package com.zurrtum.create.content.trains.track;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.decoration.girder.GirderBlock;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TrackPaver {

    public static int paveStraight(
        World level,
        BlockPos startPos,
        Vec3d direction,
        int extent,
        Block block,
        boolean simulate,
        Set<BlockPos> visited
    ) {
        int itemsNeeded = 0;

        BlockState defaultBlockState = block.getDefaultState();
        boolean slabLike = defaultBlockState.contains(SlabBlock.TYPE);
        boolean wallLike = isWallLike(defaultBlockState);

        if (slabLike)
            defaultBlockState = defaultBlockState.with(SlabBlock.TYPE, SlabType.DOUBLE);

        if (defaultBlockState.getBlock() instanceof GirderBlock)
            for (Direction d : Iterate.horizontalDirections)
                if (Vec3d.of(d.getVector()).equals(direction))
                    defaultBlockState = defaultBlockState.with(GirderBlock.TOP, false).with(GirderBlock.BOTTOM, false)
                        .with(GirderBlock.AXIS, d.getAxis()).with(d.getAxis() == Axis.X ? GirderBlock.X : GirderBlock.Z, true);

        Set<BlockPos> toPlaceOn = new HashSet<>();
        Vec3d start = VecHelper.getCenterOf(startPos);
        Vec3d mainNormal = direction.crossProduct(new Vec3d(0, 1, 0));
        Vec3d normalizedNormal = mainNormal.normalize();
        Vec3d normalizedDirection = direction.normalize();

        float diagFiller = 0.45f;
        for (int i = 0; i < extent; i++) {
            Vec3d offset = direction.multiply(i);
            Vec3d mainPos = start.add(offset.x, offset.y, offset.z);
            toPlaceOn.add(BlockPos.ofFloored(mainPos.add(mainNormal)));
            toPlaceOn.add(BlockPos.ofFloored(mainPos.subtract(mainNormal)));
            if (wallLike)
                continue;
            toPlaceOn.add(BlockPos.ofFloored(mainPos));
            if (i < extent - 1)
                for (int x : Iterate.positiveAndNegative)
                    toPlaceOn.add(BlockPos.ofFloored(mainPos.add(normalizedNormal.multiply(x * diagFiller))
                        .add(normalizedDirection.multiply(diagFiller))));
            if (i > 0)
                for (int x : Iterate.positiveAndNegative)
                    toPlaceOn.add(BlockPos.ofFloored(mainPos.add(normalizedNormal.multiply(x * diagFiller))
                        .add(normalizedDirection.multiply(-diagFiller))));
        }

        final BlockState state = defaultBlockState;
        for (BlockPos p : toPlaceOn) {
            if (!visited.add(p))
                continue;
            if (placeBlockIfFree(level, p, state, simulate))
                itemsNeeded += slabLike ? 2 : 1;
        }
        visited.addAll(toPlaceOn);

        return itemsNeeded;
    }

    public static int paveCurve(World level, BezierConnection bc, Block block, boolean simulate, Set<BlockPos> visited) {
        int itemsNeeded = 0;

        BlockState defaultBlockState = block.getDefaultState();
        boolean slabLike = defaultBlockState.contains(SlabBlock.TYPE);
        if (slabLike)
            defaultBlockState = defaultBlockState.with(SlabBlock.TYPE, SlabType.DOUBLE);
        if (isWallLike(defaultBlockState)) {
            if (defaultBlockState.isOf(AllBlocks.METAL_GIRDER))
                return ((bc.getSegmentCount() + 1) / 2) * 2;
            return 0;
        }

        Map<Pair<Integer, Integer>, Double> yLevels = new HashMap<>();
        BlockPos tePosition = bc.bePositions.getFirst();
        Vec3d end1 = bc.starts.getFirst().subtract(Vec3d.of(tePosition)).add(0, 3 / 16f, 0);
        Vec3d end2 = bc.starts.getSecond().subtract(Vec3d.of(tePosition)).add(0, 3 / 16f, 0);
        Vec3d axis1 = bc.axes.getFirst();
        Vec3d axis2 = bc.axes.getSecond();

        double handleLength = bc.getHandleLength();

        Vec3d finish1 = axis1.multiply(handleLength).add(end1);
        Vec3d finish2 = axis2.multiply(handleLength).add(end2);

        Vec3d faceNormal1 = bc.normals.getFirst();
        Vec3d faceNormal2 = bc.normals.getSecond();

        int segCount = bc.getSegmentCount();
        float[] lut = bc.getStepLUT();

        for (int i = 0; i < segCount; i++) {
            float t = i == segCount ? 1 : i * lut[i] / segCount;
            t += 0.5f / segCount;

            Vec3d result = VecHelper.bezier(end1, end2, finish1, finish2, t);
            Vec3d derivative = VecHelper.bezierDerivative(end1, end2, finish1, finish2, t).normalize();
            Vec3d faceNormal = faceNormal1.equals(faceNormal2) ? faceNormal1 : VecHelper.slerp(t, faceNormal1, faceNormal2);
            Vec3d normal = faceNormal.crossProduct(derivative).normalize();
            Vec3d below = result.add(faceNormal.multiply(-1.125f));
            Vec3d rail1 = below.add(normal.multiply(.97f));
            Vec3d rail2 = below.subtract(normal.multiply(.97f));
            Vec3d railMiddle = rail1.add(rail2).multiply(.5);

            for (Vec3d vec : new Vec3d[]{rail1, rail2, railMiddle}) {
                BlockPos pos = BlockPos.ofFloored(vec);
                Pair<Integer, Integer> key = Pair.of(pos.getX(), pos.getZ());
                if (!yLevels.containsKey(key) || yLevels.get(key) > vec.y)
                    yLevels.put(key, vec.y);
            }
        }

        for (Map.Entry<Pair<Integer, Integer>, Double> entry : yLevels.entrySet()) {
            double yValue = entry.getValue();
            int floor = MathHelper.floor(yValue);
            boolean placeSlab = slabLike && yValue - floor >= .5;
            BlockPos targetPos = new BlockPos(entry.getKey().getFirst(), floor, entry.getKey().getSecond());
            targetPos = targetPos.add(tePosition).up(placeSlab ? 1 : 0);
            BlockState stateToPlace = placeSlab ? defaultBlockState.with(SlabBlock.TYPE, SlabType.BOTTOM) : defaultBlockState;
            if (!visited.add(targetPos))
                continue;
            if (placeBlockIfFree(level, targetPos, stateToPlace, simulate))
                itemsNeeded += !placeSlab ? 2 : 1;
            if (placeSlab) {
                if (!visited.add(targetPos.down()))
                    continue;
                BlockState topSlab = stateToPlace.with(SlabBlock.TYPE, SlabType.TOP);
                if (placeBlockIfFree(level, targetPos.down(), topSlab, simulate))
                    itemsNeeded++;
            }
        }

        return itemsNeeded;
    }

    private static boolean isWallLike(BlockState defaultBlockState) {
        return defaultBlockState.getBlock() instanceof WallBlock || defaultBlockState.isOf(AllBlocks.METAL_GIRDER);
    }

    private static boolean placeBlockIfFree(World level, BlockPos pos, BlockState state, boolean simulate) {
        BlockState stateAtPos = level.getBlockState(pos);
        if (stateAtPos.getBlock() != state.getBlock() && stateAtPos.isReplaceable()) {
            if (!simulate)
                level.setBlockState(pos, ProperWaterloggedBlock.withWater(level, state, pos), Block.NOTIFY_ALL);
            return true;
        }
        return false;
    }

}
