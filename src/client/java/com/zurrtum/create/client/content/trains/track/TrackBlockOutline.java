package com.zurrtum.create.client.content.trains.track;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.catnip.data.WorldAttached;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.foundation.utility.RaycastHelper;
import com.zurrtum.create.content.trains.track.*;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TrackBlockOutline {
    public static final int BLACK_COLOR = 0x66000000;
    public static final int WHITE_COLOR = 0x66ffffff;
    public static final int RED_COLOR = 0x66ff1f3f;
    public static WorldAttached<Map<BlockPos, TrackBlockEntity>> TRACKS_WITH_TURNS = new WorldAttached<>(w -> new HashMap<>());
    public static @Nullable BezierPointSelection result;

    private static final VoxelShape LONG_CROSS = Shapes.or(
        TrackVoxelShapes.longOrthogonalZ(),
        TrackVoxelShapes.longOrthogonalX()
    );
    private static final VoxelShape LONG_ORTHO = TrackVoxelShapes.longOrthogonalZ();
    private static final VoxelShape LONG_ORTHO_OFFSET = TrackVoxelShapes.longOrthogonalZOffset();

    public static void pickCurves(Minecraft mc) {
        if (!(mc.getCameraEntity() instanceof LocalPlayer player)) {
            return;
        }
        if (mc.level == null) {
            return;
        }

        Vec3 origin = player.getEyePosition(AnimationTickHolder.getPartialTicks(mc.level));

        double maxRange = mc.hitResult == null ? Double.MAX_VALUE : mc.hitResult.getLocation().distanceToSqr(origin);

        result = null;

        double range = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
        Vec3 target = RaycastHelper.getTraceTarget(player, Math.min(maxRange, range) + 1, origin);
        Map<BlockPos, TrackBlockEntity> turns = TRACKS_WITH_TURNS.get(mc.level);

        for (TrackBlockEntity be : turns.values()) {
            for (BezierConnection bc : be.getConnections().values()) {
                if (!bc.isPrimary()) {
                    continue;
                }

                AABB bounds = bc.getBounds();
                if (!bounds.contains(origin) && bounds.clip(origin, target).isEmpty()) {
                    continue;
                }

                float[] stepLUT = bc.getStepLUT();
                int segments = (int) (bc.getLength() * 2);
                AABB segmentBounds = AllShapes.TRACK_ORTHO.get(Direction.SOUTH).bounds();
                segmentBounds = segmentBounds.move(-0.5, segmentBounds.getYsize() / -2, -0.5);

                int bestSegment = -1;
                double bestDistance = Double.MAX_VALUE;
                double newMaxRange = maxRange;

                for (int i = 0; i < stepLUT.length - 2; i++) {
                    float t = stepLUT[i] * i / segments;
                    float t1 = stepLUT[i + 1] * (i + 1) / segments;
                    float t2 = stepLUT[i + 2] * (i + 2) / segments;

                    Vec3 v1 = bc.getPosition(t);
                    Vec3 v2 = bc.getPosition(t2);
                    Vec3 diff = v2.subtract(v1);
                    Vec3 angles = TrackRenderer.getModelAngles(bc.getNormal(t1), diff);

                    Vec3 anchor = v1.add(diff.scale(0.5));
                    Vec3 localOrigin = origin.subtract(anchor);
                    Vec3 localDirection = target.subtract(origin);
                    localOrigin = VecHelper.rotate(localOrigin, AngleHelper.deg(-angles.x), Direction.Axis.X);
                    localOrigin = VecHelper.rotate(localOrigin, AngleHelper.deg(-angles.y), Direction.Axis.Y);
                    localDirection = VecHelper.rotate(localDirection, AngleHelper.deg(-angles.x), Direction.Axis.X);
                    localDirection = VecHelper.rotate(localDirection, AngleHelper.deg(-angles.y), Direction.Axis.Y);

                    Optional<Vec3> clip = segmentBounds.clip(localOrigin, localOrigin.add(localDirection));
                    if (clip.isEmpty()) {
                        continue;
                    }

                    if (bestSegment != -1 && bestDistance < clip.get().distanceToSqr(0, 0.25f, 0)) {
                        continue;
                    }

                    double distanceToSqr = clip.get().distanceToSqr(localOrigin);
                    if (distanceToSqr > maxRange) {
                        continue;
                    }

                    bestSegment = i;
                    newMaxRange = distanceToSqr;
                    bestDistance = clip.get().distanceToSqr(0, 0.25f, 0);

                    BezierTrackPointLocation location = new BezierTrackPointLocation(bc.getKey(), i);
                    result = new BezierPointSelection(be, location, anchor, angles, diff.normalize());
                }

                if (bestSegment != -1) {
                    maxRange = newMaxRange;
                }
            }
        }

        if (result == null) {
            return;
        }

        if (mc.hitResult != null && mc.hitResult.getType() != Type.MISS) {
            Vec3 priorLoc = mc.hitResult.getLocation();
            mc.hitResult = BlockHitResult.miss(priorLoc, Direction.UP, BlockPos.containing(priorLoc));
        }
    }

    public static void drawCurveSelection(
        Minecraft mc,
        PoseStack ms,
        SubmitNodeCollector queue,
        Vec3 camera,
        float lineWidth
    ) {
        if (mc.gui.hud.isHidden() || mc.gameMode.getPlayerMode() == GameType.SPECTATOR) {
            return;
        }
        BezierPointSelection result = TrackBlockOutline.result;
        if (result == null) {
            return;
        }
        Vec3 vec = result.vec().subtract(camera);
        Vec3 angles = result.angles();
        ms.pushPose();
        Pose pose = ms.last();
        pose.translate((float) vec.x, (float) (vec.y + 0.125f), (float) vec.z);
        pose.rotate(Axis.YP, (float) angles.y);
        pose.rotate(Axis.XP, (float) angles.x);
        pose.translate(-0.5f, -0.125f, -0.5f);
        int color = mc.player.getMainHandItem().is(AllItemTags.TRACKS) ? RED_COLOR : BLACK_COLOR;
        submitShape(mc, AllShapes.TRACK_ORTHO.get(Direction.SOUTH), ms, queue, color, lineWidth);
        ms.popPose();
    }

    public static boolean drawCustomBlockSelection(
        Minecraft mc,
        BlockPos pos,
        float width,
        SubmitNodeCollector queue,
        PoseStack ms
    ) {
        BlockState blockstate = mc.level.getBlockState(pos);
        if (!(blockstate.getBlock() instanceof TrackBlock)) {
            return false;
        }
        if (!mc.level.getWorldBorder().isWithinBounds(pos)) {
            return false;
        }
        TrackShape shape = blockstate.getValue(TrackBlock.SHAPE);
        if (shape == TrackShape.NONE) {
            return false;
        }
        int color;
        if (mc.player.getMainHandItem().is(AllItemTags.TRACKS)) {
            if (!shape.isJunction() && !(mc.level.getBlockEntity(pos) instanceof TrackBlockEntity tbe && tbe.isTilted())) {
                color = WHITE_COLOR;
            } else {
                color = RED_COLOR;
            }
        } else {
            color = BLACK_COLOR;
        }
        submitShape(mc, shape, ms, queue, color, width);
        return true;
    }

    public static void submitShape(
        Minecraft mc,
        VoxelShape shape,
        PoseStack ms,
        SubmitNodeCollector queue,
        int color,
        float width
    ) {
        RenderType blockOutlineRenderType;
        if (mc.gameRenderer.useImprovedTransparency()) {
            blockOutlineRenderType = RenderTypes.linesTranslucentNoDepthWrite();
        } else {
            blockOutlineRenderType = RenderTypes.linesTranslucent();
        }
        queue.submitShapeOutline(ms, shape, blockOutlineRenderType, color, width, true);
    }

    private static void submitShape(
        Minecraft mc,
        VoxelShape shape,
        float angle,
        PoseStack ms,
        SubmitNodeCollector queue,
        int color,
        float width
    ) {
        ms.rotateYCentered(angle);
        submitShape(mc, shape, ms, queue, color, width);
    }

    public static void submitShape(
        Minecraft mc,
        VoxelShape first,
        VoxelShape second,
        float angle,
        PoseStack ms,
        SubmitNodeCollector queue,
        int color,
        float width
    ) {
        submitShape(mc, first, ms, queue, color, width);
        submitShape(mc, second, angle, ms, queue, color, width);
    }

    private static void submitAscendingShape(
        Minecraft mc,
        float yRadians,
        PoseStack ms,
        SubmitNodeCollector queue,
        int color,
        float width
    ) {
        Pose pose = ms.last();
        pose.translate(0, 1, 0);
        pose.rotateYCentered(yRadians);
        pose.rotate(Axis.XP, Radians.DEG_45);
        pose.translate(0, -0.1875f, 0.0625f);
        submitShape(mc, LONG_ORTHO, ms, queue, color, width);
    }

    public static void submitShape(
        Minecraft mc,
        TrackShape shape,
        PoseStack ms,
        SubmitNodeCollector queue,
        int color,
        float width
    ) {
        switch (shape) {
            case ZO -> submitShape(mc, AllShapes.TRACK_ORTHO.get(Direction.SOUTH), ms, queue, color, width);
            case XO -> submitShape(mc, AllShapes.TRACK_ORTHO.get(Direction.EAST), ms, queue, color, width);
            case PD -> submitShape(mc, LONG_ORTHO, Radians.DEG_45, ms, queue, color, width);
            case ND -> submitShape(mc, LONG_ORTHO, Radians.DEG_315, ms, queue, color, width);
            case AN -> {
                Pose pose = ms.last();
                pose.translate(0, 1, 0);
                pose.rotate(Axis.XP, Radians.DEG_45);
                pose.translate(0, -0.1875f, 0.0625f);
                submitShape(mc, LONG_ORTHO, ms, queue, color, width);
            }
            case AS -> submitAscendingShape(mc, Radians.DEG_180, ms, queue, color, width);
            case AE -> submitAscendingShape(mc, Radians.DEG_270, ms, queue, color, width);
            case AW -> submitAscendingShape(mc, Radians.DEG_90, ms, queue, color, width);
            case TN -> submitShape(mc, LONG_ORTHO_OFFSET, Radians.DEG_180, ms, queue, color, width);
            case TS -> submitShape(mc, LONG_ORTHO_OFFSET, ms, queue, color, width);
            case TE -> submitShape(mc, LONG_ORTHO_OFFSET, Radians.DEG_90, ms, queue, color, width);
            case TW -> submitShape(mc, LONG_ORTHO_OFFSET, Radians.DEG_270, ms, queue, color, width);
            case CR_O -> submitShape(mc, AllShapes.TRACK_CROSS, ms, queue, color, width);
            case CR_D -> submitShape(mc, LONG_CROSS, Radians.DEG_45, ms, queue, color, width);
            case CR_PDX -> submitShape(
                mc,
                AllShapes.TRACK_ORTHO.get(Direction.EAST),
                LONG_ORTHO,
                Radians.DEG_45,
                ms,
                queue,
                color,
                width
            );
            case CR_PDZ -> submitShape(
                mc,
                AllShapes.TRACK_ORTHO.get(Direction.SOUTH),
                LONG_ORTHO,
                Radians.DEG_45,
                ms,
                queue,
                color,
                width
            );
            case CR_NDX -> submitShape(
                mc,
                AllShapes.TRACK_ORTHO.get(Direction.EAST),
                LONG_ORTHO,
                Radians.DEG_315,
                ms,
                queue,
                color,
                width
            );
            case CR_NDZ -> submitShape(
                mc,
                AllShapes.TRACK_ORTHO.get(Direction.SOUTH),
                LONG_ORTHO,
                Radians.DEG_315,
                ms,
                queue,
                color,
                width
            );
        }
    }

    public record BezierPointSelection(TrackBlockEntity blockEntity, BezierTrackPointLocation loc, Vec3 vec,
                                       Vec3 angles, Vec3 direction) {
    }

    public static void registerToCurveInteraction(TrackBlockEntity be) {
        TRACKS_WITH_TURNS.get(be.getLevel()).put(be.getBlockPos(), be);
    }

    public static void removeFromCurveInteraction(TrackBlockEntity be) {
        TRACKS_WITH_TURNS.get(be.getLevel()).remove(be.getBlockPos());
    }
}
