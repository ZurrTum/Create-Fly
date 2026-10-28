package com.zurrtum.create.client.content.fluids.drain;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper.FluidRenderState;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.fluids.drain.ItemDrainRenderer.ItemDrainRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.content.fluids.drain.ItemDrainBlockEntity;
import com.zurrtum.create.content.fluids.transfer.GenericItemEmptying;
import com.zurrtum.create.content.kinetics.belt.BeltHelper;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import java.util.Random;

public class ItemDrainRenderer implements BlockEntityRenderer<ItemDrainBlockEntity, ItemDrainRenderState> {
    protected final ItemModelResolver itemModelManager;
    protected final FluidStateModelSet fluidStateModelSet;

    public ItemDrainRenderer(Context context) {
        itemModelManager = context.itemModelResolver();
        fluidStateModelSet = context.blockModelResolver().modelManager.getFluidStateModelSet();
    }

    @Override
    public ItemDrainRenderState createRenderState() {
        return new ItemDrainRenderState();
    }

    @Override
    public void extractRenderState(
        ItemDrainBlockEntity be,
        ItemDrainRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        updateFluidRenderState(be, state, fluidStateModelSet, tickProgress);
        updateItemRenderState(be, state, itemModelManager, tickProgress);
    }

    @Override
    public void submit(
        ItemDrainRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.process != null) {
            state.process.submit(matrices, queue);
        }
        if (state.item != null) {
            state.item.submit(matrices, queue, cameraState.pos, state.lightCoords);
        }
        if (state.fluid != null) {
            matrices.translate(0, state.offset, 0);
            state.fluid.submit(matrices, queue);
        }
    }

    public static void updateFluidRenderState(
        ItemDrainBlockEntity be,
        ItemDrainRenderState state,
        FluidStateModelSet fluidStateModelSet,
        float tickProgress
    ) {
        SmartFluidTankBehaviour tank = be.internalTank;
        if (tank == null) {
            return;
        }
        TankSegment primaryTank = tank.getPrimaryTank();
        FluidStack fluidStack = primaryTank.getRenderedFluid();
        BlockAndTintGetter world = (BlockAndTintGetter) be.getLevel();
        if (!fluidStack.isEmpty()) {
            float level = primaryTank.getFluidLevel().getValue(tickProgress);
            if (level != 0) {
                state.offset = 0.4375f * level;
                state.fluid = FluidRenderHelper.extractFluidRenderState(
                    world,
                    state.blockPos,
                    fluidStateModelSet,
                    fluidStack.getFluid(),
                    fluidStack.getComponentChanges(),
                    0.125f,
                    0.3125f - state.offset,
                    0.125f,
                    0.875f,
                    0.3125f,
                    0.875f,
                    state.lightCoords,
                    false,
                    false
                );
            }
        }
        ItemStack heldItemStack = be.getHeldItemStack();
        if (heldItemStack.isEmpty()) {
            return;
        }
        int processingTicks = be.processingTicks;
        if (processingTicks == -1) {
            return;
        }
        FluidStack fluidStack2 = GenericItemEmptying.emptyItem(world, heldItemStack, true).getFirst();
        if (fluidStack2.isEmpty()) {
            if (fluidStack.isEmpty()) {
                return;
            }
            fluidStack2 = fluidStack;
        }
        float processingPT = processingTicks - tickProgress;
        float processingProgress = 1 - (processingPT - 5) / 10;
        processingProgress = Mth.clamp(processingProgress, 0, 1);
        float radius = (float) (Math.pow(2 * processingProgress - 1, 2) - 1);
        AABB box = new AABB(0.5, 1.0, 0.5, 0.5, 0.25, 0.5).inflate(radius / 32.0f);
        state.process = FluidRenderHelper.extractFluidRenderState(
            world,
            state.blockPos,
            fluidStateModelSet,
            fluidStack2.getFluid(),
            fluidStack2.getComponentChanges(),
            (float) box.minX,
            (float) box.minY,
            (float) box.minZ,
            (float) box.maxX,
            (float) box.maxY,
            (float) box.maxZ,
            state.lightCoords,
            true,
            false
        );
    }

    public static void updateItemRenderState(
        ItemDrainBlockEntity be,
        ItemDrainRenderState state,
        ItemModelResolver itemModelManager,
        float tickProgress
    ) {
        TransportedItemStack transported = be.heldItem;
        if (transported == null) {
            return;
        }
        Direction insertedFrom = transported.insertedFrom;
        if (!insertedFrom.getAxis().isHorizontal()) {
            return;
        }
        HeldItemRenderState item = state.item = new HeldItemRenderState();
        item.itemPosition = VecHelper.getCenterOf(state.blockPos);
        float offset = Mth.lerp(tickProgress, transported.prevBeltPosition, transported.beltPosition);
        float sideOffset = Mth.lerp(tickProgress, transported.prevSideOffset, transported.sideOffset);
        item.offsetVec = Vec3.atLowerCornerOf(insertedFrom.getOpposite().getUnitVec3i()).scale(0.5f - offset);
        boolean alongX = insertedFrom.getClockWise().getAxis() == Direction.Axis.X;
        if (!alongX) {
            sideOffset *= -1;
        }
        ItemStack itemStack = transported.stack;
        boolean renderUpright = BeltHelper.isItemUpright(itemStack);
        item.translate = item.offsetVec.add(alongX ? sideOffset : 0, 0, alongX ? 0 : sideOffset)
            .add(0.5f, renderUpright ? 1.03125 : 0.9375f, 0.5f);
        item.count = Mth.log2(itemStack.getCount()) / 2;
        item.upright = renderUpright;
        int positive = insertedFrom.getAxisDirection().getStep();
        item.axis = insertedFrom.getAxis();
        item.verticalAngle = positive * offset * 360;
        ItemStackRenderState renderState = state.item.state = new ItemStackRenderState();
        renderState.displayContext = ItemDisplayContext.FIXED;
        itemModelManager.appendItemLayers(renderState, itemStack, renderState.displayContext, be.getLevel(), null, 0);
    }

    public static class ItemDrainRenderState extends BlockEntityRenderState {
        public float offset;
        public @Nullable FluidRenderState fluid;
        public @Nullable FluidRenderState process;
        public @Nullable HeldItemRenderState item;
    }

    public static class HeldItemRenderState {
        public @UnknownNullability ItemStackRenderState state;
        public @UnknownNullability Vec3 itemPosition;
        public @UnknownNullability Vec3 translate;
        public @UnknownNullability Vec3 offsetVec;
        public int count;
        public boolean upright;
        public Direction.Axis axis;
        public float verticalAngle;

        public void submit(PoseStack matrices, SubmitNodeCollector queue, Vec3 positionVec, int light) {
            matrices.pushPose();
            Pose pose = matrices.last();
            pose.translate((float) translate.x, (float) translate.y, (float) translate.z);
            SuperByteBuffer.nudge(pose, 0);
            if (axis != Direction.Axis.X) {
                pose.rotate(Axis.XP, Mth.DEG_TO_RAD * verticalAngle);
            }
            if (axis != Direction.Axis.Z) {
                pose.rotate(Axis.ZP, Mth.DEG_TO_RAD * -verticalAngle);
            }
            boolean renderUpright = upright;
            if (renderUpright) {
                Vec3 vectorForOffset = itemPosition.add(offsetVec);
                Vec3 diff = vectorForOffset.subtract(positionVec);
                if (axis != Direction.Axis.X) {
                    diff = VecHelper.rotate(diff, verticalAngle, Direction.Axis.X);
                }
                if (axis != Direction.Axis.Z) {
                    diff = VecHelper.rotate(diff, -verticalAngle, Direction.Axis.Z);
                }
                pose.rotate(Axis.YP, (float) (Mth.atan2(diff.z, -diff.x) - Radians.DEG_90));
                pose.translate(0, 0, -0.0625f);
            }
            Random r = new Random(0);
            boolean blockItem = state.usesBlockLight();
            for (int i = 0; i < count; i++) {
                matrices.pushPose();
                pose = matrices.last();
                if (blockItem) {
                    pose.translate(r.nextFloat() * 0.0625f * i, 0, r.nextFloat() * 0.0625f * i);
                }
                pose.scale(0.5f, 0.5f, 0.5f);
                if (!blockItem && !renderUpright) {
                    pose.rotate(Axis.XP, Radians.DEG_90);
                }
                state.submit(matrices, queue, light, OverlayTexture.NO_OVERLAY, 0);
                matrices.popPose();
                if (!renderUpright) {
                    pose = matrices.last();
                    if (!blockItem) {
                        pose.rotate(Axis.YP, Radians.DEG_10);
                    }
                    pose.translate(0, blockItem ? 0.015625f : 0.0625f, 0);
                } else {
                    matrices.translate(0, 0, -0.0625f);
                }
            }
            matrices.pushPose();
            pose = matrices.last();
            if (blockItem) {
                pose.translate(r.nextFloat() * 0.0625f * count, 0, r.nextFloat() * 0.0625f * count);
            }
            pose.scale(0.5f, 0.5f, 0.5f);
            if (!blockItem && !renderUpright) {
                pose.rotate(Axis.XP, Radians.DEG_90);
            }
            state.submit(matrices, queue, light, OverlayTexture.NO_OVERLAY, 0);
            matrices.popPose();
            matrices.popPose();
        }
    }
}
