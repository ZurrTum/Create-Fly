package com.zurrtum.create.client.content.logistics.chute;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.logistics.chute.ChuteBlock;
import com.zurrtum.create.content.logistics.chute.ChuteBlock.Shape;
import com.zurrtum.create.content.logistics.chute.ChuteBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ChuteRenderer implements BlockEntityRenderer<ChuteBlockEntity, ChuteRenderer.ChuteRenderState> {
    protected final ItemModelResolver itemModelManager;

    public ChuteRenderer(BlockEntityRendererProvider.Context context) {
        itemModelManager = context.itemModelResolver();
    }

    @Override
    public ChuteRenderState createRenderState() {
        return new ChuteRenderState();
    }

    @Override
    public void extractRenderState(
        ChuteBlockEntity be,
        ChuteRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        ItemStack item = be.getItem();
        if (item.isEmpty()) {
            return;
        }
        if (state.blockState.getValue(ChuteBlock.FACING) != Direction.DOWN) {
            return;
        }
        boolean notWindow = state.blockState.getValue(ChuteBlock.SHAPE) != Shape.WINDOW;
        if (notWindow && be.bottomPullDistance == 0) {
            return;
        }
        float itemPosition = be.itemPosition.getValue(tickProgress);
        if (notWindow && itemPosition > .5f) {
            return;
        }
        state.item = ChuteItemRenderState.create(itemModelManager, item, itemPosition, be.getLevel());
    }

    @Override
    public void submit(ChuteRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        if (state.item != null) {
            state.item.render(matrices, queue, state.lightCoords);
        }
    }

    public static class ChuteRenderState extends BlockEntityRenderState {
        public ChuteItemRenderState item;
    }

    public record ChuteItemRenderState(ItemStackRenderState item, float offset, float rotate) {
        public static ChuteItemRenderState create(ItemModelResolver itemModelManager, ItemStack stack, float itemPosition, Level world) {
            float offset = itemPosition - .5f;
            float rotate;
            if (PackageItem.isPackage(stack)) {
                rotate = -1;
            } else {
                rotate = Mth.DEG_TO_RAD * itemPosition * 180;
            }
            ItemStackRenderState item = new ItemStackRenderState();
            item.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.appendItemLayers(item, stack, item.displayContext, world, null, 0);
            return new ChuteItemRenderState(item, offset, rotate);
        }

        public void render(PoseStack matrices, SubmitNodeCollector queue, int light) {
            matrices.pushPose();
            matrices.translate(0.5f, 0.5f, 0.5f);
            matrices.translate(0, offset, 0);
            if (rotate == -1) {
                matrices.scale(1.5f, 1.5f, 1.5f);
            } else {
                matrices.scale(0.5f, 0.5f, 0.5f);
                matrices.mulPose(Axis.XP.rotation(rotate));
                matrices.mulPose(Axis.YP.rotation(rotate));
            }
            item.submit(matrices, queue, light, OverlayTexture.NO_OVERLAY, 0);
            matrices.popPose();
        }
    }
}
