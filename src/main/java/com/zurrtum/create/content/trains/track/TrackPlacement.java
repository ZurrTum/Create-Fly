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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class TrackPlacement {
    public static class PlacementInfo {

        public PlacementInfo(TrackMaterial material) {
            this.trackMaterial = material;
        }

        public @Nullable BezierConnection curve = null;
        public boolean valid = false;
        public int end1Extent = 0;
        public int end2Extent = 0;
        public @Nullable String message = null;

        public int requiredTracks = 0;
        public boolean hasRequiredTracks = false;

        public int requiredPavement = 0;
        public boolean hasRequiredPavement = false;
        public final TrackMaterial trackMaterial;

        // for visualisation
        public Vec3 end1;
        public Vec3 end2;
        public Vec3 normal1;
        public Vec3 normal2;
        public Vec3 axis1;
        public Vec3 axis2;
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

    public static @Nullable PlacementInfo cached;

    public static @Nullable BlockPos hoveringPos;
    public static boolean hoveringMaxed;
    static int hoveringAngle;
    static ItemStack lastItem;

    public static PlacementInfo tryConnect(
        Level level,
        Player player,
        BlockPos pos2,
        BlockState state2,
        ItemStack stack,
        boolean girder,
        boolean maximiseTurn
    ) {
        Vec3 lookVec = player.getLookAngle();
        int lookAngle = (int) (22.5 + AngleHelper.deg(Mth.atan2(lookVec.z, lookVec.x)) % 360) / 8;
        int maxLength = AllConfigs.server().trains.maxTrackPlacementLength.get();

        if (level.isClientSide() && cached != null && pos2.equals(hoveringPos) && stack.equals(lastItem) && hoveringMaxed == maximiseTurn && lookAngle == hoveringAngle)
            return cached;

        PlacementInfo info = new PlacementInfo(TrackMaterial.fromItem(stack.getItem()));
        hoveringMaxed = maximiseTurn;
        hoveringAngle = lookAngle;
        hoveringPos = pos2;
        lastItem = stack;
        cached = info;

        ITrackBlock track = (ITrackBlock) state2.getBlock();
        Pair<Vec3, AxisDirection> nearestTrackAxis = track.getNearestTrackAxis(level, pos2, state2, lookVec);
        Vec3 axis2 = nearestTrackAxis.getFirst().scale(nearestTrackAxis.getSecond() == AxisDirection.POSITIVE ? -1 : 1);
        Vec3 normal2 = track.getUpNormal(level, pos2, state2).normalize();
        Vec3 normedAxis2 = axis2.normalize();
        Vec3 end2 = track.getCurveStart(level, pos2, state2, axis2);

        ConnectingFrom connectingFrom = stack.get(AllDataComponents.TRACK_CONNECTING_FROM);

        BlockPos pos1 = connectingFrom.pos();
        Vec3 axis1 = connectingFrom.axis();
        Vec3 normedAxis1 = axis1.normalize();
        Vec3 end1 = connectingFrom.end();
        Vec3 normal1 = connectingFrom.normal();
        BlockState state1 = level.getBlockState(pos1);

        if (level.isClientSide()) {
            info.end1 = end1;
            info.end2 = end2;
            info.normal1 = normal1;
            info.normal2 = normal2;
            info.axis1 = axis1;
            info.axis2 = axis2;
        }

        if (pos1.equals(pos2))
            return info.withMessage("second_point");
        if (pos1.distSqr(pos2) > maxLength * maxLength)
            return info.withMessage("too_far").tooJumbly();
        if (!state1.hasProperty(TrackBlock.HAS_BE))
            return info.withMessage("original_missing");
        if (level.getBlockEntity(pos2) instanceof TrackBlockEntity tbe && tbe.isTilted())
            return info.withMessage("turn_start");

        if (axis1.dot(end2.subtract(end1)) < 0) {
            axis1 = axis1.scale(-1);
            normedAxis1 = normedAxis1.scale(-1);
            end1 = track.getCurveStart(level, pos1, state1, axis1);
            if (level.isClientSide()) {
                info.end1 = end1;
                info.axis1 = axis1;
            }
        }

        double[] intersect = VecHelper.intersect(end1, end2, normedAxis1, normedAxis2, Axis.Y);
        boolean parallel = intersect == null;
        boolean skipCurve = false;

        if ((parallel && normedAxis1.dot(normedAxis2) > 0) || (!parallel && (intersect[0] < 0 || intersect[1] < 0))) {
            axis2 = axis2.scale(-1);
            normedAxis2 = normedAxis2.scale(-1);
            end2 = track.getCurveStart(level, pos2, state2, axis2);
            if (level.isClientSide()) {
                info.end2 = end2;
                info.axis2 = axis2;
            }
        }

        Vec3 cross2 = normedAxis2.cross(new Vec3(0, 1, 0));

        double a1 = Mth.atan2(normedAxis2.z, normedAxis2.x);
        double a2 = Mth.atan2(normedAxis1.z, normedAxis1.x);
        double angle = a1 - a2;
        double ascend = end2.subtract(end1).y;
        double absAscend = Math.abs(ascend);
        boolean slope = !normal1.equals(normal2);

        if (level.isClientSide()) {
            Vec3 offset1 = axis1.scale(info.end1Extent);
            Vec3 offset2 = axis2.scale(info.end2Extent);
            BlockPos targetPos1 = pos1.offset(BlockPos.containing(offset1));
            BlockPos targetPos2 = pos2.offset(BlockPos.containing(offset2));
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

                skipCurve = Mth.equal(u, 0);

                if (!skipCurve && sTest[0] < 0)
                    return info.withMessage("perpendicular").tooJumbly();

                if (skipCurve) {
                    dist = VecHelper.getCenterOf(pos1).distanceTo(VecHelper.getCenterOf(pos2));
                    info.end1Extent = (int) Math.round((dist + 1) / axis1.length());

                } else {
                    if (!Mth.equal(ascend, 0) || normedAxis1.y != 0)
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
            if (Mth.equal(normal1.dot(normal2), 0))
                return info.withMessage("opposing_slopes");
            if ((axis1.y < 0 || axis2.y > 0) && ascend > 0)
                return info.withMessage("leave_slope_ascending");
            if ((axis1.y > 0 || axis2.y < 0) && ascend < 0)
                return info.withMessage("leave_slope_descending");

            skipCurve = false;
            info.end1Extent = 0;
            info.end2Extent = 0;

            Axis plane = Mth.equal(axis1.x, 0) ? Axis.X : Axis.Z;
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

        if (skipCurve && !Mth.equal(ascend, 0)) {
            int hDistance = info.end1Extent;
            if (axis1.y == 0 || !Mth.equal(absAscend + 1, dist / axis1.length())) {

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
            info.end1Extent = Mth.floor(ex1);
            info.end2Extent = Mth.floor(ex2);
            turnSize = turnSizeToFitAscend;
        }

        Vec3 offset1 = axis1.scale(info.end1Extent);
        Vec3 offset2 = axis2.scale(info.end2Extent);
        BlockPos targetPos1 = pos1.offset(BlockPos.containing(offset1));
        BlockPos targetPos2 = pos2.offset(BlockPos.containing(offset2));

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

        ItemStack offhandItem = player.getOffhandItem().copy();
        boolean shouldPave = offhandItem.getItem() instanceof BlockItem && !offhandItem.is(AllItemTags.INVALID_FOR_TRACK_PAVING);
        if (shouldPave) {
            BlockItem paveItem = (BlockItem) offhandItem.getItem();
            paveTracks(level, info, paveItem, true);
            info.hasRequiredPavement = true;
        }

        info.hasRequiredTracks = true;

        if (!player.isCreative()) {
            for (boolean simulate : Iterate.trueAndFalse) {
                if (level.isClientSide() && !simulate)
                    break;

                int tracks = info.requiredTracks;
                int pavement = info.requiredPavement;
                int foundTracks = 0;
                int foundPavement = 0;

                Inventory inv = player.getInventory();
                NonNullList<ItemStack> main = inv.getNonEquipmentItems();
                for (int j = 0, end = Inventory.INVENTORY_SIZE + 1; j <= end; j++) {
                    int i = j;
                    boolean offhand = j == end;
                    if (j == Inventory.INVENTORY_SIZE)
                        i = inv.getSelectedSlot();
                    else if (offhand)
                        i = 0;
                    else if (j == inv.getSelectedSlot())
                        continue;

                    ItemStack stackInSlot = offhand ? inv.getItem(Inventory.SLOT_OFFHAND) : main.get(i);
                    boolean isTrack = stackInSlot.is(AllItemTags.TRACKS) && stackInSlot.is(stack.getItem());
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
                            player.setItemInHand(InteractionHand.OFF_HAND, newItem);
                        else
                            inv.setItem(i, newItem);
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

        if (level.isClientSide())
            return info;
        if (shouldPave) {
            BlockItem paveItem = (BlockItem) offhandItem.getItem();
            paveTracks(level, info, paveItem, false);
        }
        return placeTracks(level, info, state1, state2, targetPos1, targetPos2, false);
    }

    private static void paveTracks(Level level, PlacementInfo info, BlockItem blockItem, boolean simulate) {
        Block block = blockItem.getBlock();
        info.requiredPavement = 0;
        if (block == null || block instanceof EntityBlock || block.defaultBlockState().getCollisionShape(level, info.pos1).isEmpty())
            return;

        Set<BlockPos> visited = new HashSet<>();

        for (boolean first : Iterate.trueAndFalse) {
            int extent = (first ? info.end1Extent : info.end2Extent) + (info.curve != null ? 1 : 0);
            Vec3 axis = first ? info.axis1 : info.axis2;
            BlockPos pavePos = first ? info.pos1 : info.pos2;
            info.requiredPavement += TrackPaver.paveStraight(level, pavePos.below(), axis, extent, block, simulate, visited);
        }

        if (info.curve != null)
            info.requiredPavement += TrackPaver.paveCurve(level, info.curve, block, simulate, visited);
    }

    private static PlacementInfo placeTracks(
        Level level,
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
            Vec3 axis = first ? info.axis1 : info.axis2;
            BlockPos pos = first ? info.pos1 : info.pos2;
            BlockState state = first ? state1 : state2;
            if (state.hasProperty(TrackBlock.HAS_BE) && !simulate)
                state = state.setValue(TrackBlock.HAS_BE, false);

            switch (state.getValue(TrackBlock.SHAPE)) {
                case TE, TW:
                    state = state.setValue(TrackBlock.SHAPE, TrackShape.XO);
                    break;
                case TN, TS:
                    state = state.setValue(TrackBlock.SHAPE, TrackShape.ZO);
                    break;
                default:
                    break;
            }

            for (int i = 0; i < (info.curve != null ? extent + 1 : extent); i++) {
                Vec3 offset = axis.scale(i);
                BlockPos offsetPos = pos.offset(BlockPos.containing(offset));
                BlockState stateAtPos = level.getBlockState(offsetPos);
                // copy over all shared properties from the shaped state to the correct track material block
                BlockState toPlace = BlockHelper.copyProperties(state, info.trackMaterial.getBlock().defaultBlockState());

                boolean canPlace = stateAtPos.canBeReplaced() || stateAtPos.is(BlockTags.FLOWERS);
                if (canPlace)
                    info.requiredTracks++;
                if (simulate)
                    continue;

                if (stateAtPos.getBlock() instanceof ITrackBlock trackAtPos) {
                    toPlace = trackAtPos.overlay(level, offsetPos, stateAtPos, toPlace);
                    canPlace = true;
                }

                if (canPlace)
                    level.setBlock(offsetPos, ProperWaterloggedBlock.withWater(level, toPlace, offsetPos), Block.UPDATE_ALL);
            }
        }

        if (info.curve == null)
            return info;

        if (!simulate) {
            BlockState onto = info.trackMaterial.getBlock().defaultBlockState();
            BlockState stateAtPos = level.getBlockState(targetPos1);
            level.setBlock(
                targetPos1, ProperWaterloggedBlock.withWater(
                    level,
                    (stateAtPos.is(AllBlockTags.TRACKS) ? stateAtPos : BlockHelper.copyProperties(state1, onto)).setValue(TrackBlock.HAS_BE, true),
                    targetPos1
                ), Block.UPDATE_ALL
            );

            stateAtPos = level.getBlockState(targetPos2);
            level.setBlock(
                targetPos2, ProperWaterloggedBlock.withWater(
                    level,
                    (stateAtPos.is(AllBlockTags.TRACKS) ? stateAtPos : BlockHelper.copyProperties(state2, onto)).setValue(TrackBlock.HAS_BE, true),
                    targetPos2
                ), Block.UPDATE_ALL
            );
        }

        BlockEntity te1 = level.getBlockEntity(targetPos1);
        BlockEntity te2 = level.getBlockEntity(targetPos2);
        int requiredTracksForTurn = (info.curve.getSegmentCount() + 1) / 2;

        if (!(te1 instanceof TrackBlockEntity tte1) || !(te2 instanceof TrackBlockEntity tte2)) {
            info.requiredTracks += requiredTracksForTurn;
            return info;
        }

        if (!tte1.getConnections().containsKey(tte2.getBlockPos()))
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
