package com.zurrtum.create.client.content.contraptions.render;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.flywheel.lib.transform.PoseTransformStack;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.contraptions.ControlledContraptionEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;

public class ControlledContraptionEntityRenderer extends ContraptionEntityRenderer<ControlledContraptionEntity, ControlledContraptionEntityRenderer.ControlledContraptionState> {
    public ControlledContraptionEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public ControlledContraptionState createRenderState() {
        return new ControlledContraptionState();
    }

    @Override
    public void updateRenderState(ControlledContraptionEntity entity, ControlledContraptionState state, float tickProgress) {
        super.updateRenderState(entity, state, tickProgress);
        state.angle = MathHelper.RADIANS_PER_DEGREE * (tickProgress == 1.0F ? entity.angle : AngleHelper.angleLerp(
            tickProgress,
            entity.prevAngle,
            entity.angle
        ));
        state.axis = entity.getRotationAxis();
        state.seed = entity.getId();
    }

    @Override
    public void transform(ControlledContraptionState state, MatrixStack matrixStack, float partialTicks) {
        PoseTransformStack transformStack = TransformStack.of(matrixStack).nudge(state.seed);
        if (state.axis != null) {
            transformStack.center().rotate(state.angle, state.axis).uncenter();
        }
    }

    public static class ControlledContraptionState extends AbstractContraptionState {
        float angle;
        int seed;
        Axis axis;
    }
}
