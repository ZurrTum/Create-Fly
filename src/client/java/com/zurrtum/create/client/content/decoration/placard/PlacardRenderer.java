package com.zurrtum.create.client.content.decoration.placard;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.content.decoration.placard.PlacardBlock;
import com.zurrtum.create.content.decoration.placard.PlacardBlockEntity;
import net.minecraft.block.enums.BlockFace;
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
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public class PlacardRenderer implements BlockEntityRenderer<PlacardBlockEntity, PlacardRenderer.PlacardRenderState> {
    protected final ItemModelManager itemModelManager;

    public PlacardRenderer(BlockEntityRendererFactory.Context context) {
        itemModelManager = context.itemModelManager();
    }

    @Override
    public PlacardRenderState createRenderState() {
        return new PlacardRenderState();
    }

    @Override
    public void updateRenderState(
        PlacardBlockEntity be,
        PlacardRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        ItemStack heldItem = be.getHeldItem();
        if (heldItem.isEmpty()) {
            return;
        }
        BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        Direction facing = state.blockState.get(PlacardBlock.FACING);
        BlockFace face = state.blockState.get(PlacardBlock.FACE);
        ItemRenderState item = state.item = new ItemRenderState();
        item.displayContext = ItemDisplayContext.FIXED;
        itemModelManager.update(item, heldItem, item.displayContext, be.getWorld(), null, 0);
        boolean isCeiling = face == BlockFace.CEILING;
        state.upAngle = (isCeiling ? MathHelper.PI : 0) + AngleHelper.rad(180 + AngleHelper.horizontalAngle(facing));
        state.eastAngle = isCeiling ? -MathHelper.PI / 2 : face == BlockFace.FLOOR ? MathHelper.PI / 2 : 0;
        state.scale = item.isSideLit() ? 0.5f : 0.375f;
    }

    @Override
    public void render(PlacardRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.multiply(new Quaternionf().setAngleAxis(state.upAngle, 0, 1, 0));
        matrices.multiply(new Quaternionf().setAngleAxis(state.eastAngle, 1, 0, 0));
        matrices.translate(0, 0, 0.28125f);
        float scale = state.scale;
        matrices.scale(scale, scale, scale);
        state.item.render(matrices, queue, state.lightmapCoordinates, OverlayTexture.DEFAULT_UV, 0);
    }

    public static class PlacardRenderState extends BlockEntityRenderState {
        public ItemRenderState item;
        public float upAngle;
        public float eastAngle;
        public float scale;
    }
}
