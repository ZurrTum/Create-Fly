package com.zurrtum.create.client.content.kinetics.speedController;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.speedController.SpeedControllerBlock;
import com.zurrtum.create.content.kinetics.speedController.SpeedControllerBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;

public class SpeedControllerRenderer extends SmartBlockEntityRenderer<SpeedControllerBlockEntity> {

    public SpeedControllerRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(
        SpeedControllerBlockEntity blockEntity,
        float partialTicks,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay
    ) {
        super.renderSafe(blockEntity, partialTicks, ms, buffer, light, overlay);

        VertexConsumer builder = buffer.getBuffer(TexturedRenderLayers.getEntitySolid());
        if (!VisualizationManager.supportsVisualization(blockEntity.getWorld())) {
            KineticBlockEntityRenderer.renderRotatingBuffer(blockEntity, getRotatedModel(blockEntity), ms, builder, light);
        }

        if (!blockEntity.hasBracket)
            return;

        BlockPos pos = blockEntity.getPos();
        World world = blockEntity.getWorld();
        BlockState blockState = blockEntity.getCachedState();
        boolean alongX = blockState.get(SpeedControllerBlock.HORIZONTAL_AXIS) == Axis.X;

        SuperByteBuffer bracket = CachedBuffers.partial(AllPartialModels.SPEED_CONTROLLER_BRACKET, blockState);
        bracket.translate(0, 1, 0);
        bracket.rotateCentered((float) (alongX ? Math.PI : Math.PI / 2), Direction.UP);
        bracket.light(WorldRenderer.getLightmapCoordinates(world, pos.up()));
        bracket.renderInto(ms, builder);
    }

    private SuperByteBuffer getRotatedModel(SpeedControllerBlockEntity blockEntity) {
        return CachedBuffers.block(
            KineticBlockEntityRenderer.KINETIC_BLOCK,
            KineticBlockEntityRenderer.shaft(KineticBlockEntityRenderer.getRotationAxisOf(blockEntity))
        );
    }

}
