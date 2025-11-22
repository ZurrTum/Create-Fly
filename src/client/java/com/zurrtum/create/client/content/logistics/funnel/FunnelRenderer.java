package com.zurrtum.create.client.content.logistics.funnel;

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
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class FunnelRenderer extends SmartBlockEntityRenderer<FunnelBlockEntity, FunnelRenderer.FunnelRenderState> {
    public FunnelRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public FunnelRenderState createRenderState() {
        return new FunnelRenderState();
    }

    @Override
    public void updateRenderState(
        FunnelBlockEntity be,
        FunnelRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        super.updateRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        if (!be.hasFlap() || VisualizationManager.supportsVisualization(be.getWorld())) {
            return;
        }
        Direction funnelFacing = FunnelBlock.getFunnelFacing(state.blockState);
        if (funnelFacing == null) {
            return;
        }
        PartialModel partialModel = (state.blockState.getBlock() instanceof FunnelBlock ? AllPartialModels.FUNNEL_FLAP : AllPartialModels.BELT_FUNNEL_FLAP);
        SuperByteBuffer flapBuffer = CachedBuffers.partial(partialModel, state.blockState);
        float f = be.flap.getValue(tickProgress);
        state.flap = FlapStuffs.getFlapsRenderState(
            flapBuffer,
            FlapStuffs.FUNNEL_PIVOT,
            funnelFacing,
            f,
            -be.getFlapOffset(),
            state.lightmapCoordinates
        );
    }

    @Override
    public void render(FunnelRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        super.render(state, matrices, queue, cameraState);
        if (state.flap != null) {
            state.flap.render(RenderLayer.getSolid(), matrices, queue);
        }
    }

    public static class FunnelRenderState extends SmartRenderState {
        public FlapsRenderState flap;
    }
}
