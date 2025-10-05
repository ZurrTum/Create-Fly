package com.zurrtum.create.client.content.logistics.depot;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.flywheel.lib.transform.PoseTransformStack;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.belt.BeltHelper;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.logistics.depot.DepotBehaviour;
import com.zurrtum.create.content.logistics.depot.DepotBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import java.util.Random;
import java.util.function.BiConsumer;

public class DepotRenderer extends SafeBlockEntityRenderer<DepotBlockEntity> {

    public DepotRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(DepotBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        renderItemsOf(be, partialTicks, ms, buffer, light, overlay, be.depotBehaviour);
    }

    public static void renderItemsOf(
        SmartBlockEntity be,
        float partialTicks,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay,
        DepotBehaviour depotBehaviour
    ) {

        TransportedItemStack transported = depotBehaviour.heldItem;
        var msr = TransformStack.of(ms);
        Vec3d itemPosition = VecHelper.getCenterOf(be.getPos());

        ms.push();
        ms.translate(.5f, 15 / 16f, .5f);

        boolean hasTransported = transported != null && !transported.stack.isEmpty();
        if (hasTransported)
            depotBehaviour.incoming.add(transported);

        // Render main items
        for (TransportedItemStack tis : depotBehaviour.incoming) {
            ms.push();
            msr.nudge(0);
            float offset = MathHelper.lerp(partialTicks, tis.prevBeltPosition, tis.beltPosition);
            float sideOffset = MathHelper.lerp(partialTicks, tis.prevSideOffset, tis.sideOffset);

            if (tis.insertedFrom.getAxis().isHorizontal()) {
                Vec3d offsetVec = Vec3d.of(tis.insertedFrom.getOpposite().getVector()).multiply(.5f - offset);
                ms.translate(offsetVec.x, offsetVec.y, offsetVec.z);
                boolean alongX = tis.insertedFrom.rotateYClockwise().getAxis() == Direction.Axis.X;
                if (!alongX)
                    sideOffset *= -1;
                ms.translate(alongX ? sideOffset : 0, 0, alongX ? 0 : sideOffset);
            }

            ItemStack itemStack = tis.stack;
            int angle = tis.angle;
            Random r = new Random(0);
            renderItem(ms, buffer, light, overlay, itemStack, angle, r, itemPosition, false, null);
            ms.pop();
        }

        if (hasTransported)
            depotBehaviour.incoming.remove(transported);

        // Render output items
        for (int i = 0; i < depotBehaviour.processingOutputBuffer.size(); i++) {
            ItemStack stack = depotBehaviour.processingOutputBuffer.getStack(i);
            if (stack.isEmpty())
                continue;
            ms.push();
            msr.nudge(i);

            boolean renderUpright = BeltHelper.isItemUpright(stack);
            msr.rotateYDegrees(360 / 8f * i);
            ms.translate(.35f, 0, 0);
            if (renderUpright)
                msr.rotateYDegrees(-(360 / 8f * i));
            Random r = new Random(i + 1);
            int angle = (int) (360 * r.nextFloat());
            renderItem(ms, buffer, light, overlay, stack, renderUpright ? angle + 90 : angle, r, itemPosition, false, null);
            ms.pop();
        }

        ms.pop();
    }

    public static void renderItem(
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay,
        ItemStack itemStack,
        int angle,
        Random r,
        Vec3d itemPosition,
        boolean alwaysUpright,
        BiConsumer<PoseTransformStack, Boolean> transform
    ) {
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        itemRenderer.itemModelManager.clearAndUpdate(itemRenderer.itemRenderState, itemStack, ItemDisplayContext.FIXED, null, null, 0);
        boolean blockItem = itemRenderer.itemRenderState.isSideLit();
        var msr = TransformStack.of(ms);
        if (transform != null) {
            transform.accept(msr, blockItem);
        }
        int count = MathHelper.floorLog2(itemStack.getCount()) / 2;
        boolean renderUpright = BeltHelper.isItemUpright(itemStack) || alwaysUpright && !blockItem;

        ms.push();
        msr.rotateYDegrees(angle);

        if (renderUpright) {
            Entity renderViewEntity = MinecraftClient.getInstance().cameraEntity;
            if (renderViewEntity != null) {
                Vec3d positionVec = renderViewEntity.getEntityPos();
                Vec3d vectorForOffset = itemPosition;
                Vec3d diff = vectorForOffset.subtract(positionVec);
                float yRot = (float) (MathHelper.atan2(diff.x, diff.z) + Math.PI);
                ms.multiply(RotationAxis.POSITIVE_Y.rotation(yRot));
            }
            ms.translate(0, 3 / 32d, -1 / 16f);
        }

        for (int i = 0; i <= count; i++) {
            ms.push();
            if (blockItem && r != null)
                ms.translate(r.nextFloat() * .0625f * i, 0, r.nextFloat() * .0625f * i);

            if (PackageItem.isPackage(itemStack) && !alwaysUpright) {
                ms.translate(0, 4 / 16f, 0);
                ms.scale(1.5f, 1.5f, 1.5f);
            } else if (blockItem && alwaysUpright) {
                ms.translate(0, 1 / 16f, 0);
                ms.scale(.755f, .755f, .755f);
            } else
                ms.scale(.5f, .5f, .5f);

            if (!blockItem && !renderUpright) {
                ms.translate(0, -3 / 16f, 0);
                msr.rotateXDegrees(90);
            }
            itemRenderer.itemRenderState.render(ms, buffer, light, overlay);
            ms.pop();

            if (!renderUpright) {
                if (!blockItem)
                    msr.rotateYDegrees(10);
                ms.translate(0, blockItem ? 1 / 64d : 1 / 16d, 0);
            } else
                ms.translate(0, 0, -1 / 16f);
        }

        ms.pop();
    }

}
