package com.zurrtum.create.client.content.trains.track;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllTrackMaterialModels.TrackModelHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import com.zurrtum.create.content.trains.track.TrackMaterial;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class TrackRenderer implements BlockEntityRenderer<TrackBlockEntity, TrackRenderer.TrackRenderState> {
    public TrackRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public TrackRenderState createRenderState() {
        return new TrackRenderState();
    }

    @Override
    public void extractRenderState(
        TrackBlockEntity be,
        TrackRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        Level world = be.getLevel();
        if (VisualizationManager.supportsVisualization(world)) {
            return;
        }
        GirderRenderState girder = null;
        Map<TrackMaterial, TrackSegmentRenderState> tracks = null;
        for (BezierConnection bc : be.getConnections().values()) {
            if (!bc.isPrimary()) {
                continue;
            }
            BlockPos bePosition = bc.bePositions.getFirst();
            if (bc.hasGirder) {
                GirderAngles segment = bc.getBakedGirders(GirderAngles::new);
                int length = segment.length;
                if (length > 1) {
                    if (girder == null) {
                        girder = GirderRenderState.create();
                    }
                    for (int i = 1; i < length; i++) {
                        girder.add(
                            LevelRenderer.getLightColor(world, segment.lightPosition[i].offset(bePosition)),
                            segment.beams[i],
                            segment.beamCaps[i]
                        );
                    }
                }
            }
            SegmentAngles segment = bc.getBakedSegments(SegmentAngles::new);
            int length = segment.length;
            if (length > 1) {
                if (tracks == null) {
                    tracks = new IdentityHashMap<>();
                }
                TrackSegmentRenderState renderState = tracks.computeIfAbsent(bc.getMaterial(), TrackSegmentRenderState::create);
                for (int i = 1; i < length; i++) {
                    renderState.add(
                        LevelRenderer.getLightColor(world, segment.lightPosition[i].offset(bePosition)),
                        segment.tieTransform[i],
                        segment.railTransforms[i]
                    );
                }
            }
        }
        if (tracks == null && girder == null) {
            return;
        }
        state.blockPos = be.getBlockPos();
        state.blockEntityType = be.getType();
        state.layer = RenderTypes.cutoutMovingBlock();
        state.girder = girder;
        state.tracks = tracks;
    }

    @Override
    public void submit(TrackRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        queue.submitCustomGeometry(matrices, state.layer, state);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 96 * 2;
    }

    public static Vec3 getModelAngles(Vec3 normal, Vec3 diff) {
        double diffX = diff.x();
        double diffY = diff.y();
        double diffZ = diff.z();
        double len = Mth.sqrt((float) (diffX * diffX + diffZ * diffZ));
        double yaw = Mth.atan2(diffX, diffZ);
        double pitch = Mth.atan2(len, diffY) - Math.PI * .5;

        Vec3 yawPitchNormal = VecHelper.rotate(VecHelper.rotate(new Vec3(0, 1, 0), AngleHelper.deg(pitch), Axis.X), AngleHelper.deg(yaw), Axis.Y);

        double signum = Math.signum(yawPitchNormal.dot(normal));
        if (Math.abs(signum) < 0.5f)
            signum = yawPitchNormal.distanceToSqr(normal) < 0.5f ? -1 : 1;
        double dot = diff.cross(normal).normalize().dot(yawPitchNormal);
        double roll = Math.acos(Mth.clamp(dot, -1, 1)) * signum;
        return new Vec3(pitch, yaw, roll);
    }

    public static class SegmentAngles {
        public final int length;
        public final @NotNull Pose[] tieTransform;
        public final @NotNull Couple<Pose>[] railTransforms;
        public final @NotNull BlockPos[] lightPosition;

        @SuppressWarnings("unchecked")
        SegmentAngles(BezierConnection bc) {
            int segmentCount = bc.getSegmentCount();

            length = segmentCount + 1;

            tieTransform = new Pose[segmentCount + 1];
            railTransforms = new Couple[segmentCount + 1];
            lightPosition = new BlockPos[segmentCount + 1];

            Couple<Vec3> previousOffsets = null;

            for (BezierConnection.Segment segment : bc) {
                int i = segment.index;
                boolean end = i == 0 || i == segmentCount;

                Couple<Vec3> railOffsets = Couple.create(
                    segment.position.add(segment.normal.scale(.965f)),
                    segment.position.subtract(segment.normal.scale(.965f))
                );
                Vec3 railMiddle = railOffsets.getFirst().add(railOffsets.getSecond()).scale(.5);

                if (previousOffsets == null) {
                    previousOffsets = railOffsets;
                    continue;
                }

                // Tie
                Vec3 prevMiddle = previousOffsets.getFirst().add(previousOffsets.getSecond()).scale(.5);
                Vec3 tieAngles = TrackRenderer.getModelAngles(segment.normal, railMiddle.subtract(prevMiddle));
                lightPosition[i] = BlockPos.containing(railMiddle);
                railTransforms[i] = Couple.create(null, null);

                PoseStack poseStack = new PoseStack();
                TransformStack.of(poseStack).translate(prevMiddle).rotateY((float) tieAngles.y).rotateX((float) tieAngles.x)
                    .rotateZ((float) tieAngles.z).translate(-1 / 2f, -2 / 16f - 1 / 256f, 0);
                tieTransform[i] = poseStack.last();

                // Rails
                float scale = end ? 2.2f : 2.1f;
                for (boolean first : Iterate.trueAndFalse) {
                    Vec3 railI = railOffsets.get(first);
                    Vec3 prevI = previousOffsets.get(first);
                    Vec3 diff = railI.subtract(prevI);
                    Vec3 anglesI = TrackRenderer.getModelAngles(segment.normal, diff);

                    poseStack = new PoseStack();
                    TransformStack.of(poseStack).translate(prevI).rotateY((float) anglesI.y).rotateX((float) anglesI.x).rotateZ((float) anglesI.z)
                        .translate(0, -2 / 16f - 1 / 256f, -1 / 32f).scale(1, 1, (float) diff.length() * scale);
                    railTransforms[i].set(first, poseStack.last());
                }

                previousOffsets = railOffsets;
            }
        }

    }

    public static class GirderAngles {
        public final int length;
        public final Couple<Pose>[] beams;
        public final Couple<Couple<Pose>>[] beamCaps;
        public final BlockPos[] lightPosition;

        @SuppressWarnings("unchecked")
        GirderAngles(BezierConnection bc) {
            int segmentCount = bc.getSegmentCount();
            length = segmentCount + 1;

            beams = new Couple[length];
            beamCaps = new Couple[length];
            lightPosition = new BlockPos[length];

            Couple<Couple<Vec3>> previousOffsets = null;

            for (BezierConnection.Segment segment : bc) {
                int i = segment.index;
                boolean end = i == 0 || i == segmentCount;
                Vec3 leftGirder = segment.position.add(segment.normal.scale(.965f));
                Vec3 rightGirder = segment.position.subtract(segment.normal.scale(.965f));
                Vec3 upNormal = segment.derivative.normalize().cross(segment.normal);
                Vec3 firstGirderOffset = upNormal.scale(-8 / 16f);
                Vec3 secondGirderOffset = upNormal.scale(-10 / 16f);
                Vec3 leftTop = segment.position.add(segment.normal.scale(1)).add(firstGirderOffset);
                Vec3 rightTop = segment.position.subtract(segment.normal.scale(1)).add(firstGirderOffset);
                Vec3 leftBottom = leftTop.add(secondGirderOffset);
                Vec3 rightBottom = rightTop.add(secondGirderOffset);

                lightPosition[i] = BlockPos.containing(leftGirder.add(rightGirder).scale(.5));

                Couple<Couple<Vec3>> offsets = Couple.create(Couple.create(leftTop, rightTop), Couple.create(leftBottom, rightBottom));

                if (previousOffsets == null) {
                    previousOffsets = offsets;
                    continue;
                }

                beams[i] = Couple.create(null, null);
                beamCaps[i] = Couple.create(Couple.create(null, null), Couple.create(null, null));
                float scale = end ? 2.3f : 2.2f;

                for (boolean first : Iterate.trueAndFalse) {

                    // Middle
                    Vec3 currentBeam = offsets.getFirst().get(first).add(offsets.getSecond().get(first)).scale(.5);
                    Vec3 previousBeam = previousOffsets.getFirst().get(first).add(previousOffsets.getSecond().get(first)).scale(.5);
                    Vec3 beamDiff = currentBeam.subtract(previousBeam);
                    Vec3 beamAngles = TrackRenderer.getModelAngles(segment.normal, beamDiff);

                    PoseStack poseStack = new PoseStack();
                    TransformStack.of(poseStack).translate(previousBeam).rotateY((float) beamAngles.y).rotateX((float) beamAngles.x)
                        .rotateZ((float) beamAngles.z).translate(0, 2 / 16f + (segment.index % 2 == 0 ? 1 : -1) / 2048f - 1 / 1024f, -1 / 32f)
                        .scale(1, 1, (float) beamDiff.length() * scale);
                    beams[i].set(first, poseStack.last());

                    // Caps
                    for (boolean top : Iterate.trueAndFalse) {
                        Vec3 current = offsets.get(top).get(first);
                        Vec3 previous = previousOffsets.get(top).get(first);
                        Vec3 diff = current.subtract(previous);
                        Vec3 capAngles = TrackRenderer.getModelAngles(segment.normal, diff);

                        poseStack = new PoseStack();
                        TransformStack.of(poseStack).translate(previous).rotateY((float) capAngles.y).rotateX((float) capAngles.x)
                            .rotateZ((float) capAngles.z).translate(0, 2 / 16f + (segment.index % 2 == 0 ? 1 : -1) / 2048f - 1 / 1024f, -1 / 32f)
                            .rotateZ(0).scale(1, 1, (float) diff.length() * scale);
                        beamCaps[i].get(top).set(first, poseStack.last());
                    }
                }

                previousOffsets = offsets;

            }
        }

    }

    public static class TrackRenderState extends BlockEntityRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public GirderRenderState girder;
        public Map<TrackMaterial, TrackSegmentRenderState> tracks;

        @Override
        public void render(Pose matricesEntry, VertexConsumer vertexConsumer) {
            if (girder != null) {
                girder.render(matricesEntry, vertexConsumer);
            }
            if (tracks != null) {
                for (TrackSegmentRenderState track : tracks.values()) {
                    track.render(matricesEntry, vertexConsumer);
                }
            }
        }
    }

    public record GirderRenderState(
        SuperByteBuffer girderMiddle, SuperByteBuffer girderTop, SuperByteBuffer girderBottom, List<GirderSegmentData> girders
    ) {
        public static GirderRenderState create() {
            BlockState air = Blocks.AIR.defaultBlockState();
            SuperByteBuffer middle = CachedBuffers.partial(AllPartialModels.GIRDER_SEGMENT_MIDDLE, air);
            SuperByteBuffer top = CachedBuffers.partial(AllPartialModels.GIRDER_SEGMENT_TOP, air);
            SuperByteBuffer bottom = CachedBuffers.partial(AllPartialModels.GIRDER_SEGMENT_BOTTOM, air);
            return new GirderRenderState(middle, top, bottom, new ArrayList<>());
        }

        public void add(int light, Couple<Pose> beam, Couple<Couple<Pose>> beamCap) {
            girders.add(new GirderSegmentData(light, beam, beamCap));
        }

        public void render(Pose matricesEntry, VertexConsumer vertexConsumer) {
            for (GirderSegmentData girder : girders) {
                for (boolean first : Iterate.trueAndFalse) {
                    Pose beamTransform = girder.beam.get(first);
                    girderMiddle.mulPose(beamTransform.pose()).mulNormal(beamTransform.normal()).light(girder.light)
                        .renderInto(matricesEntry, vertexConsumer);
                    for (boolean top : Iterate.trueAndFalse) {
                        Pose beamCapTransform = girder.beamCaps.get(top).get(first);
                        (top ? girderTop : girderBottom).mulPose(beamCapTransform.pose()).mulNormal(beamCapTransform.normal()).light(girder.light)
                            .renderInto(matricesEntry, vertexConsumer);
                    }
                }
            }
        }

        public record GirderSegmentData(
            int light, Couple<Pose> beam, Couple<Couple<Pose>> beamCaps
        ) {
        }
    }

    public record TrackSegmentRenderState(SuperByteBuffer tie, SuperByteBuffer left, SuperByteBuffer right, List<TrackSegmentData> tracks) {
        public static TrackSegmentRenderState create(TrackMaterial material) {
            TrackModelHolder modelHolder = material.getModelHolder();
            BlockState air = Blocks.AIR.defaultBlockState();
            SuperByteBuffer tie = CachedBuffers.partial(modelHolder.tie(), air);
            SuperByteBuffer left = CachedBuffers.partial(modelHolder.leftSegment(), air);
            SuperByteBuffer right = CachedBuffers.partial(modelHolder.rightSegment(), air);
            return new TrackSegmentRenderState(tie, left, right, new ArrayList<>());
        }

        public void add(int light, Pose tieTransform, Couple<Pose> railTransforms) {
            tracks.add(new TrackSegmentData(light, tieTransform, railTransforms));
        }

        public void render(Pose matricesEntry, VertexConsumer vertexConsumer) {
            for (TrackSegmentData track : tracks) {
                tie.mulPose(track.tieTransform.pose()).mulNormal(track.tieTransform.normal()).light(track.light)
                    .renderInto(matricesEntry, vertexConsumer);
                for (boolean first : Iterate.trueAndFalse) {
                    Pose transform = track.railTransforms.get(first);
                    (first ? left : right).mulPose(transform.pose()).mulNormal(transform.normal()).light(track.light)
                        .renderInto(matricesEntry, vertexConsumer);
                }
            }
        }

        public record TrackSegmentData(
            int light, Pose tieTransform, Couple<Pose> railTransforms
        ) {
        }
    }
}