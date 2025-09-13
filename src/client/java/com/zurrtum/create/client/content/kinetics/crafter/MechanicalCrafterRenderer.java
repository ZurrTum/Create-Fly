package com.zurrtum.create.client.content.kinetics.crafter;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlock;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlockEntity.Phase;
import com.zurrtum.create.content.kinetics.crafter.RecipeGridHandler.GroupedItems;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import static com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;

public class MechanicalCrafterRenderer extends SafeBlockEntityRenderer<MechanicalCrafterBlockEntity> {

    public MechanicalCrafterRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(
        MechanicalCrafterBlockEntity be,
        float partialTicks,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay
    ) {
        ms.push();
        Direction facing = be.getCachedState().get(HORIZONTAL_FACING);
        Vec3d vec = Vec3d.of(facing.getVector()).multiply(.58).add(.5, .5, .5);

        if (be.phase == Phase.EXPORTING) {
            Direction targetDirection = MechanicalCrafterBlock.getTargetDirection(be.getCachedState());
            float progress = MathHelper.clamp((1000 - be.countDown + be.getCountDownSpeed() * partialTicks) / 1000f, 0, 1);
            vec = vec.add(Vec3d.of(targetDirection.getVector()).multiply(progress * .75f));
        }

        ms.translate(vec.x, vec.y, vec.z);
        ms.scale(1 / 2f, 1 / 2f, 1 / 2f);
        float yRot = AngleHelper.horizontalAngle(facing);
        ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yRot));
        renderItems(be, partialTicks, ms, buffer, light, overlay);
        ms.pop();

        renderFast(be, partialTicks, ms, buffer, light);
    }

    public void renderItems(
        MechanicalCrafterBlockEntity be,
        float partialTicks,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay
    ) {
        if (be.phase == Phase.IDLE) {
            ItemStack stack = be.getInventory().getStack();
            if (!stack.isEmpty()) {
                ms.push();
                ms.translate(0, 0, -1 / 256f);
                ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
                MinecraftClient.getInstance().getItemRenderer()
                    .renderItem(stack, ItemDisplayContext.FIXED, light, overlay, ms, buffer, be.getWorld(), 0);
                ms.pop();
            }
        } else {
            // render grouped items
            GroupedItems items = be.groupedItems;
            float distance = .5f;

            ms.push();

            if (be.phase == Phase.CRAFTING) {
                items = be.groupedItemsBeforeCraft;
                items.calcStats();
                float progress = MathHelper.clamp((2000 - be.countDown + be.getCountDownSpeed() * partialTicks) / 1000f, 0, 1);
                float earlyProgress = MathHelper.clamp(progress * 2, 0, 1);
                float lateProgress = MathHelper.clamp(progress * 2 - 1, 0, 1);

                ms.scale(1 - lateProgress, 1 - lateProgress, 1 - lateProgress);
                Vec3d centering = new Vec3d(-items.minX + (-items.width + 1) / 2f, -items.minY + (-items.height + 1) / 2f, 0).multiply(earlyProgress);
                ms.translate(centering.x * .5f, centering.y * .5f, 0);
                distance += (-4 * (progress - .5f) * (progress - .5f) + 1) * .25f;
            }

            boolean onlyRenderFirst = be.phase == Phase.INSERTING || be.phase == Phase.CRAFTING && be.countDown < 1000;
            final float spacing = distance;
            ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
            World world = be.getWorld();
            items.grid.forEach((pair, stack) -> {
                if (onlyRenderFirst && (pair.getFirst() != 0 || pair.getSecond() != 0))
                    return;

                ms.push();
                Integer x = pair.getFirst();
                Integer y = pair.getSecond();
                ms.translate(x * spacing, y * spacing, 0);

                int offset = 0;
                if (be.phase == Phase.EXPORTING) {
                    BlockState state = be.getCachedState();
                    if (state.contains(MechanicalCrafterBlock.POINTING)) {
                        Pointing value = state.get(MechanicalCrafterBlock.POINTING);
                        offset = value == Pointing.UP ? -1 : value == Pointing.LEFT ? 2 : value == Pointing.RIGHT ? -2 : 1;
                    }
                }

                TransformStack.of(ms).rotateYDegrees(180).translate(0, 0, (x + y * 3 + offset * 9) / 1024f);
                itemRenderer.renderItem(stack, ItemDisplayContext.FIXED, light, overlay, ms, buffer, world, 0);
                ms.pop();
            });

            ms.pop();

            if (be.phase == Phase.CRAFTING) {
                items = be.groupedItems;
                float progress = MathHelper.clamp((1000 - be.countDown + be.getCountDownSpeed() * partialTicks) / 1000f, 0, 1);
                float earlyProgress = MathHelper.clamp(progress * 2, 0, 1);
                float lateProgress = MathHelper.clamp(progress * 2 - 1, 0, 1);

                ms.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(earlyProgress * 2 * 360));
                float upScaling = earlyProgress * 1.125f;
                float downScaling = 1 + (1 - lateProgress) * .125f;
                ms.scale(upScaling, upScaling, upScaling);
                ms.scale(downScaling, downScaling, downScaling);

                items.grid.forEach((pair, stack) -> {
                    if (pair.getFirst() != 0 || pair.getSecond() != 0)
                        return;
                    ms.push();
                    ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
                    itemRenderer.renderItem(stack, ItemDisplayContext.FIXED, light, overlay, ms, buffer, world, 0);
                    ms.pop();
                });
            }

        }
    }

    public void renderFast(MechanicalCrafterBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light) {
        BlockState blockState = be.getCachedState();
        VertexConsumer vb = buffer.getBuffer(RenderLayer.getSolid());

        if (!VisualizationManager.supportsVisualization(be.getWorld())) {
            SuperByteBuffer superBuffer = CachedBuffers.partial(AllPartialModels.SHAFTLESS_COGWHEEL, blockState);
            KineticBlockEntityRenderer.standardKineticRotationTransform(superBuffer, be, light);
            superBuffer.rotateCentered((float) (blockState.get(HORIZONTAL_FACING).getAxis() != Direction.Axis.X ? 0 : Math.PI / 2), Direction.UP);
            superBuffer.rotateCentered((float) (Math.PI / 2), Direction.EAST);
            superBuffer.renderInto(ms, vb);
        }

        Direction targetDirection = MechanicalCrafterBlock.getTargetDirection(blockState);
        BlockPos pos = be.getPos();
        if ((be.covered || be.phase != Phase.IDLE) && be.phase != Phase.CRAFTING && be.phase != Phase.INSERTING) {
            SuperByteBuffer lidBuffer = renderAndTransform(AllPartialModels.MECHANICAL_CRAFTER_LID, blockState);
            lidBuffer.light(light).renderInto(ms, vb);
        }

        if (MechanicalCrafterBlock.isValidTarget(be.getWorld(), pos.offset(targetDirection), blockState)) {
            SuperByteBuffer beltBuffer = renderAndTransform(AllPartialModels.MECHANICAL_CRAFTER_BELT, blockState);
            SuperByteBuffer beltFrameBuffer = renderAndTransform(AllPartialModels.MECHANICAL_CRAFTER_BELT_FRAME, blockState);

            if (be.phase == Phase.EXPORTING) {
                int textureIndex = (int) ((be.getCountDownSpeed() / 128f * AnimationTickHolder.getTicks()));
                beltBuffer.shiftUVtoSheet(AllSpriteShifts.CRAFTER_THINGIES, (textureIndex % 4) / 4f, 0, 1);
            }

            beltBuffer.light(light).renderInto(ms, vb);
            beltFrameBuffer.light(light).renderInto(ms, vb);

        } else {
            SuperByteBuffer arrowBuffer = renderAndTransform(AllPartialModels.MECHANICAL_CRAFTER_ARROW, blockState);
            arrowBuffer.light(light).renderInto(ms, vb);
        }

    }

    private SuperByteBuffer renderAndTransform(PartialModel renderBlock, BlockState crafterState) {
        SuperByteBuffer buffer = CachedBuffers.partial(renderBlock, crafterState);
        float xRot = crafterState.get(MechanicalCrafterBlock.POINTING).getXRotation();
        float yRot = AngleHelper.horizontalAngle(crafterState.get(HORIZONTAL_FACING));
        buffer.rotateCentered((float) ((yRot + 90) / 180 * Math.PI), Direction.UP);
        buffer.rotateCentered((float) ((xRot) / 180 * Math.PI), Direction.EAST);
        return buffer;
    }

}
