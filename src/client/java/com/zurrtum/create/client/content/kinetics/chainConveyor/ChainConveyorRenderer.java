package com.zurrtum.create.client.content.kinetics.chainConveyor;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.render.RenderTypes;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity.ConnectionStats;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import com.zurrtum.create.content.logistics.box.PackageItem;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Map;

public class ChainConveyorRenderer extends KineticBlockEntityRenderer<ChainConveyorBlockEntity> {

    public static final Identifier CHAIN_LOCATION = Identifier.ofVanilla("textures/block/chain.png");
    public static final int MIP_DISTANCE = 48;

    public ChainConveyorRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(
        ChainConveyorBlockEntity be,
        float partialTicks,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay
    ) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
        BlockPos pos = be.getPos();

        renderChains(be, ms, buffer, light, overlay);

        if (VisualizationManager.supportsVisualization(be.getWorld()))
            return;

        CachedBuffers.partial(AllPartialModels.CHAIN_CONVEYOR_WHEEL, be.getCachedState()).light(light).overlay(overlay)
            .renderInto(ms, buffer.getBuffer(RenderLayer.getCutoutMipped()));

        for (ChainConveyorPackage box : be.getLoopingPackages())
            renderBox(be, ms, buffer, overlay, pos, box, partialTicks);
        for (Map.Entry<BlockPos, List<ChainConveyorPackage>> entry : be.getTravellingPackages().entrySet())
            for (ChainConveyorPackage box : entry.getValue())
                renderBox(be, ms, buffer, overlay, pos, box, partialTicks);
    }

    private void renderBox(
        ChainConveyorBlockEntity be,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int overlay,
        BlockPos pos,
        ChainConveyorPackage box,
        float partialTicks
    ) {
        if (box.worldPosition == null)
            return;
        if (box.item == null || box.item.isEmpty())
            return;

        ChainConveyorPackagePhysicsData physicsData = ChainConveyorClientBehaviour.physicsData(box, be.getWorld());
        if (physicsData.prevPos == null)
            return;

        Vec3d position = physicsData.prevPos.lerp(physicsData.pos, partialTicks);
        Vec3d targetPosition = physicsData.prevTargetPos.lerp(physicsData.targetPos, partialTicks);
        float yaw = AngleHelper.angleLerp(partialTicks, physicsData.prevYaw, physicsData.yaw);
        Vec3d offset = new Vec3d(targetPosition.x - pos.getX(), targetPosition.y - pos.getY(), targetPosition.z - pos.getZ());

        BlockPos containingPos = BlockPos.ofFloored(position);
        World level = be.getWorld();
        BlockState blockState = be.getCachedState();
        int light = LightmapTextureManager.pack(
            level.getLightLevel(LightType.BLOCK, containingPos),
            level.getLightLevel(LightType.SKY, containingPos)
        );

        if (physicsData.modelKey == null) {
            Identifier key = Registries.ITEM.getId(box.item.getItem());
            if (key == Registries.ITEM.getDefaultId())
                return;
            physicsData.modelKey = key;
        }

        SuperByteBuffer rigBuffer = CachedBuffers.partial(AllPartialModels.PACKAGE_RIGGING.get(physicsData.modelKey), blockState);
        SuperByteBuffer boxBuffer = CachedBuffers.partial(AllPartialModels.PACKAGES.get(physicsData.modelKey), blockState);

        Vec3d dangleDiff = VecHelper.rotate(targetPosition.add(0, 0.5, 0).subtract(position), -yaw, Axis.Y);
        float zRot = MathHelper.wrapDegrees((float) MathHelper.atan2(-dangleDiff.x, dangleDiff.y) * MathHelper.DEGREES_PER_RADIAN) / 2;
        float xRot = MathHelper.wrapDegrees((float) MathHelper.atan2(dangleDiff.z, dangleDiff.y) * MathHelper.DEGREES_PER_RADIAN) / 2;
        zRot = MathHelper.clamp(zRot, -25, 25);
        xRot = MathHelper.clamp(xRot, -25, 25);

        for (SuperByteBuffer buf : new SuperByteBuffer[]{rigBuffer, boxBuffer}) {
            buf.translate(offset);
            buf.translate(0, 10 / 16f, 0);
            buf.rotateYDegrees(yaw);

            buf.rotateZDegrees(zRot);
            buf.rotateXDegrees(xRot);

            if (physicsData.flipped && buf == rigBuffer)
                buf.rotateYDegrees(180);

            buf.uncenter();
            buf.translate(0, -PackageItem.getHookDistance(box.item) + 7 / 16f, 0);

            buf.light(light).overlay(overlay).renderInto(ms, buffer.getBuffer(RenderLayer.getCutoutMipped()));
        }
    }

    private void renderChains(ChainConveyorBlockEntity be, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        float time = AnimationTickHolder.getRenderTime(be.getWorld()) / (360f / Math.abs(be.getSpeed()));
        time %= 1;
        if (time < 0)
            time += 1;

        float animation = time - 0.5f;

        for (BlockPos blockPos : be.connections) {
            ConnectionStats stats = be.connectionStats.get(blockPos);
            if (stats == null)
                continue;

            Vec3d diff = stats.end().subtract(stats.start());
            double yaw = (float) MathHelper.DEGREES_PER_RADIAN * MathHelper.atan2(diff.x, diff.z);
            double pitch = (float) MathHelper.DEGREES_PER_RADIAN * MathHelper.atan2(diff.y, diff.multiply(1, 0, 1).length());

            World level = be.getWorld();
            BlockPos tilePos = be.getPos();
            Vec3d startOffset = stats.start().subtract(Vec3d.ofCenter(tilePos));

            if (!VisualizationManager.supportsVisualization(be.getWorld())) {
                SuperByteBuffer guard = CachedBuffers.partial(AllPartialModels.CHAIN_CONVEYOR_GUARD, be.getCachedState());
                guard.center();
                guard.rotateYDegrees((float) yaw);

                guard.uncenter();
                guard.light(light).overlay(overlay).renderInto(ms, buffer.getBuffer(RenderLayer.getCutoutMipped()));
            }

            ms.push();
            var chain = TransformStack.of(ms);
            chain.center();
            chain.translate(startOffset);
            chain.rotateYDegrees((float) yaw);
            chain.rotateXDegrees(90 - (float) pitch);
            chain.rotateYDegrees(45);
            chain.translate(0, 8 / 16f, 0);
            chain.uncenter();

            int light1 = LightmapTextureManager.pack(level.getLightLevel(LightType.BLOCK, tilePos), level.getLightLevel(LightType.SKY, tilePos));
            int light2 = LightmapTextureManager.pack(
                level.getLightLevel(LightType.BLOCK, tilePos.add(blockPos)),
                level.getLightLevel(LightType.SKY, tilePos.add(blockPos))
            );

            boolean far = MinecraftClient.getInstance().world == be.getWorld() && !MinecraftClient.getInstance()
                .getBlockEntityRenderDispatcher().camera.getPos()
                .isInRange(Vec3d.ofCenter(tilePos).add(blockPos.getX() / 2f, blockPos.getY() / 2f, blockPos.getZ() / 2f), MIP_DISTANCE);

            renderChain(ms, buffer, animation, stats.chainLength(), light1, light2, far);

            ms.pop();
        }
    }

    public static void renderChain(
        MatrixStack ms,
        VertexConsumerProvider buffer,
        float animation,
        float length,
        int light1,
        int light2,
        boolean far
    ) {
        float radius = far ? 1f / 16f : 1.5f / 16f;
        float minV = far ? 0 : animation;
        float maxV = far ? 1 / 16f : length + minV;
        float minU = far ? 3 / 16f : 0;
        float maxU = far ? 4 / 16f : 3 / 16f;

        ms.push();
        ms.translate(0.5D, 0.0D, 0.5D);

        VertexConsumer vc = buffer.getBuffer(RenderTypes.chain(CHAIN_LOCATION));
        renderPart(ms, vc, length, 0.0F, radius, radius, 0.0F, -radius, 0.0F, 0.0F, -radius, minU, maxU, minV, maxV, light1, light2, far);

        ms.pop();
    }

    private static void renderPart(
        MatrixStack pPoseStack,
        VertexConsumer pConsumer,
        float pMaxY,
        float pX0,
        float pZ0,
        float pX1,
        float pZ1,
        float pX2,
        float pZ2,
        float pX3,
        float pZ3,
        float pMinU,
        float pMaxU,
        float pMinV,
        float pMaxV,
        int light1,
        int light2,
        boolean far
    ) {
        MatrixStack.Entry posestack$pose = pPoseStack.peek();
        Matrix4f matrix4f = posestack$pose.getPositionMatrix();

        float uO = far ? 0f : 3 / 16f;
        renderQuad(matrix4f, posestack$pose, pConsumer, 0, pMaxY, pX0, pZ0, pX3, pZ3, pMinU, pMaxU, pMinV, pMaxV, light1, light2);
        renderQuad(matrix4f, posestack$pose, pConsumer, 0, pMaxY, pX3, pZ3, pX0, pZ0, pMinU, pMaxU, pMinV, pMaxV, light1, light2);
        renderQuad(matrix4f, posestack$pose, pConsumer, 0, pMaxY, pX1, pZ1, pX2, pZ2, pMinU + uO, pMaxU + uO, pMinV, pMaxV, light1, light2);
        renderQuad(matrix4f, posestack$pose, pConsumer, 0, pMaxY, pX2, pZ2, pX1, pZ1, pMinU + uO, pMaxU + uO, pMinV, pMaxV, light1, light2);
    }

    private static void renderQuad(
        Matrix4f pPose,
        MatrixStack.Entry pNormal,
        VertexConsumer pConsumer,
        float pMinY,
        float pMaxY,
        float pMinX,
        float pMinZ,
        float pMaxX,
        float pMaxZ,
        float pMinU,
        float pMaxU,
        float pMinV,
        float pMaxV,
        int light1,
        int light2
    ) {
        addVertex(pPose, pNormal, pConsumer, pMaxY, pMinX, pMinZ, pMaxU, pMinV, light2);
        addVertex(pPose, pNormal, pConsumer, pMinY, pMinX, pMinZ, pMaxU, pMaxV, light1);
        addVertex(pPose, pNormal, pConsumer, pMinY, pMaxX, pMaxZ, pMinU, pMaxV, light1);
        addVertex(pPose, pNormal, pConsumer, pMaxY, pMaxX, pMaxZ, pMinU, pMinV, light2);
    }

    private static void addVertex(
        Matrix4f pPose,
        MatrixStack.Entry pNormal,
        VertexConsumer pConsumer,
        float pY,
        float pX,
        float pZ,
        float pU,
        float pV,
        int light
    ) {
        pConsumer.vertex(pPose, pX, pY, pZ).color(1.0f, 1.0f, 1.0f, 1.0f).texture(pU, pV).overlay(OverlayTexture.DEFAULT_UV).light(light)
            .normal(pNormal, 0.0F, 1.0F, 0.0F);
    }

    @Override
    public int getRenderDistance() {
        return 256;
    }

    @Override
    public boolean rendersOutsideBoundingBox() {
        return true;
    }

    @Override
    protected SuperByteBuffer getRotatedModel(ChainConveyorBlockEntity be, BlockState state) {
        return CachedBuffers.partial(AllPartialModels.CHAIN_CONVEYOR_SHAFT, state);
    }

    @Override
    protected RenderLayer getRenderType(ChainConveyorBlockEntity be, BlockState state) {
        return RenderLayer.getCutoutMipped();
    }

}
