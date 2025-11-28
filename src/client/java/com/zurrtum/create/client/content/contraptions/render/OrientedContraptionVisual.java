package com.zurrtum.create.client.content.contraptions.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.OrientedContraptionEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import net.minecraft.world.phys.Vec3;

public class OrientedContraptionVisual<T extends OrientedContraptionEntity> extends ContraptionVisual<T> {
    public OrientedContraptionVisual(VisualizationContext ctx, T entity, float partialTick) {
        super(ctx, entity, partialTick);
    }

    @Override
    public void transform(PoseStack matrixStack, float partialTicks) {
        float angleInitialYaw = entity.getInitialYaw();
        float angleYaw = entity.getViewYRot(partialTicks);
        float anglePitch = entity.getViewXRot(partialTicks);

        matrixStack.translate(-.5f, 0, -.5f);

        Entity ridingEntity = entity.getVehicle();
        if (ridingEntity instanceof AbstractMinecart cart)
            repositionOnCart(matrixStack, partialTicks, cart);
        else if (ridingEntity instanceof AbstractContraptionEntity be) {
            if (ridingEntity.getVehicle() instanceof AbstractMinecart cart)
                repositionOnCart(matrixStack, partialTicks, cart);
            else
                repositionOnContraption(entity, matrixStack, partialTicks, be);
        }

        TransformStack.of(matrixStack).nudge(entity.getId()).center().rotateYDegrees(angleYaw).rotateZDegrees(anglePitch)
            .rotateYDegrees(angleInitialYaw).uncenter();
    }

    // Minecarts do not always render at their exact location, so the contraption
    // has to adjust aswell
    public static void repositionOnCart(PoseStack matrixStack, float partialTicks, AbstractMinecart ridingEntity) {
        Vec3 cartPos = getCartOffset(partialTicks, ridingEntity);

        if (cartPos == Vec3.ZERO)
            return;

        matrixStack.translate(cartPos.x, cartPos.y, cartPos.z);
    }

    public static Vec3 getCartOffset(float partialTicks, AbstractMinecart cart) {
        MinecartBehavior behavior = cart.getBehavior();
        if (behavior instanceof OldMinecartBehavior controller) {
            double cartX = Mth.lerp(partialTicks, cart.xOld, cart.getX());
            double cartY = Mth.lerp(partialTicks, cart.yOld, cart.getY());
            double cartZ = Mth.lerp(partialTicks, cart.zOld, cart.getZ());
            Vec3 cartPos = controller.getPos(cartX, cartY, cartZ);
            if (cartPos != null) {
                Vec3 cartPosFront = controller.getPosOffs(cartX, cartY, cartZ, 0.3F);
                Vec3 cartPosBack = controller.getPosOffs(cartX, cartY, cartZ, -0.3F);
                if (cartPosFront == null)
                    cartPosFront = cartPos;
                if (cartPosBack == null)
                    cartPosBack = cartPos;

                cartX = cartPos.x - cartX;
                cartY = (cartPosFront.y + cartPosBack.y) / 2.0D - cartY;
                cartZ = cartPos.z - cartZ;

                return new Vec3(cartX, cartY, cartZ);
            }
        } else if (behavior instanceof NewMinecartBehavior controller && controller.cartHasPosRotLerp()) {
            double cartX = Mth.lerp(partialTicks, cart.xOld, cart.getX());
            double cartY = Mth.lerp(partialTicks, cart.yOld, cart.getY());
            double cartZ = Mth.lerp(partialTicks, cart.zOld, cart.getZ());
            Vec3 cartPos = controller.getCartLerpPosition(partialTicks);
            return new Vec3(cartPos.x - cartX, cartPos.y - cartY, cartPos.z - cartZ);
        }

        return Vec3.ZERO;
    }

    public static void repositionOnContraption(
        OrientedContraptionEntity entity,
        PoseStack matrixStack,
        float partialTicks,
        AbstractContraptionEntity ridingEntity
    ) {
        Vec3 pos = getContraptionOffset(entity, partialTicks, ridingEntity);
        matrixStack.translate(pos.x, pos.y, pos.z);
    }

    public static Vec3 getContraptionOffset(OrientedContraptionEntity entity, float partialTicks, AbstractContraptionEntity parent) {
        Vec3 passengerPosition = parent.getPassengerPosition(entity, partialTicks);
        if (passengerPosition == null)
            return Vec3.ZERO;

        double x = passengerPosition.x - Mth.lerp(partialTicks, entity.xOld, entity.getX());
        double y = passengerPosition.y - Mth.lerp(partialTicks, entity.yOld, entity.getY());
        double z = passengerPosition.z - Mth.lerp(partialTicks, entity.zOld, entity.getZ());

        return new Vec3(x, y, z);
    }
}
