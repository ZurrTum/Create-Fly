package com.zurrtum.create.client.content.kinetics.steamEngine;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import com.zurrtum.create.content.kinetics.steamEngine.SteamEngineBlock;
import com.zurrtum.create.content.kinetics.steamEngine.SteamEngineBlockEntity;
import net.minecraft.block.BlockState;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class SteamEngineRenderer implements BlockEntityRenderer<SteamEngineBlockEntity, SteamEngineRenderer.SteamEngineRenderState> {
    public SteamEngineRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public SteamEngineRenderState createRenderState() {
        return new SteamEngineRenderState();
    }

    @Override
    public void updateRenderState(
        SteamEngineBlockEntity be,
        SteamEngineRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        if (VisualizationManager.supportsVisualization(be.getWorld())) {
            return;
        }
        Float angle = getTargetAngle(be);
        if (angle == null) {
            return;
        }
        BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        state.layer = RenderLayer.getSolid();
        Direction facing = SteamEngineBlock.getFacing(state.blockState);
        Axis facingAxis = facing.getAxis();
        Axis axis = Axis.Y;
        PoweredShaftBlockEntity shaft = be.getShaft();
        if (shaft != null) {
            axis = KineticBlockEntityRenderer.getRotationAxisOf(shaft);
        }
        boolean roll90 = facingAxis.isHorizontal() && axis == Axis.Y || facingAxis.isVertical() && axis == Axis.Z;
        float sinAngle = MathHelper.sin(angle);
        float cosAngle = MathHelper.cos(angle);
        float piston = ((6 / 16f) * sinAngle - MathHelper.sqrt(MathHelper.square(14 / 16f) - MathHelper.square(6 / 16f) * MathHelper.square(cosAngle)));
        float distance = MathHelper.sqrt(MathHelper.square(piston - 6 / 16f * sinAngle));
        state.piston = CachedBuffers.partial(AllPartialModels.ENGINE_PISTON, state.blockState);
        state.linkage = CachedBuffers.partial(AllPartialModels.ENGINE_LINKAGE, state.blockState);
        state.connector = CachedBuffers.partial(AllPartialModels.ENGINE_CONNECTOR, state.blockState);
        state.yRot = AngleHelper.horizontalAngle(facing);
        state.xRot = AngleHelper.verticalAngle(facing) + 90;
        state.roll = roll90 ? -90 : 0;
        state.linkageRotate = (float) Math.acos(distance / (14 / 16f)) * (cosAngle >= 0 ? 1f : -1f);
        state.pistonTranslate = piston + 20 / 16f;
        state.connectorRotate = -(angle + MathHelper.HALF_PI);
    }

    @Override
    public void render(SteamEngineRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        if (state.layer != null) {
            queue.submitCustom(matrices, state.layer, state);
        }
    }

    @Override
    public int getRenderDistance() {
        return 128;
    }

    @Nullable
    public static Float getTargetAngle(SteamEngineBlockEntity be) {
        BlockState blockState = be.getCachedState();
        if (!blockState.isOf(AllBlocks.STEAM_ENGINE))
            return null;

        Direction facing = SteamEngineBlock.getFacing(blockState);
        PoweredShaftBlockEntity shaft = be.getShaft();
        Axis facingAxis = facing.getAxis();

        if (shaft == null)
            return null;

        Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(shaft);
        float angle = KineticBlockEntityRenderer.getAngleForBe(shaft, shaft.getPos(), axis);

        if (axis == facingAxis)
            return null;
        if (axis.isHorizontal() && (facingAxis == Axis.X ^ facing.getDirection() == Direction.AxisDirection.POSITIVE))
            angle *= -1;
        if (axis == Axis.X && facing == Direction.DOWN)
            angle *= -1;
        return angle;
    }

    public static class SteamEngineRenderState extends BlockEntityRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public SuperByteBuffer piston;
        public SuperByteBuffer linkage;
        public SuperByteBuffer connector;
        public float yRot;
        public float xRot;
        public int roll;
        public float linkageRotate;
        public float pistonTranslate;
        public float connectorRotate;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            piston.center().rotateYDegrees(yRot).rotateXDegrees(xRot).rotateYDegrees(roll).uncenter().translate(0, pistonTranslate, 0)
                .light(lightmapCoordinates).renderInto(matricesEntry, vertexConsumer);
            linkage.center().rotateYDegrees(yRot).rotateXDegrees(xRot).rotateYDegrees(roll).translate(0, 1, 0).uncenter()
                .translate(0, pistonTranslate, 0).translate(0, 0.25f, 0.5f).rotateX(linkageRotate).translate(0, -0.25f, -0.5f)
                .light(lightmapCoordinates).renderInto(matricesEntry, vertexConsumer);
            connector.center().rotateYDegrees(yRot).rotateXDegrees(xRot).rotateYDegrees(roll).uncenter().translate(0, 2, 0).center()
                .rotateX(connectorRotate).uncenter().light(lightmapCoordinates).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
