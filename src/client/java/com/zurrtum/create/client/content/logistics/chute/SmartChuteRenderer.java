package com.zurrtum.create.client.content.logistics.chute;

import com.zurrtum.create.client.content.logistics.chute.ChuteRenderer.ChuteItemRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.logistics.chute.SmartChuteBlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class SmartChuteRenderer extends SmartBlockEntityRenderer<SmartChuteBlockEntity, SmartChuteRenderer.SmartChuteRenderState> {
    public SmartChuteRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public SmartChuteRenderState createRenderState() {
        return new SmartChuteRenderState();
    }

    @Override
    public void updateRenderState(
        SmartChuteBlockEntity be,
        SmartChuteRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay
    ) {
        super.updateRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        ItemStack item = be.getItem();
        if (item.isEmpty()) {
            return;
        }
        float itemPosition = be.itemPosition.getValue(tickProgress);
        if (itemPosition > 0) {
            return;
        }
        state.item = ChuteItemRenderState.create(itemModelManager, item, itemPosition, be.getWorld());
    }

    @Override
    public void render(SmartChuteRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        super.render(state, matrices, queue, cameraState);
        if (state.item != null) {
            state.item.render(matrices, queue, state.lightmapCoordinates);
        }
    }

    public static class SmartChuteRenderState extends SmartRenderState {
        public ChuteItemRenderState item;
    }
}
