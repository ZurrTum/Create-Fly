package com.zurrtum.create.client.content.kinetics.speedController;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.speedController.SpeedControllerBlock;
import com.zurrtum.create.content.kinetics.speedController.SpeedControllerBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SpeedControllerRenderer implements BlockEntityRenderer<SpeedControllerBlockEntity, SpeedControllerRenderer.SpeedControllerRenderState> {
    public SpeedControllerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public SpeedControllerRenderState createRenderState() {
        return new SpeedControllerRenderState();
    }

    @Override
    public void extractRenderState(
        SpeedControllerBlockEntity be,
        SpeedControllerRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        Level world = be.getLevel();
        state.render = !VisualizationManager.supportsVisualization(world);
        if (state.render) {
            state.model = getRotatedModel(be);
            Axis axis = ((IRotate) state.blockState.getBlock()).getRotationAxis(state.blockState);
            state.direction = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
            state.angle = KineticBlockEntityRenderer.getAngleForBe(be, state.blockPos, axis);
            state.color = KineticBlockEntityRenderer.getColor(be);
        }
        state.hasBracket = be.hasBracket;
        if (state.hasBracket) {
            state.bracket = CachedBuffers.partial(AllPartialModels.SPEED_CONTROLLER_BRACKET, state.blockState);
            boolean alongX = state.blockState.getValue(SpeedControllerBlock.HORIZONTAL_AXIS) == Axis.X;
            state.bracketAngle = (float) (alongX ? Math.PI : Math.PI / 2);
            state.bracketLight = world != null ? LevelRenderer.getLightColor(world, state.blockPos.above()) : LightTexture.FULL_BRIGHT;
        }
        if (state.render || state.hasBracket) {
            state.layer = RenderType.solid();
        }
    }

    @Override
    public void submit(SpeedControllerRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        if (state.render || state.hasBracket) {
            queue.submitCustomGeometry(matrices, state.layer, state);
        }
    }

    private SuperByteBuffer getRotatedModel(SpeedControllerBlockEntity blockEntity) {
        return CachedBuffers.block(
            KineticBlockEntityRenderer.KINETIC_BLOCK,
            KineticBlockEntityRenderer.shaft(KineticBlockEntityRenderer.getRotationAxisOf(blockEntity))
        );
    }

    public static class SpeedControllerRenderState extends BlockEntityRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public boolean render;
        public SuperByteBuffer model;
        public Direction direction;
        public float angle;
        public Color color;
        public boolean hasBracket;
        public SuperByteBuffer bracket;
        public float bracketAngle;
        public int bracketLight;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            if (render) {
                model.light(lightCoords);
                model.rotateCentered(angle, direction);
                model.color(color);
                model.renderInto(matricesEntry, vertexConsumer);
            }
            if (hasBracket) {
                bracket.translate(0, 1, 0);
                bracket.rotateCentered(bracketAngle, Direction.UP);
                bracket.light(bracketLight);
                bracket.renderInto(matricesEntry, vertexConsumer);
            }
        }
    }
}
