package com.zurrtum.create.client.content.contraptions.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.content.contraptions.ControlledContraptionEntity;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;

public class ControlledContraptionVisual extends ContraptionVisual<ControlledContraptionEntity> {
    public ControlledContraptionVisual(
        VisualizationContext ctx,
        ControlledContraptionEntity entity,
        float partialTick
    ) {
        super(ctx, entity, partialTick);
    }

    @Override
    public void transform(PoseStack matrixStack, float partialTicks) {
        Axis axis = entity.getRotationAxis();
        if (axis != null) {
            float angle = entity.getAngle(partialTicks);
            Pose pose = matrixStack.last();
            SuperByteBuffer.nudge(pose, entity.getId());
            pose.rotateCentered(axis, Mth.DEG_TO_RAD * angle);
        }
    }
}
