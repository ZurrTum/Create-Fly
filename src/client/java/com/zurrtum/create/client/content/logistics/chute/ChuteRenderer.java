package com.zurrtum.create.client.content.logistics.chute;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.client.content.logistics.chute.ChuteRenderer.ChuteRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.logistics.chute.ChuteBlock;
import com.zurrtum.create.content.logistics.chute.ChuteBlock.Shape;
import com.zurrtum.create.content.logistics.chute.ChuteBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

public class ChuteRenderer implements BlockEntityRenderer<ChuteBlockEntity, ChuteRenderState> {
    protected final ItemModelResolver itemModelManager;

    public ChuteRenderer(Context context) {
        itemModelManager = context.itemModelResolver();
    }

    @Override
    public boolean shouldRender(ChuteBlockEntity blockEntity, Vec3 cameraPosition) {
        if (BlockEntityRenderer.super.shouldRender(blockEntity, cameraPosition)) {
            if (blockEntity.getItem().isEmpty()) {
                return false;
            }
            BlockState blockState = blockEntity.getBlockState();
            if (blockState.getValue(ChuteBlock.FACING) != Direction.DOWN) {
                return false;
            }
            return blockState.getValue(ChuteBlock.SHAPE) == Shape.WINDOW || blockEntity.bottomPullDistance != 0;
        }
        return false;
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
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        float itemPosition = be.itemPosition.getValue(tickProgress);
        BlockState blockState = be.getBlockState();
        if (itemPosition > 0.5f && blockState.getValue(ChuteBlock.SHAPE) != Shape.WINDOW) {
            return;
        }
        Level level = be.getLevel();
        state.blockPos = be.getBlockPos();
        state.blockState = blockState;
        state.blockEntityType = be.getType();
        state.lightCoords = SmartBlockEntityRenderer.getLightCoords(level, state.blockPos);
        state.breakProgress = crumblingOverlay;
        state.item = ChuteItemRenderState.create(itemModelManager, be.getItem(), itemPosition, level);
    }

    @Override
    public void submit(
        ChuteRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        state.item.submit(matrices, queue, state.lightCoords);
    }

    public static class ChuteRenderState extends BlockEntityRenderState {
        public @UnknownNullability ChuteItemRenderState item;
    }

    public record ChuteItemRenderState(ItemStackRenderState item, float offset, @Nullable Quaternionf xRot,
                                       Quaternionf yRot) {
        public static ChuteItemRenderState create(
            ItemModelResolver itemModelManager,
            ItemStack stack,
            float itemPosition,
            @Nullable Level world
        ) {
            Quaternionf xRot, yRot;
            if (itemPosition != 0 && !PackageItem.isPackage(stack)) {
                float angle = Mth.DEG_TO_RAD * itemPosition * 180;
                xRot = Axis.XP.rotation(angle);
                yRot = Axis.YP.rotation(angle);
            } else {
                xRot = yRot = null;
            }
            ItemStackRenderState item = new ItemStackRenderState();
            item.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.appendItemLayers(item, stack, item.displayContext, world, null, 0);
            return new ChuteItemRenderState(item, itemPosition, xRot, yRot);
        }

        public void submit(PoseStack matrices, SubmitNodeCollector queue, int light) {
            matrices.pushPose();
            matrices.translate(0.5f, offset, 0.5f);
            if (xRot == null) {
                matrices.scale(1.5f, 1.5f, 1.5f);
            } else {
                matrices.scale(0.5f, 0.5f, 0.5f);
                matrices.mulPose(xRot);
                matrices.mulPose(yRot);
            }
            item.submit(matrices, queue, light, OverlayTexture.NO_OVERLAY, 0);
            matrices.popPose();
        }
    }
}
