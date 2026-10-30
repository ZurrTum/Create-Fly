package com.zurrtum.create.client.content.contraptions.render;

import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.AllContraptionTypeTags;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.contraptions.render.ContraptionEntityRenderer.AbstractContraptionState;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.OrientedContraptionEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.phys.Vec3;


public class OrientedContraptionEntityRenderer<C extends OrientedContraptionEntity, S extends AbstractContraptionState> extends ContraptionEntityRenderer<C, S> {
    public OrientedContraptionEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(
        C entity,
        Frustum frustum,
        double cameraX,
        double cameraY,
        double cameraZ,
        float partialTicks
    ) {
        if (!super.shouldRender(entity, frustum, cameraX, cameraY, cameraZ, partialTicks)) {
            return false;
        }
        return entity.getVehicle() != null || !entity.getContraption().getType()
            .is(AllContraptionTypeTags.REQUIRES_VEHICLE_FOR_RENDER);
    }

    @Override
    public Pose createTransform(OrientedContraptionEntity entity, float tickProgress) {
        Pose pose = new Pose();
        pose.translate(-0.5f, 0, -0.5f);
        Entity ridingEntity = entity.getVehicle();
        Vec3 offset;
        if (ridingEntity instanceof AbstractMinecart cart) {
            offset = OrientedContraptionVisual.getCartOffset(tickProgress, cart);
        } else if (ridingEntity instanceof AbstractContraptionEntity be) {
            if (ridingEntity.getVehicle() instanceof AbstractMinecart cart) {
                offset = OrientedContraptionVisual.getCartOffset(tickProgress, cart);
            } else {
                offset = OrientedContraptionVisual.getContraptionOffset(entity, tickProgress, be);
            }
        } else {
            offset = Vec3.ZERO;
        }
        if (offset != Vec3.ZERO) {
            pose.translate((float) offset.x, (float) offset.y, (float) offset.z);
        }
        SuperByteBuffer.nudge(pose, entity.getId());
        pose.translate(0.5f, 0.5f, 0.5f);
        float angleYaw, anglePitch;
        if (tickProgress == 1.0F) {
            angleYaw = -entity.yaw;
            anglePitch = entity.pitch;
        } else {
            angleYaw = -AngleHelper.angleLerp(tickProgress, entity.prevYaw, entity.yaw);
            anglePitch = AngleHelper.angleLerp(tickProgress, entity.prevPitch, entity.pitch);
        }
        if (angleYaw != 0) {
            pose.rotate(Axis.YP, Mth.DEG_TO_RAD * angleYaw);
        }
        if (anglePitch != 0) {
            pose.rotate(Axis.ZP, Mth.DEG_TO_RAD * anglePitch);
        }
        float angleInitialYaw = entity.getInitialYaw();
        if (angleInitialYaw != 0) {
            pose.rotate(Axis.YP, Mth.DEG_TO_RAD * angleInitialYaw);
        }
        pose.translate(-0.5f, -0.5f, -0.5f);
        return pose;
    }
}
