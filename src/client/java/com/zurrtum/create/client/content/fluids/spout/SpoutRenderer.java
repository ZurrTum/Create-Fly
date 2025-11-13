package com.zurrtum.create.client.content.fluids.spout;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.catnip.render.PonderRenderTypes;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.lib.util.ShadersModHelper;
import com.zurrtum.create.content.fluids.spout.SpoutBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SpoutRenderer implements BlockEntityRenderer<SpoutBlockEntity, SpoutRenderer.SpoutRenderState> {
    public SpoutRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public SpoutRenderState createRenderState() {
        return new SpoutRenderState();
    }

    @Override
    public void extractRenderState(
        SpoutBlockEntity be,
        SpoutRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        SmartFluidTankBehaviour tank = be.tank;
        if (tank == null) {
            return;
        }
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
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
                    ShadersModHelper.isShaderPackInUse() ? RenderType.translucentMovingBlock() : PonderRenderTypes.fluid(),
                    fluidStack.getFluid(),
                    fluidStack.getComponentChanges(),
                    min,
                    max,
                    yMin,
                    offset,
                    state.lightCoords
                );
            }
            if (processingTicks != -1) {
                float processingProgress = 1 - (processingPT - 5) / 10;
                processingProgress = Mth.clamp(processingProgress, 0, 1);
                radius = (float) (Math.pow(((2 * processingProgress) - 1), 2) - 1);
                AABB box = new AABB(0.5, 0.0, 0.5, 0.5, -1.2, 0.5).inflate(radius / 32f);
                state.process = new ProcessRenderState(
                    PonderRenderTypes.fluid(),
                    fluidStack.getFluid(),
                    fluidStack.getComponentChanges(),
                    box,
                    state.lightCoords
                );
            }
        }
        float squeeze;
        if (processingPT < 0) {
            squeeze = 0;
        } else if (processingPT < 2) {
            squeeze = Mth.lerpInt(processingPT / 2f, 0, -1);
        } else if (processingPT < 10) {
            squeeze = -1;
        } else {
            squeeze = radius;
        }
        SuperByteBuffer top = CachedBuffers.partial(AllPartialModels.SPOUT_TOP, state.blockState);
        SuperByteBuffer middle = CachedBuffers.partial(AllPartialModels.SPOUT_MIDDLE, state.blockState);
        SuperByteBuffer bottom = CachedBuffers.partial(AllPartialModels.SPOUT_BOTTOM, state.blockState);
        float offset = -3 * squeeze / 32f;
        state.bits = new BitsRenderState(RenderType.solid(), top, middle, bottom, offset, state.lightCoords);
    }

    @Override
    public void submit(SpoutRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        if (state.process != null) {
            queue.submitCustomGeometry(matrices, state.process.layer, state.process);
        }
        queue.submitCustomGeometry(matrices, state.bits.layer, state.bits);
        if (state.fluid != null) {
            matrices.translate(0, state.fluid.offset, 0);
            queue.submitCustomGeometry(matrices, state.fluid.layer, state.fluid);
        }
    }

    public static class SpoutRenderState extends BlockEntityRenderState {
        public FluidRenderState fluid;
        public ProcessRenderState process;
        public BitsRenderState bits;
    }

    public record FluidRenderState(
        RenderType layer, Fluid fluid, DataComponentPatch changes, float min, float max, float yMin, float offset, int light
    ) implements SubmitNodeCollector.CustomGeometryRenderer {
        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            FluidRenderHelper.renderFluidBox(fluid, changes, min, yMin, min, max, min, max, vertexConsumer, matricesEntry, light, false, true);
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
                true
            );
        }
    }

    public record BitsRenderState(
        RenderType layer, SuperByteBuffer top, SuperByteBuffer middle, SuperByteBuffer bottom, float offset, int light
    ) implements SubmitNodeCollector.CustomGeometryRenderer {
        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            top.light(light).renderInto(matricesEntry, vertexConsumer);
            matricesEntry.translate(0, offset, 0);
            middle.light(light).renderInto(matricesEntry, vertexConsumer);
            matricesEntry.translate(0, offset, 0);
            bottom.light(light).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
