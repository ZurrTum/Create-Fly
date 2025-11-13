package com.zurrtum.create.client.content.fluids.tank;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.catnip.render.PonderRenderTypes;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.lib.util.ShadersModHelper;
import com.zurrtum.create.content.fluids.tank.FluidTankBlockEntity;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class FluidTankRenderer implements BlockEntityRenderer<FluidTankBlockEntity, FluidTankRenderer.FluidTankRenderState> {
    public FluidTankRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public FluidTankRenderState createRenderState() {
        return new FluidTankRenderState();
    }

    @Override
    public void extractRenderState(
        FluidTankBlockEntity be,
        FluidTankRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        if (!be.isController()) {
            return;
        }
        if (be.window) {
            updateFluidTankState(be, state, tickProgress, crumblingOverlay);
        } else if (be.boiler.isActive()) {
            updateBoilerState(be, state, tickProgress, crumblingOverlay);
        }
    }

    public void updateFluidTankState(
        FluidTankBlockEntity be,
        FluidTankRenderState state,
        float tickProgress,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        LerpedFloat fluidLevel = be.getFluidLevel();
        if (fluidLevel == null) {
            return;
        }
        float capHeight = 1 / 4f;
        float minPuddleHeight = 1 / 16f;
        float totalHeight = be.getHeight() - 2 * capHeight - minPuddleHeight;
        float level = fluidLevel.getValue(tickProgress);
        if (level < 1 / (512f * totalHeight)) {
            return;
        }
        FluidStack fluidStack = be.getTankInventory().getFluid();
        if (fluidStack.isEmpty()) {
            return;
        }
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.layer = ShadersModHelper.isShaderPackInUse() ? RenderType.translucentMovingBlock() : PonderRenderTypes.fluid();
        FluidTankRenderData data = new FluidTankRenderData();
        state.data = data;
        float clampedLevel = Mth.clamp(level * totalHeight, 0, totalHeight);
        data.translateY = clampedLevel - totalHeight;
        data.light = state.lightCoords;
        data.fluid = fluidStack.getFluid();
        data.changes = fluidStack.getComponentChanges();

        //TODO
        boolean top = false;//fluidStack.getFluid()
        //			.getFluidType()
        //            .isLighterThanAir();

        int width = be.getWidth();
        float tankHullWidth = 1 / 16f + 1 / 128f;
        data.xMin = tankHullWidth;
        data.xMax = data.xMin + width - 2 * tankHullWidth;
        data.yMin = totalHeight + capHeight + minPuddleHeight - clampedLevel;
        data.yMax = data.yMin + clampedLevel;

        if (top) {
            data.yMin += totalHeight - clampedLevel;
            data.yMax += totalHeight - clampedLevel;
        }

        data.zMin = tankHullWidth;
        data.zMax = data.zMin + width - 2 * tankHullWidth;
    }

    public void updateBoilerState(
        FluidTankBlockEntity be,
        FluidTankRenderState state,
        float tickProgress,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        boolean[] occludedDirections = be.boiler.occludedDirections;
        if (occludedDirections[0] && occludedDirections[1] && occludedDirections[2] && occludedDirections[3]) {
            return;
        }
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.layer = RenderType.cutout();
        BoilerRenderData data = new BoilerRenderData();
        state.data = data;
        data.translateXZ = be.getWidth() / 2f;
        data.light = state.lightCoords;
        data.translateX = data.translateXZ - 6 / 16f;
        data.dialPivotY = 6f / 16;
        data.dialPivotZ = 8f / 16;
        data.progress = -145 * be.boiler.gauge.getValue(tickProgress) + 90;
        data.gauge = CachedBuffers.partial(AllPartialModels.BOILER_GAUGE, state.blockState);
        data.gaugeDial = CachedBuffers.partial(AllPartialModels.BOILER_GAUGE_DIAL, state.blockState);
        data.south = !occludedDirections[0];
        data.west = !occludedDirections[1];
        data.north = !occludedDirections[2];
        data.east = !occludedDirections[3];
    }

    @Override
    public void submit(FluidTankRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        if (state.data != null) {
            state.data.translate(matrices);
            queue.submitCustomGeometry(matrices, state.layer, state.data);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(/*FluidTankBlockEntity be*/) {
        //TODO
        //        return be.isController();
        return true;
    }

    public static class FluidTankRenderState extends BlockEntityRenderState {
        public RenderType layer;
        public RenderData data;
    }

    public interface RenderData extends SubmitNodeCollector.CustomGeometryRenderer {
        void translate(PoseStack matrices);
    }

    public static class FluidTankRenderData implements RenderData {
        public Fluid fluid;
        public DataComponentPatch changes;
        public float xMin, xMax, yMin, yMax, zMin, zMax, translateY;
        public int light;

        @Override
        public void translate(PoseStack matrices) {
            matrices.translate(0, translateY, 0);
        }

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            FluidRenderHelper.renderFluidBox(fluid, changes, xMin, yMin, zMin, xMax, yMax, zMax, vertexConsumer, matricesEntry, light, false, true);
        }
    }

    public static class BoilerRenderData implements RenderData {
        public float translateX, dialPivotY, dialPivotZ, progress, translateXZ;
        public SuperByteBuffer gauge, gaugeDial;
        public boolean south, west, north, east;
        public int light;

        @Override
        public void translate(PoseStack matrices) {
            matrices.translate(translateXZ, 0.5, translateXZ);
        }

        public void render(int yRot, PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            gauge.rotateYDegrees(yRot).uncenter().translate(translateX, 0, 0).light(light).renderInto(matricesEntry, vertexConsumer);
            gaugeDial.rotateYDegrees(yRot).uncenter().translate(translateX, 0, 0).translate(0, dialPivotY, dialPivotZ).rotateXDegrees(progress)
                .translate(0, -dialPivotY, -dialPivotZ).light(light).renderInto(matricesEntry, vertexConsumer);
        }

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            if (south) {
                render(-90, matricesEntry, vertexConsumer);
            }
            if (west) {
                render(-180, matricesEntry, vertexConsumer);
            }
            if (north) {
                render(-270, matricesEntry, vertexConsumer);
            }
            if (east) {
                render(-360, matricesEntry, vertexConsumer);
            }
        }
    }
}
