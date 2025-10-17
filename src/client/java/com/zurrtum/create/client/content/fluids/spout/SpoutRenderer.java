package com.zurrtum.create.client.content.fluids.spout;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.catnip.render.PonderRenderTypes;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.content.fluids.spout.SpoutBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.ComponentChanges;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class SpoutRenderer implements BlockEntityRenderer<SpoutBlockEntity, SpoutRenderer.SpoutRenderState> {
    public SpoutRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public SpoutRenderState createRenderState() {
        return new SpoutRenderState();
    }

    @Override
    public void updateRenderState(
        SpoutBlockEntity be,
        SpoutRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        SmartFluidTankBehaviour tank = be.tank;
        if (tank == null) {
            return;
        }
        BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        TankSegment primaryTank = tank.getPrimaryTank();
        FluidStack fluidStack = primaryTank.getRenderedFluid();
        float radius = 0;
        int processingTicks = be.processingTicks;
        float processingPT = processingTicks - tickProgress;
        if (!fluidStack.isEmpty()) {
            float level = primaryTank.getFluidLevel().getValue(tickProgress);
            if (level != 0) {
                boolean top = false;//TODO fluidStack.getFluid().getFluidType().isLighterThanAir();
                float min = 2.5f / 16f;
                float n = 11 / 16f;
                float max = min + n;
                float yOffset = n * Math.max(level, 0.175f);
                float yMin = min - yOffset;
                float offset = top ? max - min : yOffset;
                state.fluid = new FluidRenderState(
                    PonderRenderTypes.fluid(),
                    fluidStack.getFluid(),
                    fluidStack.getComponentChanges(),
                    min,
                    max,
                    yMin,
                    offset,
                    state.lightmapCoordinates
                );
            }
            if (processingTicks != -1) {
                float processingProgress = 1 - (processingPT - 5) / 10;
                processingProgress = MathHelper.clamp(processingProgress, 0, 1);
                radius = (float) (Math.pow(((2 * processingProgress) - 1), 2) - 1);
                Box box = new Box(0.5, 0.0, 0.5, 0.5, -1.2, 0.5).expand(radius / 32f);
                state.process = new ProcessRenderState(
                    PonderRenderTypes.fluid(),
                    fluidStack.getFluid(),
                    fluidStack.getComponentChanges(),
                    box,
                    state.lightmapCoordinates
                );
            }
        }
        float squeeze;
        if (processingPT < 0) {
            squeeze = 0;
        } else if (processingPT < 2) {
            squeeze = MathHelper.lerp(processingPT / 2f, 0, -1);
        } else if (processingPT < 10) {
            squeeze = -1;
        } else {
            squeeze = radius;
        }
        SuperByteBuffer top = CachedBuffers.partial(AllPartialModels.SPOUT_TOP, state.blockState);
        SuperByteBuffer middle = CachedBuffers.partial(AllPartialModels.SPOUT_MIDDLE, state.blockState);
        SuperByteBuffer bottom = CachedBuffers.partial(AllPartialModels.SPOUT_BOTTOM, state.blockState);
        float offset = -3 * squeeze / 32f;
        state.bits = new BitsRenderState(RenderLayer.getSolid(), top, middle, bottom, offset, state.lightmapCoordinates);
    }

    @Override
    public void render(SpoutRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        if (state.process != null) {
            queue.submitCustom(matrices, state.process.layer, state.process);
        }
        queue.submitCustom(matrices, state.bits.layer, state.bits);
        if (state.fluid != null) {
            matrices.translate(0, state.fluid.offset, 0);
            queue.submitCustom(matrices, state.fluid.layer, state.fluid);
        }
    }

    public static class SpoutRenderState extends BlockEntityRenderState {
        public FluidRenderState fluid;
        public ProcessRenderState process;
        public BitsRenderState bits;
    }

    public record FluidRenderState(
        RenderLayer layer, Fluid fluid, ComponentChanges changes, float min, float max, float yMin, float offset, int light
    ) implements OrderedRenderCommandQueue.Custom {
        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            FluidRenderHelper.renderFluidBox(fluid, changes, min, yMin, min, max, min, max, vertexConsumer, matricesEntry, light, false, true);
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
                true
            );
        }
    }

    public record BitsRenderState(
        RenderLayer layer, SuperByteBuffer top, SuperByteBuffer middle, SuperByteBuffer bottom, float offset, int light
    ) implements OrderedRenderCommandQueue.Custom {
        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            top.light(light).renderInto(matricesEntry, vertexConsumer);
            matricesEntry.translate(0, offset, 0);
            middle.light(light).renderInto(matricesEntry, vertexConsumer);
            matricesEntry.translate(0, offset, 0);
            bottom.light(light).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
