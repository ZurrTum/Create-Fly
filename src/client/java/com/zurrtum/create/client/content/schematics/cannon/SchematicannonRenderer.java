package com.zurrtum.create.client.content.schematics.cannon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.content.schematics.cannon.LaunchedItem;
import com.zurrtum.create.content.schematics.cannon.LaunchedItem.ForBelt;
import com.zurrtum.create.content.schematics.cannon.LaunchedItem.ForBlockState;
import com.zurrtum.create.content.schematics.cannon.LaunchedItem.ForEntity;
import com.zurrtum.create.content.schematics.cannon.SchematicannonBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SchematicannonRenderer implements BlockEntityRenderer<SchematicannonBlockEntity, SchematicannonRenderer.SchematicannonRenderState> {
    protected final ItemModelResolver itemModelManager;

    public SchematicannonRenderer(BlockEntityRendererProvider.Context context) {
        itemModelManager = context.itemModelResolver();
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
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        Level world = be.getLevel();
        boolean support = VisualizationManager.supportsVisualization(world);
        boolean empty = be.flyingBlocks.isEmpty();
        if (support && empty) {
            return;
        }
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        if (!empty) {
            state.blocks = getFlyBlocksRenderState(be, world, state.blockPos, tickProgress);
        }
        if (support) {
            return;
        }
        SchematicannonRenderData data = state.data = new SchematicannonRenderData();
        data.layer = RenderTypes.solidMovingBlock();
        double[] cannonAngles = getCannonAngles(be, state.blockPos, tickProgress);
        double recoil = getRecoil(be, tickProgress);
        data.connector = CachedBuffers.partial(AllPartialModels.SCHEMATICANNON_CONNECTOR, state.blockState);
        data.yaw = (float) (Mth.DEG_TO_RAD * (cannonAngles[0] + 90));
        data.pipe = CachedBuffers.partial(AllPartialModels.SCHEMATICANNON_PIPE, state.blockState);
        data.pitch = (float) (Mth.DEG_TO_RAD * cannonAngles[1]);
        data.offset = (float) (-recoil / 100);
        data.light = state.lightCoords;
    }

    @Override
    public void submit(SchematicannonRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        if (state.blocks != null) {
            for (LaunchedRenderState block : state.blocks) {
                block.render(matrices, queue, state.lightCoords);
            }
        }
        if (state.data != null) {
            queue.submitCustomGeometry(matrices, state.data.layer, state.data);
        }
    }

    @Nullable
    public List<LaunchedRenderState> getFlyBlocksRenderState(SchematicannonBlockEntity be, Level world, BlockPos pos, float partialTicks) {
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
            double throwHeight = Math.sqrt(distance.lengthSqr()) * .6f + yDifference;
            Vec3 cannonOffset = distance.add(0, throwHeight, 0).normalize().scale(2);
            Vec3 start = position.add(cannonOffset);
            yDifference = target.y - start.y;
            float t = ((float) launched.totalTicks - (launched.ticksRemaining + 1 - partialTicks)) / launched.totalTicks;
            Vec3 blockLocationXZ = target.subtract(start).scale(t).multiply(1, 0, 1);
            // Height is determined through a bezier curve
            double yOffset = 2 * (1 - t) * t * throwHeight + t * t * yDifference;
            Vec3 blockLocation = blockLocationXZ.add(0.5, yOffset + 1.5, 0.5).add(cannonOffset);
            float angle = Mth.DEG_TO_RAD * 360 * t;
            if (launched instanceof ForBlockState forBlockState) {
                // Render the Block
                BlockState state;
                if (launched instanceof ForBelt) {
                    // Render a shaft instead of the belt
                    state = AllBlocks.SHAFT.defaultBlockState();
                } else {
                    state = forBlockState.state;
                }
                blocks.add(new LaunchedBlockRenderState(blockLocation, angle, 0.3f, state));
            } else if (launched instanceof ForEntity) {
                ItemStackRenderState item = new ItemStackRenderState();
                item.displayContext = ItemDisplayContext.GROUND;
                itemModelManager.appendItemLayers(item, launched.stack, item.displayContext, world, null, 0);
                blocks.add(new LaunchedEntityRenderState(blockLocation, angle, 1.2f, item));
            }
            // Render particles for launch
            if (launched.ticksRemaining == launched.totalTicks && be.firstRenderTick) {
                start = start.subtract(.5, .5, .5);
                be.firstRenderTick = false;
                RandomSource r = world.getRandom();
                for (int i = 0; i < 10; i++) {
                    double sX = cannonOffset.x * .01f;
                    double sY = (cannonOffset.y + 1) * .01f;
                    double sZ = cannonOffset.z * .01f;
                    double rX = r.nextFloat() - sX * 40;
                    double rY = r.nextFloat() - sY * 40;
                    double rZ = r.nextFloat() - sZ * 40;
                    world.addParticle(ParticleTypes.CLOUD, start.x + rX, start.y + rY, start.z + rZ, sX, sY, sZ);
                }
            }
        }
        if (blocks.isEmpty()) {
            return null;
        }
        return blocks;
    }

    public static double[] getCannonAngles(SchematicannonBlockEntity blockEntity, BlockPos pos, float partialTicks) {
        double yaw;
        double pitch;

        BlockPos target = blockEntity.printer.getCurrentTarget();
        if (target != null) {

            // Calculate Angle of Cannon
            Vec3 diff = Vec3.atLowerCornerOf(target.subtract(pos));
            if (blockEntity.previousTarget != null) {
                diff = (Vec3.atLowerCornerOf(blockEntity.previousTarget)
                    .add(Vec3.atLowerCornerOf(target.subtract(blockEntity.previousTarget)).scale(partialTicks))).subtract(Vec3.atLowerCornerOf(pos));
            }

            double diffX = diff.x();
            double diffZ = diff.z();
            yaw = Mth.atan2(diffX, diffZ);
            yaw = yaw / Math.PI * 180;

            float distance = Mth.sqrt((float) (diffX * diffX + diffZ * diffZ));
            double yOffset = 0 + distance * 2f;
            pitch = Mth.atan2(distance, diff.y() * 3 + yOffset);
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

            if (launched.ticksRemaining == 0)
                continue;

            // Apply Recoil if block was just launched
            if ((launched.ticksRemaining + 1 - partialTicks) > launched.totalTicks - 10)
                recoil = Math.max(recoil, (launched.ticksRemaining + 1 - partialTicks) - launched.totalTicks + 10);
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
        public List<LaunchedRenderState> blocks;
        public SchematicannonRenderData data;
    }

    public static class SchematicannonRenderData implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public SuperByteBuffer connector;
        public float yaw;
        public SuperByteBuffer pipe;
        public float pitch;
        public float offset;
        public int light;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            connector.translate(0.5f, 0, 0.5f).rotate(yaw, Direction.UP).translate(-0.5f, 0, -0.5f).light(light)
                .renderInto(matricesEntry, vertexConsumer);
            pipe.translate(0.5f, 0.9375f, 0.5f).rotate(yaw, Direction.UP).rotate(pitch, Direction.SOUTH).translate(-0.5f, -0.9375f, -0.5f)
                .translate(0, offset, 0).light(light).renderInto(matricesEntry, vertexConsumer);
        }
    }

    public static abstract class LaunchedRenderState {
        public Vec3 offset;
        public float angle;
        public float scale;

        public LaunchedRenderState(Vec3 offset, float angle, float scale) {
            this.offset = offset;
            this.angle = angle;
            this.scale = scale;
        }

        public void render(PoseStack matrices, SubmitNodeCollector queue, int light) {
            matrices.pushPose();
            matrices.translate(offset);
            matrices.translate(.125f, .125f, .125f);
            matrices.mulPose(Axis.YP.rotation(angle));
            matrices.mulPose(Axis.XP.rotation(angle));
            matrices.translate(-.125f, -.125f, -.125f);
            matrices.scale(scale, scale, scale);
            submit(queue, matrices, light);
            matrices.popPose();
        }

        public abstract void submit(SubmitNodeCollector queue, PoseStack matrices, int light);
    }

    public static class LaunchedBlockRenderState extends LaunchedRenderState {
        public BlockState state;

        public LaunchedBlockRenderState(Vec3 offset, float angle, float scale, BlockState state) {
            super(offset, angle, scale);
            this.state = state;
        }

        @Override
        public void submit(SubmitNodeCollector queue, PoseStack matrices, int light) {
            queue.submitBlock(matrices, state, light, OverlayTexture.NO_OVERLAY, 0);
        }
    }

    public static class LaunchedEntityRenderState extends LaunchedRenderState {
        public ItemStackRenderState item;

        public LaunchedEntityRenderState(Vec3 offset, float angle, float scale, ItemStackRenderState item) {
            super(offset, angle, scale);
            this.item = item;
        }

        @Override
        public void submit(SubmitNodeCollector queue, PoseStack matrices, int light) {
            item.submit(matrices, queue, light, OverlayTexture.NO_OVERLAY, 0);
        }
    }
}