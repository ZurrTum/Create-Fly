package com.zurrtum.create.client.content.fluids.drain;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.fluids.drain.ItemDrainBlockEntity;
import com.zurrtum.create.content.fluids.transfer.GenericItemEmptying;
import com.zurrtum.create.content.kinetics.belt.BeltHelper;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Random;

public class ItemDrainRenderer implements BlockEntityRenderer<ItemDrainBlockEntity, ItemDrainRenderer.ItemDrainRenderState> {
    protected final ItemModelResolver itemModelManager;

    public ItemDrainRenderer(BlockEntityRendererProvider.Context context) {
        itemModelManager = context.itemModelResolver();
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
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        updateFluidRenderState(be, state, tickProgress);
        updateItemRenderState(be, state, itemModelManager, tickProgress);
    }

    @Override
    public void submit(ItemDrainRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        if (state.process != null) {
            queue.submitCustomGeometry(matrices, state.process.layer, state.process);
        }
        if (state.item != null) {
            state.item.render(matrices, queue, cameraState.pos, state.lightCoords);
        }
        if (state.fluid != null) {
            matrices.translate(0, state.fluid.offset, 0);
            queue.submitCustomGeometry(matrices, state.fluid.layer, state.fluid);
        }
    }

    public static void updateFluidRenderState(ItemDrainBlockEntity be, ItemDrainRenderState state, float tickProgress) {
        SmartFluidTankBehaviour tank = be.internalTank;
        if (tank == null) {
            return;
        }
        TankSegment primaryTank = tank.getPrimaryTank();
        FluidStack fluidStack = primaryTank.getRenderedFluid();
        if (!fluidStack.isEmpty()) {
            float level = primaryTank.getFluidLevel().getValue(tickProgress);
            if (level != 0) {
                float yMax = 5f / 16f;
                float min = 2f / 16f;
                float max = min + (12 / 16f);
                float yOffset = (7 / 16f) * level;
                float yMin = yMax - yOffset;
                state.fluid = new FluidRenderState(
                    RenderTypes.translucentMovingBlock(),
                    fluidStack.getFluid(),
                    fluidStack.getComponentChanges(),
                    min,
                    max,
                    yMin,
                    yMax,
                    yOffset,
                    state.lightCoords
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
        FluidStack fluidStack2 = GenericItemEmptying.emptyItem(be.getLevel(), heldItemStack, true).getFirst();
        if (fluidStack2.isEmpty()) {
            if (fluidStack.isEmpty()) {
                return;
            }
            fluidStack2 = fluidStack;
        }
        float processingPT = processingTicks - tickProgress;
        float processingProgress = 1 - (processingPT - 5) / 10;
        processingProgress = Mth.clamp(processingProgress, 0, 1);
        float radius = (float) (Math.pow(((2 * processingProgress) - 1), 2) - 1);
        AABB box = new AABB(0.5, 1.0, 0.5, 0.5, 0.25, 0.5).inflate(radius / 32f);
        state.process = new ProcessRenderState(
            RenderTypes.translucentMovingBlock(),
            fluidStack2.getFluid(),
            fluidStack2.getComponentChanges(),
            box,
            state.lightCoords
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
        item.offsetVec = Vec3.atLowerCornerOf(insertedFrom.getOpposite().getUnitVec3i()).scale(.5f - offset);
        boolean alongX = insertedFrom.getClockWise().getAxis() == Axis.X;
        if (!alongX)
            sideOffset *= -1;
        item.translate = item.offsetVec.add(alongX ? sideOffset : 0, 0, alongX ? 0 : sideOffset);
        ItemStack itemStack = transported.stack;
        item.count = Mth.log2(itemStack.getCount()) / 2;
        item.upright = BeltHelper.isItemUpright(itemStack);
        int positive = insertedFrom.getAxisDirection().getStep();
        item.axis = insertedFrom.getAxis();
        item.verticalAngle = positive * offset * 360;
        ItemStackRenderState renderState = state.item.state = new ItemStackRenderState();
        renderState.displayContext = ItemDisplayContext.FIXED;
        itemModelManager.appendItemLayers(renderState, itemStack, renderState.displayContext, be.getLevel(), null, 0);
    }

    public static class ItemDrainRenderState extends BlockEntityRenderState {
        public FluidRenderState fluid;
        public ProcessRenderState process;
        public HeldItemRenderState item;
    }

    public record FluidRenderState(
        RenderType layer, Fluid fluid, DataComponentPatch changes, float min, float max, float yMin, float yMax, float offset, int light
    ) implements SubmitNodeCollector.CustomGeometryRenderer {
        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            FluidRenderHelper.renderFluidBox(fluid, changes, min, yMin, min, max, yMax, max, vertexConsumer, matricesEntry, light, false, false);
        }
    }

    public record ProcessRenderState(
        RenderType layer, Fluid fluid, DataComponentPatch changes, AABB box, int light
    ) implements SubmitNodeCollector.CustomGeometryRenderer {
        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            FluidRenderHelper.renderFluidBox(
                fluid,
                changes,
                (float) box.minX,
                (float) box.minY,
                (float) box.minZ,
                (float) box.maxX,
                (float) box.maxY,
                (float) box.maxZ,
                vertexConsumer,
                matricesEntry,
                light,
                true,
                false
            );
        }
    }

    public static class HeldItemRenderState {
        public ItemStackRenderState state;
        public Vec3 itemPosition;
        public Vec3 translate;
        public Vec3 offsetVec;
        public int count;
        public boolean upright;
        public Axis axis;
        public float verticalAngle;

        public void render(PoseStack matrices, SubmitNodeCollector queue, Vec3 positionVec, int light) {
            var msr = TransformStack.of(matrices);
            matrices.pushPose();
            matrices.translate(.5f, 15 / 16f, .5f);
            msr.nudge(0);
            matrices.translate(translate);
            boolean renderUpright = upright;
            if (renderUpright) {
                matrices.translate(0, 3 / 32d, 0);
            }
            if (axis != Axis.X) {
                msr.rotateXDegrees(verticalAngle);
            }
            if (axis != Axis.Z) {
                msr.rotateZDegrees(-verticalAngle);
            }
            if (renderUpright) {
                Vec3 vectorForOffset = itemPosition.add(offsetVec);
                Vec3 diff = vectorForOffset.subtract(positionVec);
                if (axis != Axis.X) {
                    diff = VecHelper.rotate(diff, verticalAngle, Axis.X);
                }
                if (axis != Axis.Z) {
                    diff = VecHelper.rotate(diff, -verticalAngle, Axis.Z);
                }
                float yRot = (float) Mth.atan2(diff.z, -diff.x);
                matrices.mulPose(com.mojang.math.Axis.YP.rotation((float) (yRot - Math.PI / 2)));
                matrices.translate(0, 0, -1 / 16f);
            }
            Random r = new Random(0);
            boolean blockItem = state.usesBlockLight();
            for (int i = 0; i < count; i++) {
                matrices.pushPose();
                if (blockItem) {
                    matrices.translate(r.nextFloat() * .0625f * i, 0, r.nextFloat() * .0625f * i);
                }
                matrices.scale(.5f, .5f, .5f);
                if (!blockItem && !renderUpright) {
                    msr.rotateXDegrees(90);
                }
                state.submit(matrices, queue, light, OverlayTexture.NO_OVERLAY, 0);
                matrices.popPose();
                if (!renderUpright) {
                    if (!blockItem) {
                        msr.rotateYDegrees(10);
                    }
                    matrices.translate(0, blockItem ? 1 / 64d : 1 / 16d, 0);
                } else {
                    matrices.translate(0, 0, -1 / 16f);
                }
            }
            matrices.pushPose();
            if (blockItem) {
                matrices.translate(r.nextFloat() * .0625f * count, 0, r.nextFloat() * .0625f * count);
            }
            matrices.scale(.5f, .5f, .5f);
            if (!blockItem && !renderUpright) {
                msr.rotateXDegrees(90);
            }
            state.submit(matrices, queue, light, OverlayTexture.NO_OVERLAY, 0);
            matrices.popPose();
            matrices.popPose();
        }
    }
}
