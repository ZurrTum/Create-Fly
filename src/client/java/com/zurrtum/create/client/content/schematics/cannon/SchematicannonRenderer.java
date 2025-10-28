package com.zurrtum.create.client.content.schematics.cannon;

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
import net.minecraft.block.BlockState;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SchematicannonRenderer implements BlockEntityRenderer<SchematicannonBlockEntity, SchematicannonRenderer.SchematicannonRenderState> {
    protected final ItemModelManager itemModelManager;

    public SchematicannonRenderer(BlockEntityRendererFactory.Context context) {
        itemModelManager = context.itemModelManager();
    }

    @Override
    public SchematicannonRenderState createRenderState() {
        return new SchematicannonRenderState();
    }

    @Override
    public void updateRenderState(
        SchematicannonBlockEntity be,
        SchematicannonRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        World world = be.getWorld();
        boolean support = VisualizationManager.supportsVisualization(world);
        boolean empty = be.flyingBlocks.isEmpty();
        if (support && empty) {
            return;
        }
        BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        if (!empty) {
            state.blocks = getFlyBlocksRenderState(be, world, state.pos, tickProgress);
        }
        if (support) {
            return;
        }
        SchematicannonRenderData data = state.data = new SchematicannonRenderData();
        data.layer = RenderLayer.getSolid();
        double[] cannonAngles = getCannonAngles(be, state.pos, tickProgress);
        double recoil = getRecoil(be, tickProgress);
        data.connector = CachedBuffers.partial(AllPartialModels.SCHEMATICANNON_CONNECTOR, state.blockState);
        data.yaw = (float) (MathHelper.RADIANS_PER_DEGREE * (cannonAngles[0] + 90));
        data.pipe = CachedBuffers.partial(AllPartialModels.SCHEMATICANNON_PIPE, state.blockState);
        data.pitch = (float) (MathHelper.RADIANS_PER_DEGREE * cannonAngles[1]);
        data.offset = (float) (-recoil / 100);
        data.light = state.lightmapCoordinates;
    }

    @Override
    public void render(SchematicannonRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        if (state.blocks != null) {
            for (LaunchedRenderState block : state.blocks) {
                block.render(matrices, queue, state.lightmapCoordinates);
            }
        }
        if (state.data != null) {
            queue.submitCustom(matrices, state.data.layer, state.data);
        }
    }

    @Nullable
    public List<LaunchedRenderState> getFlyBlocksRenderState(SchematicannonBlockEntity be, World world, BlockPos pos, float partialTicks) {
        List<LaunchedRenderState> blocks = new ArrayList<>();
        Vec3d position = Vec3d.ofCenter(pos.up());
        for (LaunchedItem launched : be.flyingBlocks) {
            if (launched.ticksRemaining == 0) {
                continue;
            }
            // Calculate position of flying block
            Vec3d target = Vec3d.ofCenter(launched.target);
            Vec3d distance = target.subtract(position);
            double yDifference = target.y - position.y;
            double throwHeight = Math.sqrt(distance.lengthSquared()) * .6f + yDifference;
            Vec3d cannonOffset = distance.add(0, throwHeight, 0).normalize().multiply(2);
            Vec3d start = position.add(cannonOffset);
            yDifference = target.y - start.y;
            float t = ((float) launched.totalTicks - (launched.ticksRemaining + 1 - partialTicks)) / launched.totalTicks;
            Vec3d blockLocationXZ = target.subtract(start).multiply(t).multiply(1, 0, 1);
            // Height is determined through a bezier curve
            double yOffset = 2 * (1 - t) * t * throwHeight + t * t * yDifference;
            Vec3d blockLocation = blockLocationXZ.add(0.5, yOffset + 1.5, 0.5).add(cannonOffset);
            float angle = MathHelper.RADIANS_PER_DEGREE * 360 * t;
            if (launched instanceof ForBlockState forBlockState) {
                // Render the Block
                BlockState state;
                if (launched instanceof ForBelt) {
                    // Render a shaft instead of the belt
                    state = AllBlocks.SHAFT.getDefaultState();
                } else {
                    state = forBlockState.state;
                }
                blocks.add(new LaunchedBlockRenderState(blockLocation, angle, 0.3f, state));
            } else if (launched instanceof ForEntity) {
                ItemRenderState item = new ItemRenderState();
                item.displayContext = ItemDisplayContext.GROUND;
                itemModelManager.update(item, launched.stack, item.displayContext, world, null, 0);
                blocks.add(new LaunchedEntityRenderState(blockLocation, angle, 1.2f, item));
            }
            // Render particles for launch
            if (launched.ticksRemaining == launched.totalTicks && be.firstRenderTick) {
                start = start.subtract(.5, .5, .5);
                be.firstRenderTick = false;
                Random r = world.getRandom();
                for (int i = 0; i < 10; i++) {
                    double sX = cannonOffset.x * .01f;
                    double sY = (cannonOffset.y + 1) * .01f;
                    double sZ = cannonOffset.z * .01f;
                    double rX = r.nextFloat() - sX * 40;
                    double rY = r.nextFloat() - sY * 40;
                    double rZ = r.nextFloat() - sZ * 40;
                    world.addParticleClient(ParticleTypes.CLOUD, start.x + rX, start.y + rY, start.z + rZ, sX, sY, sZ);
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
            Vec3d diff = Vec3d.of(target.subtract(pos));
            if (blockEntity.previousTarget != null) {
                diff = (Vec3d.of(blockEntity.previousTarget)
                    .add(Vec3d.of(target.subtract(blockEntity.previousTarget)).multiply(partialTicks))).subtract(Vec3d.of(pos));
            }

            double diffX = diff.getX();
            double diffZ = diff.getZ();
            yaw = MathHelper.atan2(diffX, diffZ);
            yaw = yaw / Math.PI * 180;

            float distance = MathHelper.sqrt((float) (diffX * diffX + diffZ * diffZ));
            double yOffset = 0 + distance * 2f;
            pitch = MathHelper.atan2(distance, diff.getY() * 3 + yOffset);
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
    public boolean rendersOutsideBoundingBox() {
        return true;
    }

    @Override
    public int getRenderDistance() {
        return 128;
    }

    public static class SchematicannonRenderState extends BlockEntityRenderState {
        public List<LaunchedRenderState> blocks;
        public SchematicannonRenderData data;
    }

    public static class SchematicannonRenderData implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public SuperByteBuffer connector;
        public float yaw;
        public SuperByteBuffer pipe;
        public float pitch;
        public float offset;
        public int light;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            connector.translate(0.5f, 0, 0.5f).rotate(yaw, Direction.UP).translate(-0.5f, 0, -0.5f).light(light)
                .renderInto(matricesEntry, vertexConsumer);
            pipe.translate(0.5f, 0.9375f, 0.5f).rotate(yaw, Direction.UP).rotate(pitch, Direction.SOUTH).translate(-0.5f, -0.9375f, -0.5f)
                .translate(0, offset, 0).light(light).renderInto(matricesEntry, vertexConsumer);
        }
    }

    public static abstract class LaunchedRenderState {
        public Vec3d offset;
        public float angle;
        public float scale;

        public LaunchedRenderState(Vec3d offset, float angle, float scale) {
            this.offset = offset;
            this.angle = angle;
            this.scale = scale;
        }

        public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light) {
            matrices.push();
            matrices.translate(offset);
            matrices.translate(.125f, .125f, .125f);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(angle));
            matrices.multiply(RotationAxis.POSITIVE_X.rotation(angle));
            matrices.translate(-.125f, -.125f, -.125f);
            matrices.scale(scale, scale, scale);
            submit(queue, matrices, light);
            matrices.pop();
        }

        public abstract void submit(OrderedRenderCommandQueue queue, MatrixStack matrices, int light);
    }

    public static class LaunchedBlockRenderState extends LaunchedRenderState {
        public BlockState state;

        public LaunchedBlockRenderState(Vec3d offset, float angle, float scale, BlockState state) {
            super(offset, angle, scale);
            this.state = state;
        }

        @Override
        public void submit(OrderedRenderCommandQueue queue, MatrixStack matrices, int light) {
            queue.submitBlock(matrices, state, light, OverlayTexture.DEFAULT_UV, 0);
        }
    }

    public static class LaunchedEntityRenderState extends LaunchedRenderState {
        public ItemRenderState item;

        public LaunchedEntityRenderState(Vec3d offset, float angle, float scale, ItemRenderState item) {
            super(offset, angle, scale);
            this.item = item;
        }

        @Override
        public void submit(OrderedRenderCommandQueue queue, MatrixStack matrices, int light) {
            item.render(matrices, queue, light, OverlayTexture.DEFAULT_UV, 0);
        }
    }
}