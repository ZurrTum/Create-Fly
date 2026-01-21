package com.zurrtum.create.client.content.logistics.depot;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.flywheel.lib.transform.PoseTransformStack;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.kinetics.belt.BeltHelper;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.logistics.depot.DepotBehaviour;
import com.zurrtum.create.content.logistics.depot.DepotBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

public class DepotRenderer implements BlockEntityRenderer<DepotBlockEntity, DepotRenderer.DepotRenderState> {
    protected final ItemModelResolver itemModelManager;

    public DepotRenderer(BlockEntityRendererProvider.Context context) {
        itemModelManager = context.itemModelResolver();
    }

    @Override
    public DepotRenderState createRenderState() {
        return new DepotRenderState();
    }

    @Override
    public void extractRenderState(
        DepotBlockEntity be,
        DepotRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        state.blockPos = be.getBlockPos();
        state.blockEntityType = be.getType();
        Level world = be.getLevel();
        state.lightCoords = world != null ? LevelRenderer.getLightCoords(world, state.blockPos) : LightCoordsUtil.FULL_BRIGHT;
        DepotBehaviour depotBehaviour = be.depotBehaviour;
        state.incoming = createIncomingStateList(depotBehaviour, itemModelManager, tickProgress, world);
        state.outputs = createOutputStateList(depotBehaviour, itemModelManager, world);
    }

    public static DepotItemState @Nullable [] createIncomingStateList(
        DepotBehaviour depotBehaviour,
        ItemModelResolver itemModelManager,
        float tickProgress,
        @Nullable Level world
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
    public static List<DepotOutputItemState> createOutputStateList(
        DepotBehaviour depotBehaviour,
        ItemModelResolver itemModelManager,
        @Nullable Level world
    ) {
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
    public void submit(DepotRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        if (state.incoming != null || state.outputs != null) {
            renderItemsOf(state.incoming, state.outputs, state.blockPos, cameraState.pos, queue, matrices, state.lightCoords);
        }
    }

    public static void renderItemsOf(
        DepotItemState @Nullable [] incoming,
        @Nullable List<DepotOutputItemState> outputs,
        BlockPos pos,
        Vec3 cameraPos,
        SubmitNodeCollector queue,
        PoseStack ms,
        int light
    ) {
        var msr = TransformStack.of(ms);
        Vec3 itemPosition = VecHelper.getCenterOf(pos);

        ms.pushPose();
        ms.translate(.5f, 15 / 16f, .5f);

        // Render main items
        if (incoming != null) {
            for (DepotItemState item : incoming) {
                ms.pushPose();
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
                ms.popPose();
            }
        }

        // Render output items
        if (outputs != null) {
            for (int i = 0, size = outputs.size(); i < size; i++) {
                DepotOutputItemState item = outputs.get(i);
                ms.pushPose();
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
                ms.popPose();
            }
        }

        ms.popPose();
    }

    public static void renderItem(
        SubmitNodeCollector queue,
        PoseStack ms,
        int light,
        ItemStackRenderState state,
        int angle,
        boolean upright,
        boolean box,
        int count,
        @Nullable Random r,
        Vec3 itemPosition,
        Vec3 cameraPos,
        boolean alwaysUpright,
        @Nullable BiConsumer<PoseTransformStack, Boolean> transform
    ) {
        boolean blockItem = state.usesBlockLight();
        var msr = TransformStack.of(ms);
        if (transform != null) {
            transform.accept(msr, blockItem);
        }
        boolean renderUpright = upright || alwaysUpright && !blockItem;

        ms.pushPose();
        msr.rotateYDegrees(angle);

        if (renderUpright) {
            Vec3 diff = itemPosition.subtract(cameraPos);
            float yRot = (float) (Mth.atan2(diff.x, diff.z) + Math.PI);
            ms.mulPose(Axis.YP.rotation(yRot));
            ms.translate(0, 3 / 32d, -1 / 16f);
        }

        for (int i = 0; i <= count; i++) {
            ms.pushPose();
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
            state.submit(ms, queue, light, OverlayTexture.NO_OVERLAY, 0);
            ms.popPose();

            if (!renderUpright) {
                if (!blockItem)
                    msr.rotateYDegrees(10);
                ms.translate(0, blockItem ? 1 / 64d : 1 / 16d, 0);
            } else
                ms.translate(0, 0, -1 / 16f);
        }

        ms.popPose();
    }

    public static class DepotRenderState extends BlockEntityRenderState {
        public DepotItemState @Nullable [] incoming;
        public @Nullable List<DepotOutputItemState> outputs;
    }

    public record DepotItemState(ItemStackRenderState state, int angle, @Nullable Vec3 offset, boolean upright, boolean box, int count) {
        public static DepotItemState create(ItemModelResolver itemModelManager, TransportedItemStack tis, float partialTicks, @Nullable Level world) {
            Vec3 offsetVec;
            if (tis.insertedFrom.getAxis().isHorizontal()) {
                float offset = Mth.lerp(partialTicks, tis.prevBeltPosition, tis.beltPosition);
                float sideOffset = Mth.lerp(partialTicks, tis.prevSideOffset, tis.sideOffset);
                boolean alongX = tis.insertedFrom.getClockWise().getAxis() == Direction.Axis.X;
                if (!alongX)
                    sideOffset *= -1;
                offsetVec = Vec3.atLowerCornerOf(tis.insertedFrom.getOpposite().getUnitVec3i()).scale(.5f - offset)
                    .add(alongX ? sideOffset : 0, 0, alongX ? 0 : sideOffset);
            } else {
                offsetVec = null;
            }
            ItemStack stack = tis.stack;
            ItemStackRenderState state = new ItemStackRenderState();
            state.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.appendItemLayers(state, stack, state.displayContext, world, null, 0);
            boolean upright = BeltHelper.isItemUpright(stack);
            boolean box = PackageItem.isPackage(stack);
            int count = Mth.log2(stack.getCount()) / 2;
            return new DepotItemState(state, tis.angle, offsetVec, upright, box, count);
        }
    }

    public record DepotOutputItemState(ItemStackRenderState state, boolean upright, boolean box, int count) {
        public static DepotOutputItemState create(ItemModelResolver itemModelManager, ItemStack stack, @Nullable Level world) {
            ItemStackRenderState state = new ItemStackRenderState();
            state.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.appendItemLayers(state, stack, state.displayContext, world, null, 0);
            boolean upright = BeltHelper.isItemUpright(stack);
            boolean box = PackageItem.isPackage(stack);
            int count = Mth.log2(stack.getCount()) / 2;
            return new DepotOutputItemState(state, upright, box, count);
        }
    }
}
