package com.zurrtum.create.client.content.contraptions.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.flywheel.lib.transform.PoseTransformStack;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.contraptions.ControlledContraptionEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

public class ControlledContraptionEntityRenderer extends ContraptionEntityRenderer<ControlledContraptionEntity, ControlledContraptionEntityRenderer.ControlledContraptionState> {
    public ControlledContraptionEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ControlledContraptionState createRenderState() {
        return new ControlledContraptionState();
    }

    @Override
    public void extractRenderState(ControlledContraptionEntity entity, ControlledContraptionState state, float tickProgress) {
        state.angle = Mth.DEG_TO_RAD * (tickProgress == 1.0F ? entity.angle : AngleHelper.angleLerp(tickProgress, entity.prevAngle, entity.angle));
        state.axis = entity.getRotationAxis();
        state.seed = entity.getId();
        super.extractRenderState(entity, state, tickProgress);
    }

    @Override
    public void transform(ControlledContraptionState state, PoseStack matrixStack) {
        PoseTransformStack transformStack = TransformStack.of(matrixStack).nudge(state.seed);
        if (state.axis != null) {
            transformStack.center().rotate(state.angle, state.axis).uncenter();
        }
    }

    public static class ControlledContraptionState extends AbstractContraptionState {
        float angle;
        int seed;
        @Nullable Axis axis;
    }
}
