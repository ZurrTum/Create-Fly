package com.zurrtum.create.client.content.kinetics.gauge;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.kinetics.base.ShaftVisual;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.Instancer;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.FlatLit;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.content.kinetics.gauge.GaugeBlock;
import com.zurrtum.create.content.kinetics.gauge.GaugeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.function.Consumer;

public abstract class GaugeVisual extends ShaftVisual<GaugeBlockEntity> implements SimpleDynamicVisual {

    protected final ArrayList<DialFace> faces = new ArrayList<>(2);

    protected final PoseStack ms = new PoseStack();

    protected GaugeVisual(VisualizationContext context, GaugeBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        GaugeBlock gaugeBlock = (GaugeBlock) blockState.getBlock();

        Instancer<TransformedInstance> dialModel = instancerProvider().instancer(
            InstanceTypes.TRANSFORMED,
            Models.chunkPartial(AllPartialModels.GAUGE_DIAL)
        );
        Instancer<TransformedInstance> headModel = getHeadModel();

        BlockPos position = getVisualPosition();
        ms.translate(position.getX(), position.getY(), position.getZ());

        float progress = Mth.lerp(
            AnimationTickHolder.getPartialTicks(),
            blockEntity.prevDialState,
            blockEntity.dialState
        );

        for (Direction facing : Iterate.directions) {
            if (!gaugeBlock.shouldRenderHeadOnFace(level, pos, blockState, facing)) {
                continue;
            }

            DialFace face = makeFace(facing, dialModel, headModel);

            faces.add(face);

            face.setupTransform(ms, progress);
        }
    }

    @Override
    public void setSectionCollector(SectionCollector sectionCollector) {
        int minX = 0;
        int minZ = 0;
        int maxX = 0;
        int maxZ = 0;
        for (DialFace face : faces) {
            switch (face.face) {
                case WEST -> minX = -1;
                case NORTH -> minZ = -1;
                case EAST -> maxX = 1;
                case SOUTH -> maxZ = 1;
            }
        }
        setSectionCollector(sectionCollector, minX, 0, minZ, maxX, 0, maxZ);
    }

    private DialFace makeFace(
        Direction face,
        Instancer<TransformedInstance> dialModel,
        Instancer<TransformedInstance> headModel
    ) {
        return new DialFace(face, dialModel.createInstance(), headModel.createInstance());
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        if (Mth.equal(blockEntity.prevDialState, blockEntity.dialState)) {
            return;
        }

        float progress = Mth.lerp(ctx.partialTick(), blockEntity.prevDialState, blockEntity.dialState);

        for (DialFace faceEntry : faces) {
            faceEntry.updateTransform(ms, progress);
        }
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);

        relight(faces.stream().flatMap(Couple::stream).toArray(FlatLit[]::new));
    }

    @Override
    protected void _delete() {
        super._delete();

        faces.forEach(DialFace::delete);
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        for (DialFace face : faces) {
            face.forEach(consumer);
        }
    }

    protected abstract Instancer<TransformedInstance> getHeadModel();

    protected class DialFace extends Couple<TransformedInstance> {

        Direction face;

        public DialFace(Direction face, TransformedInstance first, TransformedInstance second) {
            super(first, second);
            this.face = face;
        }

        private void setupTransform(PoseStack ms, float progress) {
            float dialPivot = 5.75f / 16;

            ms.pushPose();
            Pose pose = ms.last();
            rotateToFace(pose);

            getSecond().setTransform(pose).setChanged();

            if (progress != 0) {
                pose.rotateXAround(-Radians.DEG_90 * progress, 0, dialPivot, dialPivot);
            }

            getFirst().setTransform(pose).setChanged();

            ms.popPose();
        }

        private void updateTransform(PoseStack ms, float progress) {
            float dialPivot = 5.75f / 16;

            ms.pushPose();

            Pose pose = ms.last();
            rotateToFace(pose);
            if (progress != 0) {
                pose.rotateXAround(progress * -Radians.DEG_90, 0, dialPivot, dialPivot);
            }

            getFirst().setTransform(pose).setChanged();

            ms.popPose();
        }

        protected void rotateToFace(Pose pose) {
            float angle = -face.toYRot() - 90;
            if (angle != 0) {
                pose.rotateYCentered(Mth.DEG_TO_RAD * angle);
            }
        }

        private void delete() {
            getFirst().delete();
            getSecond().delete();
        }
    }

    public static class Speed extends GaugeVisual {
        public Speed(VisualizationContext context, GaugeBlockEntity blockEntity, float partialTick) {
            super(context, blockEntity, partialTick);
        }

        @Override
        protected Instancer<TransformedInstance> getHeadModel() {
            return instancerProvider().instancer(
                InstanceTypes.TRANSFORMED,
                Models.chunkPartial(AllPartialModels.GAUGE_HEAD_SPEED)
            );
        }
    }

    public static class Stress extends GaugeVisual {
        public Stress(VisualizationContext context, GaugeBlockEntity blockEntity, float partialTick) {
            super(context, blockEntity, partialTick);
        }

        @Override
        protected Instancer<TransformedInstance> getHeadModel() {
            return instancerProvider().instancer(
                InstanceTypes.TRANSFORMED,
                Models.chunkPartial(AllPartialModels.GAUGE_HEAD_STRESS)
            );
        }
    }
}
