package com.zurrtum.create.client.content.kinetics.chainConveyor;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.foundation.render.RenderTypes;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity.ConnectionStats;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import com.zurrtum.create.content.logistics.box.PackageItem;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChainConveyorRenderer extends KineticBlockEntityRenderer<ChainConveyorBlockEntity, ChainConveyorRenderer.ChainConveyorRenderState> {
    public static final Identifier CHAIN_LOCATION = Identifier.ofVanilla("textures/block/iron_chain.png");
    public static final int MIP_DISTANCE = 48;

    public ChainConveyorRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public ChainConveyorRenderState createRenderState() {
        return new ChainConveyorRenderState();
    }

    @Override
    public void updateRenderState(
        ChainConveyorBlockEntity be,
        ChainConveyorRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay
    ) {
        super.updateRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        World world = be.getWorld();
        if (state.support) {
            BlockPos pos = be.getPos();
            state.chains = getChainsRenderState(be, world, pos, cameraPos);
            if (state.chains == null) {
                return;
            }
            state.pos = pos;
            state.type = be.getType();
            state.chain = RenderTypes.chain(CHAIN_LOCATION);
            return;
        }
        state.chains = getChainsRenderState(be, world, state.pos, cameraPos);
        state.wheel = CachedBuffers.partial(AllPartialModels.CHAIN_CONVEYOR_WHEEL, state.blockState);
        if (state.chains != null) {
            state.chain = RenderTypes.chain(CHAIN_LOCATION);
            state.guard = CachedBuffers.partial(AllPartialModels.CHAIN_CONVEYOR_GUARD, state.blockState);
        }
        List<BoxRenderState> boxes = new ArrayList<>();
        for (ChainConveyorPackage box : be.getLoopingPackages()) {
            ChainConveyorPackagePhysicsData data = getPhysicsData(world, box);
            if (data != null) {
                boxes.add(getBoxRenderState(world, state.blockState, state.pos, box, data, tickProgress));
            }
        }
        for (Map.Entry<BlockPos, List<ChainConveyorPackage>> entry : be.getTravellingPackages().entrySet()) {
            for (ChainConveyorPackage box : entry.getValue()) {
                ChainConveyorPackagePhysicsData data = getPhysicsData(world, box);
                if (data != null) {
                    boxes.add(getBoxRenderState(world, state.blockState, state.pos, box, data, tickProgress));
                }
            }
        }
        if (boxes.isEmpty()) {
            return;
        }
        state.boxes = boxes;
    }

    @Override
    public void render(ChainConveyorRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        super.render(state, matrices, queue, cameraState);
        if (state.chains != null) {
            for (ChainRenderState chain : state.chains) {
                chain.render(matrices, state.chain, queue);
            }
        }
    }

    public ChainConveyorPackagePhysicsData getPhysicsData(World world, ChainConveyorPackage box) {
        if (box.worldPosition == null || box.item == null || box.item.isEmpty()) {
            return null;
        }
        ChainConveyorPackagePhysicsData physicsData = ChainConveyorClientBehaviour.physicsData(box, world);
        if (physicsData.prevPos == null) {
            return null;
        }
        if (physicsData.modelKey == null) {
            Identifier key = Registries.ITEM.getId(box.item.getItem());
            if (key == Registries.ITEM.getDefaultId()) {
                return null;
            }
            physicsData.modelKey = key;
        }
        return physicsData;
    }

    public BoxRenderState getBoxRenderState(
        World world,
        BlockState blockState,
        BlockPos pos,
        ChainConveyorPackage box,
        ChainConveyorPackagePhysicsData physicsData,
        float partialTicks
    ) {
        BoxRenderState state = new BoxRenderState();
        Vec3d position = physicsData.prevPos.lerp(physicsData.pos, partialTicks);
        Vec3d targetPosition = physicsData.prevTargetPos.lerp(physicsData.targetPos, partialTicks);
        float yaw = AngleHelper.angleLerp(partialTicks, physicsData.prevYaw, physicsData.yaw);
        state.yaw = MathHelper.RADIANS_PER_DEGREE * yaw;
        state.offset = new Vec3d(targetPosition.x - pos.getX(), targetPosition.y - pos.getY(), targetPosition.z - pos.getZ());
        BlockPos containingPos = BlockPos.ofFloored(position);
        state.light = LightmapTextureManager.pack(
            world.getLightLevel(LightType.BLOCK, containingPos),
            world.getLightLevel(LightType.SKY, containingPos)
        );
        Vec3d dangleDiff = VecHelper.rotate(targetPosition.add(0, 0.5, 0).subtract(position), -yaw, Axis.Y);
        float zRot = MathHelper.wrapDegrees((float) MathHelper.atan2(-dangleDiff.x, dangleDiff.y) * MathHelper.DEGREES_PER_RADIAN) / 2;
        float xRot = MathHelper.wrapDegrees((float) MathHelper.atan2(dangleDiff.z, dangleDiff.y) * MathHelper.DEGREES_PER_RADIAN) / 2;
        state.zRot = MathHelper.RADIANS_PER_DEGREE * MathHelper.clamp(zRot, -25, 25);
        state.xRot = MathHelper.RADIANS_PER_DEGREE * MathHelper.clamp(xRot, -25, 25);
        if (physicsData.flipped) {
            state.yRot = MathHelper.RADIANS_PER_DEGREE * 180;
        }
        state.offsetY = -PackageItem.getHookDistance(box.item) + 7 / 16f;
        state.rig = CachedBuffers.partial(AllPartialModels.PACKAGE_RIGGING.get(physicsData.modelKey), blockState);
        state.box = CachedBuffers.partial(AllPartialModels.PACKAGES.get(physicsData.modelKey), blockState);
        return state;
    }

    @Nullable
    public List<ChainRenderState> getChainsRenderState(ChainConveyorBlockEntity be, World world, BlockPos tilePos, Vec3d cameraPos) {
        List<ChainRenderState> chains = new ArrayList<>();
        Vec3d position = Vec3d.ofCenter(tilePos);
        boolean renderWorld = MinecraftClient.getInstance().world == world;
        float time = AnimationTickHolder.getRenderTime(world) / (360f / Math.abs(be.getSpeed()));
        time %= 1;
        if (time < 0) {
            time += 1;
        }
        float animation = time - 0.5f;
        int light1 = LightmapTextureManager.pack(world.getLightLevel(LightType.BLOCK, tilePos), world.getLightLevel(LightType.SKY, tilePos));
        float yRot = MathHelper.RADIANS_PER_DEGREE * 45;
        for (BlockPos blockPos : be.connections) {
            ConnectionStats stats = be.connectionStats.get(blockPos);
            if (stats == null) {
                continue;
            }
            boolean far = renderWorld && !cameraPos.isInRange(
                Vec3d.ofCenter(tilePos)
                    .add(blockPos.getX() / 2f, blockPos.getY() / 2f, blockPos.getZ() / 2f), MIP_DISTANCE
            );
            ChainRenderState state = far ? new FarChainRenderState() : new ChainRenderState();
            Vec3d diff = stats.end().subtract(stats.start());
            state.startOffset = stats.start().subtract(position);
            state.yaw = (float) MathHelper.atan2(diff.x, diff.z);
            state.pitch = (float) (MathHelper.RADIANS_PER_DEGREE * (90 - MathHelper.DEGREES_PER_RADIAN * MathHelper.atan2(
                diff.y,
                diff.multiply(1, 0, 1).length()
            )));
            state.yRot = yRot;
            BlockPos pos = tilePos.add(blockPos);
            state.light1 = light1;
            state.light2 = LightmapTextureManager.pack(world.getLightLevel(LightType.BLOCK, pos), world.getLightLevel(LightType.SKY, pos));
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
        MatrixStack.Entry pose,
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
        Matrix4f matrix4f = pose.getPositionMatrix();
        renderQuad(matrix4f, pose, pConsumer, 0, pMaxY, pX0, pZ0, pX3, pZ3, pMinU, pMaxU, pMinV, pMaxV, light1, light2);
        renderQuad(matrix4f, pose, pConsumer, 0, pMaxY, pX3, pZ3, pX0, pZ0, pMinU, pMaxU, pMinV, pMaxV, light1, light2);
        renderQuad(matrix4f, pose, pConsumer, 0, pMaxY, pX1, pZ1, pX2, pZ2, pMinU + uO, pMaxU + uO, pMinV, pMaxV, light1, light2);
        renderQuad(matrix4f, pose, pConsumer, 0, pMaxY, pX2, pZ2, pX1, pZ1, pMinU + uO, pMaxU + uO, pMinV, pMaxV, light1, light2);
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
    protected SuperByteBuffer getRotatedModel(ChainConveyorBlockEntity be, ChainConveyorRenderState state) {
        return CachedBuffers.partial(AllPartialModels.CHAIN_CONVEYOR_SHAFT, state.blockState);
    }

    @Override
    protected RenderLayer getRenderType(ChainConveyorBlockEntity be, BlockState state) {
        return RenderLayer.getCutoutMipped();
    }

    public static class ChainConveyorRenderState extends KineticRenderState {
        public SuperByteBuffer wheel;
        public SuperByteBuffer guard;
        public RenderLayer chain;
        public List<ChainRenderState> chains;
        public List<BoxRenderState> boxes;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            super.render(matricesEntry, vertexConsumer);
            wheel.light(lightmapCoordinates).overlay(OverlayTexture.DEFAULT_UV).renderInto(matricesEntry, vertexConsumer);
            if (guard != null) {
                for (ChainRenderState chain : chains) {
                    guard.center().rotateY(chain.yaw).uncenter().light(lightmapCoordinates).overlay(OverlayTexture.DEFAULT_UV)
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

    public static class ChainRenderState implements OrderedRenderCommandQueue.Custom {
        public Vec3d startOffset;
        public float yaw;
        public float pitch;
        public float yRot;
        public float animation;
        public float length;
        public int light1;
        public int light2;
        public float maxV;

        public void render(MatrixStack matrices, RenderLayer layer, OrderedRenderCommandQueue queue) {
            matrices.push();
            matrices.translate(0.5f, 0.5f, 0.5f);
            matrices.translate(startOffset);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(yaw));
            matrices.multiply(RotationAxis.POSITIVE_X.rotation(pitch));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(yRot));
            queue.submitCustom(matrices, layer, this);
            matrices.pop();
        }

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
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
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
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
        public Vec3d offset;
        public float zRot;
        public float xRot;
        public float yRot;
        public float offsetY;
        public int light;

        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            rig.translate(offset).translate(0, 0.625f, 0).rotateY(yaw).rotateZ(zRot).rotateX(xRot).rotateY(yRot).uncenter();
            rig.translate(0, offsetY, 0).light(light).overlay(OverlayTexture.DEFAULT_UV).renderInto(matricesEntry, vertexConsumer);
            box.translate(offset).translate(0, 0.625f, 0).rotateY(yaw).rotateZ(zRot).rotateX(xRot).uncenter();
            box.translate(0, offsetY, 0).light(light).overlay(OverlayTexture.DEFAULT_UV).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
