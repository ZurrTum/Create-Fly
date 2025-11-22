package com.zurrtum.create.client.content.kinetics.speedController;

import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.speedController.SpeedControllerBlock;
import com.zurrtum.create.content.kinetics.speedController.SpeedControllerBlockEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
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

public class SpeedControllerRenderer implements BlockEntityRenderer<SpeedControllerBlockEntity, SpeedControllerRenderer.SpeedControllerRenderState> {
    public SpeedControllerRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public SpeedControllerRenderState createRenderState() {
        return new SpeedControllerRenderState();
    }

    @Override
    public void updateRenderState(
        SpeedControllerBlockEntity be,
        SpeedControllerRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        World world = be.getWorld();
        state.render = !VisualizationManager.supportsVisualization(world);
        if (state.render) {
            state.model = getRotatedModel(be);
            Axis axis = ((IRotate) state.blockState.getBlock()).getRotationAxis(state.blockState);
            state.direction = Direction.from(axis, Direction.AxisDirection.POSITIVE);
            state.angle = KineticBlockEntityRenderer.getAngleForBe(be, state.pos, axis);
            state.color = KineticBlockEntityRenderer.getColor(be);
        }
        state.hasBracket = be.hasBracket;
        if (state.hasBracket) {
            state.bracket = CachedBuffers.partial(AllPartialModels.SPEED_CONTROLLER_BRACKET, state.blockState);
            boolean alongX = state.blockState.get(SpeedControllerBlock.HORIZONTAL_AXIS) == Axis.X;
            state.bracketAngle = (float) (alongX ? Math.PI : Math.PI / 2);
            state.bracketLight = world != null ? WorldRenderer.getLightmapCoordinates(
                world,
                state.pos.up()
            ) : LightmapTextureManager.MAX_LIGHT_COORDINATE;
        }
        if (state.render || state.hasBracket) {
            state.layer = RenderLayer.getSolid();
        }
    }

    @Override
    public void render(SpeedControllerRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        if (state.render || state.hasBracket) {
            queue.submitCustom(matrices, state.layer, state);
        }
    }

    private SuperByteBuffer getRotatedModel(SpeedControllerBlockEntity blockEntity) {
        return CachedBuffers.block(
            KineticBlockEntityRenderer.KINETIC_BLOCK,
            KineticBlockEntityRenderer.shaft(KineticBlockEntityRenderer.getRotationAxisOf(blockEntity))
        );
    }

    public static class SpeedControllerRenderState extends BlockEntityRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
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
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            if (render) {
                model.light(lightmapCoordinates);
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
