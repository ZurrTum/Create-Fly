package com.zurrtum.create.client.content.kinetics.mixer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class MechanicalMixerRenderer implements BlockEntityRenderer<MechanicalMixerBlockEntity, MechanicalMixerRenderer.MechanicalMixerRenderState> {
    public MechanicalMixerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public MechanicalMixerRenderState createRenderState() {
        return new MechanicalMixerRenderState();
    }

    @Override
    public void extractRenderState(
        MechanicalMixerBlockEntity be,
        MechanicalMixerRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        Level world = be.getLevel();
        if (VisualizationManager.supportsVisualization(world)) {
            return;
        }
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.layer = RenderTypes.cutoutMovingBlock();
        state.cogwheel = CachedBuffers.partial(AllPartialModels.SHAFTLESS_COGWHEEL, state.blockState);
        Axis axis = ((IRotate) state.blockState.getBlock()).getRotationAxis(state.blockState);
        state.angle = KineticBlockEntityRenderer.getAngleForBe(be, state.blockPos, axis);
        state.direction = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
        state.color = KineticBlockEntityRenderer.getColor(be);
        state.headOffset = -be.getRenderedHeadOffset(tickProgress);
        state.pole = CachedBuffers.partial(AllPartialModels.MECHANICAL_MIXER_POLE, state.blockState);
        state.head = CachedBuffers.partial(AllPartialModels.MECHANICAL_MIXER_HEAD, state.blockState);
        float speed = be.getRenderedHeadRotationSpeed(tickProgress);
        float time = AnimationTickHolder.getRenderTime(world);
        state.headAngle = ((time * speed * 6 / 10f) % 360) / 180 * (float) Math.PI;
    }

    @Override
    public void submit(MechanicalMixerRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        queue.submitCustomGeometry(matrices, state.layer, state);
    }

    public static class MechanicalMixerRenderState extends BlockEntityRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public float angle;
        public Direction direction;
        public Color color;
        public SuperByteBuffer cogwheel;
        public float headOffset;
        public SuperByteBuffer pole;
        public SuperByteBuffer head;
        public float headAngle;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            cogwheel.rotateCentered(angle, direction).color(color).light(lightCoords).renderInto(matricesEntry, vertexConsumer);
            pole.translate(0, headOffset, 0).light(lightCoords).renderInto(matricesEntry, vertexConsumer);
            head.rotateCentered(headAngle, Direction.UP).translate(0, headOffset, 0).light(lightCoords).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
