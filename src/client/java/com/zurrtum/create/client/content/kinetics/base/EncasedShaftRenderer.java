package com.zurrtum.create.client.content.kinetics.base;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.*;

public class EncasedShaftRenderer implements BlockEntityRenderer<KineticBlockEntity, SingleKineticRenderState> {
    public EncasedShaftRenderer(Context context) {
    }

    @Override
    public SingleKineticRenderState createRenderState() {
        return new SingleKineticRenderState();
    }

    @Override
    public void extractRenderState(
        KineticBlockEntity be,
        SingleKineticRenderState state,
        float partialTicks,
        Vec3 cameraPosition,
        @Nullable CrumblingOverlay breakProgress
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, breakProgress);
        Axis axis = getRotationAxisOf(state.blockState);
        state.model = CachedBuffers.partialFacingVertical(
            AllPartialModels.ENCASED_SHAFT,
            state.blockState,
            axis.getPositive()
        ).cardinalLighting(level).light(state.lightCoords).color(getTintColor(be)).extractRenderState();
        state.angle = getRotateAngleWithoutBeOffset(axis, be, state, level);
    }

    @Override
    public void submit(
        SingleKineticRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState camera
    ) {
        state.submit(matrices, queue);
    }
}
