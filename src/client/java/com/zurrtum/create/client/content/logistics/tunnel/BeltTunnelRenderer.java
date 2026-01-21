package com.zurrtum.create.client.content.logistics.tunnel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.logistics.FlapStuffs;
import com.zurrtum.create.client.content.logistics.FlapStuffs.FlapsRenderState;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.logistics.tunnel.BeltTunnelBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BeltTunnelRenderer extends SmartBlockEntityRenderer<BeltTunnelBlockEntity, BeltTunnelRenderer.BeltTunnelRenderState> {
    public BeltTunnelRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public BeltTunnelRenderState createRenderState() {
        return new BeltTunnelRenderState();
    }

    @Override
    public void extractRenderState(
        BeltTunnelBlockEntity be,
        BeltTunnelRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        if (VisualizationManager.supportsVisualization(be.getLevel())) {
            return;
        }
        SuperByteBuffer flapBuffer = CachedBuffers.partial(AllPartialModels.BELT_TUNNEL_FLAP, state.blockState);
        List<FlapsRenderState> flaps = new ArrayList<>();
        for (Direction direction : Iterate.directions) {
            if (!be.flaps.containsKey(direction)) {
                continue;
            }
            float f = be.flaps.get(direction).getValue(tickProgress);
            flaps.add(FlapStuffs.getFlapsRenderState(flapBuffer, FlapStuffs.TUNNEL_PIVOT, direction, f, 0, state.lightCoords));
        }
        state.flaps = flaps;
    }

    @Override
    public void submit(BeltTunnelRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if (state.flaps != null) {
            RenderType layer = RenderTypes.solidMovingBlock();
            for (FlapsRenderState flap : state.flaps) {
                flap.render(layer, matrices, queue);
            }
        }
    }

    public static class BeltTunnelRenderState extends SmartRenderState {
        public @Nullable List<FlapsRenderState> flaps;
    }
}
