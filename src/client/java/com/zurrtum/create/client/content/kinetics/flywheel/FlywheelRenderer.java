package com.zurrtum.create.client.content.kinetics.flywheel;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.flywheel.FlywheelBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class FlywheelRenderer extends KineticBlockEntityRenderer<FlywheelBlockEntity, FlywheelRenderer.FlywheelRenderState> {
    public FlywheelRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public FlywheelRenderState createRenderState() {
        return new FlywheelRenderState();
    }

    @Override
    public void updateRenderState(
        FlywheelBlockEntity be,
        FlywheelRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay
    ) {
        super.updateRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        BlockState blockState = be.getCachedState();
        state.wheel = CachedBuffers.block(blockState);
        float speed = be.visualSpeed.getValue(tickProgress) * 3 / 10f;
        state.wheelAngle = AngleHelper.rad(be.angle + speed * tickProgress);
    }

    @Override
    protected RenderLayer getRenderType(FlywheelBlockEntity be, BlockState state) {
        return RenderLayer.getSolid();
    }

    @Override
    protected BlockState getRenderedBlockState(FlywheelBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }

    public static class FlywheelRenderState extends KineticRenderState {
        public SuperByteBuffer wheel;
        public float wheelAngle;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            super.render(matricesEntry, vertexConsumer);
            wheel.light(lightmapCoordinates).rotateCentered(wheelAngle, direction).color(color).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
