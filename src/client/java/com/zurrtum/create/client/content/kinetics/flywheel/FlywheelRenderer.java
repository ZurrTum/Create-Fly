package com.zurrtum.create.client.content.kinetics.flywheel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.flywheel.FlywheelBlockEntity;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class FlywheelRenderer extends KineticBlockEntityRenderer<FlywheelBlockEntity, FlywheelRenderer.FlywheelRenderState> {
    public FlywheelRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public FlywheelRenderState createRenderState() {
        return new FlywheelRenderState();
    }

    @Override
    public void extractRenderState(
        FlywheelBlockEntity be,
        FlywheelRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        BlockState blockState = be.getBlockState();
        state.wheel = CachedBuffers.block(blockState);
        float speed = be.visualSpeed.getValue(tickProgress) * 3 / 10f;
        state.wheelAngle = AngleHelper.rad(be.angle + speed * tickProgress);
    }

    @Override
    protected RenderType getRenderType(FlywheelBlockEntity be, BlockState state) {
        return RenderType.solid();
    }

    @Override
    protected BlockState getRenderedBlockState(FlywheelBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }

    public static class FlywheelRenderState extends KineticRenderState {
        public SuperByteBuffer wheel;
        public float wheelAngle;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            super.render(matricesEntry, vertexConsumer);
            wheel.light(lightCoords).rotateCentered(wheelAngle, direction).color(color).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
