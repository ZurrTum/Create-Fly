package com.zurrtum.create.client.content.schematics.cannon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.schematics.cannon.SchematicannonRenderer.SchematicannonRenderState;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.schematics.cannon.LaunchedItem;
import com.zurrtum.create.content.schematics.cannon.LaunchedItem.ForBelt;
import com.zurrtum.create.content.schematics.cannon.LaunchedItem.ForBlockState;
import com.zurrtum.create.content.schematics.cannon.LaunchedItem.ForEntity;
import com.zurrtum.create.content.schematics.cannon.SchematicannonBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SchematicannonRenderer implements BlockEntityRenderer<SchematicannonBlockEntity, SchematicannonRenderState> {
    public static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();
    protected final ItemModelResolver itemModelManager;
    protected final BlockModelResolver blockModelResolver;

    public SchematicannonRenderer(Context context) {
        itemModelManager = context.itemModelResolver();
        blockModelResolver = context.blockModelResolver();
    }

    @Override
    public SchematicannonRenderState createRenderState() {
        return new SchematicannonRenderState();
    }

    @Override
    public void extractRenderState(
        SchematicannonBlockEntity be,
        SchematicannonRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = be.getLevel();
        if (VisualizationManager.supportsVisualization(level)) {
            if (be.flyingBlocks.isEmpty()) {
                return;
            }
            BlockPos blockPos = be.getBlockPos();
            state.blocks = getFlyBlocksRenderState(be, level, blockPos, tickProgress);
            if (state.blocks != null) {
                state.blockPos = blockPos;
                state.blockEntityType = be.getType();
                state.lightCoords = SmartBlockEntityRenderer.getLightCoords(level, state.blockPos);
            }
            return;
        }
        SmartBlockEntityRenderer.extractBase(level, be, state, crumblingOverlay);
        SchematicannonRenderData data = state.cannon = new SchematicannonRenderData();
        updateCannonAngles(data, be, state.blockPos, tickProgress);
        double recoil = getRecoil(be, tickProgress);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        data.connector = CachedBuffers.partial(AllPartialModels.SCHEMATICANNON_CONNECTOR, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        data.pipe = CachedBuffers.partial(AllPartialModels.SCHEMATICANNON_PIPE, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        data.offset = (float) (-recoil / 100);
        if (be.flyingBlocks.isEmpty()) {
            return;
        }
        state.blocks = getFlyBlocksRenderState(be, level, state.blockPos, tickProgress);
    }

    @Override
    public void submit(
        SchematicannonRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.blocks != null) {
            for (LaunchedRenderState block : state.blocks) {
                block.submit(matrices, queue, state.lightCoords);
            }
        }
        if (state.cannon != null) {
            state.cannon.submit(matrices, queue);
        }
    }

    @Nullable
    public List<LaunchedRenderState> getFlyBlocksRenderState(
        SchematicannonBlockEntity be,
        @Nullable Level level,
        BlockPos pos,
        float partialTicks
    ) {
        List<LaunchedRenderState> blocks = new ArrayList<>();
        Vec3 position = Vec3.atCenterOf(pos.above());
        for (LaunchedItem launched : be.flyingBlocks) {
            if (launched.ticksRemaining == 0) {
                continue;
            }
            // Calculate position of flying block
            Vec3 target = Vec3.atCenterOf(launched.target);
            Vec3 distance = target.subtract(position);
            double yDifference = target.y - position.y;
            double throwHeight = Math.sqrt(distance.lengthSqr()) * 0.6f + yDifference;
            Vec3 cannonOffset = distance.add(0, throwHeight, 0).normalize().scale(2);
            Vec3 start = position.add(cannonOffset);
            yDifference = target.y - start.y;
            float t = (launched.totalTicks - (launched.ticksRemaining + 1 - partialTicks)) / launched.totalTicks;
            Vec3 blockLocationXZ = target.subtract(start).scale(t).multiply(1, 0, 1);
            // Height is determined through a bezier curve
            double yOffset = 2 * (1 - t) * t * throwHeight + t * t * yDifference;
            Vec3 blockLocation = blockLocationXZ.add(0.5, yOffset + 1.5, 0.5).add(cannonOffset);
            float radians = Radians.DEG_360 * t;
            if (launched instanceof ForBlockState forBlockState) {
                // Render the Block
                BlockState state;
                if (launched instanceof ForBelt) {
                    // Render a shaft instead of the belt
                    state = AllBlocks.SHAFT.defaultBlockState();
                } else {
                    state = forBlockState.state;
                }
                BlockModelRenderState model = new BlockModelRenderState();
                blockModelResolver.update(model, state, BLOCK_DISPLAY_CONTEXT);
                blocks.add(new LaunchedBlockRenderState(blockLocation, radians, 0.3f, model));
            } else if (launched instanceof ForEntity) {
                ItemStackRenderState item = new ItemStackRenderState();
                item.displayContext = ItemDisplayContext.GROUND;
                itemModelManager.appendItemLayers(item, launched.stack, item.displayContext, level, null, 0);
                blocks.add(new LaunchedEntityRenderState(blockLocation, radians, 1.2f, item));
            }
            // Render particles for launch
            if (launched.ticksRemaining == launched.totalTicks && be.firstRenderTick) {
                start = start.subtract(0.5, 0.5, 0.5);
                be.firstRenderTick = false;
                RandomSource r = level.getRandom();
                for (int i = 0; i < 10; i++) {
                    double sX = cannonOffset.x * 0.01f;
                    double sY = (cannonOffset.y + 1) * 0.01f;
                    double sZ = cannonOffset.z * 0.01f;
                    double rX = r.nextFloat() - sX * 40;
                    double rY = r.nextFloat() - sY * 40;
                    double rZ = r.nextFloat() - sZ * 40;
                    level.addParticle(ParticleTypes.CLOUD, start.x + rX, start.y + rY, start.z + rZ, sX, sY, sZ);
                }
            }
        }
        if (blocks.isEmpty()) {
            return null;
        }
        return blocks;
    }

    public static void updateCannonAngles(
        SchematicannonRenderData data,
        SchematicannonBlockEntity blockEntity,
        BlockPos pos,
        float partialTicks
    ) {
        BlockPos target = blockEntity.printer.getCurrentTarget();
        if (target != null) {
            Vec3 diff;
            if (blockEntity.previousTarget != null) {
                diff = Vec3.atLowerCornerOf(blockEntity.previousTarget)
                    .add(Vec3.atLowerCornerOf(target.subtract(blockEntity.previousTarget)).scale(partialTicks))
                    .subtract(Vec3.atLowerCornerOf(pos));
            } else {
                diff = Vec3.atLowerCornerOf(target.subtract(pos));
            }
            double diffX = diff.x();
            double diffZ = diff.z();
            data.yawRadians = (float) (Mth.atan2(diffX, diffZ) + Radians.DEG_90);
            float distance = Mth.sqrt((float) (diffX * diffX + diffZ * diffZ));
            data.pitchRadians = (float) (Mth.atan2(distance, diff.y() * 3 + distance * 2.0f) + Radians.DEG_10);
        } else {
            data.yawRadians = Mth.DEG_TO_RAD * blockEntity.defaultYaw + Radians.DEG_90;
            data.pitchRadians = Radians.DEG_40;
        }
    }

    public static double[] getCannonAngles(SchematicannonBlockEntity blockEntity, BlockPos pos, float partialTicks) {
        double yaw;
        double pitch;

        BlockPos target = blockEntity.printer.getCurrentTarget();
        if (target != null) {

            // Calculate Angle of Cannon
            Vec3 diff = Vec3.atLowerCornerOf(target.subtract(pos));
            if (blockEntity.previousTarget != null) {
                diff = Vec3.atLowerCornerOf(blockEntity.previousTarget)
                    .add(Vec3.atLowerCornerOf(target.subtract(blockEntity.previousTarget)).scale(partialTicks))
                    .subtract(Vec3.atLowerCornerOf(pos));
            }

            double diffX = diff.x();
            double diffZ = diff.z();
            yaw = Mth.atan2(diffX, diffZ);
            yaw = yaw / Math.PI * 180;

            float distance = Mth.sqrt((float) (diffX * diffX + diffZ * diffZ));
            pitch = Mth.atan2(distance, diff.y() * 3 + distance * 2.0f);
            pitch = pitch / Math.PI * 180 + 10;

        } else {
            yaw = blockEntity.defaultYaw;
            pitch = 40;
        }

        return new double[]{yaw, pitch};
    }

    public static double getRecoil(SchematicannonBlockEntity blockEntity, float partialTicks) {
        double recoil = 0;

        for (LaunchedItem launched : blockEntity.flyingBlocks) {

            if (launched.ticksRemaining == 0) {
                continue;
            }

            // Apply Recoil if block was just launched
            if (launched.ticksRemaining + 1 - partialTicks > launched.totalTicks - 10) {
                recoil = Math.max(recoil, launched.ticksRemaining + 1 - partialTicks - launched.totalTicks + 10);
            }
        }

        return recoil;
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    public static class SchematicannonRenderState extends BlockEntityRenderState {
        public @Nullable List<LaunchedRenderState> blocks;
        public @Nullable SchematicannonRenderData cannon;
    }

    public static class SchematicannonRenderData {
        public @UnknownNullability SuperByteBufferRenderState connector;
        public @UnknownNullability SuperByteBufferRenderState pipe;
        public float yawRadians;
        public float pitchRadians;
        public float offset;

        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            matrices.pushPose();
            Pose pose = matrices.last();
            pose.translate(0.5f, 0.9375f, 0.5f);
            if (yawRadians != 0) {
                pose.rotate(Axis.YP, yawRadians);
            }
            if (pitchRadians != 0) {
                pose.rotate(Axis.ZP, pitchRadians);
            }
            pose.translate(-0.5f, offset - 0.9375f, -0.5f);
            pipe.submit(matrices, queue);
            matrices.popPose();
            if (yawRadians != 0) {
                matrices.rotateYAround(yawRadians, 0.5f, 0, 0.5f);
            }
            connector.submit(matrices, queue);
        }
    }

    public static abstract class LaunchedRenderState {
        public Vec3 offset;
        public float radians;
        public float scale;

        public LaunchedRenderState(Vec3 offset, float radians, float scale) {
            this.offset = offset;
            this.scale = scale;
            this.radians = radians;
        }

        public void submit(PoseStack matrices, SubmitNodeCollector queue, int light) {
            matrices.pushPose();
            Pose pose = matrices.last();
            pose.translate((float) offset.x, (float) offset.y, (float) offset.z);
            if (radians != 0) {
                pose.translate(0.125f, 0.125f, 0.125f);
                pose.rotate(Axis.YP, radians);
                pose.rotate(Axis.XP, radians);
                pose.translate(-0.125f, -0.125f, -0.125f);
            }
            pose.scale(scale, scale, scale);
            submit(queue, matrices, light);
            matrices.popPose();
        }

        public abstract void submit(SubmitNodeCollector queue, PoseStack matrices, int light);
    }

    public static class LaunchedBlockRenderState extends LaunchedRenderState {
        public BlockModelRenderState model;

        public LaunchedBlockRenderState(Vec3 offset, float radians, float scale, BlockModelRenderState model) {
            super(offset, radians, scale);
            this.model = model;
        }

        @Override
        public void submit(SubmitNodeCollector queue, PoseStack matrices, int light) {
            model.submit(matrices, queue, light, OverlayTexture.NO_OVERLAY, 0);
        }
    }

    public static class LaunchedEntityRenderState extends LaunchedRenderState {
        public ItemStackRenderState item;

        public LaunchedEntityRenderState(Vec3 offset, float radians, float scale, ItemStackRenderState item) {
            super(offset, radians, scale);
            this.item = item;
        }

        @Override
        public void submit(SubmitNodeCollector queue, PoseStack matrices, int light) {
            item.submit(matrices, queue, light, OverlayTexture.NO_OVERLAY, 0);
        }
    }
}
