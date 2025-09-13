package com.zurrtum.create.client.content.contraptions.render;

import com.zurrtum.create.AllContraptionTypeTags;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.OrientedContraptionEntity;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;


public class OrientedContraptionEntityRenderer<C extends OrientedContraptionEntity, S extends OrientedContraptionEntityRenderer.OrientedContraptionState> extends ContraptionEntityRenderer<C, S> {
    public OrientedContraptionEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    @SuppressWarnings("unchecked")
    public S createRenderState() {
        return (S) new OrientedContraptionState();
    }

    @Override
    public boolean shouldRender(C entity, Frustum frustum, double cameraX, double cameraY, double cameraZ) {
        if (!super.shouldRender(entity, frustum, cameraX, cameraY, cameraZ))
            return false;
        return entity.getVehicle() != null || !entity.getContraption().getType().is(AllContraptionTypeTags.REQUIRES_VEHICLE_FOR_RENDER);
    }

    @Override
    public void updateRenderState(C entity, S state, float tickProgress) {
        super.updateRenderState(entity, state, tickProgress);
        state.seed = entity.getId();
        state.angleInitialYaw = entity.getInitialYaw();
        state.prevYaw = entity.prevYaw;
        state.yaw = entity.yaw;
        state.prevPitch = entity.prevPitch;
        state.pitch = entity.pitch;
        Entity ridingEntity = entity.getVehicle();
        if (ridingEntity instanceof AbstractMinecartEntity cart) {
            state.cart = cart;
        } else if (ridingEntity instanceof AbstractContraptionEntity be) {
            if (ridingEntity.getVehicle() instanceof AbstractMinecartEntity cart) {
                state.cart = cart;
            } else {
                state.cart = null;
                state.entity = entity;
                state.riding = be;
                return;
            }
        } else {
            state.cart = null;
        }
        state.entity = null;
        state.riding = null;
    }

    @Override
    public void transform(OrientedContraptionState state, MatrixStack matrixStack, float partialTicks) {
        float angleYaw = -(partialTicks == 1.0F ? state.yaw : AngleHelper.angleLerp(partialTicks, state.prevYaw, state.yaw));
        float anglePitch = partialTicks == 1.0F ? state.pitch : AngleHelper.angleLerp(partialTicks, state.prevPitch, state.pitch);
        matrixStack.translate(-.5f, 0, -.5f);

        if (state.cart != null) {
            OrientedContraptionVisual.repositionOnCart(matrixStack, partialTicks, state.cart);
        } else if (state.riding != null) {
            OrientedContraptionVisual.repositionOnContraption(state.entity, matrixStack, partialTicks, state.riding);
        }

        TransformStack.of(matrixStack).nudge(state.seed).center().rotateYDegrees(angleYaw).rotateZDegrees(anglePitch)
            .rotateYDegrees(state.angleInitialYaw).uncenter();
    }

    public static class OrientedContraptionState extends AbstractContraptionState {
        int seed;
        float angleInitialYaw;
        float prevYaw;
        float yaw;
        float prevPitch;
        float pitch;
        AbstractMinecartEntity cart = null;
        OrientedContraptionEntity entity = null;
        AbstractContraptionEntity riding = null;
    }
}
