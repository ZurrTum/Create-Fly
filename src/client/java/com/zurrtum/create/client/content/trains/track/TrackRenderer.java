package com.zurrtum.create.client.content.trains.track;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllTrackMaterialModels.TrackModelHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class TrackRenderer extends SafeBlockEntityRenderer<TrackBlockEntity> {

    public TrackRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(TrackBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        World level = be.getWorld();
        if (VisualizationManager.supportsVisualization(level))
            return;
        VertexConsumer vb = buffer.getBuffer(RenderLayer.getCutoutMipped());
        be.getConnections().values().forEach(bc -> renderBezierTurn(level, bc, ms, vb));
    }

    public static void renderBezierTurn(World level, BezierConnection bc, MatrixStack ms, VertexConsumer vb) {
        if (!bc.isPrimary())
            return;

        ms.push();
        BlockPos bePosition = bc.bePositions.getFirst();
        BlockState air = Blocks.AIR.getDefaultState();
        SegmentAngles segment = bc.getBakedSegments(SegmentAngles::new);

        renderGirder(level, bc, ms, vb, bePosition);

        for (int i = 1; i < segment.length; i++) {
            int light = WorldRenderer.getLightmapCoordinates(level, segment.lightPosition[i].add(bePosition));

            TrackModelHolder modelHolder = bc.getMaterial().getModelHolder();

            CachedBuffers.partial(modelHolder.tie(), air).mulPose(segment.tieTransform[i].getPositionMatrix())
                .mulNormal(segment.tieTransform[i].getNormalMatrix()).light(light).renderInto(ms, vb);

            for (boolean first : Iterate.trueAndFalse) {
                Entry transform = segment.railTransforms[i].get(first);
                CachedBuffers.partial(first ? modelHolder.leftSegment() : modelHolder.rightSegment(), air).mulPose(transform.getPositionMatrix())
                    .mulNormal(transform.getNormalMatrix()).light(light).renderInto(ms, vb);
            }
        }

        ms.pop();
    }

    private static void renderGirder(World level, BezierConnection bc, MatrixStack ms, VertexConsumer vb, BlockPos tePosition) {
        if (!bc.hasGirder)
            return;

        BlockState air = Blocks.AIR.getDefaultState();
        GirderAngles segment = bc.getBakedGirders(GirderAngles::new);

        for (int i = 1; i < segment.length; i++) {
            int light = WorldRenderer.getLightmapCoordinates(level, segment.lightPosition[i].add(tePosition));

            for (boolean first : Iterate.trueAndFalse) {
                Entry beamTransform = segment.beams[i].get(first);
                CachedBuffers.partial(AllPartialModels.GIRDER_SEGMENT_MIDDLE, air).mulPose(beamTransform.getPositionMatrix())
                    .mulNormal(beamTransform.getNormalMatrix()).light(light).renderInto(ms, vb);

                for (boolean top : Iterate.trueAndFalse) {
                    Entry beamCapTransform = segment.beamCaps[i].get(top).get(first);
                    CachedBuffers.partial(top ? AllPartialModels.GIRDER_SEGMENT_TOP : AllPartialModels.GIRDER_SEGMENT_BOTTOM, air)
                        .mulPose(beamCapTransform.getPositionMatrix()).mulNormal(beamCapTransform.getNormalMatrix()).light(light).renderInto(ms, vb);
                }
            }
        }
    }

    public static Vec3d getModelAngles(Vec3d normal, Vec3d diff) {
        double diffX = diff.getX();
        double diffY = diff.getY();
        double diffZ = diff.getZ();
        double len = MathHelper.sqrt((float) (diffX * diffX + diffZ * diffZ));
        double yaw = MathHelper.atan2(diffX, diffZ);
        double pitch = MathHelper.atan2(len, diffY) - Math.PI * .5;

        Vec3d yawPitchNormal = VecHelper.rotate(VecHelper.rotate(new Vec3d(0, 1, 0), AngleHelper.deg(pitch), Axis.X), AngleHelper.deg(yaw), Axis.Y);

        double signum = Math.signum(yawPitchNormal.dotProduct(normal));
        if (Math.abs(signum) < 0.5f)
            signum = yawPitchNormal.squaredDistanceTo(normal) < 0.5f ? -1 : 1;
        double dot = diff.crossProduct(normal).normalize().dotProduct(yawPitchNormal);
        double roll = Math.acos(MathHelper.clamp(dot, -1, 1)) * signum;
        return new Vec3d(pitch, yaw, roll);
    }

    @Override
    public boolean rendersOutsideBoundingBox() {
        return true;
    }

    @Override
    public int getRenderDistance() {
        return 96 * 2;
    }


    public static class SegmentAngles {
        public final int length;
        public final @NotNull Entry[] tieTransform;
        public final @NotNull Couple<Entry>[] railTransforms;
        public final @NotNull BlockPos[] lightPosition;

        @SuppressWarnings("unchecked")
        SegmentAngles(BezierConnection bc) {
            int segmentCount = bc.getSegmentCount();

            length = segmentCount + 1;

            tieTransform = new Entry[segmentCount + 1];
            railTransforms = new Couple[segmentCount + 1];
            lightPosition = new BlockPos[segmentCount + 1];

            Couple<Vec3d> previousOffsets = null;

            for (BezierConnection.Segment segment : bc) {
                int i = segment.index;
                boolean end = i == 0 || i == segmentCount;

                Couple<Vec3d> railOffsets = Couple.create(
                    segment.position.add(segment.normal.multiply(.965f)),
                    segment.position.subtract(segment.normal.multiply(.965f))
                );
                Vec3d railMiddle = railOffsets.getFirst().add(railOffsets.getSecond()).multiply(.5);

                if (previousOffsets == null) {
                    previousOffsets = railOffsets;
                    continue;
                }

                // Tie
                Vec3d prevMiddle = previousOffsets.getFirst().add(previousOffsets.getSecond()).multiply(.5);
                Vec3d tieAngles = TrackRenderer.getModelAngles(segment.normal, railMiddle.subtract(prevMiddle));
                lightPosition[i] = BlockPos.ofFloored(railMiddle);
                railTransforms[i] = Couple.create(null, null);

                MatrixStack poseStack = new MatrixStack();
                TransformStack.of(poseStack).translate(prevMiddle).rotateY((float) tieAngles.y).rotateX((float) tieAngles.x)
                    .rotateZ((float) tieAngles.z).translate(-1 / 2f, -2 / 16f - 1 / 256f, 0);
                tieTransform[i] = poseStack.peek();

                // Rails
                float scale = end ? 2.2f : 2.1f;
                for (boolean first : Iterate.trueAndFalse) {
                    Vec3d railI = railOffsets.get(first);
                    Vec3d prevI = previousOffsets.get(first);
                    Vec3d diff = railI.subtract(prevI);
                    Vec3d anglesI = TrackRenderer.getModelAngles(segment.normal, diff);

                    poseStack = new MatrixStack();
                    TransformStack.of(poseStack).translate(prevI).rotateY((float) anglesI.y).rotateX((float) anglesI.x).rotateZ((float) anglesI.z)
                        .translate(0, -2 / 16f - 1 / 256f, -1 / 32f).scale(1, 1, (float) diff.length() * scale);
                    railTransforms[i].set(first, poseStack.peek());
                }

                previousOffsets = railOffsets;
            }
        }

    }

    public static class GirderAngles {
        public final int length;
        public final Couple<Entry>[] beams;
        public final Couple<Couple<Entry>>[] beamCaps;
        public final BlockPos[] lightPosition;

        @SuppressWarnings("unchecked")
        GirderAngles(BezierConnection bc) {
            int segmentCount = bc.getSegmentCount();
            length = segmentCount + 1;

            beams = new Couple[length];
            beamCaps = new Couple[length];
            lightPosition = new BlockPos[length];

            Couple<Couple<Vec3d>> previousOffsets = null;

            for (BezierConnection.Segment segment : bc) {
                int i = segment.index;
                boolean end = i == 0 || i == segmentCount;
                Vec3d leftGirder = segment.position.add(segment.normal.multiply(.965f));
                Vec3d rightGirder = segment.position.subtract(segment.normal.multiply(.965f));
                Vec3d upNormal = segment.derivative.normalize().crossProduct(segment.normal);
                Vec3d firstGirderOffset = upNormal.multiply(-8 / 16f);
                Vec3d secondGirderOffset = upNormal.multiply(-10 / 16f);
                Vec3d leftTop = segment.position.add(segment.normal.multiply(1)).add(firstGirderOffset);
                Vec3d rightTop = segment.position.subtract(segment.normal.multiply(1)).add(firstGirderOffset);
                Vec3d leftBottom = leftTop.add(secondGirderOffset);
                Vec3d rightBottom = rightTop.add(secondGirderOffset);

                lightPosition[i] = BlockPos.ofFloored(leftGirder.add(rightGirder).multiply(.5));

                Couple<Couple<Vec3d>> offsets = Couple.create(Couple.create(leftTop, rightTop), Couple.create(leftBottom, rightBottom));

                if (previousOffsets == null) {
                    previousOffsets = offsets;
                    continue;
                }

                beams[i] = Couple.create(null, null);
                beamCaps[i] = Couple.create(Couple.create(null, null), Couple.create(null, null));
                float scale = end ? 2.3f : 2.2f;

                for (boolean first : Iterate.trueAndFalse) {

                    // Middle
                    Vec3d currentBeam = offsets.getFirst().get(first).add(offsets.getSecond().get(first)).multiply(.5);
                    Vec3d previousBeam = previousOffsets.getFirst().get(first).add(previousOffsets.getSecond().get(first)).multiply(.5);
                    Vec3d beamDiff = currentBeam.subtract(previousBeam);
                    Vec3d beamAngles = TrackRenderer.getModelAngles(segment.normal, beamDiff);

                    MatrixStack poseStack = new MatrixStack();
                    TransformStack.of(poseStack).translate(previousBeam).rotateY((float) beamAngles.y).rotateX((float) beamAngles.x)
                        .rotateZ((float) beamAngles.z).translate(0, 2 / 16f + (segment.index % 2 == 0 ? 1 : -1) / 2048f - 1 / 1024f, -1 / 32f)
                        .scale(1, 1, (float) beamDiff.length() * scale);
                    beams[i].set(first, poseStack.peek());

                    // Caps
                    for (boolean top : Iterate.trueAndFalse) {
                        Vec3d current = offsets.get(top).get(first);
                        Vec3d previous = previousOffsets.get(top).get(first);
                        Vec3d diff = current.subtract(previous);
                        Vec3d capAngles = TrackRenderer.getModelAngles(segment.normal, diff);

                        poseStack = new MatrixStack();
                        TransformStack.of(poseStack).translate(previous).rotateY((float) capAngles.y).rotateX((float) capAngles.x)
                            .rotateZ((float) capAngles.z).translate(0, 2 / 16f + (segment.index % 2 == 0 ? 1 : -1) / 2048f - 1 / 1024f, -1 / 32f)
                            .rotateZ(0).scale(1, 1, (float) diff.length() * scale);
                        beamCaps[i].get(top).set(first, poseStack.peek());
                    }
                }

                previousOffsets = offsets;

            }
        }

    }
}