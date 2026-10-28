package com.zurrtum.create.client.content.trains.track;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllTrackMaterialModels.TrackModelHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.trains.track.TrackRenderer.TrackRenderState;
import com.zurrtum.create.client.content.trains.track.TrackRenderer.TrackSegmentRenderState.TrackSegmentBuffers;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.BezierConnection.Segment;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import com.zurrtum.create.content.trains.track.TrackMaterial;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TrackRenderer implements BlockEntityRenderer<TrackBlockEntity, TrackRenderState> {
    public TrackRenderer(Context context) {
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
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = be.getLevel();
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        GirderRenderState girder = null;
        TrackSegmentRenderState track = null;
        for (BezierConnection bc : be.getConnections().values()) {
            if (!bc.isPrimary()) {
                continue;
            }
            BlockPos bePosition = bc.bePositions.getFirst();
            if (bc.hasGirder) {
                GirderAngles segment = bc.getBakedGirders(GirderAngles::new);
                int length = segment.length;
                if (length != 0) {
                    if (girder == null) {
                        girder = GirderRenderState.create(cardinalLighting);
                    }
                    for (int i = 0; i < length; i++) {
                        girder.add(
                            LightCoordsUtil.getLightCoords(level, segment.lightPosition[i].offset(bePosition)),
                            segment.beams[i],
                            segment.beamCaps[i]
                        );
                    }
                }
            }
            SegmentAngles segment = bc.getBakedSegments(SegmentAngles::new);
            int length = segment.length;
            if (length != 0) {
                if (track == null) {
                    track = TrackSegmentRenderState.create(cardinalLighting);
                }
                TrackSegmentBuffers buffers = track.getBuffers(bc.getMaterial());
                for (int i = 0; i < length; i++) {
                    track.add(
                        buffers,
                        LightCoordsUtil.getLightCoords(level, segment.lightPosition[i].offset(bePosition)),
                        segment.tieTransform[i],
                        segment.railTransforms[i]
                    );
                }
            }
        }
        if (track == null && girder == null) {
            return;
        }
        state.blockPos = be.getBlockPos();
        state.blockEntityType = be.getType();
        state.girder = girder;
        state.track = track;
    }

    @Override
    public void submit(
        TrackRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.girder != null) {
            state.girder.submit(matrices, queue);
        }
        if (state.track != null) {
            state.track.submit(matrices, queue);
        }
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
        double pitch = Mth.atan2(len, diffY) - Math.PI * 0.5;

        Vec3 yawPitchNormal = VecHelper.rotate(
            VecHelper.rotate(new Vec3(0, 1, 0), AngleHelper.deg(pitch), Direction.Axis.X),
            AngleHelper.deg(yaw),
            Direction.Axis.Y
        );

        double signum = Math.signum(yawPitchNormal.dot(normal));
        if (Math.abs(signum) < 0.5f) {
            signum = yawPitchNormal.distanceToSqr(normal) < 0.5f ? -1 : 1;
        }
        double dot = diff.cross(normal).normalize().dot(yawPitchNormal);
        double roll = Math.acos(Mth.clamp(dot, -1, 1)) * signum;
        return new Vec3(pitch, yaw, roll);
    }

    public static class SegmentAngles {
        public final int length;
        public final Pose[] tieTransform;
        public final Couple<Pose>[] railTransforms;
        public final BlockPos[] lightPosition;

        @SuppressWarnings({"unchecked", "DataFlowIssue"})
        SegmentAngles(BezierConnection bc) {
            length = bc.getSegmentCount();
            if (length == 0) {
                tieTransform = null;
                railTransforms = null;
                lightPosition = null;
                return;
            }
            tieTransform = new Pose[length];
            railTransforms = new Couple[length];
            lightPosition = new BlockPos[length];
            Iterator<Segment> iterator = bc.iterator();
            Segment segment = iterator.next();
            Couple<Vec3> previousOffsets = Couple.create(
                segment.position.add(segment.normal.scale(0.965f)),
                segment.position.subtract(segment.normal.scale(0.965f))
            );
            Pose pose;
            int i = 0;
            while (iterator.hasNext()) {
                segment = iterator.next();
                Couple<Vec3> railOffsets = Couple.create(
                    segment.position.add(segment.normal.scale(0.965f)),
                    segment.position.subtract(segment.normal.scale(0.965f))
                );
                Vec3 railMiddle = railOffsets.getFirst().add(railOffsets.getSecond()).scale(0.5);

                // Tie
                Vec3 prevMiddle = previousOffsets.getFirst().add(previousOffsets.getSecond()).scale(0.5);
                Vec3 tieAngles = getModelAngles(segment.normal, railMiddle.subtract(prevMiddle));
                lightPosition[i] = BlockPos.containing(railMiddle);
                railTransforms[i] = Couple.create(null, null);

                pose = new Pose();
                pose.translate((float) prevMiddle.x, (float) prevMiddle.y, (float) prevMiddle.z);
                pose.rotateYXZ((float) tieAngles.y, (float) tieAngles.x, (float) tieAngles.z);
                pose.translate(-0.5f, -0.125f - 1 / 256.0f, 0);
                tieTransform[i] = pose;

                // Rails
                float scale = segment.index == length ? 2.2f : 2.1f;
                for (boolean first : Iterate.trueAndFalse) {
                    Vec3 railI = railOffsets.get(first);
                    Vec3 prevI = previousOffsets.get(first);
                    Vec3 diff = railI.subtract(prevI);
                    Vec3 anglesI = getModelAngles(segment.normal, diff);

                    pose = new Pose();
                    pose.translate((float) prevI.x, (float) prevI.y, (float) prevI.z);
                    pose.rotateYXZ((float) anglesI.y, (float) anglesI.x, (float) anglesI.z);
                    pose.translate(0, -0.125f - 1 / 256.0f, -0.03125f);
                    pose.scale(1, 1, (float) diff.length() * scale);
                    railTransforms[i].set(first, pose);
                }

                previousOffsets = railOffsets;
                i++;
            }
        }

    }

    public static class GirderAngles {
        public final int length;
        public final Couple<Pose>[] beams;
        public final Couple<Couple<Pose>>[] beamCaps;
        public final BlockPos[] lightPosition;

        @SuppressWarnings({"unchecked", "DataFlowIssue"})
        GirderAngles(BezierConnection bc) {
            length = bc.getSegmentCount();
            if (length == 0) {
                beams = null;
                beamCaps = null;
                lightPosition = null;
                return;
            }
            beams = new Couple[length];
            beamCaps = new Couple[length];
            lightPosition = new BlockPos[length];
            Iterator<Segment> iterator = bc.iterator();
            Segment segment = iterator.next();
            Vec3 upNormal = segment.derivative.normalize().cross(segment.normal);
            Vec3 firstGirderOffset = upNormal.scale(-0.5f);
            Vec3 secondGirderOffset = upNormal.scale(-0.625f);
            Vec3 leftTop = segment.position.add(segment.normal.scale(1)).add(firstGirderOffset);
            Vec3 rightTop = segment.position.subtract(segment.normal.scale(1)).add(firstGirderOffset);
            Vec3 leftBottom = leftTop.add(secondGirderOffset);
            Vec3 rightBottom = rightTop.add(secondGirderOffset);
            Couple<Couple<Vec3>> previousOffsets = Couple.create(
                Couple.create(leftTop, rightTop),
                Couple.create(leftBottom, rightBottom)
            );
            Pose pose;
            int i = 0;
            while (iterator.hasNext()) {
                segment = iterator.next();
                upNormal = segment.derivative.normalize().cross(segment.normal);
                firstGirderOffset = upNormal.scale(-0.5f);
                secondGirderOffset = upNormal.scale(-0.625f);
                leftTop = segment.position.add(segment.normal.scale(1)).add(firstGirderOffset);
                rightTop = segment.position.subtract(segment.normal.scale(1)).add(firstGirderOffset);
                leftBottom = leftTop.add(secondGirderOffset);
                rightBottom = rightTop.add(secondGirderOffset);
                Couple<Couple<Vec3>> offsets = Couple.create(
                    Couple.create(leftTop, rightTop),
                    Couple.create(leftBottom, rightBottom)
                );
                Vec3 leftGirder = segment.position.add(segment.normal.scale(0.965f));
                Vec3 rightGirder = segment.position.subtract(segment.normal.scale(0.965f));
                lightPosition[i] = BlockPos.containing(leftGirder.add(rightGirder).scale(0.5));
                beams[i] = Couple.create(null, null);
                beamCaps[i] = Couple.create(Couple.create(null, null), Couple.create(null, null));
                float scale = segment.index == length ? 2.3f : 2.2f;

                int offsetSign = i % 2 == 0 ? -1 : 1;
                for (boolean first : Iterate.trueAndFalse) {
                    // Middle
                    Vec3 currentBeam = offsets.getFirst().get(first).add(offsets.getSecond().get(first)).scale(0.5);
                    Vec3 previousBeam = previousOffsets.getFirst().get(first)
                        .add(previousOffsets.getSecond().get(first)).scale(0.5);
                    Vec3 beamDiff = currentBeam.subtract(previousBeam);
                    Vec3 beamAngles = getModelAngles(segment.normal, beamDiff);

                    pose = new Pose();
                    pose.translate((float) previousBeam.x, (float) previousBeam.y, (float) previousBeam.z);
                    pose.rotateYXZ((float) beamAngles.y, (float) beamAngles.x, (float) beamAngles.z);
                    pose.translate(0, 0.125f + offsetSign / 2048.0f - 1 / 1024.0f, -0.03125f);
                    pose.scale(1, 1, (float) beamDiff.length() * scale);
                    beams[i].set(first, pose);

                    // Caps
                    for (boolean top : Iterate.trueAndFalse) {
                        Vec3 current = offsets.get(top).get(first);
                        Vec3 previous = previousOffsets.get(top).get(first);
                        Vec3 diff = current.subtract(previous);
                        Vec3 capAngles = getModelAngles(segment.normal, diff);

                        pose = new Pose();
                        pose.translate((float) previous.x, (float) previous.y, (float) previous.z);
                        pose.rotateYXZ((float) capAngles.y, (float) capAngles.x, (float) capAngles.z);
                        pose.translate(0, 0.125f + offsetSign / 2048.0f - 1 / 1024.0f, -0.03125f);
                        pose.scale(1, 1, (float) diff.length() * scale);
                        beamCaps[i].get(top).set(first, pose);
                    }
                }
                previousOffsets = offsets;
                i++;
            }
        }
    }

    public static class TrackRenderState extends BlockEntityRenderState {
        public @Nullable GirderRenderState girder;
        public @Nullable TrackSegmentRenderState track;
    }

    public record GirderRenderState(@Nullable CardinalLighting cardinalLighting, SuperByteBuffer middle,
                                    SuperByteBuffer top, SuperByteBuffer bottom, Int2ObjectMap<GirderModels> cache,
                                    List<GirderSegmentData> girders) {
        public static GirderRenderState create(@Nullable CardinalLighting cardinalLighting) {
            BlockState state = Blocks.AIR.defaultBlockState();
            return new GirderRenderState(
                cardinalLighting,
                CachedBuffers.partial(AllPartialModels.GIRDER_SEGMENT_MIDDLE, state),
                CachedBuffers.partial(AllPartialModels.GIRDER_SEGMENT_TOP, state),
                CachedBuffers.partial(AllPartialModels.GIRDER_SEGMENT_BOTTOM, state),
                new Int2ObjectOpenHashMap<>(),
                new ArrayList<>()
            );
        }

        public void add(int light, Couple<Pose> beam, Couple<Couple<Pose>> beamCap) {
            girders.add(new GirderSegmentData(cache.computeIfAbsent(light, this::createModels), beam, beamCap));
        }

        public GirderModels createModels(int light) {
            return new GirderModels(
                middle.cardinalLighting(cardinalLighting).light(light).extractRenderState(),
                top.cardinalLighting(cardinalLighting).light(light).extractRenderState(),
                bottom.cardinalLighting(cardinalLighting).light(light).extractRenderState()
            );
        }

        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            for (GirderSegmentData girder : girders) {
                girder.model.submit(matrices, queue, girder.beam, girder.beamCaps);
            }
        }

        public record GirderModels(SuperByteBufferRenderState middle, SuperByteBufferRenderState top,
                                   SuperByteBufferRenderState bottom) {
            public void submit(
                PoseStack matrices,
                SubmitNodeCollector queue,
                Couple<Pose> beam,
                Couple<Couple<Pose>> beamCaps
            ) {
                middle.submit(beam.getFirst(), matrices, queue);
                top.submit(beamCaps.getFirst().getFirst(), matrices, queue);
                bottom.submit(beamCaps.getSecond().getFirst(), matrices, queue);
                middle.submit(beam.getSecond(), matrices, queue);
                top.submit(beamCaps.getFirst().getSecond(), matrices, queue);
                bottom.submit(beamCaps.getSecond().getSecond(), matrices, queue);
            }
        }

        public record GirderSegmentData(GirderModels model, Couple<Pose> beam, Couple<Couple<Pose>> beamCaps) {
        }
    }

    public record TrackSegmentRenderState(@Nullable CardinalLighting cardinalLighting,
                                          Object2ObjectMap<TrackMaterial, TrackSegmentBuffers> models,
                                          List<TrackSegmentData> tracks) {
        public static TrackSegmentRenderState create(@Nullable CardinalLighting cardinalLighting) {
            return new TrackSegmentRenderState(cardinalLighting, new Object2ObjectOpenHashMap<>(), new ArrayList<>());
        }

        public TrackSegmentBuffers getBuffers(TrackMaterial material) {
            return models.computeIfAbsent(material, this::createBuffers);
        }

        private TrackSegmentBuffers createBuffers(TrackMaterial material) {
            TrackModelHolder modelHolder = material.getModelHolder();
            BlockState air = Blocks.AIR.defaultBlockState();
            return new TrackSegmentBuffers(
                cardinalLighting,
                CachedBuffers.partial(modelHolder.tie(), air),
                CachedBuffers.partial(modelHolder.leftSegment(), air),
                CachedBuffers.partial(modelHolder.rightSegment(), air),
                new Int2ObjectOpenHashMap<>()
            );
        }

        public void add(TrackSegmentBuffers buffers, int light, Pose tieTransform, Couple<Pose> railTransforms) {
            tracks.add(new TrackSegmentData(buffers.getModels(light), tieTransform, railTransforms));
        }

        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            for (TrackSegmentData track : tracks) {
                track.model.submit(matrices, queue, track.tieTransform, track.railTransforms);
            }
        }

        public record TrackSegmentBuffers(@Nullable CardinalLighting cardinalLighting, SuperByteBuffer tie,
                                          SuperByteBuffer left, SuperByteBuffer right,
                                          Int2ObjectMap<TrackSegmentModels> cache) {
            public TrackSegmentModels getModels(int light) {
                return cache.computeIfAbsent(light, this::createModels);
            }

            private TrackSegmentModels createModels(int light) {
                return new TrackSegmentModels(
                    tie.cardinalLighting(cardinalLighting).light(light).extractRenderState(),
                    left.cardinalLighting(cardinalLighting).light(light).extractRenderState(),
                    right.cardinalLighting(cardinalLighting).light(light).extractRenderState()
                );
            }
        }

        public record TrackSegmentModels(SuperByteBufferRenderState tie, SuperByteBufferRenderState left,
                                         SuperByteBufferRenderState right) {
            public void submit(
                PoseStack matrices,
                SubmitNodeCollector queue,
                Pose tieTransform,
                Couple<Pose> railTransforms
            ) {
                tie.submit(tieTransform, matrices, queue);
                left.submit(railTransforms.getFirst(), matrices, queue);
                right.submit(railTransforms.getSecond(), matrices, queue);
            }
        }

        public record TrackSegmentData(TrackSegmentModels model, Pose tieTransform, Couple<Pose> railTransforms) {
        }
    }
}