package com.zurrtum.create.client.content.contraptions.render;

import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.contraptions.ControlledContraptionEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction.Axis;

public class ControlledContraptionVisual extends ContraptionVisual<ControlledContraptionEntity> {
    public ControlledContraptionVisual(VisualizationContext ctx, ControlledContraptionEntity entity, float partialTick) {
        super(ctx, entity, partialTick);
    }

    @Override
    public void transform(MatrixStack matrixStack, float partialTicks) {
        Axis axis = entity.getRotationAxis();
        if (axis != null) {
            float angle = entity.getAngle(partialTicks);
            TransformStack.of(matrixStack).nudge(entity.getId()).center().rotateDegrees(angle, axis).uncenter();
        }
    }
}
