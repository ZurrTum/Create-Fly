package com.zurrtum.create.client.content.contraptions.render;

import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.contraptions.render.ContraptionEntityRenderer.AbstractContraptionState;
import com.zurrtum.create.content.contraptions.ControlledContraptionEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;

public class ControlledContraptionEntityRenderer extends ContraptionEntityRenderer<ControlledContraptionEntity, AbstractContraptionState> {
    public ControlledContraptionEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Pose createTransform(ControlledContraptionEntity entity, float tickProgress) {
        Pose pose = new Pose();
        SuperByteBuffer.nudge(pose, entity.getId());
        Axis axis = entity.getRotationAxis();
        if (axis != null) {
            float angle = entity.getAngle(tickProgress);
            if (angle != 0) {
                pose.rotateCentered(axis, Mth.DEG_TO_RAD * angle);
            }
        }
        return pose;
    }
}
