package com.zurrtum.create.client.content.logistics.funnel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.logistics.FlapStuffs;
import com.zurrtum.create.client.content.logistics.FlapStuffs.FlapsRenderState;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.logistics.funnel.FunnelBlock;
import com.zurrtum.create.content.logistics.funnel.FunnelBlockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class FunnelRenderer extends SmartBlockEntityRenderer<FunnelBlockEntity, FunnelRenderer.FunnelRenderState> {
    public FunnelRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public FunnelRenderState createRenderState() {
        return new FunnelRenderState();
    }

    @Override
    public void extractRenderState(
        FunnelBlockEntity be,
        FunnelRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        if (!be.hasFlap() || VisualizationManager.supportsVisualization(be.getLevel())) {
            return;
        }
        Direction funnelFacing = FunnelBlock.getFunnelFacing(state.blockState);
        if (funnelFacing == null) {
            return;
        }
        PartialModel partialModel = (state.blockState.getBlock() instanceof FunnelBlock ? AllPartialModels.FUNNEL_FLAP : AllPartialModels.BELT_FUNNEL_FLAP);
        SuperByteBuffer flapBuffer = CachedBuffers.partial(partialModel, state.blockState);
        float f = be.flap.getValue(tickProgress);
        state.flap = FlapStuffs.getFlapsRenderState(flapBuffer, FlapStuffs.FUNNEL_PIVOT, funnelFacing, f, -be.getFlapOffset(), state.lightCoords);
    }

    @Override
    public void submit(FunnelRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if (state.flap != null) {
            state.flap.render(RenderType.solid(), matrices, queue);
        }
    }

    public static class FunnelRenderState extends SmartRenderState {
        public FlapsRenderState flap;
    }
}
