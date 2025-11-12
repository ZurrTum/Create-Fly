package com.zurrtum.create.client.content.equipment.bell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.content.equipment.bell.AbstractBellBlockEntity;
import com.zurrtum.create.content.equipment.bell.PeculiarBellBlockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BellRenderer<BE extends AbstractBellBlockEntity> implements BlockEntityRenderer<BE, BellRenderer.BellRenderState> {
    public BellRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public BellRenderState createRenderState() {
        return new BellRenderState();
    }

    @Override
    public void extractRenderState(
        BE be,
        BellRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.layer = RenderType.cutout();
        state.model = CachedBuffers.partial(
            be instanceof PeculiarBellBlockEntity ? AllPartialModels.PECULIAR_BELL : AllPartialModels.HAUNTED_BELL,
            state.blockState
        );
        if (be.isRinging) {
            state.direction = be.ringDirection.getCounterClockWise();
            state.angle = getSwingAngle(be.ringingTicks + tickProgress);
        }
        Direction facing = state.blockState.getValue(BellBlock.FACING);
        BellAttachType attachment = state.blockState.getValue(BellBlock.ATTACHMENT);
        float rY = AngleHelper.horizontalAngle(facing);
        if (attachment == BellAttachType.SINGLE_WALL || attachment == BellAttachType.DOUBLE_WALL)
            rY += 90;
        state.upAngle = AngleHelper.rad(rY);
    }

    @Override
    public void submit(BellRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        queue.submitCustomGeometry(matrices, state.layer, state);
    }

    public static float getSwingAngle(float time) {
        float t = time / 1.5f;
        return 1.2f * Mth.sin(t / (float) Math.PI) / (2.5f + t / 3.0f);
    }

    public static class BellRenderState extends BlockEntityRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public SuperByteBuffer model;
        public float upAngle;
        public Direction direction;
        public float angle;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            if (direction != null) {
                model.rotateCentered(angle, direction);
            }
            model.rotateCentered(upAngle, Direction.UP);
            model.light(lightCoords);
            model.renderInto(matricesEntry, vertexConsumer);
        }
    }
}
