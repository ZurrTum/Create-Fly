package com.zurrtum.create.client.content.kinetics.chainConveyor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.foundation.render.CreateRenderTypes;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity.ConnectionStats;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import com.zurrtum.create.content.logistics.box.PackageItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChainConveyorRenderer extends KineticBlockEntityRenderer<ChainConveyorBlockEntity, ChainConveyorRenderer.ChainConveyorRenderState> {
    public static final Identifier CHAIN_LOCATION = Identifier.withDefaultNamespace("textures/block/iron_chain.png");
    public static final int MIP_DISTANCE = 48;

    public ChainConveyorRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
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
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        Level world = be.getLevel();
        if (state.support) {
            BlockPos pos = be.getBlockPos();
            state.chains = getChainsRenderState(be, world, pos, cameraPos);
            if (state.chains == null) {
                return;
            }
            state.blockPos = pos;
            state.blockEntityType = be.getType();
            state.chain = CreateRenderTypes.chain(CHAIN_LOCATION);
            return;
        }
        state.chains = getChainsRenderState(be, world, state.blockPos, cameraPos);
        state.wheel = CachedBuffers.partial(AllPartialModels.CHAIN_CONVEYOR_WHEEL, state.blockState);
        if (state.chains != null) {
            state.chain = CreateRenderTypes.chain(CHAIN_LOCATION);
            state.guard = CachedBuffers.partial(AllPartialModels.CHAIN_CONVEYOR_GUARD, state.blockState);
        }
        List<BoxRenderState> boxes = new ArrayList<>();
        for (ChainConveyorPackage box : be.getLoopingPackages()) {
            ChainConveyorPackagePhysicsData data = getPhysicsData(world, box);
            if (data != null) {
                boxes.add(getBoxRenderState(world, state.blockState, state.blockPos, box, data, tickProgress));
            }
        }
        for (Map.Entry<BlockPos, List<ChainConveyorPackage>> entry : be.getTravellingPackages().entrySet()) {
            for (ChainConveyorPackage box : entry.getValue()) {
                ChainConveyorPackagePhysicsData data = getPhysicsData(world, box);
                if (data != null) {
                    boxes.add(getBoxRenderState(world, state.blockState, state.blockPos, box, data, tickProgress));
                }
            }
        }
        if (boxes.isEmpty()) {
            return;
        }
        state.boxes = boxes;
    }

    @Override
    public void submit(ChainConveyorRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if (state.chains != null) {
            for (ChainRenderState chain : state.chains) {
                chain.render(matrices, state.chain, queue);
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
        state.yaw = Mth.DEG_TO_RAD * yaw;
        state.offset = new Vec3(targetPosition.x - pos.getX(), targetPosition.y - pos.getY(), targetPosition.z - pos.getZ());
        BlockPos containingPos = BlockPos.containing(position);
        state.light = LightCoordsUtil.pack(world.getBrightness(LightLayer.BLOCK, containingPos), world.getBrightness(LightLayer.SKY, containingPos));
        Vec3 dangleDiff = VecHelper.rotate(targetPosition.add(0, 0.5, 0).subtract(position), -yaw, Axis.Y);
        float zRot = Mth.wrapDegrees((float) Mth.atan2(-dangleDiff.x, dangleDiff.y) * Mth.RAD_TO_DEG) / 2;
        float xRot = Mth.wrapDegrees((float) Mth.atan2(dangleDiff.z, dangleDiff.y) * Mth.RAD_TO_DEG) / 2;
        state.zRot = Mth.DEG_TO_RAD * Mth.clamp(zRot, -25, 25);
        state.xRot = Mth.DEG_TO_RAD * Mth.clamp(xRot, -25, 25);
        if (physicsData.flipped) {
            state.yRot = Mth.DEG_TO_RAD * 180;
        }
        state.offsetY = -PackageItem.getHookDistance(box.item) + 7 / 16f;
        state.rig = CachedBuffers.partial(AllPartialModels.PACKAGE_RIGGING.get(physicsData.modelKey), blockState);
        state.box = CachedBuffers.partial(AllPartialModels.PACKAGES.get(physicsData.modelKey), blockState);
        return state;
    }

    @Nullable
    public List<ChainRenderState> getChainsRenderState(ChainConveyorBlockEntity be, Level world, BlockPos tilePos, Vec3 cameraPos) {
        List<ChainRenderState> chains = new ArrayList<>();
        Vec3 position = Vec3.atCenterOf(tilePos);
        boolean renderWorld = Minecraft.getInstance().level == world;
        float time = AnimationTickHolder.getRenderTime(world) / (360f / Math.abs(be.getSpeed()));
        time %= 1;
        if (time < 0) {
            time += 1;
        }
        float animation = time - 0.5f;
        int light1 = LightCoordsUtil.pack(world.getBrightness(LightLayer.BLOCK, tilePos), world.getBrightness(LightLayer.SKY, tilePos));
        float yRot = Mth.DEG_TO_RAD * 45;
        for (BlockPos blockPos : be.connections) {
            ConnectionStats stats = be.connectionStats.get(blockPos);
            if (stats == null) {
                continue;
            }
            boolean far = renderWorld && !cameraPos.closerThan(
                Vec3.atCenterOf(tilePos)
                    .add(blockPos.getX() / 2f, blockPos.getY() / 2f, blockPos.getZ() / 2f), MIP_DISTANCE
            );
            ChainRenderState state = far ? new FarChainRenderState() : new ChainRenderState();
            Vec3 diff = stats.end().subtract(stats.start());
            state.startOffset = stats.start().subtract(position);
            state.yaw = (float) Mth.atan2(diff.x, diff.z);
            state.pitch = (float) (Mth.DEG_TO_RAD * (90 - Mth.RAD_TO_DEG * Mth.atan2(diff.y, diff.multiply(1, 0, 1).length())));
            state.yRot = yRot;
            BlockPos pos = tilePos.offset(blockPos);
            state.light1 = light1;
            state.light2 = LightCoordsUtil.pack(world.getBrightness(LightLayer.BLOCK, pos), world.getBrightness(LightLayer.SKY, pos));
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
        PoseStack.Pose pose,
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
        renderQuad(matrix4f, pose, pConsumer, 0, pMaxY, pX0, pZ0, pX3, pZ3, pMinU, pMaxU, pMinV, pMaxV, light1, light2);
        renderQuad(matrix4f, pose, pConsumer, 0, pMaxY, pX3, pZ3, pX0, pZ0, pMinU, pMaxU, pMinV, pMaxV, light1, light2);
        renderQuad(matrix4f, pose, pConsumer, 0, pMaxY, pX1, pZ1, pX2, pZ2, pMinU + uO, pMaxU + uO, pMinV, pMaxV, light1, light2);
        renderQuad(matrix4f, pose, pConsumer, 0, pMaxY, pX2, pZ2, pX1, pZ1, pMinU + uO, pMaxU + uO, pMinV, pMaxV, light1, light2);
    }

    private static void renderQuad(
        Matrix4f pPose,
        PoseStack.Pose pNormal,
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
        PoseStack.Pose pNormal,
        VertexConsumer pConsumer,
        float pY,
        float pX,
        float pZ,
        float pU,
        float pV,
        int light
    ) {
        pConsumer.addVertex(pPose, pX, pY, pZ).setColor(1.0f, 1.0f, 1.0f, 1.0f).setUv(pU, pV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
            .setNormal(pNormal, 0.0F, 1.0F, 0.0F);
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    protected SuperByteBuffer getRotatedModel(ChainConveyorBlockEntity be, ChainConveyorRenderState state) {
        return CachedBuffers.partial(AllPartialModels.CHAIN_CONVEYOR_SHAFT, state.blockState);
    }

    @Override
    protected RenderType getRenderType(ChainConveyorBlockEntity be, BlockState state) {
        return RenderTypes.cutoutMovingBlock();
    }

    public static class ChainConveyorRenderState extends KineticRenderState {
        public SuperByteBuffer wheel;
        public @Nullable SuperByteBuffer guard;
        public RenderType chain;
        public @Nullable List<ChainRenderState> chains;
        public @Nullable List<BoxRenderState> boxes;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            super.render(matricesEntry, vertexConsumer);
            wheel.light(lightCoords).overlay(OverlayTexture.NO_OVERLAY).renderInto(matricesEntry, vertexConsumer);
            if (guard != null) {
                for (ChainRenderState chain : chains) {
                    guard.center().rotateY(chain.yaw).uncenter().light(lightCoords).overlay(OverlayTexture.NO_OVERLAY)
                        .renderInto(matricesEntry, vertexConsumer);
                }
            }
            if (boxes != null) {
                for (BoxRenderState box : boxes) {
                    box.render(matricesEntry, vertexConsumer);
                }
            }
        }
    }

    public static class ChainRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public Vec3 startOffset;
        public float yaw;
        public float pitch;
        public float yRot;
        public float animation;
        public float length;
        public int light1;
        public int light2;
        public float maxV;

        public void render(PoseStack matrices, RenderType layer, SubmitNodeCollector queue) {
            matrices.pushPose();
            matrices.translate(0.5f, 0.5f, 0.5f);
            matrices.translate(startOffset);
            matrices.mulPose(com.mojang.math.Axis.YP.rotation(yaw));
            matrices.mulPose(com.mojang.math.Axis.XP.rotation(pitch));
            matrices.mulPose(com.mojang.math.Axis.YP.rotation(yRot));
            queue.submitCustomGeometry(matrices, layer, this);
            matrices.popPose();
        }

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
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
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
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
        public SuperByteBuffer rig;
        public SuperByteBuffer box;
        public float yaw;
        public Vec3 offset;
        public float zRot;
        public float xRot;
        public float yRot;
        public float offsetY;
        public int light;

        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            rig.translate(offset).translate(0, 0.625f, 0).rotateY(yaw).rotateZ(zRot).rotateX(xRot).rotateY(yRot).uncenter();
            rig.translate(0, offsetY, 0).light(light).overlay(OverlayTexture.NO_OVERLAY).renderInto(matricesEntry, vertexConsumer);
            box.translate(offset).translate(0, 0.625f, 0).rotateY(yaw).rotateZ(zRot).rotateX(xRot).uncenter();
            box.translate(0, offsetY, 0).light(light).overlay(OverlayTexture.NO_OVERLAY).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
