package com.zurrtum.create.client.content.contraptions.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.AllContraptionTypeTags;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.OrientedContraptionEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.phys.Vec3;


public class OrientedContraptionEntityRenderer<C extends OrientedContraptionEntity, S extends OrientedContraptionEntityRenderer.OrientedContraptionState> extends ContraptionEntityRenderer<C, S> {
    public OrientedContraptionEntityRenderer(EntityRendererProvider.Context context) {
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
    public void extractRenderState(C entity, S state, float tickProgress) {
        Entity ridingEntity = entity.getVehicle();
        if (ridingEntity instanceof AbstractMinecart cart) {
            state.offset = OrientedContraptionVisual.getCartOffset(tickProgress, cart);
        } else if (ridingEntity instanceof AbstractContraptionEntity be) {
            if (ridingEntity.getVehicle() instanceof AbstractMinecart cart) {
                state.offset = OrientedContraptionVisual.getCartOffset(tickProgress, cart);
            } else {
                state.offset = OrientedContraptionVisual.getContraptionOffset(entity, tickProgress, be);
            }
        }
        state.seed = entity.getId();
        boolean done = tickProgress == 1.0F;
        state.angleYaw = Mth.DEG_TO_RAD * (done ? -entity.yaw : -AngleHelper.angleLerp(tickProgress, entity.prevYaw, entity.yaw));
        state.anglePitch = Mth.DEG_TO_RAD * (done ? entity.pitch : AngleHelper.angleLerp(tickProgress, entity.prevPitch, entity.pitch));
        state.angleInitialYaw = Mth.DEG_TO_RAD * entity.getInitialYaw();
        super.extractRenderState(entity, state, tickProgress);
    }

    @Override
    public void transform(OrientedContraptionState state, PoseStack matrixStack) {
        matrixStack.translate(-.5f, 0, -.5f);
        if (state.offset != null) {
            matrixStack.translate(state.offset);
        }
        TransformStack.of(matrixStack).nudge(state.seed).center().rotateY(state.angleYaw).rotateZ(state.anglePitch).rotateY(state.angleInitialYaw)
            .uncenter();
    }

    public static class OrientedContraptionState extends AbstractContraptionState {
        public float angleYaw;
        public float anglePitch;
        public float angleInitialYaw;
        int seed;
        Vec3 offset;
    }
}
