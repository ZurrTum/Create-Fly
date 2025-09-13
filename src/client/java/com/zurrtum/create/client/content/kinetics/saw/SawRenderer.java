package com.zurrtum.create.client.content.kinetics.saw;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.contraptions.render.ContraptionMatrices;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.saw.SawBlock;
import com.zurrtum.create.content.kinetics.saw.SawBlockEntity;
import com.zurrtum.create.content.logistics.box.PackageItem;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import static net.minecraft.state.property.Properties.FACING;

public class SawRenderer extends SafeBlockEntityRenderer<SawBlockEntity> {

    public SawRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(SawBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        renderBlade(be, ms, buffer, light);
        renderItems(be, partialTicks, ms, buffer, light, overlay);
        FilteringRenderer.renderOnBlockEntity(be, partialTicks, ms, buffer, light, overlay);

        if (VisualizationManager.supportsVisualization(be.getWorld()))
            return;

        renderShaft(be, ms, buffer, light, overlay);
    }

    protected void renderBlade(SawBlockEntity be, MatrixStack ms, VertexConsumerProvider buffer, int light) {
        BlockState blockState = be.getCachedState();
        PartialModel partial;
        float speed = be.getSpeed();
        boolean rotate = false;

        if (SawBlock.isHorizontal(blockState)) {
            if (speed > 0) {
                partial = AllPartialModels.SAW_BLADE_HORIZONTAL_ACTIVE;
            } else if (speed < 0) {
                partial = AllPartialModels.SAW_BLADE_HORIZONTAL_REVERSED;
            } else {
                partial = AllPartialModels.SAW_BLADE_HORIZONTAL_INACTIVE;
            }
        } else {
            if (be.getSpeed() > 0) {
                partial = AllPartialModels.SAW_BLADE_VERTICAL_ACTIVE;
            } else if (speed < 0) {
                partial = AllPartialModels.SAW_BLADE_VERTICAL_REVERSED;
            } else {
                partial = AllPartialModels.SAW_BLADE_VERTICAL_INACTIVE;
            }

            if (blockState.get(SawBlock.AXIS_ALONG_FIRST_COORDINATE))
                rotate = true;
        }

        SuperByteBuffer superBuffer = CachedBuffers.partialFacing(partial, blockState);
        if (rotate) {
            superBuffer.rotateCentered(AngleHelper.rad(90), Direction.UP);
        }
        superBuffer.color(0xFFFFFF).light(light).renderInto(ms, buffer.getBuffer(RenderLayer.getCutoutMipped()));
    }

    protected void renderShaft(SawBlockEntity be, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        KineticBlockEntityRenderer.renderRotatingBuffer(be, getRotatedModel(be), ms, buffer.getBuffer(RenderLayer.getSolid()), light);
    }

    protected void renderItems(SawBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        if (be.getCachedState().get(SawBlock.FACING) != Direction.UP)
            return;
        if (be.inventory.isEmpty())
            return;

        boolean alongZ = !be.getCachedState().get(SawBlock.AXIS_ALONG_FIRST_COORDINATE);

        float duration = be.inventory.recipeDuration;
        boolean moving = duration != 0;
        float offset = moving ? be.inventory.remainingTime / duration : 0;
        float processingSpeed = MathHelper.clamp(Math.abs(be.getSpeed()) / 32, 1, 128);
        if (moving) {
            offset = MathHelper.clamp(offset + ((-partialTicks + .5f) * processingSpeed) / duration, 0.125f, 1f);
            if (!be.inventory.appliedRecipe)
                offset += 1;
            offset /= 2;
        }

        if (be.getSpeed() == 0)
            offset = .5f;
        if (be.getSpeed() < 0 ^ alongZ)
            offset = 1 - offset;

        int outputs = 0;
        for (int i = 1, size = be.inventory.size(); i < size; i++)
            if (!be.inventory.getStack(i).isEmpty())
                outputs++;

        ms.push();
        if (alongZ)
            ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
        ms.translate(outputs <= 1 ? .5 : .25, 0, offset);
        ms.translate(alongZ ? -1 : 0, 0, 0);

        int renderedI = 0;
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        for (int i = 0, size = be.inventory.size(); i < size; i++) {
            ItemStack stack = be.inventory.getStack(i);
            if (stack.isEmpty())
                continue;
            itemRenderer.itemModelManager.clearAndUpdate(itemRenderer.itemRenderState, stack, ItemDisplayContext.FIXED, be.getWorld(), null, 0);
            boolean blockItem = itemRenderer.itemRenderState.isSideLit();

            ms.push();
            ms.translate(0, blockItem ? .925f : 13f / 16f, 0);

            if (i > 0 && outputs > 1) {
                ms.translate((0.5 / (outputs - 1)) * renderedI, 0, 0);
                TransformStack.of(ms).nudge(i * 133);
            }

            boolean box = PackageItem.isPackage(stack);
            if (box) {
                ms.translate(0, 4 / 16f, 0);
                ms.scale(1.5f, 1.5f, 1.5f);
            } else
                ms.scale(.5f, .5f, .5f);

            if (!box)
                ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));

            itemRenderer.itemRenderState.render(ms, buffer, light, overlay);
            renderedI++;

            ms.pop();
        }

        ms.pop();
    }

    protected SuperByteBuffer getRotatedModel(KineticBlockEntity be) {
        BlockState state = be.getCachedState();
        if (state.get(FACING).getAxis().isHorizontal())
            return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state.getBlock().rotate(state, BlockRotation.CLOCKWISE_180));
        return CachedBuffers.block(KineticBlockEntityRenderer.KINETIC_BLOCK, getRenderedBlockState(be));
    }

    protected BlockState getRenderedBlockState(KineticBlockEntity be) {
        return KineticBlockEntityRenderer.shaft(KineticBlockEntityRenderer.getRotationAxisOf(be));
    }

    public static void renderInContraption(
        MovementContext context,
        VirtualRenderWorld renderWorld,
        ContraptionMatrices matrices,
        VertexConsumerProvider buffer
    ) {
        BlockState state = context.state;
        Direction facing = state.get(SawBlock.FACING);

        Vec3d facingVec = Vec3d.of(context.state.get(SawBlock.FACING).getVector());
        facingVec = context.rotation.apply(facingVec);

        Direction closestToFacing = Direction.getFacing(facingVec.x, facingVec.y, facingVec.z);

        boolean horizontal = closestToFacing.getAxis().isHorizontal();
        boolean backwards = VecHelper.isVecPointingTowards(context.relativeMotion, facing.getOpposite());
        boolean moving = context.getAnimationSpeed() != 0;
        boolean shouldAnimate = (context.contraption.stalled && horizontal) || (!context.contraption.stalled && !backwards && moving);

        SuperByteBuffer superBuffer;
        if (SawBlock.isHorizontal(state)) {
            if (shouldAnimate)
                superBuffer = CachedBuffers.partial(AllPartialModels.SAW_BLADE_HORIZONTAL_ACTIVE, state);
            else
                superBuffer = CachedBuffers.partial(AllPartialModels.SAW_BLADE_HORIZONTAL_INACTIVE, state);
        } else {
            if (shouldAnimate)
                superBuffer = CachedBuffers.partial(AllPartialModels.SAW_BLADE_VERTICAL_ACTIVE, state);
            else
                superBuffer = CachedBuffers.partial(AllPartialModels.SAW_BLADE_VERTICAL_INACTIVE, state);
        }

        superBuffer.transform(matrices.getModel()).center().rotateYDegrees(AngleHelper.horizontalAngle(facing))
            .rotateXDegrees(AngleHelper.verticalAngle(facing));

        if (!SawBlock.isHorizontal(state)) {
            superBuffer.rotateZDegrees(state.get(SawBlock.AXIS_ALONG_FIRST_COORDINATE) ? 90 : 0);
        }

        superBuffer.uncenter().light(WorldRenderer.getLightmapCoordinates(renderWorld, context.localPos))
            .useLevelLight(context.world, matrices.getWorld())
            .renderInto(matrices.getViewProjection(), buffer.getBuffer(RenderLayer.getCutoutMipped()));
    }

}
