package com.zurrtum.create.client.content.logistics.tunnel;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.logistics.FlapStuffs;
import com.zurrtum.create.client.content.logistics.FlapStuffs.FlapsRenderState;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.logistics.tunnel.BeltTunnelBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BeltTunnelRenderer extends SmartBlockEntityRenderer<BeltTunnelBlockEntity, BeltTunnelRenderer.BeltTunnelRenderState> {
    public BeltTunnelRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public BeltTunnelRenderState createRenderState() {
        return new BeltTunnelRenderState();
    }

    @Override
    public void updateRenderState(
        BeltTunnelBlockEntity be,
        BeltTunnelRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        super.updateRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        if (VisualizationManager.supportsVisualization(be.getWorld())) {
            return;
        }
        SuperByteBuffer flapBuffer = CachedBuffers.partial(AllPartialModels.BELT_TUNNEL_FLAP, state.blockState);
        List<FlapsRenderState> flaps = new ArrayList<>();
        for (Direction direction : Iterate.directions) {
            if (!be.flaps.containsKey(direction)) {
                continue;
            }
            float f = be.flaps.get(direction).getValue(tickProgress);
            flaps.add(FlapStuffs.getFlapsRenderState(flapBuffer, FlapStuffs.TUNNEL_PIVOT, direction, f, 0, state.lightmapCoordinates));
        }
        state.flaps = flaps;
    }

    @Override
    public void render(BeltTunnelRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        super.render(state, matrices, queue, cameraState);
        if (state.flaps != null) {
            RenderLayer layer = RenderLayer.getSolid();
            for (FlapsRenderState flap : state.flaps) {
                flap.render(layer, matrices, queue);
            }
        }
    }

    public static class BeltTunnelRenderState extends SmartRenderState {
        public List<FlapsRenderState> flaps;
    }
}
