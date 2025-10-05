package com.zurrtum.create.client.content.contraptions.minecart;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.KineticDebugger;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.contraptions.minecart.CouplingHandler;
import com.zurrtum.create.content.contraptions.minecart.capability.MinecartController;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.DefaultMinecartController;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class CouplingRenderer {

    public static void renderAll(MinecraftClient mc, MatrixStack ms, VertexConsumerProvider buffer, Vec3d camera) {
        ClientWorld world = mc.world;
        CouplingHandler.forEachLoadedCoupling(
            world, c -> {
                if (c.getFirst().hasContraptionCoupling(true))
                    return;
                CouplingRenderer.renderCoupling(world, ms, buffer, camera, c.map(MinecartController::cart));
            }
        );
    }

    public static void tickDebugModeRenders(MinecraftClient mc) {
        if (KineticDebugger.isActive())
            CouplingHandler.forEachLoadedCoupling(mc.world, CouplingRenderer::doDebugRender);
    }

    public static void renderCoupling(
        ClientWorld world,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        Vec3d camera,
        Couple<AbstractMinecartEntity> carts
    ) {
        if (carts.getFirst() == null || carts.getSecond() == null)
            return;

        Couple<Integer> lightValues = carts.map(c -> WorldRenderer.getLightmapCoordinates(world, BlockPos.ofFloored(c.getBoundingBox().getCenter())));

        Vec3d center = carts.getFirst().getEntityPos().add(carts.getSecond().getEntityPos()).multiply(.5f);

        Couple<CartEndpoint> transforms = carts.map(c -> getSuitableCartEndpoint(c, center));

        BlockState renderState = Blocks.AIR.getDefaultState();
        VertexConsumer builder = buffer.getBuffer(RenderLayer.getSolid());
        SuperByteBuffer attachment = CachedBuffers.partial(AllPartialModels.COUPLING_ATTACHMENT, renderState);
        SuperByteBuffer ring = CachedBuffers.partial(AllPartialModels.COUPLING_RING, renderState);
        SuperByteBuffer connector = CachedBuffers.partial(AllPartialModels.COUPLING_CONNECTOR, renderState);

        Vec3d zero = Vec3d.ZERO;
        Vec3d firstEndpoint = transforms.getFirst().apply(zero);
        Vec3d secondEndpoint = transforms.getSecond().apply(zero);
        Vec3d endPointDiff = secondEndpoint.subtract(firstEndpoint);
        double connectorYaw = -Math.atan2(endPointDiff.z, endPointDiff.x) * 180.0D / Math.PI;
        double connectorPitch = Math.atan2(endPointDiff.y, endPointDiff.multiply(1, 0, 1).length()) * 180 / Math.PI;

        var msr = TransformStack.of(ms);
        carts.forEachWithContext((cart, isFirst) -> {
            CartEndpoint cartTransform = transforms.get(isFirst);

            ms.push();
            cartTransform.apply(ms, camera);
            attachment.light(lightValues.get(isFirst)).renderInto(ms, builder);
            msr.rotateYDegrees((float) connectorYaw - cartTransform.yaw);
            ring.light(lightValues.get(isFirst)).renderInto(ms, builder);
            ms.pop();
        });

        int l1 = lightValues.getFirst();
        int l2 = lightValues.getSecond();
        int meanBlockLight = (((l1 >> 4) & 0xf) + ((l2 >> 4) & 0xf)) / 2;
        int meanSkyLight = (((l1 >> 20) & 0xf) + ((l2 >> 20) & 0xf)) / 2;

        ms.push();
        msr.translate(firstEndpoint.subtract(camera)).rotateYDegrees((float) connectorYaw).rotateZDegrees((float) connectorPitch);
        ms.scale((float) endPointDiff.length(), 1, 1);

        connector.light(meanSkyLight << 20 | meanBlockLight << 4).renderInto(ms, builder);
        ms.pop();
    }

    private static CartEndpoint getSuitableCartEndpoint(AbstractMinecartEntity cart, Vec3d centerOfCoupling) {
        long i = cart.getId() * 493286711L;
        i = i * i * 4392167121L + i * 98761L;
        double x = (((float) (i >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        double y = (((float) (i >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F + 0.375F;
        double z = (((float) (i >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;

        float pt = AnimationTickHolder.getPartialTicks();

        double xIn = MathHelper.lerp(pt, cart.lastRenderX, cart.getX());
        double yIn = MathHelper.lerp(pt, cart.lastRenderY, cart.getY());
        double zIn = MathHelper.lerp(pt, cart.lastRenderZ, cart.getZ());

        float yaw = MathHelper.lerp(pt, cart.lastYaw, cart.getYaw());
        float pitch = MathHelper.lerp(pt, cart.lastPitch, cart.getPitch());
        float roll = cart.getDamageWobbleTicks() - pt;

        float rollAmplifier = cart.getDamageWobbleStrength() - pt;
        if (rollAmplifier < 0.0F)
            rollAmplifier = 0.0F;
        roll = roll > 0 ? MathHelper.sin(roll) * roll * rollAmplifier / 10.0F * cart.getDamageWobbleSide() : 0;

        Vec3d positionVec = new Vec3d(xIn, yIn, zIn);
        Vec3d frontVec = positionVec.add(VecHelper.rotate(new Vec3d(.5, 0, 0), 180 - yaw, Direction.Axis.Y));
        Vec3d backVec = positionVec.add(VecHelper.rotate(new Vec3d(-.5, 0, 0), 180 - yaw, Direction.Axis.Y));

        if (cart.getController() instanceof DefaultMinecartController defaultMinecartController) {
            Vec3d railVecOfPos = defaultMinecartController.snapPositionToRail(xIn, yIn, zIn);
            if (railVecOfPos != null) {
                frontVec = defaultMinecartController.simulateMovement(xIn, yIn, zIn, (double) 0.3F);
                backVec = defaultMinecartController.simulateMovement(xIn, yIn, zIn, (double) -0.3F);
                if (frontVec == null)
                    frontVec = railVecOfPos;
                if (backVec == null)
                    backVec = railVecOfPos;

                x += railVecOfPos.x;
                y += (frontVec.y + backVec.y) / 2;
                z += railVecOfPos.z;

                Vec3d endPointDiff = backVec.add(-frontVec.x, -frontVec.y, -frontVec.z);
                if (endPointDiff.length() != 0.0D) {
                    endPointDiff = endPointDiff.normalize();
                    yaw = (float) (Math.atan2(endPointDiff.z, endPointDiff.x) * 180.0D / Math.PI);
                    pitch = (float) (Math.atan(endPointDiff.y) * 73.0D);
                }
            }
        } else {
            x += xIn;
            y += yIn;
            z += zIn;
        }

        final float offsetMagnitude = 13 / 16f;
        boolean isBackFaceCloser = frontVec.squaredDistanceTo(centerOfCoupling) > backVec.squaredDistanceTo(centerOfCoupling);
        float offset = isBackFaceCloser ? -offsetMagnitude : offsetMagnitude;

        return new CartEndpoint(x, y + 2 / 16f, z, 180 - yaw, -pitch, roll, offset, isBackFaceCloser);
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

        public CartEndpoint(double x, double y, double z, float yaw, float pitch, float roll, float offset, boolean flip) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
            this.roll = roll;
            this.offset = offset;
            this.flip = flip;
        }

        public Vec3d apply(Vec3d vec) {
            vec = vec.add(offset, 0, 0);
            vec = VecHelper.rotate(vec, roll, Direction.Axis.X);
            vec = VecHelper.rotate(vec, pitch, Direction.Axis.Z);
            vec = VecHelper.rotate(vec, yaw, Direction.Axis.Y);
            return vec.add(x, y, z);
        }

        public void apply(MatrixStack ms, Vec3d camera) {
            TransformStack.of(ms).translate(camera.multiply(-1).add(x, y, z)).rotateYDegrees(yaw).rotateZDegrees(pitch).rotateXDegrees(roll)
                .translate(offset, 0, 0).rotateYDegrees(flip ? 180 : 0);
        }

    }

    public static void doDebugRender(Couple<MinecartController> c) {
        int yOffset = 1;
        MinecartController first = c.getFirst();
        AbstractMinecartEntity mainCart = first.cart();
        Vec3d mainCenter = mainCart.getEntityPos().add(0, yOffset, 0);
        Vec3d connectedCenter = c.getSecond().cart().getEntityPos().add(0, yOffset, 0);

        int color = Color.mixColors(
            0xabf0e9,
            0xee8572,
            (float) MathHelper.clamp(Math.abs(first.getCouplingLength(true) - connectedCenter.distanceTo(mainCenter)) * 8, 0, 1)
        );

        Outliner.getInstance().showLine(mainCart.getId() + "", mainCenter, connectedCenter).colored(color).lineWidth(1 / 8f);

        Vec3d point = mainCart.getEntityPos().add(0, yOffset, 0);
        Outliner.getInstance().showLine(mainCart.getId() + "_dot", point, point.add(0, 1 / 128f, 0)).colored(0xffffff).lineWidth(1 / 4f);
    }

}
