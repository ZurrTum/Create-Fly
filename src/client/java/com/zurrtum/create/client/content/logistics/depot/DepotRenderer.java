package com.zurrtum.create.client.content.logistics.depot;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.flywheel.lib.transform.PoseTransformStack;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.kinetics.belt.BeltHelper;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.logistics.depot.DepotBehaviour;
import com.zurrtum.create.content.logistics.depot.DepotBlockEntity;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

public class DepotRenderer implements BlockEntityRenderer<DepotBlockEntity, DepotRenderer.DepotRenderState> {
    protected final ItemModelManager itemModelManager;

    public DepotRenderer(BlockEntityRendererFactory.Context context) {
        itemModelManager = context.itemModelManager();
    }

    @Override
    public DepotRenderState createRenderState() {
        return new DepotRenderState();
    }

    @Override
    public void updateRenderState(
        DepotBlockEntity be,
        DepotRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        state.pos = be.getPos();
        state.type = be.getType();
        World world = be.getWorld();
        state.lightmapCoordinates = world != null ? WorldRenderer.getLightmapCoordinates(
            world,
            state.pos
        ) : LightmapTextureManager.MAX_LIGHT_COORDINATE;
        DepotBehaviour depotBehaviour = be.depotBehaviour;
        state.incoming = createIncomingStateList(depotBehaviour, itemModelManager, tickProgress, world);
        state.outputs = createOutputStateList(depotBehaviour, itemModelManager, world);
    }

    @Nullable
    public static DepotItemState[] createIncomingStateList(
        DepotBehaviour depotBehaviour,
        ItemModelManager itemModelManager,
        float tickProgress,
        World world
    ) {
        List<TransportedItemStack> incomingList = depotBehaviour.incoming;
        int incomingSize = incomingList.size();
        TransportedItemStack heldItem = depotBehaviour.heldItem;
        boolean notHeld = heldItem == null;
        if (incomingSize == 0 && notHeld) {
            return null;
        }
        DepotItemState[] incoming = new DepotItemState[notHeld ? incomingSize : incomingSize + 1];
        for (int i = 0; i < incomingSize; i++) {
            incoming[i] = DepotItemState.create(itemModelManager, incomingList.get(i), tickProgress, world);
        }
        if (!notHeld) {
            incoming[incomingSize] = DepotItemState.create(itemModelManager, heldItem, tickProgress, world);
        }
        return incoming;
    }

    @Nullable
    public static List<DepotOutputItemState> createOutputStateList(DepotBehaviour depotBehaviour, ItemModelManager itemModelManager, World world) {
        List<DepotOutputItemState> outputs = null;
        for (ItemStack stack : depotBehaviour.processingOutputBuffer) {
            if (stack.isEmpty()) {
                continue;
            }
            if (outputs == null) {
                outputs = new ArrayList<>();
            }
            outputs.add(DepotOutputItemState.create(itemModelManager, stack, world));
        }
        return outputs;
    }

    @Override
    public void render(DepotRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        if (state.incoming != null || state.outputs != null) {
            renderItemsOf(state.incoming, state.outputs, state.pos, cameraState.pos, queue, matrices, state.lightmapCoordinates);
        }
    }

    public static void renderItemsOf(
        DepotItemState[] incoming,
        List<DepotOutputItemState> outputs,
        BlockPos pos,
        Vec3d cameraPos,
        OrderedRenderCommandQueue queue,
        MatrixStack ms,
        int light
    ) {
        var msr = TransformStack.of(ms);
        Vec3d itemPosition = VecHelper.getCenterOf(pos);

        ms.push();
        ms.translate(.5f, 15 / 16f, .5f);

        // Render main items
        if (incoming != null) {
            for (DepotItemState item : incoming) {
                ms.push();
                msr.nudge(0);

                if (item.offset != null) {
                    ms.translate(item.offset.x, item.offset.y, item.offset.z);
                }

                renderItem(
                    queue,
                    ms,
                    light,
                    item.state,
                    item.angle,
                    item.upright,
                    item.box,
                    item.count,
                    new Random(0),
                    itemPosition,
                    cameraPos,
                    false,
                    null
                );
                ms.pop();
            }
        }

        // Render output items
        if (outputs != null) {
            for (int i = 0, size = outputs.size(); i < size; i++) {
                DepotOutputItemState item = outputs.get(i);
                ms.push();
                msr.nudge(i);

                boolean renderUpright = item.upright;
                msr.rotateYDegrees(360 / 8f * i);
                ms.translate(.35f, 0, 0);
                if (renderUpright)
                    msr.rotateYDegrees(-(360 / 8f * i));
                Random r = new Random(i + 1);
                int angle = (int) (360 * r.nextFloat());
                renderItem(
                    queue,
                    ms,
                    light,
                    item.state,
                    renderUpright ? angle + 90 : angle,
                    item.upright,
                    item.box,
                    item.count,
                    r,
                    itemPosition,
                    cameraPos,
                    false,
                    null
                );
                ms.pop();
            }
        }

        ms.pop();
    }

    public static void renderItem(
        OrderedRenderCommandQueue queue,
        MatrixStack ms,
        int light,
        ItemRenderState state,
        int angle,
        boolean upright,
        boolean box,
        int count,
        Random r,
        Vec3d itemPosition,
        Vec3d cameraPos,
        boolean alwaysUpright,
        BiConsumer<PoseTransformStack, Boolean> transform
    ) {
        boolean blockItem = state.isSideLit();
        var msr = TransformStack.of(ms);
        if (transform != null) {
            transform.accept(msr, blockItem);
        }
        boolean renderUpright = upright || alwaysUpright && !blockItem;

        ms.push();
        msr.rotateYDegrees(angle);

        if (renderUpright) {
            Vec3d diff = itemPosition.subtract(cameraPos);
            float yRot = (float) (MathHelper.atan2(diff.x, diff.z) + Math.PI);
            ms.multiply(RotationAxis.POSITIVE_Y.rotation(yRot));
            ms.translate(0, 3 / 32d, -1 / 16f);
        }

        for (int i = 0; i <= count; i++) {
            ms.push();
            if (blockItem && r != null)
                ms.translate(r.nextFloat() * .0625f * i, 0, r.nextFloat() * .0625f * i);

            if (box && !alwaysUpright) {
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
            state.render(ms, queue, light, OverlayTexture.DEFAULT_UV, 0);
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

    public static class DepotRenderState extends BlockEntityRenderState {
        public DepotItemState[] incoming;
        public List<DepotOutputItemState> outputs;
    }

    public record DepotItemState(ItemRenderState state, int angle, Vec3d offset, boolean upright, boolean box, int count) {
        public static DepotItemState create(ItemModelManager itemModelManager, TransportedItemStack tis, float partialTicks, World world) {
            Vec3d offsetVec;
            if (tis.insertedFrom.getAxis().isHorizontal()) {
                float offset = MathHelper.lerp(partialTicks, tis.prevBeltPosition, tis.beltPosition);
                float sideOffset = MathHelper.lerp(partialTicks, tis.prevSideOffset, tis.sideOffset);
                boolean alongX = tis.insertedFrom.rotateYClockwise().getAxis() == Direction.Axis.X;
                if (!alongX)
                    sideOffset *= -1;
                offsetVec = Vec3d.of(tis.insertedFrom.getOpposite().getVector()).multiply(.5f - offset)
                    .add(alongX ? sideOffset : 0, 0, alongX ? 0 : sideOffset);
            } else {
                offsetVec = null;
            }
            ItemStack stack = tis.stack;
            ItemRenderState state = new ItemRenderState();
            state.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.update(state, stack, state.displayContext, world, null, 0);
            boolean upright = BeltHelper.isItemUpright(stack);
            boolean box = PackageItem.isPackage(stack);
            int count = MathHelper.floorLog2(stack.getCount()) / 2;
            return new DepotItemState(state, tis.angle, offsetVec, upright, box, count);
        }
    }

    public record DepotOutputItemState(ItemRenderState state, boolean upright, boolean box, int count) {
        public static DepotOutputItemState create(ItemModelManager itemModelManager, ItemStack stack, World world) {
            ItemRenderState state = new ItemRenderState();
            state.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.update(state, stack, state.displayContext, world, null, 0);
            boolean upright = BeltHelper.isItemUpright(stack);
            boolean box = PackageItem.isPackage(stack);
            int count = MathHelper.floorLog2(stack.getCount()) / 2;
            return new DepotOutputItemState(state, upright, box, count);
        }
    }
}
