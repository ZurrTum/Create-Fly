package com.zurrtum.create.client.content.kinetics.gauge;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.ShaftRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.kinetics.gauge.GaugeBlock;
import com.zurrtum.create.content.kinetics.gauge.GaugeBlock.Type;
import com.zurrtum.create.content.kinetics.gauge.GaugeBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GaugeRenderer extends ShaftRenderer<GaugeBlockEntity, GaugeRenderer.GaugeRenderState> {
    protected Type type;

    public static GaugeRenderer speed(BlockEntityRendererProvider.Context context) {
        return new GaugeRenderer(context, Type.SPEED);
    }

    public static GaugeRenderer stress(BlockEntityRendererProvider.Context context) {
        return new GaugeRenderer(context, Type.STRESS);
    }

    protected GaugeRenderer(BlockEntityRendererProvider.Context context, Type type) {
        super(context);
        this.type = type;
    }

    @Override
    public GaugeRenderState createRenderState() {
        return new GaugeRenderState();
    }

    @Override
    public void extractRenderState(
        GaugeBlockEntity be,
        GaugeRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        if (state.support) {
            return;
        }
        BlockState gaugeState = be.getBlockState();
        GaugeBlock block = (GaugeBlock) gaugeState.getBlock();
        Level world = be.getLevel();
        List<Direction> facings = new ArrayList<>();
        for (Direction facing : Iterate.directions) {
            if (block.shouldRenderHeadOnFace(world, state.blockPos, gaugeState, facing)) {
                facings.add(facing);
            }
        }
        if (facings.isEmpty()) {
            return;
        }
        int size = facings.size();
        float[] angles = new float[size];
        for (int i = 0; i < size; i++) {
            angles[i] = (float) ((-facings.get(i).toYRot() - 90) / 180 * Math.PI);
        }
        state.angles = angles;
        PartialModel partialModel = (type == Type.SPEED ? AllPartialModels.GAUGE_HEAD_SPEED : AllPartialModels.GAUGE_HEAD_STRESS);
        state.head = CachedBuffers.partial(partialModel, gaugeState);
        state.dial = CachedBuffers.partial(AllPartialModels.GAUGE_DIAL, gaugeState);
        state.dialPivot = 5.75f / 16;
        float progress = Mth.lerp(tickProgress, be.prevDialState, be.dialState);
        state.rotate = (float) (Math.PI / 2 * -progress);
    }

    public static class GaugeRenderState extends KineticRenderState {
        public float @Nullable [] angles;
        public SuperByteBuffer head;
        public SuperByteBuffer dial;
        public float dialPivot;
        public float rotate;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            super.render(matricesEntry, vertexConsumer);
            if (angles != null) {
                for (float angle : angles) {
                    dial.rotateCentered(angle, Direction.UP).translate(0, dialPivot, dialPivot).rotate(rotate, Direction.EAST)
                        .translate(0, -dialPivot, -dialPivot).light(lightCoords).renderInto(matricesEntry, vertexConsumer);
                    head.rotateCentered(angle, Direction.UP).light(lightCoords).renderInto(matricesEntry, vertexConsumer);
                }
            }
        }
    }
}
