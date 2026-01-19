package com.zurrtum.create.client.content.logistics.chute;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.content.logistics.chute.ChuteRenderer.ChuteItemRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.logistics.chute.SmartChuteBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SmartChuteRenderer extends SmartBlockEntityRenderer<SmartChuteBlockEntity, SmartChuteRenderer.SmartChuteRenderState> {
    public SmartChuteRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public SmartChuteRenderState createRenderState() {
        return new SmartChuteRenderState();
    }

    @Override
    public void extractRenderState(
        SmartChuteBlockEntity be,
        SmartChuteRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        ItemStack item = be.getItem();
        if (item.isEmpty()) {
            return;
        }
        float itemPosition = be.itemPosition.getValue(tickProgress);
        if (itemPosition > 0) {
            return;
        }
        state.item = ChuteItemRenderState.create(itemModelManager, item, itemPosition, be.getLevel());
    }

    @Override
    public void submit(SmartChuteRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if (state.item != null) {
            state.item.render(matrices, queue, state.lightCoords);
        }
    }

    public static class SmartChuteRenderState extends SmartRenderState {
        public ChuteItemRenderState item;
    }
}
