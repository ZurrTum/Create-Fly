package com.zurrtum.create.client.content.kinetics.steamEngine;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SteamEngineRenderer implements BlockEntityRenderer<SteamEngineBlockEntity, SteamEngineRenderer.SteamEngineRenderState> {
    public SteamEngineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public SteamEngineRenderState createRenderState() {
        return new SteamEngineRenderState();
    }

    @Override
    public void extractRenderState(
        SteamEngineBlockEntity be,
        SteamEngineRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        if (VisualizationManager.supportsVisualization(be.getLevel())) {
            return;
        }
        Float angle = getTargetAngle(be);
        if (angle == null) {
            return;
        }
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.layer = RenderType.solid();
        Direction facing = SteamEngineBlock.getFacing(state.blockState);
        Axis facingAxis = facing.getAxis();
        Axis axis = Axis.Y;
        PoweredShaftBlockEntity shaft = be.getShaft();
        if (shaft != null) {
            axis = KineticBlockEntityRenderer.getRotationAxisOf(shaft);
        }
        boolean roll90 = facingAxis.isHorizontal() && axis == Axis.Y || facingAxis.isVertical() && axis == Axis.Z;
        float sinAngle = Mth.sin(angle);
        float cosAngle = Mth.cos(angle);
        float piston = ((6 / 16f) * sinAngle - Mth.sqrt(Mth.square(14 / 16f) - Mth.square(6 / 16f) * Mth.square(cosAngle)));
        float distance = Mth.sqrt(Mth.square(piston - 6 / 16f * sinAngle));
        state.piston = CachedBuffers.partial(AllPartialModels.ENGINE_PISTON, state.blockState);
        state.linkage = CachedBuffers.partial(AllPartialModels.ENGINE_LINKAGE, state.blockState);
        state.connector = CachedBuffers.partial(AllPartialModels.ENGINE_CONNECTOR, state.blockState);
        state.yRot = AngleHelper.horizontalAngle(facing);
        state.xRot = AngleHelper.verticalAngle(facing) + 90;
        state.roll = roll90 ? -90 : 0;
        state.linkageRotate = (float) Math.acos(distance / (14 / 16f)) * (cosAngle >= 0 ? 1f : -1f);
        state.pistonTranslate = piston + 20 / 16f;
        state.connectorRotate = -(angle + Mth.HALF_PI);
    }

    @Override
    public void submit(SteamEngineRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        if (state.layer != null) {
            queue.submitCustomGeometry(matrices, state.layer, state);
        }
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Nullable
    public static Float getTargetAngle(SteamEngineBlockEntity be) {
        BlockState blockState = be.getBlockState();
        if (!blockState.is(AllBlocks.STEAM_ENGINE))
            return null;

        Direction facing = SteamEngineBlock.getFacing(blockState);
        PoweredShaftBlockEntity shaft = be.getShaft();
        Axis facingAxis = facing.getAxis();

        if (shaft == null)
            return null;

        Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(shaft);
        float angle = KineticBlockEntityRenderer.getAngleForBe(shaft, shaft.getBlockPos(), axis);

        if (axis == facingAxis)
            return null;
        if (axis.isHorizontal() && (facingAxis == Axis.X ^ facing.getAxisDirection() == Direction.AxisDirection.POSITIVE))
            angle *= -1;
        if (axis == Axis.X && facing == Direction.DOWN)
            angle *= -1;
        return angle;
    }

    public static class SteamEngineRenderState extends BlockEntityRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
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
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            piston.center().rotateYDegrees(yRot).rotateXDegrees(xRot).rotateYDegrees(roll).uncenter().translate(0, pistonTranslate, 0)
                .light(lightCoords).renderInto(matricesEntry, vertexConsumer);
            linkage.center().rotateYDegrees(yRot).rotateXDegrees(xRot).rotateYDegrees(roll).translate(0, 1, 0).uncenter()
                .translate(0, pistonTranslate, 0).translate(0, 0.25f, 0.5f).rotateX(linkageRotate).translate(0, -0.25f, -0.5f).light(lightCoords)
                .renderInto(matricesEntry, vertexConsumer);
            connector.center().rotateYDegrees(yRot).rotateXDegrees(xRot).rotateYDegrees(roll).uncenter().translate(0, 2, 0).center()
                .rotateX(connectorRotate).uncenter().light(lightCoords).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
