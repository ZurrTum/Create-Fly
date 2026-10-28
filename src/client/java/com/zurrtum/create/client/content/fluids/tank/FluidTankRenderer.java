package com.zurrtum.create.client.content.fluids.tank;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper.FluidRenderState;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.fluids.tank.FluidTankRenderer.FluidTankRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.fluids.tank.FluidTankBlockEntity;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public class FluidTankRenderer implements BlockEntityRenderer<FluidTankBlockEntity, FluidTankRenderState> {
    protected final FluidStateModelSet fluidStateModelSet;

    public FluidTankRenderer(Context context) {
        fluidStateModelSet = context.blockModelResolver().modelManager.getFluidStateModelSet();
    }

    @Override
    public FluidTankRenderState createRenderState() {
        return new FluidTankRenderState();
    }

    @Override
    public boolean shouldRender(FluidTankBlockEntity be, Vec3 cameraPosition) {
        return be.isController() && BlockEntityRenderer.super.shouldRender(be, cameraPosition);
    }

    @Override
    public void extractRenderState(
        FluidTankBlockEntity be,
        FluidTankRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
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
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        LerpedFloat fluidLevel = be.getFluidLevel();
        if (fluidLevel == null) {
            return;
        }
        float capHeight = 0.25f;
        float minPuddleHeight = 0.0625f;
        float totalHeight = be.getHeight() - 2 * capHeight - minPuddleHeight;
        float level = fluidLevel.getValue(tickProgress);
        if (level < 1 / (512.0f * totalHeight)) {
            return;
        }
        FluidStack fluidStack = be.getTankInventory().getFluid();
        if (fluidStack.isEmpty()) {
            return;
        }
        Level world = be.getLevel();
        state.blockPos = be.getBlockPos();
        state.blockState = be.getBlockState();
        state.blockEntityType = be.getType();
        state.lightCoords =
            world != null ? LightCoordsUtil.getLightCoords(world, state.blockPos) : LightCoordsUtil.FULL_BRIGHT;
        state.breakProgress = crumblingOverlay;
        float clampedLevel = Mth.clamp(level * totalHeight, 0, totalHeight);
        state.translate = clampedLevel - totalHeight;

        //TODO
        boolean top = false;//fluidStack.getFluid()
        //			.getFluidType()
        //            .isLighterThanAir();

        int width = be.getWidth();
        float xMin = 0.0625f + 1 / 128.0f;
        float xMax = xMin + width - 2 * xMin;
        float yMin = totalHeight + capHeight + minPuddleHeight - clampedLevel;
        float yMax = yMin + clampedLevel;

        if (top) {
            yMin += totalHeight - clampedLevel;
            yMax += totalHeight - clampedLevel;
        }

        float zMax = xMin + width - 2 * xMin;
        state.fluid = FluidRenderHelper.extractFluidRenderState(
            world instanceof BlockAndTintGetter getter ? getter : null,
            state.blockPos,
            fluidStateModelSet,
            fluidStack.getFluid(),
            fluidStack.getComponentChanges(),
            xMin,
            yMin,
            xMin,
            xMax,
            yMax,
            zMax,
            state.lightCoords,
            false,
            true
        );
    }

    public void updateBoilerState(
        FluidTankBlockEntity be,
        FluidTankRenderState state,
        float tickProgress,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        boolean[] occludedDirections = be.boiler.occludedDirections;
        if (occludedDirections[0] && occludedDirections[1] && occludedDirections[2] && occludedDirections[3]) {
            return;
        }
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        state.translate = be.getWidth() / 2.0f;
        BoilerRenderState data = new BoilerRenderState();
        state.boiler = data;
        data.translateX = state.translate - 0.875f;
        data.xRadians = -Radians.DEG_145 * be.boiler.gauge.getValue(tickProgress) + Radians.DEG_90;
        data.gauge = CachedBuffers.partial(AllPartialModels.BOILER_GAUGE, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        data.gaugeDial = CachedBuffers.partial(AllPartialModels.BOILER_GAUGE_DIAL, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        data.south = !occludedDirections[0];
        data.west = !occludedDirections[1];
        data.north = !occludedDirections[2];
        data.east = !occludedDirections[3];
    }

    @Override
    public void submit(
        FluidTankRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.fluid != null) {
            matrices.translate(0, state.translate, 0);
            state.fluid.submit(matrices, queue);
        } else if (state.boiler != null) {
            matrices.translate(state.translate, 0.5, state.translate);
            state.boiler.submit(queue, matrices);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(/*FluidTankBlockEntity be*/) {
        //TODO
        //        return be.isController();
        return true;
    }

    public static class FluidTankRenderState extends BlockEntityRenderState {
        public float translate;
        public @Nullable FluidRenderState fluid;
        public @Nullable BoilerRenderState boiler;
    }

    public static class BoilerRenderState {
        @UnknownNullability
        public SuperByteBufferRenderState gauge, gaugeDial;
        public float translateX;
        public float xRadians;
        public boolean south, west, north, east;

        public void submit(SubmitNodeCollector queue, PoseStack poseStack) {
            if (south) {
                submit(poseStack, queue, Radians.DEG_270);
            }
            if (west) {
                submit(poseStack, queue, Radians.DEG_180);
            }
            if (north) {
                submit(poseStack, queue, Radians.DEG_90);
            }
            if (east) {
                submit(poseStack, queue, 0);
            }
        }

        public void submit(PoseStack poseStack, SubmitNodeCollector queue, float yRadians) {
            poseStack.pushPose();
            Pose pose = poseStack.last();
            pose.rotate(Axis.YP, yRadians);
            pose.translate(translateX, -0.5f, -0.5f);
            gauge.submit(poseStack, queue);
            pose.rotateXAround(xRadians, 0, 0.375f, 0.5f);
            gaugeDial.submit(poseStack, queue);
            poseStack.popPose();
        }
    }
}
