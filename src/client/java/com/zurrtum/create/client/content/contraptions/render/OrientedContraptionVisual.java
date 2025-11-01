package com.zurrtum.create.client.content.contraptions.render;

import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.OrientedContraptionEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.DefaultMinecartController;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class OrientedContraptionVisual<T extends OrientedContraptionEntity> extends ContraptionVisual<T> {
    public OrientedContraptionVisual(VisualizationContext ctx, T entity, float partialTick) {
        super(ctx, entity, partialTick);
    }

    @Override
    public void transform(MatrixStack matrixStack, float partialTicks) {
        float angleInitialYaw = entity.getInitialYaw();
        float angleYaw = entity.getViewYRot(partialTicks);
        float anglePitch = entity.getViewXRot(partialTicks);

        matrixStack.translate(-.5f, 0, -.5f);

        Entity ridingEntity = entity.getVehicle();
        if (ridingEntity instanceof AbstractMinecartEntity cart)
            repositionOnCart(matrixStack, partialTicks, cart);
        else if (ridingEntity instanceof AbstractContraptionEntity be) {
            if (ridingEntity.getVehicle() instanceof AbstractMinecartEntity cart)
                repositionOnCart(matrixStack, partialTicks, cart);
            else
                repositionOnContraption(entity, matrixStack, partialTicks, be);
        }

        TransformStack.of(matrixStack).nudge(entity.getId()).center().rotateYDegrees(angleYaw).rotateZDegrees(anglePitch)
            .rotateYDegrees(angleInitialYaw).uncenter();
    }

    // Minecarts do not always render at their exact location, so the contraption
    // has to adjust aswell
    public static void repositionOnCart(MatrixStack matrixStack, float partialTicks, AbstractMinecartEntity ridingEntity) {
        Vec3d cartPos = getCartOffset(partialTicks, ridingEntity);

        if (cartPos == Vec3d.ZERO)
            return;

        matrixStack.translate(cartPos.x, cartPos.y, cartPos.z);
    }

    public static Vec3d getCartOffset(float partialTicks, AbstractMinecartEntity cart) {
        if (!(cart.getController() instanceof DefaultMinecartController controller)) {
            return Vec3d.ZERO;
        }
        double cartX = MathHelper.lerp(partialTicks, cart.lastRenderX, cart.getX());
        double cartY = MathHelper.lerp(partialTicks, cart.lastRenderY, cart.getY());
        double cartZ = MathHelper.lerp(partialTicks, cart.lastRenderZ, cart.getZ());

        Vec3d cartPos = controller.snapPositionToRail(cartX, cartY, cartZ);
        if (cartPos != null) {
            Vec3d cartPosFront = controller.simulateMovement(cartX, cartY, cartZ, 0.3F);
            Vec3d cartPosBack = controller.simulateMovement(cartX, cartY, cartZ, -0.3F);
            if (cartPosFront == null)
                cartPosFront = cartPos;
            if (cartPosBack == null)
                cartPosBack = cartPos;

            cartX = cartPos.x - cartX;
            cartY = (cartPosFront.y + cartPosBack.y) / 2.0D - cartY;
            cartZ = cartPos.z - cartZ;

            return new Vec3d(cartX, cartY, cartZ);
        }

        return Vec3d.ZERO;
    }

    public static void repositionOnContraption(
        OrientedContraptionEntity entity,
        MatrixStack matrixStack,
        float partialTicks,
        AbstractContraptionEntity ridingEntity
    ) {
        Vec3d pos = getContraptionOffset(entity, partialTicks, ridingEntity);
        matrixStack.translate(pos.x, pos.y, pos.z);
    }

    public static Vec3d getContraptionOffset(OrientedContraptionEntity entity, float partialTicks, AbstractContraptionEntity parent) {
        Vec3d passengerPosition = parent.getPassengerPosition(entity, partialTicks);
        if (passengerPosition == null)
            return Vec3d.ZERO;

        double x = passengerPosition.x - MathHelper.lerp(partialTicks, entity.lastRenderX, entity.getX());
        double y = passengerPosition.y - MathHelper.lerp(partialTicks, entity.lastRenderY, entity.getY());
        double z = passengerPosition.z - MathHelper.lerp(partialTicks, entity.lastRenderZ, entity.getZ());

        return new Vec3d(x, y, z);
    }
}
