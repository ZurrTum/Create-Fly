package com.zurrtum.create.client.content.schematics.cannon;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.zurrtum.create.content.schematics.cannon.LaunchedItem;
import com.zurrtum.create.content.schematics.cannon.LaunchedItem.ForBelt;
import com.zurrtum.create.content.schematics.cannon.LaunchedItem.ForBlockState;
import com.zurrtum.create.content.schematics.cannon.LaunchedItem.ForEntity;
import com.zurrtum.create.content.schematics.cannon.SchematicannonBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;

public class SchematicannonRenderer extends SafeBlockEntityRenderer<SchematicannonBlockEntity> {

    public SchematicannonRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(
        SchematicannonBlockEntity blockEntity,
        float partialTicks,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay
    ) {

        boolean blocksLaunching = !blockEntity.flyingBlocks.isEmpty();
        if (blocksLaunching)
            renderLaunchedBlocks(blockEntity, partialTicks, ms, buffer, light, overlay);

        if (VisualizationManager.supportsVisualization(blockEntity.getWorld()))
            return;

        BlockPos pos = blockEntity.getPos();
        BlockState state = blockEntity.getCachedState();

        double[] cannonAngles = getCannonAngles(blockEntity, pos, partialTicks);

        double yaw = cannonAngles[0];
        double pitch = cannonAngles[1];

        double recoil = getRecoil(blockEntity, partialTicks);

        ms.push();

        VertexConsumer vb = buffer.getBuffer(RenderLayer.getSolid());

        SuperByteBuffer connector = CachedBuffers.partial(AllPartialModels.SCHEMATICANNON_CONNECTOR, state);
        connector.translate(.5f, 0, .5f);
        connector.rotate((float) ((yaw + 90) / 180 * Math.PI), Direction.UP);
        connector.translate(-.5f, 0, -.5f);
        connector.light(light).renderInto(ms, vb);

        SuperByteBuffer pipe = CachedBuffers.partial(AllPartialModels.SCHEMATICANNON_PIPE, state);
        pipe.translate(.5f, 15 / 16f, .5f);
        pipe.rotate((float) ((yaw + 90) / 180 * Math.PI), Direction.UP);
        pipe.rotate((float) (pitch / 180 * Math.PI), Direction.SOUTH);
        pipe.translate(-.5f, -15 / 16f, -.5f);
        pipe.translate(0, -recoil / 100, 0);
        pipe.light(light).renderInto(ms, vb);

        ms.pop();
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

    private static void renderLaunchedBlocks(
        SchematicannonBlockEntity blockEntity,
        float partialTicks,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay
    ) {
        MinecraftClient mc = MinecraftClient.getInstance();
        BlockRenderManager blockRenderManager = mc.getBlockRenderManager();
        ItemRenderer itemRenderer = mc.getItemRenderer();
        for (LaunchedItem launched : blockEntity.flyingBlocks) {

            if (launched.ticksRemaining == 0)
                continue;

            // Calculate position of flying block
            Vec3d start = Vec3d.ofCenter(blockEntity.getPos().up());
            Vec3d target = Vec3d.ofCenter(launched.target);
            Vec3d distance = target.subtract(start);

            double yDifference = target.y - start.y;
            double throwHeight = Math.sqrt(distance.lengthSquared()) * .6f + yDifference;
            Vec3d cannonOffset = distance.add(0, throwHeight, 0).normalize().multiply(2);
            start = start.add(cannonOffset);
            yDifference = target.y - start.y;

            float progress = ((float) launched.totalTicks - (launched.ticksRemaining + 1 - partialTicks)) / launched.totalTicks;
            Vec3d blockLocationXZ = target.subtract(start).multiply(progress).multiply(1, 0, 1);

            // Height is determined through a bezier curve
            float t = progress;
            double yOffset = 2 * (1 - t) * t * throwHeight + t * t * yDifference;
            Vec3d blockLocation = blockLocationXZ.add(0.5, yOffset + 1.5, 0.5).add(cannonOffset);

            // Offset to position
            ms.push();
            ms.translate(blockLocation.x, blockLocation.y, blockLocation.z);

            ms.translate(.125f, .125f, .125f);
            ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(360 * t));
            ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(360 * t));
            ms.translate(-.125f, -.125f, -.125f);

            if (launched instanceof ForBlockState) {
                // Render the Block
                BlockState state;
                if (launched instanceof ForBelt) {
                    // Render a shaft instead of the belt
                    state = AllBlocks.SHAFT.getDefaultState();
                } else {
                    state = ((ForBlockState) launched).state;
                }
                float scale = .3f;
                ms.scale(scale, scale, scale);
                blockRenderManager.renderBlockAsEntity(state, ms, buffer, light, overlay);
            } else if (launched instanceof ForEntity) {
                // Render the item
                float scale = 1.2f;
                ms.scale(scale, scale, scale);
                itemRenderer.renderItem(launched.stack, ItemDisplayContext.GROUND, light, overlay, ms, buffer, blockEntity.getWorld(), 0);
            }

            ms.pop();

            // Render particles for launch
            if (launched.ticksRemaining == launched.totalTicks && blockEntity.firstRenderTick) {
                start = start.subtract(.5, .5, .5);
                blockEntity.firstRenderTick = false;
                for (int i = 0; i < 10; i++) {
                    Random r = blockEntity.getWorld().getRandom();
                    double sX = cannonOffset.x * .01f;
                    double sY = (cannonOffset.y + 1) * .01f;
                    double sZ = cannonOffset.z * .01f;
                    double rX = r.nextFloat() - sX * 40;
                    double rY = r.nextFloat() - sY * 40;
                    double rZ = r.nextFloat() - sZ * 40;
                    blockEntity.getWorld().addParticleClient(ParticleTypes.CLOUD, start.x + rX, start.y + rY, start.z + rZ, sX, sY, sZ);
                }
            }

        }
    }

    @Override
    public boolean rendersOutsideBoundingBox() {
        return true;
    }

    @Override
    public int getRenderDistance() {
        return 128;
    }

}