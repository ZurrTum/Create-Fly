package com.zurrtum.create.client.content.fluids.drain;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.catnip.render.PonderRenderTypes;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.flywheel.lib.util.ShadersModHelper;
import com.zurrtum.create.content.fluids.drain.ItemDrainBlockEntity;
import com.zurrtum.create.content.fluids.transfer.GenericItemEmptying;
import com.zurrtum.create.content.kinetics.belt.BeltHelper;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.ComponentChanges;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.Axis;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class ItemDrainRenderer implements BlockEntityRenderer<ItemDrainBlockEntity, ItemDrainRenderer.ItemDrainRenderState> {
    protected final ItemModelManager itemModelManager;

    public ItemDrainRenderer(BlockEntityRendererFactory.Context context) {
        itemModelManager = context.itemModelManager();
    }

    @Override
    public ItemDrainRenderState createRenderState() {
        return new ItemDrainRenderState();
    }

    @Override
    public void updateRenderState(
        ItemDrainBlockEntity be,
        ItemDrainRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        updateFluidRenderState(be, state, tickProgress);
        updateItemRenderState(be, state, itemModelManager, tickProgress);
    }

    @Override
    public void render(ItemDrainRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        if (state.process != null) {
            queue.submitCustom(matrices, state.process.layer, state.process);
        }
        if (state.item != null) {
            state.item.render(matrices, queue, cameraState.pos, state.lightmapCoordinates);
        }
        if (state.fluid != null) {
            matrices.translate(0, state.fluid.offset, 0);
            queue.submitCustom(matrices, state.fluid.layer, state.fluid);
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
                    ShadersModHelper.isShaderPackInUse() ? RenderLayer.getTranslucentMovingBlock() : PonderRenderTypes.fluid(),
                    fluidStack.getFluid(),
                    fluidStack.getComponentChanges(),
                    min,
                    max,
                    yMin,
                    yMax,
                    yOffset,
                    state.lightmapCoordinates
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
        FluidStack fluidStack2 = GenericItemEmptying.emptyItem(be.getWorld(), heldItemStack, true).getFirst();
        if (fluidStack2.isEmpty()) {
            if (fluidStack.isEmpty()) {
                return;
            }
            fluidStack2 = fluidStack;
        }
        float processingPT = processingTicks - tickProgress;
        float processingProgress = 1 - (processingPT - 5) / 10;
        processingProgress = MathHelper.clamp(processingProgress, 0, 1);
        float radius = (float) (Math.pow(((2 * processingProgress) - 1), 2) - 1);
        Box box = new Box(0.5, 1.0, 0.5, 0.5, 0.25, 0.5).expand(radius / 32f);
        state.process = new ProcessRenderState(
            PonderRenderTypes.fluid(),
            fluidStack2.getFluid(),
            fluidStack2.getComponentChanges(),
            box,
            state.lightmapCoordinates
        );
    }

    public static void updateItemRenderState(
        ItemDrainBlockEntity be,
        ItemDrainRenderState state,
        ItemModelManager itemModelManager,
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
        item.itemPosition = VecHelper.getCenterOf(state.pos);
        float offset = MathHelper.lerp(tickProgress, transported.prevBeltPosition, transported.beltPosition);
        float sideOffset = MathHelper.lerp(tickProgress, transported.prevSideOffset, transported.sideOffset);
        item.offsetVec = Vec3d.of(insertedFrom.getOpposite().getVector()).multiply(.5f - offset);
        boolean alongX = insertedFrom.rotateYClockwise().getAxis() == Axis.X;
        if (!alongX)
            sideOffset *= -1;
        item.translate = item.offsetVec.add(alongX ? sideOffset : 0, 0, alongX ? 0 : sideOffset);
        ItemStack itemStack = transported.stack;
        item.count = MathHelper.floorLog2(itemStack.getCount()) / 2;
        item.upright = BeltHelper.isItemUpright(itemStack);
        int positive = insertedFrom.getDirection().offset();
        item.axis = insertedFrom.getAxis();
        item.verticalAngle = positive * offset * 360;
        ItemRenderState renderState = state.item.state = new ItemRenderState();
        renderState.displayContext = ItemDisplayContext.FIXED;
        itemModelManager.update(renderState, itemStack, renderState.displayContext, be.getWorld(), null, 0);
    }

    public static class ItemDrainRenderState extends BlockEntityRenderState {
        public FluidRenderState fluid;
        public ProcessRenderState process;
        public HeldItemRenderState item;
    }

    public record FluidRenderState(
        RenderLayer layer, Fluid fluid, ComponentChanges changes, float min, float max, float yMin, float yMax, float offset, int light
    ) implements OrderedRenderCommandQueue.Custom {
        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            FluidRenderHelper.renderFluidBox(fluid, changes, min, yMin, min, max, yMax, max, vertexConsumer, matricesEntry, light, false, false);
        }
    }

    public record ProcessRenderState(
        RenderLayer layer, Fluid fluid, ComponentChanges changes, Box box, int light
    ) implements OrderedRenderCommandQueue.Custom {
        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
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
        public ItemRenderState state;
        public Vec3d itemPosition;
        public Vec3d translate;
        public Vec3d offsetVec;
        public int count;
        public boolean upright;
        public Axis axis;
        public float verticalAngle;

        public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, Vec3d positionVec, int light) {
            var msr = TransformStack.of(matrices);
            matrices.push();
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
                Vec3d vectorForOffset = itemPosition.add(offsetVec);
                Vec3d diff = vectorForOffset.subtract(positionVec);
                if (axis != Axis.X) {
                    diff = VecHelper.rotate(diff, verticalAngle, Axis.X);
                }
                if (axis != Axis.Z) {
                    diff = VecHelper.rotate(diff, -verticalAngle, Axis.Z);
                }
                float yRot = (float) MathHelper.atan2(diff.z, -diff.x);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotation((float) (yRot - Math.PI / 2)));
                matrices.translate(0, 0, -1 / 16f);
            }
            Random r = new Random(0);
            boolean blockItem = state.isSideLit();
            for (int i = 0; i < count; i++) {
                matrices.push();
                if (blockItem) {
                    matrices.translate(r.nextFloat() * .0625f * i, 0, r.nextFloat() * .0625f * i);
                }
                matrices.scale(.5f, .5f, .5f);
                if (!blockItem && !renderUpright) {
                    msr.rotateXDegrees(90);
                }
                state.render(matrices, queue, light, OverlayTexture.DEFAULT_UV, 0);
                matrices.pop();
                if (!renderUpright) {
                    if (!blockItem) {
                        msr.rotateYDegrees(10);
                    }
                    matrices.translate(0, blockItem ? 1 / 64d : 1 / 16d, 0);
                } else {
                    matrices.translate(0, 0, -1 / 16f);
                }
            }
            matrices.push();
            if (blockItem) {
                matrices.translate(r.nextFloat() * .0625f * count, 0, r.nextFloat() * .0625f * count);
            }
            matrices.scale(.5f, .5f, .5f);
            if (!blockItem && !renderUpright) {
                msr.rotateXDegrees(90);
            }
            state.render(matrices, queue, light, OverlayTexture.DEFAULT_UV, 0);
            matrices.pop();
            matrices.pop();
        }
    }
}
