package com.zurrtum.create.client.content.logistics.depot;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.logistics.depot.DepotRenderer.DepotRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.belt.BeltHelper;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.logistics.depot.DepotBehaviour;
import com.zurrtum.create.content.logistics.depot.DepotBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DepotRenderer implements BlockEntityRenderer<DepotBlockEntity, DepotRenderState> {
    protected final ItemModelResolver itemModelManager;

    public DepotRenderer(Context context) {
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
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        DepotBehaviour depotBehaviour = be.depotBehaviour;
        state.incoming = createIncomingStateList(depotBehaviour, itemModelManager, tickProgress, level);
        state.outputs = createOutputStateList(depotBehaviour, itemModelManager, level);
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
    public void submit(
        DepotRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.incoming != null || state.outputs != null) {
            renderItemsOf(
                state.incoming,
                state.outputs,
                state.blockPos,
                cameraState.pos,
                queue,
                matrices,
                state.lightCoords
            );
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
        Vec3 itemPosition = VecHelper.getCenterOf(pos);

        ms.pushPose();
        ms.translate(0.5f, 0.9375f, 0.5f);

        // Render main items
        if (incoming != null) {
            for (DepotItemState item : incoming) {
                ms.pushPose();
                Pose pose = ms.last();
                SuperByteBuffer.nudge(pose, 0);
                if (item.offset != null) {
                    pose.translate((float) item.offset.x, (float) item.offset.y, (float) item.offset.z);
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
                    false
                );
                ms.popPose();
            }
        }

        // Render output items
        if (outputs != null) {
            for (int i = 0, size = outputs.size(); i < size; i++) {
                DepotOutputItemState item = outputs.get(i);
                ms.pushPose();
                Pose pose = ms.last();
                SuperByteBuffer.nudge(pose, i);

                boolean renderUpright = item.upright;
                float yRadians = i * Radians.DEG_45;
                pose.rotate(Axis.YP, yRadians);
                pose.translate(0.35f, 0, 0);
                if (renderUpright) {
                    pose.rotate(Axis.YP, -yRadians);
                }
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
                    false
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
        boolean alwaysUpright
    ) {
        boolean blockItem = state.usesBlockLight();
        boolean renderUpright = upright || alwaysUpright && !blockItem;

        ms.pushPose();
        Pose pose = ms.last();
        pose.rotate(Axis.YP, Mth.DEG_TO_RAD * angle);

        if (renderUpright) {
            Vec3 diff = itemPosition.subtract(cameraPos);
            float yRadians = (float) (Mth.atan2(diff.x, diff.z) + Math.PI);
            pose.rotate(Axis.YP, yRadians);
            pose.translate(0, 0.09375f, -0.0625f);
        }

        for (int i = 0; i <= count; i++) {
            ms.pushPose();
            pose = ms.last();
            if (blockItem && r != null) {
                pose.translate(r.nextFloat() * 0.0625f * i, 0, r.nextFloat() * 0.0625f * i);
            }

            if (box && !alwaysUpright) {
                pose.translate(0, 0.25f, 0);
                pose.scale(1.5f, 1.5f, 1.5f);
            } else if (blockItem && alwaysUpright) {
                pose.translate(0, 0.0625f, 0);
                pose.scale(0.755f, 0.755f, 0.755f);
            } else {
                pose.scale(0.5f, 0.5f, 0.5f);
            }

            if (!blockItem && !renderUpright) {
                pose.translate(0, -0.1875f, 0);
                pose.rotate(Axis.XP, Radians.DEG_90);
            }
            state.submit(ms, queue, light, OverlayTexture.NO_OVERLAY, 0);
            ms.popPose();

            if (!renderUpright) {
                pose = ms.last();
                if (!blockItem) {
                    pose.rotate(Axis.YP, Radians.DEG_10);
                }
                pose.translate(0, blockItem ? 0.015625f : 0.0625f, 0);
            } else {
                ms.translate(0, 0, -0.0625f);
            }
        }

        ms.popPose();
    }

    public static class DepotRenderState extends BlockEntityRenderState {
        public DepotItemState @Nullable [] incoming;
        public @Nullable List<DepotOutputItemState> outputs;
    }

    public record DepotItemState(ItemStackRenderState state, int angle, @Nullable Vec3 offset, boolean upright,
                                 boolean box, int count) {
        public static DepotItemState create(
            ItemModelResolver itemModelManager,
            TransportedItemStack tis,
            float partialTicks,
            @Nullable Level world
        ) {
            Vec3 offsetVec;
            if (tis.insertedFrom.getAxis().isHorizontal()) {
                float offset = Mth.lerp(partialTicks, tis.prevBeltPosition, tis.beltPosition);
                float sideOffset = Mth.lerp(partialTicks, tis.prevSideOffset, tis.sideOffset);
                boolean alongX = tis.insertedFrom.getClockWise().getAxis() == Direction.Axis.X;
                if (!alongX) {
                    sideOffset *= -1;
                }
                offsetVec = Vec3.atLowerCornerOf(tis.insertedFrom.getOpposite().getUnitVec3i()).scale(0.5f - offset)
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
        public static DepotOutputItemState create(
            ItemModelResolver itemModelManager,
            ItemStack stack,
            @Nullable Level world
        ) {
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
