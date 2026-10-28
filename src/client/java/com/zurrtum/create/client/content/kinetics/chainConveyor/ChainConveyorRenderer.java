package com.zurrtum.create.client.content.kinetics.chainConveyor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.kinetics.chainConveyor.ChainConveyorRenderer.ChainConveyorRenderState;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.client.foundation.render.CreateRenderTypes;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity.ConnectionStats;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import com.zurrtum.create.content.logistics.box.PackageItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector.CustomGeometryRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getDegreesWithoutBeOffset;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getRotationAxisOf;

public class ChainConveyorRenderer implements BlockEntityRenderer<ChainConveyorBlockEntity, ChainConveyorRenderState> {
    public static final Identifier CHAIN_LOCATION = Identifier.withDefaultNamespace("textures/block/iron_chain.png");
    public static final int MIP_DISTANCE = 48;

    public ChainConveyorRenderer(Context context) {
    }

    @Override
    public ChainConveyorRenderState createRenderState() {
        return new ChainConveyorRenderState();
    }

    @Override
    public void extractRenderState(
        ChainConveyorBlockEntity be,
        ChainConveyorRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = be.getLevel();
        if (VisualizationManager.supportsVisualization(level)) {
            state.chains = getChainsRenderState(be, level, be.getBlockPos(), cameraPos);
            if (state.chains == null) {
                return;
            }
            SmartBlockEntityRenderer.extractBase(level, be, state, crumblingOverlay);
            state.chain = CreateRenderTypes.chain(CHAIN_LOCATION);
            return;
        }
        SmartBlockEntityRenderer.extractBase(level, be, state, crumblingOverlay);
        BlockPos blockPos = state.blockPos;
        BlockState blockState = state.blockState;
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        state.model = CachedBuffers.partial(AllPartialModels.CHAIN_CONVEYOR_SHAFT, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        Direction.Axis axis = getRotationAxisOf(blockState);
        state.axis = axis;
        state.angle = getDegreesWithoutBeOffset(axis, be, state, level);
        state.chains = getChainsRenderState(be, level, blockPos, cameraPos);
        if (state.chains != null) {
            state.chain = CreateRenderTypes.chain(CHAIN_LOCATION);
            state.guard = CachedBuffers.partial(AllPartialModels.CHAIN_CONVEYOR_GUARD, blockState)
                .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        }
        List<BoxRenderState> boxes = new ArrayList<>();
        for (ChainConveyorPackage box : be.getLoopingPackages()) {
            ChainConveyorPackagePhysicsData data = getPhysicsData(level, box);
            if (data != null) {
                boxes.add(getBoxRenderState(level, cardinalLighting, blockState, blockPos, box, data, tickProgress));
            }
        }
        for (Entry<BlockPos, List<ChainConveyorPackage>> entry : be.getTravellingPackages().entrySet()) {
            for (ChainConveyorPackage box : entry.getValue()) {
                ChainConveyorPackagePhysicsData data = getPhysicsData(level, box);
                if (data != null) {
                    boxes.add(getBoxRenderState(
                        level,
                        cardinalLighting,
                        blockState,
                        blockPos,
                        box,
                        data,
                        tickProgress
                    ));
                }
            }
        }
        if (boxes.isEmpty()) {
            return;
        }
        state.boxes = boxes;
    }

    @Override
    public void submit(
        ChainConveyorRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.model != null) {
            if (state.angle != 0) {
                matrices.pushPose();
                matrices.rotateCentered(state.axis, Mth.DEG_TO_RAD * state.angle);
                state.model.submit(matrices, queue);
                matrices.popPose();
            } else {
                state.model.submit(matrices, queue);
            }
        }
        if (state.chains != null) {
            if (state.guard != null) {
                for (ChainRenderState chain : state.chains) {
                    chain.submit(matrices, state.chain, queue);
                    if (chain.yawRadians != 0) {
                        matrices.pushPose();
                        matrices.rotateYCentered(chain.yawRadians);
                        state.guard.submit(matrices, queue);
                        matrices.popPose();
                    } else {
                        state.guard.submit(matrices, queue);
                    }
                }
            } else {
                for (ChainRenderState chain : state.chains) {
                    chain.submit(matrices, state.chain, queue);
                }
            }
        }
        if (state.boxes != null) {
            for (BoxRenderState renderState : state.boxes) {
                renderState.submit(matrices, queue);
            }
        }
    }

    @Nullable
    public ChainConveyorPackagePhysicsData getPhysicsData(Level world, ChainConveyorPackage box) {
        if (box.worldPosition == null || box.item == null || box.item.isEmpty()) {
            return null;
        }
        ChainConveyorPackagePhysicsData physicsData = ChainConveyorClientBehaviour.physicsData(box, world);
        if (physicsData.prevPos == null) {
            return null;
        }
        if (physicsData.modelKey == null) {
            Identifier key = BuiltInRegistries.ITEM.getKey(box.item.getItem());
            if (key == BuiltInRegistries.ITEM.getDefaultKey()) {
                return null;
            }
            physicsData.modelKey = key;
        }
        return physicsData;
    }

    public BoxRenderState getBoxRenderState(
        Level world,
        @Nullable CardinalLighting cardinalLighting,
        BlockState blockState,
        BlockPos pos,
        ChainConveyorPackage box,
        ChainConveyorPackagePhysicsData physicsData,
        float partialTicks
    ) {
        BoxRenderState state = new BoxRenderState();
        Vec3 position = physicsData.prevPos.lerp(physicsData.pos, partialTicks);
        Vec3 targetPosition = physicsData.prevTargetPos.lerp(physicsData.targetPos, partialTicks);
        float yaw = AngleHelper.angleLerp(partialTicks, physicsData.prevYaw, physicsData.yaw);
        state.yaw = yaw;
        state.offset = new Vec3(
            targetPosition.x - pos.getX(),
            targetPosition.y - pos.getY() + 0.625f,
            targetPosition.z - pos.getZ()
        );
        BlockPos containingPos = BlockPos.containing(position);
        int light = LightCoordsUtil.pack(
            world.getBrightness(LightLayer.BLOCK, containingPos),
            world.getBrightness(LightLayer.SKY, containingPos)
        );
        Vec3 dangleDiff = VecHelper.rotate(targetPosition.add(0, 0.5, 0).subtract(position), -yaw, Direction.Axis.Y);
        float zRot = Mth.wrapDegrees((float) Mth.atan2(-dangleDiff.x, dangleDiff.y) * Mth.RAD_TO_DEG) / 2;
        float xRot = Mth.wrapDegrees((float) Mth.atan2(dangleDiff.z, dangleDiff.y) * Mth.RAD_TO_DEG) / 2;
        state.zRadians = Mth.DEG_TO_RAD * Mth.clamp(zRot, -25, 25);
        state.xRadians = Mth.DEG_TO_RAD * Mth.clamp(xRot, -25, 25);
        state.flipped = physicsData.flipped;
        state.offsetY = -PackageItem.getHookDistance(box.item) - 0.0625f;
        state.rig = CachedBuffers.partial(AllPartialModels.PACKAGE_RIGGING.get(physicsData.modelKey), blockState)
            .cardinalLighting(cardinalLighting).light(light).extractRenderState();
        state.box = CachedBuffers.partial(AllPartialModels.PACKAGES.get(physicsData.modelKey), blockState)
            .cardinalLighting(cardinalLighting).light(light).extractRenderState();
        return state;
    }

    @Nullable
    public List<ChainRenderState> getChainsRenderState(
        ChainConveyorBlockEntity be,
        Level level,
        BlockPos tilePos,
        Vec3 cameraPos
    ) {
        List<ChainRenderState> chains = new ArrayList<>();
        int x = tilePos.getX();
        int y = tilePos.getY();
        int z = tilePos.getZ();
        boolean renderWorld = Minecraft.getInstance().level == level;
        float time = AnimationTickHolder.getRenderTime(level) / (360.0f / Math.abs(be.getSpeed()));
        time %= 1;
        if (time < 0) {
            time += 1;
        }
        float animation = time - 0.5f;
        int light1 = LightCoordsUtil.pack(
            level.getBrightness(LightLayer.BLOCK, tilePos),
            level.getBrightness(LightLayer.SKY, tilePos)
        );
        for (BlockPos blockPos : be.connections) {
            ConnectionStats stats = be.connectionStats.get(blockPos);
            if (stats == null) {
                continue;
            }
            boolean far = renderWorld && !cameraPos.closerThan(
                Vec3.atCenterOf(tilePos).add(blockPos.getX() / 2.0f, blockPos.getY() / 2.0f, blockPos.getZ() / 2.0f),
                MIP_DISTANCE
            );
            ChainRenderState state = far ? new FarChainRenderState() : new ChainRenderState();
            Vec3 diff = stats.end().subtract(stats.start());
            state.startOffset = stats.start().subtract(x, y, z);
            state.yawRadians = (float) Mth.atan2(diff.x, diff.z);
            state.pitchRadians = (float) (Radians.DEG_90 - Mth.atan2(diff.y, diff.multiply(1, 0, 1).length()));
            BlockPos pos = tilePos.offset(blockPos);
            state.light1 = light1;
            state.light2 = LightCoordsUtil.pack(
                level.getBrightness(LightLayer.BLOCK, pos),
                level.getBrightness(LightLayer.SKY, pos)
            );
            state.animation = animation;
            state.length = stats.chainLength();
            state.maxV = far ? 0.0625f : state.length + animation;
            chains.add(state);
        }
        if (chains.isEmpty()) {
            return null;
        }
        return chains;
    }

    private static void renderPart(
        Pose pose,
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
        float uO
    ) {
        Matrix4f matrix4f = pose.pose();
        Vector3f vector3f = new Vector3f();
        Vector3f normal = pose.transformNormal(0.0F, 1.0F, 0.0F, new Vector3f());
        renderQuad(
            matrix4f,
            vector3f,
            normal.x,
            normal.y,
            normal.z,
            pConsumer,
            0,
            pMaxY,
            pX0,
            pZ0,
            pX3,
            pZ3,
            pMinU,
            pMaxU,
            pMinV,
            pMaxV,
            light1,
            light2
        );
        renderQuad(
            matrix4f,
            vector3f,
            normal.x,
            normal.y,
            normal.z,
            pConsumer,
            0,
            pMaxY,
            pX3,
            pZ3,
            pX0,
            pZ0,
            pMinU,
            pMaxU,
            pMinV,
            pMaxV,
            light1,
            light2
        );
        renderQuad(
            matrix4f,
            vector3f,
            normal.x,
            normal.y,
            normal.z,
            pConsumer,
            0,
            pMaxY,
            pX1,
            pZ1,
            pX2,
            pZ2,
            pMinU + uO,
            pMaxU + uO,
            pMinV,
            pMaxV,
            light1,
            light2
        );
        renderQuad(
            matrix4f,
            vector3f,
            normal.x,
            normal.y,
            normal.z,
            pConsumer,
            0,
            pMaxY,
            pX2,
            pZ2,
            pX1,
            pZ1,
            pMinU + uO,
            pMaxU + uO,
            pMinV,
            pMaxV,
            light1,
            light2
        );
    }

    private static void renderQuad(
        Matrix4f pPose,
        Vector3f vector3f,
        float nx,
        float ny,
        float nz,
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
        addVertex(pPose, vector3f, nx, ny, nz, pConsumer, pMaxY, pMinX, pMinZ, pMaxU, pMinV, light2);
        addVertex(pPose, vector3f, nx, ny, nz, pConsumer, pMinY, pMinX, pMinZ, pMaxU, pMaxV, light1);
        addVertex(pPose, vector3f, nx, ny, nz, pConsumer, pMinY, pMaxX, pMaxZ, pMinU, pMaxV, light1);
        addVertex(pPose, vector3f, nx, ny, nz, pConsumer, pMaxY, pMaxX, pMaxZ, pMinU, pMinV, light2);
    }

    private static void addVertex(
        Matrix4f pPose,
        Vector3f vector3f,
        float nx,
        float ny,
        float nz,
        VertexConsumer pConsumer,
        float pY,
        float pX,
        float pZ,
        float pU,
        float pV,
        int light
    ) {
        vector3f.set(pX, pY, pZ).mulPosition(pPose);
        pConsumer.addVertex(
            vector3f.x,
            vector3f.y,
            vector3f.z,
            -1,
            pU,
            pV,
            OverlayTexture.NO_OVERLAY,
            light,
            nx,
            ny,
            nz
        );
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    public static class ChainConveyorRenderState extends BlockEntityRenderState {
        public Direction.Axis axis;
        public float angle;
        public @Nullable SuperByteBufferRenderState model;
        public @Nullable SuperByteBufferRenderState guard;
        public @UnknownNullability RenderType chain;
        public @Nullable List<ChainRenderState> chains;
        public @Nullable List<BoxRenderState> boxes;
    }

    public static class ChainRenderState implements CustomGeometryRenderer {
        public @UnknownNullability Vec3 startOffset;
        public float yawRadians;
        public float pitchRadians;
        public float animation;
        public float length;
        public int light1;
        public int light2;
        public float maxV;

        public void submit(PoseStack matrices, RenderType layer, SubmitNodeCollector queue) {
            matrices.pushPose();
            Pose pose = matrices.last();
            pose.translate((float) startOffset.x, (float) startOffset.y, (float) startOffset.z);
            if (yawRadians != 0) {
                pose.rotate(Axis.YP, yawRadians);
            }
            pose.rotate(Axis.XP, pitchRadians);
            pose.rotate(Axis.YP, Radians.DEG_45);
            queue.submitCustomGeometry(matrices, layer, this);
            matrices.popPose();
        }

        @Override
        public void render(Pose matricesEntry, VertexConsumer vertexConsumer) {
            renderPart(
                matricesEntry,
                vertexConsumer,
                length,
                0,
                0.09375f,
                0.09375f,
                0,
                -0.09375f,
                0,
                0,
                -0.09375f,
                0,
                0.1875f,
                animation,
                maxV,
                light1,
                light2,
                0.1875f
            );
        }
    }

    public static class FarChainRenderState extends ChainRenderState {
        @Override
        public void render(Pose matricesEntry, VertexConsumer vertexConsumer) {
            renderPart(
                matricesEntry,
                vertexConsumer,
                length,
                0,
                0.0625f,
                0.0625f,
                0,
                -0.0625f,
                0,
                0,
                -0.0625f,
                0.1875f,
                0.25f,
                0,
                maxV,
                light1,
                light2,
                0
            );
        }
    }

    public static class BoxRenderState {
        public @UnknownNullability SuperByteBufferRenderState rig;
        public @UnknownNullability SuperByteBufferRenderState box;
        public float yaw;
        public @UnknownNullability Vec3 offset;
        public float zRadians;
        public float xRadians;
        public boolean flipped;
        public float offsetY;

        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            matrices.pushPose();
            Pose pose = matrices.last();
            pose.translate((float) offset.x, (float) offset.y, (float) offset.z);
            if (yaw != 0) {
                pose.rotate(Axis.YP, Mth.DEG_TO_RAD * yaw);
            }
            pose.rotate(Axis.ZP, zRadians);
            pose.rotate(Axis.XP, xRadians);
            if (flipped) {
                matrices.pushPose();
                pose = matrices.last();
                pose.rotate(Axis.YP, Radians.DEG_180);
                pose.translate(-0.5f, offsetY, -0.5f);
                rig.submit(matrices, queue);
                matrices.popPose();
                matrices.translate(-0.5f, offsetY, -0.5f);
                box.submit(matrices, queue);
            } else {
                pose.translate(-0.5f, offsetY, -0.5f);
                rig.submit(matrices, queue);
                box.submit(matrices, queue);
            }
            matrices.popPose();
        }
    }
}
