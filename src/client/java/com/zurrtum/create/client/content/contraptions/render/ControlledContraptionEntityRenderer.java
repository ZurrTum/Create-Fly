package com.zurrtum.create.client.content.contraptions.render;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.contraptions.ControlledContraptionEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction.Axis;

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
        state.prevAngle = entity.prevAngle;
        state.angle = entity.angle;
        state.axis = entity.getRotationAxis();
        state.seed = entity.getId();
    }

    @Override
    public void transform(ControlledContraptionState state, MatrixStack matrixStack, float partialTicks) {
        if (state.axis != null) {
            float angle = partialTicks == 1.0F ? state.angle : AngleHelper.angleLerp(partialTicks, state.prevAngle, state.angle);
            TransformStack.of(matrixStack).nudge(state.seed).center().rotateDegrees(angle, state.axis).uncenter();
        }
    }

    public static class ControlledContraptionState extends AbstractContraptionState {
        float prevAngle;
        float angle;
        int seed;
        Axis axis;
    }
}
