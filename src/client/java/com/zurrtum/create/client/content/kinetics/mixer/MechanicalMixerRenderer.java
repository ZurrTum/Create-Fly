package com.zurrtum.create.client.content.kinetics.mixer;

import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MechanicalMixerRenderer implements BlockEntityRenderer<MechanicalMixerBlockEntity, MechanicalMixerRenderer.MechanicalMixerRenderState> {
    public MechanicalMixerRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public boolean rendersOutsideBoundingBox() {
        return true;
    }

    @Override
    public MechanicalMixerRenderState createRenderState() {
        return new MechanicalMixerRenderState();
    }

    @Override
    public void updateRenderState(
        MechanicalMixerBlockEntity be,
        MechanicalMixerRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay
    ) {
        World world = be.getWorld();
        if (VisualizationManager.supportsVisualization(world)) {
            return;
        }
        BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        state.layer = RenderLayer.getCutoutMipped();
        state.cogwheel = CachedBuffers.partial(AllPartialModels.SHAFTLESS_COGWHEEL, state.blockState);
        Axis axis = ((IRotate) state.blockState.getBlock()).getRotationAxis(state.blockState);
        state.angle = KineticBlockEntityRenderer.getAngleForBe(be, state.pos, axis);
        state.direction = Direction.from(axis, Direction.AxisDirection.POSITIVE);
        state.color = KineticBlockEntityRenderer.getColor(be);
        state.headOffset = -be.getRenderedHeadOffset(tickProgress);
        state.pole = CachedBuffers.partial(AllPartialModels.MECHANICAL_MIXER_POLE, state.blockState);
        state.head = CachedBuffers.partial(AllPartialModels.MECHANICAL_MIXER_HEAD, state.blockState);
        float speed = be.getRenderedHeadRotationSpeed(tickProgress);
        float time = AnimationTickHolder.getRenderTime(world);
        state.headAngle = ((time * speed * 6 / 10f) % 360) / 180 * (float) Math.PI;
    }

    @Override
    public void render(MechanicalMixerRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        queue.submitCustom(matrices, state.layer, state);
    }

    public static class MechanicalMixerRenderState extends BlockEntityRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public float angle;
        public Direction direction;
        public Color color;
        public SuperByteBuffer cogwheel;
        public float headOffset;
        public SuperByteBuffer pole;
        public SuperByteBuffer head;
        public float headAngle;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            cogwheel.rotateCentered(angle, direction).color(color).light(lightmapCoordinates).renderInto(matricesEntry, vertexConsumer);
            pole.translate(0, headOffset, 0).light(lightmapCoordinates).renderInto(matricesEntry, vertexConsumer);
            head.rotateCentered(headAngle, Direction.UP).translate(0, headOffset, 0).light(lightmapCoordinates)
                .renderInto(matricesEntry, vertexConsumer);
        }
    }
}
