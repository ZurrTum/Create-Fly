package com.zurrtum.create.content.trains.track;

import com.zurrtum.create.AllBlockTags;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import com.zurrtum.create.foundation.utility.BlockHelper;
import com.zurrtum.create.infrastructure.component.ConnectingFrom;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class TrackPlacement {
    public static class PlacementInfo {

        public PlacementInfo(TrackMaterial material) {
            this.trackMaterial = material;
        }

        public BezierConnection curve = null;
        public boolean valid = false;
        public int end1Extent = 0;
        public int end2Extent = 0;
        public String message = null;

        public int requiredTracks = 0;
        public boolean hasRequiredTracks = false;

        public int requiredPavement = 0;
        public boolean hasRequiredPavement = false;
        public final TrackMaterial trackMaterial;

        // for visualisation
        public Vec3d end1;
        public Vec3d end2;
        public Vec3d normal1;
        public Vec3d normal2;
        public Vec3d axis1;
        public Vec3d axis2;
        BlockPos pos1;
        BlockPos pos2;

        public PlacementInfo withMessage(String message) {
            this.message = "track." + message;
            return this;
        }

        public PlacementInfo tooJumbly() {
            curve = null;
            return this;
        }
    }

    public static PlacementInfo cached;

    public static BlockPos hoveringPos;
    public static boolean hoveringMaxed;
    static int hoveringAngle;
    static ItemStack lastItem;

    public static PlacementInfo tryConnect(
        World level,
        PlayerEntity player,
        BlockPos pos2,
        BlockState state2,
        ItemStack stack,
        boolean girder,
        boolean maximiseTurn
    ) {
        Vec3d lookVec = player.getRotationVector();
        int lookAngle = (int) (22.5 + AngleHelper.deg(MathHelper.atan2(lookVec.z, lookVec.x)) % 360) / 8;
        int maxLength = AllConfigs.server().trains.maxTrackPlacementLength.get();

        if (level.isClient && cached != null && pos2.equals(hoveringPos) && stack.equals(lastItem) && hoveringMaxed == maximiseTurn && lookAngle == hoveringAngle)
            return cached;

        PlacementInfo info = new PlacementInfo(TrackMaterial.fromItem(stack.getItem()));
        hoveringMaxed = maximiseTurn;
        hoveringAngle = lookAngle;
        hoveringPos = pos2;
        lastItem = stack;
        cached = info;

        ITrackBlock track = (ITrackBlock) state2.getBlock();
        Pair<Vec3d, AxisDirection> nearestTrackAxis = track.getNearestTrackAxis(level, pos2, state2, lookVec);
        Vec3d axis2 = nearestTrackAxis.getFirst().multiply(nearestTrackAxis.getSecond() == AxisDirection.POSITIVE ? -1 : 1);
        Vec3d normal2 = track.getUpNormal(level, pos2, state2).normalize();
        Vec3d normedAxis2 = axis2.normalize();
        Vec3d end2 = track.getCurveStart(level, pos2, state2, axis2);

        ConnectingFrom connectingFrom = stack.get(AllDataComponents.TRACK_CONNECTING_FROM);

        BlockPos pos1 = connectingFrom.pos();
        Vec3d axis1 = connectingFrom.axis();
        Vec3d normedAxis1 = axis1.normalize();
        Vec3d end1 = connectingFrom.end();
        Vec3d normal1 = connectingFrom.normal();
        BlockState state1 = level.getBlockState(pos1);

        if (level.isClient) {
            info.end1 = end1;
            info.end2 = end2;
            info.normal1 = normal1;
            info.normal2 = normal2;
            info.axis1 = axis1;
            info.axis2 = axis2;
        }

        if (pos1.equals(pos2))
            return info.withMessage("second_point");
        if (pos1.getSquaredDistance(pos2) > maxLength * maxLength)
            return info.withMessage("too_far").tooJumbly();
        if (!state1.contains(TrackBlock.HAS_BE))
            return info.withMessage("original_missing");
        if (level.getBlockEntity(pos2) instanceof TrackBlockEntity tbe && tbe.isTilted())
            return info.withMessage("turn_start");

        if (axis1.dotProduct(end2.subtract(end1)) < 0) {
            axis1 = axis1.multiply(-1);
            normedAxis1 = normedAxis1.multiply(-1);
            end1 = track.getCurveStart(level, pos1, state1, axis1);
            if (level.isClient) {
                info.end1 = end1;
                info.axis1 = axis1;
            }
        }

        double[] intersect = VecHelper.intersect(end1, end2, normedAxis1, normedAxis2, Axis.Y);
        boolean parallel = intersect == null;
        boolean skipCurve = false;

        if ((parallel && normedAxis1.dotProduct(normedAxis2) > 0) || (!parallel && (intersect[0] < 0 || intersect[1] < 0))) {
            axis2 = axis2.multiply(-1);
            normedAxis2 = normedAxis2.multiply(-1);
            end2 = track.getCurveStart(level, pos2, state2, axis2);
            if (level.isClient) {
                info.end2 = end2;
                info.axis2 = axis2;
            }
        }

        Vec3d cross2 = normedAxis2.crossProduct(new Vec3d(0, 1, 0));

        double a1 = MathHelper.atan2(normedAxis2.z, normedAxis2.x);
        double a2 = MathHelper.atan2(normedAxis1.z, normedAxis1.x);
        double angle = a1 - a2;
        double ascend = end2.subtract(end1).y;
        double absAscend = Math.abs(ascend);
        boolean slope = !normal1.equals(normal2);

        if (level.isClient) {
            Vec3d offset1 = axis1.multiply(info.end1Extent);
            Vec3d offset2 = axis2.multiply(info.end2Extent);
            BlockPos targetPos1 = pos1.add(BlockPos.ofFloored(offset1));
            BlockPos targetPos2 = pos2.add(BlockPos.ofFloored(offset2));
            info.curve = new BezierConnection(
                Couple.create(targetPos1, targetPos2),
                Couple.create(end1.add(offset1), end2.add(offset2)),
                Couple.create(normedAxis1, normedAxis2),
                Couple.create(normal1, normal2),
                true,
                girder,
                TrackMaterial.fromItem(stack.getItem())
            );
        }

        // S curve or Straight

        double dist = 0;

        if (parallel) {
            double[] sTest = VecHelper.intersect(end1, end2, normedAxis1, cross2, Axis.Y);
            if (sTest != null) {
                double t = Math.abs(sTest[0]);
                double u = Math.abs(sTest[1]);

                skipCurve = MathHelper.approximatelyEquals(u, 0);

                if (!skipCurve && sTest[0] < 0)
                    return info.withMessage("perpendicular").tooJumbly();

                if (skipCurve) {
                    dist = VecHelper.getCenterOf(pos1).distanceTo(VecHelper.getCenterOf(pos2));
                    info.end1Extent = (int) Math.round((dist + 1) / axis1.length());

                } else {
                    if (!MathHelper.approximatelyEquals(ascend, 0) || normedAxis1.y != 0)
                        return info.withMessage("ascending_s_curve");

                    double targetT = u <= 1 ? 3 : u * 2;

                    if (t < targetT)
                        return info.withMessage("too_sharp");

                    // This is for standardising s curve sizes
                    if (t > targetT) {
                        int correction = (int) ((t - targetT) / axis1.length());
                        info.end1Extent = maximiseTurn ? 0 : correction / 2 + (correction % 2);
                        info.end2Extent = maximiseTurn ? 0 : correction / 2;
                    }
                }
            }
        }

        // Slope

        if (slope) {
            if (!skipCurve)
                return info.withMessage("slope_turn");
            if (MathHelper.approximatelyEquals(normal1.dotProduct(normal2), 0))
                return info.withMessage("opposing_slopes");
            if ((axis1.y < 0 || axis2.y > 0) && ascend > 0)
                return info.withMessage("leave_slope_ascending");
            if ((axis1.y > 0 || axis2.y < 0) && ascend < 0)
                return info.withMessage("leave_slope_descending");

            skipCurve = false;
            info.end1Extent = 0;
            info.end2Extent = 0;

            Axis plane = MathHelper.approximatelyEquals(axis1.x, 0) ? Axis.X : Axis.Z;
            intersect = VecHelper.intersect(end1, end2, normedAxis1, normedAxis2, plane);
            double dist1 = Math.abs(intersect[0] / axis1.length());
            double dist2 = Math.abs(intersect[1] / axis2.length());

            if (dist1 > dist2)
                info.end1Extent = (int) Math.round(dist1 - dist2);
            if (dist2 > dist1)
                info.end2Extent = (int) Math.round(dist2 - dist1);

            double turnSize = Math.min(dist1, dist2);
            if (intersect[0] < 0 || intersect[1] < 0)
                return info.withMessage("too_sharp").tooJumbly();
            if (turnSize < 2)
                return info.withMessage("too_sharp");

            // This is for standardising curve sizes
            if (turnSize > 2 && !maximiseTurn) {
                info.end1Extent += turnSize - 2;
                info.end2Extent += turnSize - 2;
                turnSize = 2;
            }
        }

        // Straight ascend

        if (skipCurve && !MathHelper.approximatelyEquals(ascend, 0)) {
            int hDistance = info.end1Extent;
            if (axis1.y == 0 || !MathHelper.approximatelyEquals(absAscend + 1, dist / axis1.length())) {

                if (axis1.y != 0 && axis1.y == -axis2.y)
                    return info.withMessage("ascending_s_curve");

                info.end1Extent = 0;
                double minHDistance = Math.max(absAscend < 4 ? absAscend * 4 : absAscend * 3, 6) / axis1.length();
                if (hDistance < minHDistance)
                    return info.withMessage("too_steep");
                if (hDistance > minHDistance) {
                    int correction = (int) (hDistance - minHDistance);
                    info.end1Extent = maximiseTurn ? 0 : correction / 2 + (correction % 2);
                    info.end2Extent = maximiseTurn ? 0 : correction / 2;
                }

                skipCurve = false;
            }
        }

        // Turn

        if (!parallel) {
            float absAngle = Math.abs(AngleHelper.deg(angle));
            if (absAngle < 60 || absAngle > 300)
                return info.withMessage("turn_90").tooJumbly();

            intersect = VecHelper.intersect(end1, end2, normedAxis1, normedAxis2, Axis.Y);
            double dist1 = Math.abs(intersect[0]);
            double dist2 = Math.abs(intersect[1]);
            float ex1 = 0;
            float ex2 = 0;

            if (dist1 > dist2)
                ex1 = (float) ((dist1 - dist2) / axis1.length());
            if (dist2 > dist1)
                ex2 = (float) ((dist2 - dist1) / axis2.length());

            double turnSize = Math.min(dist1, dist2) - .1d;
            boolean ninety = (absAngle + .25f) % 90 < 1;

            if (intersect[0] < 0 || intersect[1] < 0)
                return info.withMessage("too_sharp").tooJumbly();

            double minTurnSize = ninety ? 7 : 3.25;
            double turnSizeToFitAscend = minTurnSize + (ninety ? Math.max(0, absAscend - 3) * 2f : Math.max(0, absAscend - 1.5f) * 1.5f);

            if (turnSize < minTurnSize)
                return info.withMessage("too_sharp");
            if (turnSize < turnSizeToFitAscend)
                return info.withMessage("too_steep");

            // This is for standardising curve sizes
            if (!maximiseTurn) {
                ex1 += (turnSize - turnSizeToFitAscend) / axis1.length();
                ex2 += (turnSize - turnSizeToFitAscend) / axis2.length();
            }
            info.end1Extent = MathHelper.floor(ex1);
            info.end2Extent = MathHelper.floor(ex2);
            turnSize = turnSizeToFitAscend;
        }

        Vec3d offset1 = axis1.multiply(info.end1Extent);
        Vec3d offset2 = axis2.multiply(info.end2Extent);
        BlockPos targetPos1 = pos1.add(BlockPos.ofFloored(offset1));
        BlockPos targetPos2 = pos2.add(BlockPos.ofFloored(offset2));

        info.curve = skipCurve ? null : new BezierConnection(
            Couple.create(targetPos1, targetPos2),
            Couple.create(end1.add(offset1), end2.add(offset2)),
            Couple.create(normedAxis1, normedAxis2),
            Couple.create(normal1, normal2),
            true,
            girder,
            TrackMaterial.fromItem(stack.getItem())
        );

        info.valid = true;

        info.pos1 = pos1;
        info.pos2 = pos2;
        info.axis1 = axis1;
        info.axis2 = axis2;

        placeTracks(level, info, state1, state2, targetPos1, targetPos2, true);

        ItemStack offhandItem = player.getOffHandStack().copy();
        boolean shouldPave = offhandItem.getItem() instanceof BlockItem && !offhandItem.isIn(AllItemTags.INVALID_FOR_TRACK_PAVING);
        if (shouldPave) {
            BlockItem paveItem = (BlockItem) offhandItem.getItem();
            paveTracks(level, info, paveItem, true);
            info.hasRequiredPavement = true;
        }

        info.hasRequiredTracks = true;

        if (!player.isCreative()) {
            for (boolean simulate : Iterate.trueAndFalse) {
                if (level.isClient && !simulate)
                    break;

                int tracks = info.requiredTracks;
                int pavement = info.requiredPavement;
                int foundTracks = 0;
                int foundPavement = 0;

                PlayerInventory inv = player.getInventory();
                DefaultedList<ItemStack> main = inv.getMainStacks();
                for (int j = 0, end = PlayerInventory.MAIN_SIZE + 1; j <= end; j++) {
                    int i = j;
                    boolean offhand = j == end;
                    if (j == PlayerInventory.MAIN_SIZE)
                        i = inv.getSelectedSlot();
                    else if (offhand)
                        i = 0;
                    else if (j == inv.getSelectedSlot())
                        continue;

                    ItemStack stackInSlot = offhand ? inv.getStack(PlayerInventory.OFF_HAND_SLOT) : main.get(i);
                    boolean isTrack = stackInSlot.isIn(AllItemTags.TRACKS) && stackInSlot.isOf(stack.getItem());
                    if (!isTrack && (!shouldPave || offhandItem.getItem() != stackInSlot.getItem()))
                        continue;
                    if (isTrack ? foundTracks >= tracks : foundPavement >= pavement)
                        continue;

                    int count = stackInSlot.getCount();

                    if (!simulate) {
                        int remainingItems = count - Math.min(isTrack ? tracks - foundTracks : pavement - foundPavement, count);
                        if (i == inv.getSelectedSlot())
                            stackInSlot.remove(AllDataComponents.TRACK_CONNECTING_FROM);
                        ItemStack newItem = stackInSlot.copyWithCount(remainingItems);
                        if (offhand)
                            player.setStackInHand(Hand.OFF_HAND, newItem);
                        else
                            inv.setStack(i, newItem);
                    }

                    if (isTrack)
                        foundTracks += count;
                    else
                        foundPavement += count;
                }

                if (simulate) {
                    if (foundTracks < tracks) {
                        info.valid = false;
                        info.tooJumbly();
                        info.hasRequiredTracks = false;
                        return info.withMessage("not_enough_tracks");
                    }

                    if (foundPavement < pavement) {
                        info.valid = false;
                        info.tooJumbly();
                        info.hasRequiredPavement = false;
                        return info.withMessage("not_enough_pavement");
                    }
                }
            }
        }

        if (level.isClient())
            return info;
        if (shouldPave) {
            BlockItem paveItem = (BlockItem) offhandItem.getItem();
            paveTracks(level, info, paveItem, false);
        }
        return placeTracks(level, info, state1, state2, targetPos1, targetPos2, false);
    }

    private static void paveTracks(World level, PlacementInfo info, BlockItem blockItem, boolean simulate) {
        Block block = blockItem.getBlock();
        info.requiredPavement = 0;
        if (block == null || block instanceof BlockEntityProvider || block.getDefaultState().getCollisionShape(level, info.pos1).isEmpty())
            return;

        Set<BlockPos> visited = new HashSet<>();

        for (boolean first : Iterate.trueAndFalse) {
            int extent = (first ? info.end1Extent : info.end2Extent) + (info.curve != null ? 1 : 0);
            Vec3d axis = first ? info.axis1 : info.axis2;
            BlockPos pavePos = first ? info.pos1 : info.pos2;
            info.requiredPavement += TrackPaver.paveStraight(level, pavePos.down(), axis, extent, block, simulate, visited);
        }

        if (info.curve != null)
            info.requiredPavement += TrackPaver.paveCurve(level, info.curve, block, simulate, visited);
    }

    private static PlacementInfo placeTracks(
        World level,
        PlacementInfo info,
        BlockState state1,
        BlockState state2,
        BlockPos targetPos1,
        BlockPos targetPos2,
        boolean simulate
    ) {
        info.requiredTracks = 0;

        for (boolean first : Iterate.trueAndFalse) {
            int extent = first ? info.end1Extent : info.end2Extent;
            Vec3d axis = first ? info.axis1 : info.axis2;
            BlockPos pos = first ? info.pos1 : info.pos2;
            BlockState state = first ? state1 : state2;
            if (state.contains(TrackBlock.HAS_BE) && !simulate)
                state = state.with(TrackBlock.HAS_BE, false);

            switch (state.get(TrackBlock.SHAPE)) {
                case TE, TW:
                    state = state.with(TrackBlock.SHAPE, TrackShape.XO);
                    break;
                case TN, TS:
                    state = state.with(TrackBlock.SHAPE, TrackShape.ZO);
                    break;
                default:
                    break;
            }

            for (int i = 0; i < (info.curve != null ? extent + 1 : extent); i++) {
                Vec3d offset = axis.multiply(i);
                BlockPos offsetPos = pos.add(BlockPos.ofFloored(offset));
                BlockState stateAtPos = level.getBlockState(offsetPos);
                // copy over all shared properties from the shaped state to the correct track material block
                BlockState toPlace = BlockHelper.copyProperties(state, info.trackMaterial.getBlock().getDefaultState());

                boolean canPlace = stateAtPos.isReplaceable() || stateAtPos.isIn(BlockTags.FLOWERS);
                if (canPlace)
                    info.requiredTracks++;
                if (simulate)
                    continue;

                if (stateAtPos.getBlock() instanceof ITrackBlock trackAtPos) {
                    toPlace = trackAtPos.overlay(level, offsetPos, stateAtPos, toPlace);
                    canPlace = true;
                }

                if (canPlace)
                    level.setBlockState(offsetPos, ProperWaterloggedBlock.withWater(level, toPlace, offsetPos), Block.NOTIFY_ALL);
            }
        }

        if (info.curve == null)
            return info;

        if (!simulate) {
            BlockState onto = info.trackMaterial.getBlock().getDefaultState();
            BlockState stateAtPos = level.getBlockState(targetPos1);
            level.setBlockState(
                targetPos1, ProperWaterloggedBlock.withWater(
                    level,
                    (stateAtPos.isIn(AllBlockTags.TRACKS) ? stateAtPos : BlockHelper.copyProperties(state1, onto)).with(TrackBlock.HAS_BE, true),
                    targetPos1
                ), Block.NOTIFY_ALL
            );

            stateAtPos = level.getBlockState(targetPos2);
            level.setBlockState(
                targetPos2, ProperWaterloggedBlock.withWater(
                    level,
                    (stateAtPos.isIn(AllBlockTags.TRACKS) ? stateAtPos : BlockHelper.copyProperties(state2, onto)).with(TrackBlock.HAS_BE, true),
                    targetPos2
                ), Block.NOTIFY_ALL
            );
        }

        BlockEntity te1 = level.getBlockEntity(targetPos1);
        BlockEntity te2 = level.getBlockEntity(targetPos2);
        int requiredTracksForTurn = (info.curve.getSegmentCount() + 1) / 2;

        if (!(te1 instanceof TrackBlockEntity tte1) || !(te2 instanceof TrackBlockEntity tte2)) {
            info.requiredTracks += requiredTracksForTurn;
            return info;
        }

        if (!tte1.getConnections().containsKey(tte2.getPos()))
            info.requiredTracks += requiredTracksForTurn;

        if (simulate)
            return info;

        tte1.addConnection(info.curve);
        tte2.addConnection(info.curve.secondary());
        tte1.tilt.tryApplySmoothing();
        tte2.tilt.tryApplySmoothing();
        return info;
    }
}
