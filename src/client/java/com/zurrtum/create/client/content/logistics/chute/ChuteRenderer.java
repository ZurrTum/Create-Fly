package com.zurrtum.create.client.content.logistics.chute;

import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.logistics.chute.ChuteBlock;
import com.zurrtum.create.content.logistics.chute.ChuteBlock.Shape;
import com.zurrtum.create.content.logistics.chute.ChuteBlockEntity;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ChuteRenderer implements BlockEntityRenderer<ChuteBlockEntity, ChuteRenderer.ChuteRenderState> {
    protected final ItemModelManager itemModelManager;

    public ChuteRenderer(BlockEntityRendererFactory.Context context) {
        itemModelManager = context.itemModelManager();
    }

    @Override
    public ChuteRenderState createRenderState() {
        return new ChuteRenderState();
    }

    @Override
    public void updateRenderState(
        ChuteBlockEntity be,
        ChuteRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        ItemStack item = be.getItem();
        if (item.isEmpty()) {
            return;
        }
        if (state.blockState.get(ChuteBlock.FACING) != Direction.DOWN) {
            return;
        }
        boolean notWindow = state.blockState.get(ChuteBlock.SHAPE) != Shape.WINDOW;
        if (notWindow && be.bottomPullDistance == 0) {
            return;
        }
        float itemPosition = be.itemPosition.getValue(tickProgress);
        if (notWindow && itemPosition > .5f) {
            return;
        }
        state.item = ChuteItemRenderState.create(itemModelManager, item, itemPosition, be.getWorld());
    }

    @Override
    public void render(ChuteRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        if (state.item != null) {
            state.item.render(matrices, queue, state.lightmapCoordinates);
        }
    }

    public static class ChuteRenderState extends BlockEntityRenderState {
        public ChuteItemRenderState item;
    }

    public record ChuteItemRenderState(ItemRenderState item, float offset, float rotate) {
        public static ChuteItemRenderState create(ItemModelManager itemModelManager, ItemStack stack, float itemPosition, World world) {
            float offset = itemPosition - .5f;
            float rotate;
            if (PackageItem.isPackage(stack)) {
                rotate = -1;
            } else {
                rotate = MathHelper.RADIANS_PER_DEGREE * itemPosition * 180;
            }
            ItemRenderState item = new ItemRenderState();
            item.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.update(item, stack, item.displayContext, world, null, 0);
            return new ChuteItemRenderState(item, offset, rotate);
        }

        public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light) {
            matrices.push();
            matrices.translate(0.5f, 0.5f, 0.5f);
            matrices.translate(0, offset, 0);
            if (rotate == -1) {
                matrices.scale(1.5f, 1.5f, 1.5f);
            } else {
                matrices.scale(0.5f, 0.5f, 0.5f);
                matrices.multiply(RotationAxis.POSITIVE_X.rotation(rotate));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotation(rotate));
            }
            item.render(matrices, queue, light, OverlayTexture.DEFAULT_UV, 0);
            matrices.pop();
        }
    }
}
