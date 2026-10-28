package com.zurrtum.create.client.content.contraptions.minecart;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.KineticDebugger;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.content.contraptions.minecart.CouplingHandler;
import com.zurrtum.create.content.contraptions.minecart.capability.MinecartController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class CouplingRenderer {

    public static void renderAll(Minecraft mc, PoseStack ms, SubmitNodeCollector queue, Vec3 camera) {
        ClientLevel world = mc.level;
        CouplingHandler.forEachLoadedCoupling(
            world, c -> {
                if (c.getFirst().hasContraptionCoupling(true)) {
                    return;
                }
                renderCoupling(world, ms, queue, camera, c.map(MinecartController::cart));
            }
        );
    }

    public static void tickDebugModeRenders(Minecraft mc) {
        if (KineticDebugger.isActive()) {
            CouplingHandler.forEachLoadedCoupling(mc.level, CouplingRenderer::doDebugRender);
        }
    }

    public static void renderCoupling(
        ClientLevel world,
        PoseStack ms,
        SubmitNodeCollector queue,
        Vec3 camera,
        Couple<AbstractMinecart> carts
    ) {
        if (carts.getFirst() == null || carts.getSecond() == null) {
            return;
        }

        Couple<Integer> lightValues = carts.map(c -> LightCoordsUtil.getLightCoords(
            world,
            BlockPos.containing(c.getBoundingBox().getCenter())
        ));

        Vec3 center = carts.getFirst().position().add(carts.getSecond().position()).scale(0.5f);

        Couple<CartEndpoint> transforms = carts.map(c -> getSuitableCartEndpoint(c, center));

        BlockState renderState = Blocks.AIR.defaultBlockState();
        SuperByteBuffer attachment = CachedBuffers.partial(AllPartialModels.COUPLING_ATTACHMENT, renderState);
        SuperByteBuffer ring = CachedBuffers.partial(AllPartialModels.COUPLING_RING, renderState);
        SuperByteBuffer connector = CachedBuffers.partial(AllPartialModels.COUPLING_CONNECTOR, renderState);

        Vec3 zero = Vec3.ZERO;
        Vec3 firstEndpoint = transforms.getFirst().apply(zero);
        Vec3 secondEndpoint = transforms.getSecond().apply(zero);
        Vec3 endPointDiff = secondEndpoint.subtract(firstEndpoint);
        double connectorYaw = -Math.atan2(endPointDiff.z, endPointDiff.x);
        double connectorPitch = Math.atan2(endPointDiff.y, endPointDiff.multiply(1, 0, 1).length());

        carts.forEachWithContext((cart, isFirst) -> {
            CartEndpoint cartTransform = transforms.get(isFirst);

            ms.pushPose();
            cartTransform.apply(ms, camera);
            attachment.light(lightValues.get(isFirst)).submit(ms, queue);
            ms.rotate(Axis.YP, (float) (connectorYaw - Mth.DEG_TO_RAD * cartTransform.yaw));
            ring.light(lightValues.get(isFirst)).submit(ms, queue);
            ms.popPose();
        });

        int l1 = lightValues.getFirst();
        int l2 = lightValues.getSecond();
        int meanBlockLight = ((l1 >> 4 & 0xf) + (l2 >> 4 & 0xf)) / 2;
        int meanSkyLight = ((l1 >> 20 & 0xf) + (l2 >> 20 & 0xf)) / 2;

        ms.pushPose();
        Pose pose = ms.last();
        Vec3 offset = firstEndpoint.subtract(camera);
        pose.translate((float) offset.x, (float) offset.y, (float) offset.z);
        pose.rotate(Axis.YP, (float) connectorYaw);
        pose.rotate(Axis.ZP, (float) connectorPitch);
        pose.scale((float) endPointDiff.length(), 1, 1);

        connector.light(meanSkyLight << 20 | meanBlockLight << 4).submit(ms, queue);
        ms.popPose();
    }

    private static CartEndpoint getSuitableCartEndpoint(AbstractMinecart cart, Vec3 centerOfCoupling) {
        long i = cart.getId() * 493286711L;
        i = i * i * 4392167121L + i * 98761L;
        double x = (((i >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        double y = (((i >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        double z = (((i >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;

        float pt = AnimationTickHolder.getPartialTicks();

        float roll = cart.getHurtTime() - pt;

        float rollAmplifier = cart.getDamage() - pt;
        if (rollAmplifier < 0.0F) {
            rollAmplifier = 0.0F;
        }
        roll = roll > 0 ? Mth.sin(roll) * roll * rollAmplifier / 10.0F * cart.getHurtDir() : 0;

        Vec3 frontVec, backVec;
        float yaw, pitch;
        MinecartBehavior behavior = cart.getBehavior();
        boolean old = true;
        if (behavior instanceof OldMinecartBehavior controller) {
            double xIn = Mth.lerp(pt, cart.xOld, cart.getX());
            double yIn = Mth.lerp(pt, cart.yOld, cart.getY());
            double zIn = Mth.lerp(pt, cart.zOld, cart.getZ());
            Vec3 railVecOfPos = controller.getPos(xIn, yIn, zIn);
            if (railVecOfPos != null) {
                frontVec = Objects.requireNonNullElse(controller.getPosOffs(xIn, yIn, zIn, 0.3F), railVecOfPos);
                backVec = Objects.requireNonNullElse(controller.getPosOffs(xIn, yIn, zIn, -0.3F), railVecOfPos);

                x += railVecOfPos.x;
                y += (frontVec.y + backVec.y) / 2;
                z += railVecOfPos.z;

                Vec3 endPointDiff = backVec.add(-frontVec.x, -frontVec.y, -frontVec.z);
                if (endPointDiff.length() != 0.0D) {
                    endPointDiff = endPointDiff.normalize();
                    yaw = (float) (Math.atan2(endPointDiff.z, endPointDiff.x) * 180.0D / Math.PI);
                    pitch = (float) (Math.atan(endPointDiff.y) * 73.0D);
                } else {
                    yaw = Mth.lerp(pt, cart.yRotO, cart.getYRot());
                    pitch = Mth.lerp(pt, cart.xRotO, cart.getXRot());
                }
            } else {
                yaw = Mth.lerp(pt, cart.yRotO, cart.getYRot());
                pitch = Mth.lerp(pt, cart.xRotO, cart.getXRot());
                Vec3 positionVec = new Vec3(xIn, yIn, zIn);
                frontVec = positionVec.add(VecHelper.rotate(new Vec3(0.5, 0, 0), 180 - yaw, Direction.Axis.Y));
                backVec = positionVec.add(VecHelper.rotate(new Vec3(-0.5, 0, 0), 180 - yaw, Direction.Axis.Y));
                x += xIn;
                y += yIn;
                z += zIn;
            }
            yaw = 180 - yaw;
        } else {
            old = false;
            NewMinecartBehavior controller = (NewMinecartBehavior) behavior;
            Vec3 pos;
            if (controller.cartHasPosRotLerp()) {
                pos = controller.getCartLerpPosition(pt);
                yaw = controller.getCartLerpYRot(pt);
                pitch = controller.getCartLerpXRot(pt);
            } else {
                double xIn = Mth.lerp(pt, cart.xOld, cart.getX());
                double yIn = Mth.lerp(pt, cart.yOld, cart.getY());
                double zIn = Mth.lerp(pt, cart.zOld, cart.getZ());
                pos = new Vec3(xIn, yIn, zIn);
                yaw = cart.getYRot();
                pitch = cart.getXRot();
            }
            x += pos.x;
            y += pos.y;
            z += pos.z;
            frontVec = pos.add(VecHelper.rotate(new Vec3(0.5, 0, 0), yaw, Direction.Axis.Y));
            backVec = pos.add(VecHelper.rotate(new Vec3(-0.5, 0, 0), yaw, Direction.Axis.Y));
        }
        boolean isBackFaceCloser = frontVec.distanceToSqr(centerOfCoupling) > backVec.distanceToSqr(centerOfCoupling);
        return new CartEndpoint(
            x,
            y,
            z,
            yaw,
            -pitch,
            roll,
            isBackFaceCloser ? -13 / 16.0f : 13 / 16.0f,
            isBackFaceCloser,
            old
        );
    }

    static class CartEndpoint {

        double x;
        double y;
        double z;
        float yaw;
        float pitch;
        float roll;
        float offset;
        boolean flip;
        boolean old;

        public CartEndpoint(
            double x,
            double y,
            double z,
            float yaw,
            float pitch,
            float roll,
            float offset,
            boolean flip,
            boolean old
        ) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
            this.roll = roll;
            this.offset = offset;
            this.flip = flip;
            this.old = old;
        }

        public Vec3 apply(Vec3 vec) {
            vec = vec.add(offset, 0, 0);
            if (old) {
                vec = VecHelper.rotate(vec, roll, Direction.Axis.X);
                vec = VecHelper.rotate(vec, pitch, Direction.Axis.Z);
                vec = VecHelper.rotate(vec, yaw, Direction.Axis.Y);
                return vec.add(x, y + 0.375F + 2 / 16.0f, z);
            }
            vec = vec.add(0, 0.375F + 2 / 16.0f, 0);
            vec = VecHelper.rotate(vec, roll, Direction.Axis.X);
            vec = VecHelper.rotate(vec, pitch, Direction.Axis.Z);
            vec = VecHelper.rotate(vec, yaw, Direction.Axis.Y);
            return vec.add(x, y, z);
        }

        public void apply(PoseStack ms, Vec3 camera) {
            Pose pose = ms.last();
            Vec3 position = camera.scale(-1).add(x, y, z);
            pose.translate((float) position.x, (float) position.y, (float) position.z);
            if (old) {
                pose.translate(0, 0.5f, 0);
                pose.rotate(Axis.YP, Mth.DEG_TO_RAD * yaw);
                pose.rotate(Axis.ZP, Mth.DEG_TO_RAD * pitch);
            } else {
                pose.rotate(Axis.YP, Mth.DEG_TO_RAD * yaw);
                pose.rotate(Axis.ZP, Mth.DEG_TO_RAD * pitch);
                pose.translate(0, 0.5f, 0);
            }
            pose.rotate(Axis.XP, Mth.DEG_TO_RAD * roll);
            pose.translate(offset, 0, 0);
            if (flip) {
                pose.rotate(Axis.YP, Radians.DEG_180);
            }
        }

    }

    public static void doDebugRender(Couple<MinecartController> c) {
        int yOffset = 1;
        MinecartController first = c.getFirst();
        AbstractMinecart mainCart = first.cart();
        Vec3 mainCenter = mainCart.position().add(0, yOffset, 0);
        Vec3 connectedCenter = c.getSecond().cart().position().add(0, yOffset, 0);

        int color = Color.mixColors(
            0xabf0e9,
            0xee8572,
            (float) Mth.clamp(
                Math.abs(first.getCouplingLength(true) - connectedCenter.distanceTo(mainCenter)) * 8,
                0,
                1
            )
        );

        Outliner.getInstance().showLine(mainCart.getId() + "", mainCenter, connectedCenter).colored(color)
            .lineWidth(1 / 8.0f);

        Vec3 point = mainCart.position().add(0, yOffset, 0);
        Outliner.getInstance().showLine(mainCart.getId() + "_dot", point, point.add(0, 1 / 128.0f, 0)).colored(0xffffff)
            .lineWidth(1 / 4.0f);
    }

}
