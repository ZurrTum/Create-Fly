package com.zurrtum.create.client.content.kinetics.belt;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.levelWrappers.WrappedLevel;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.zurrtum.create.client.foundation.render.ShadowRenderHelper;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.content.kinetics.belt.*;
import com.zurrtum.create.content.kinetics.belt.transport.BeltInventory;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.content.logistics.box.PackageItem;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.AxisDirection;

import java.util.Random;
import java.util.function.Supplier;

public class BeltRenderer extends SafeBlockEntityRenderer<BeltBlockEntity> {

    public BeltRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public boolean rendersOutsideBoundingBox(/*BeltBlockEntity be*/) {
        //TODO
        //        return be.isController();
        return true;
    }

    @Override
    protected void renderSafe(BeltBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {

        if (!VisualizationManager.supportsVisualization(be.getWorld())) {

            BlockState blockState = be.getCachedState();
            if (!blockState.isOf(AllBlocks.BELT))
                return;

            BeltSlope beltSlope = blockState.get(BeltBlock.SLOPE);
            BeltPart part = blockState.get(BeltBlock.PART);
            Direction facing = blockState.get(BeltBlock.HORIZONTAL_FACING);
            AxisDirection axisDirection = facing.getDirection();

            boolean downward = beltSlope == BeltSlope.DOWNWARD;
            boolean upward = beltSlope == BeltSlope.UPWARD;
            boolean diagonal = downward || upward;
            boolean start = part == BeltPart.START;
            boolean end = part == BeltPart.END;
            boolean sideways = beltSlope == BeltSlope.SIDEWAYS;
            boolean alongX = facing.getAxis() == Direction.Axis.X;

            MatrixStack localTransforms = new MatrixStack();
            var msr = TransformStack.of(localTransforms);
            VertexConsumer vb = buffer.getBuffer(RenderLayer.getSolid());
            float renderTick = AnimationTickHolder.getRenderTime(be.getWorld());

            msr.center().rotateYDegrees(AngleHelper.horizontalAngle(facing) + (upward ? 180 : 0) + (sideways ? 270 : 0))
                .rotateZDegrees(sideways ? 90 : 0).rotateXDegrees(!diagonal && beltSlope != BeltSlope.HORIZONTAL ? 90 : 0).uncenter();

            if (downward || beltSlope == BeltSlope.VERTICAL && axisDirection == AxisDirection.POSITIVE) {
                boolean b = start;
                start = end;
                end = b;
            }

            DyeColor color = be.color.orElse(null);

            for (boolean bottom : Iterate.trueAndFalse) {

                PartialModel beltPartial = getBeltPartial(diagonal, start, end, bottom);

                SuperByteBuffer beltBuffer = CachedBuffers.partial(beltPartial, blockState).light(light);

                SpriteShiftEntry spriteShift = getSpriteShiftEntry(color, diagonal, bottom);

                // UV shift
                float speed = be.getSpeed();
                if (speed != 0 || be.color.isPresent()) {
                    float time = renderTick * axisDirection.offset();
                    if (diagonal && (downward ^ alongX) || !sideways && !diagonal && alongX || sideways && axisDirection == AxisDirection.NEGATIVE)
                        speed = -speed;

                    float scrollMult = diagonal ? 3f / 8f : 0.5f;

                    float spriteSize = spriteShift.getTarget().getMaxV() - spriteShift.getTarget().getMinV();

                    double scroll = speed * time / (31.5 * 16) + (bottom ? 0.5 : 0.0);
                    scroll = scroll - Math.floor(scroll);
                    scroll = scroll * spriteSize * scrollMult;

                    beltBuffer.shiftUVScrolling(spriteShift, (float) scroll);
                }

                beltBuffer.transform(localTransforms).renderInto(ms, vb);

                // Diagonal belt do not have a separate bottom model
                if (diagonal)
                    break;
            }

            if (be.hasPulley()) {
                Direction dir = sideways ? Direction.UP : blockState.get(BeltBlock.HORIZONTAL_FACING).rotateYClockwise();

                Supplier<MatrixStack> matrixStackSupplier = () -> {
                    MatrixStack stack = new MatrixStack();
                    var stacker = TransformStack.of(stack);
                    stacker.center();
                    if (dir.getAxis() == Direction.Axis.X)
                        stacker.rotateYDegrees(90);
                    if (dir.getAxis() == Direction.Axis.Y)
                        stacker.rotateXDegrees(90);
                    stacker.rotateXDegrees(90);
                    stacker.uncenter();
                    return stack;
                };

                SuperByteBuffer superBuffer = CachedBuffers.partialDirectional(AllPartialModels.BELT_PULLEY, blockState, dir, matrixStackSupplier);
                KineticBlockEntityRenderer.standardKineticRotationTransform(superBuffer, be, light).renderInto(ms, vb);
            }
        }

        renderItems(be, partialTicks, ms, buffer, light, overlay);
    }

    public static SpriteShiftEntry getSpriteShiftEntry(DyeColor color, boolean diagonal, boolean bottom) {
        if (color != null) {
            return (diagonal ? AllSpriteShifts.DYED_DIAGONAL_BELTS : bottom ? AllSpriteShifts.DYED_OFFSET_BELTS : AllSpriteShifts.DYED_BELTS).get(
                color);
        } else
            return diagonal ? AllSpriteShifts.BELT_DIAGONAL : bottom ? AllSpriteShifts.BELT_OFFSET : AllSpriteShifts.BELT;
    }

    public static PartialModel getBeltPartial(boolean diagonal, boolean start, boolean end, boolean bottom) {
        if (diagonal) {
            if (start)
                return AllPartialModels.BELT_DIAGONAL_START;
            if (end)
                return AllPartialModels.BELT_DIAGONAL_END;
            return AllPartialModels.BELT_DIAGONAL_MIDDLE;
        } else if (bottom) {
            if (start)
                return AllPartialModels.BELT_START_BOTTOM;
            if (end)
                return AllPartialModels.BELT_END_BOTTOM;
            return AllPartialModels.BELT_MIDDLE_BOTTOM;
        } else {
            if (start)
                return AllPartialModels.BELT_START;
            if (end)
                return AllPartialModels.BELT_END;
            return AllPartialModels.BELT_MIDDLE;
        }
    }

    protected void renderItems(BeltBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        if (!be.isController())
            return;
        if (be.beltLength == 0)
            return;

        ms.push();

        Direction beltFacing = be.getBeltFacing();
        Vec3i directionVec = beltFacing.getVector();
        Vec3d beltStartOffset = Vec3d.of(directionVec).multiply(-.5).add(.5, 15 / 16f, .5);
        ms.translate(beltStartOffset.x, beltStartOffset.y, beltStartOffset.z);
        BeltSlope slope = be.getCachedState().get(BeltBlock.SLOPE);
        int verticality = slope == BeltSlope.DOWNWARD ? -1 : slope == BeltSlope.UPWARD ? 1 : 0;
        boolean slopeAlongX = beltFacing.getAxis() == Direction.Axis.X;
        boolean onContraption = be.getWorld() instanceof WrappedLevel;

        BeltInventory inventory = be.getInventory();
        for (TransportedItemStack transported : inventory.getTransportedItems())
            renderItem(
                be,
                partialTicks,
                ms,
                buffer,
                light,
                overlay,
                beltFacing,
                directionVec,
                slope,
                verticality,
                slopeAlongX,
                onContraption,
                transported,
                beltStartOffset
            );
        if (inventory.getLazyClientItem() != null)
            renderItem(
                be,
                partialTicks,
                ms,
                buffer,
                light,
                overlay,
                beltFacing,
                directionVec,
                slope,
                verticality,
                slopeAlongX,
                onContraption,
                inventory.getLazyClientItem(),
                beltStartOffset
            );

        ms.pop();
    }

    private void renderItem(
        BeltBlockEntity be,
        float partialTicks,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay,
        Direction beltFacing,
        Vec3i directionVec,
        BeltSlope slope,
        int verticality,
        boolean slopeAlongX,
        boolean onContraption,
        TransportedItemStack transported,
        Vec3d beltStartOffset
    ) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ItemRenderer itemRenderer = mc.getItemRenderer();
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

        float offset = MathHelper.lerp(partialTicks, transported.prevBeltPosition, transported.beltPosition);
        float sideOffset = MathHelper.lerp(partialTicks, transported.prevSideOffset, transported.sideOffset);
        float verticalMovement = verticality;

        if (be.getSpeed() == 0) {
            offset = transported.beltPosition;
            sideOffset = transported.sideOffset;
        }

        if (offset < .5)
            verticalMovement = 0;
        else
            verticalMovement = verticality * (Math.min(offset, be.beltLength - .5f) - .5f);
        Vec3d offsetVec = Vec3d.of(directionVec).multiply(offset);
        if (verticalMovement != 0)
            offsetVec = offsetVec.add(0, verticalMovement, 0);
        boolean onSlope = slope != BeltSlope.HORIZONTAL && MathHelper.clamp(offset, .5f, be.beltLength - .5f) == offset;
        boolean tiltForward = (slope == BeltSlope.DOWNWARD ^ beltFacing.getDirection() == AxisDirection.POSITIVE) == (beltFacing.getAxis() == Direction.Axis.Z);
        float slopeAngle = onSlope ? tiltForward ? -45 : 45 : 0;

        BlockPos pos = be.getPos();
        Vec3d itemPos = beltStartOffset.add(pos.getX(), pos.getY(), pos.getZ()).add(offsetVec);

        if (this.shouldCullItem(itemPos, be.getWorld())) {
            return;
        }

        ms.push();
        TransformStack.of(ms).nudge(transported.angle);
        ms.translate(offsetVec.x, offsetVec.y, offsetVec.z);

        boolean alongX = beltFacing.rotateYClockwise().getAxis() == Direction.Axis.X;
        if (!alongX)
            sideOffset *= -1;
        ms.translate(alongX ? sideOffset : 0, 0, alongX ? 0 : sideOffset);

        int stackLight;
        if (onContraption) {
            stackLight = light;
        } else {
            int segment = (int) Math.floor(offset);
            mutablePos.set(pos).move(directionVec.getX() * segment, verticality * segment, directionVec.getZ() * segment);
            stackLight = WorldRenderer.getLightmapCoordinates(be.getWorld(), mutablePos);
        }

        boolean renderUpright = BeltHelper.isItemUpright(transported.stack);
        itemRenderer.itemModelManager.clearAndUpdate(
            itemRenderer.itemRenderState,
            transported.stack,
            ItemDisplayContext.FIXED,
            be.getWorld(),
            null,
            0
        );
        boolean blockItem = itemRenderer.itemRenderState.isSideLit();

        int count = 0;
        if (be.getWorld() instanceof PonderLevel || mc.player.getCameraPosVec(1.0F).distanceTo(itemPos) < 16)
            count = MathHelper.floorLog2(transported.stack.getCount()) / 2;

        Random r = new Random(transported.angle);

        boolean slopeShadowOnly = renderUpright && onSlope;
        float slopeOffset = 1 / 8f;
        if (slopeShadowOnly)
            ms.push();
        if (!renderUpright || slopeShadowOnly)
            ms.multiply((slopeAlongX ? RotationAxis.POSITIVE_Z : RotationAxis.POSITIVE_X).rotationDegrees(slopeAngle));
        if (onSlope)
            ms.translate(0, slopeOffset, 0);
        ms.push();
        ms.translate(0, -1 / 8f + 0.005f, 0);
        ShadowRenderHelper.renderShadow(ms, buffer, .75f, .2f);
        ms.pop();
        if (slopeShadowOnly) {
            ms.pop();
            ms.translate(0, slopeOffset, 0);
        }

        if (renderUpright) {
            Entity renderViewEntity = mc.cameraEntity;
            if (renderViewEntity != null) {
                Vec3d positionVec = renderViewEntity.getPos();
                Vec3d vectorForOffset = BeltHelper.getVectorForOffset(be, offset);
                Vec3d diff = vectorForOffset.subtract(positionVec);
                float yRot = (float) (MathHelper.atan2(diff.x, diff.z) + Math.PI);
                ms.multiply(RotationAxis.POSITIVE_Y.rotation(yRot));
            }
            ms.translate(0, 3 / 32d, 1 / 16f);
        }

        for (int i = 0; i <= count; i++) {
            ms.push();

            boolean box = PackageItem.isPackage(transported.stack);
            ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(transported.angle));
            if (!blockItem && !renderUpright) {
                ms.translate(0, -.09375, 0);
                ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
            }

            if (blockItem && !box)
                ms.translate(r.nextFloat() * .0625f * i, 0, r.nextFloat() * .0625f * i);

            if (box) {
                ms.translate(0, 4 / 16f, 0);
                ms.scale(1.5f, 1.5f, 1.5f);
            } else {
                ms.scale(.5f, .5f, .5f);
            }

            itemRenderer.itemRenderState.render(ms, buffer, light, overlay);
            ms.pop();

            if (!renderUpright) {
                if (!blockItem)
                    ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(10));
                ms.translate(0, blockItem ? 1 / 64d : 1 / 16d, 0);
            } else
                ms.translate(0, 0, -1 / 16f);

        }

        ms.pop();
    }
}
