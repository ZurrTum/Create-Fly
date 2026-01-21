package com.zurrtum.create.client.content.decoration.placard;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.content.decoration.placard.PlacardBlock;
import com.zurrtum.create.content.decoration.placard.PlacardBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

public class PlacardRenderer implements BlockEntityRenderer<PlacardBlockEntity, PlacardRenderer.PlacardRenderState> {
    protected final ItemModelResolver itemModelManager;

    public PlacardRenderer(BlockEntityRendererProvider.Context context) {
        itemModelManager = context.itemModelResolver();
    }

    @Override
    public PlacardRenderState createRenderState() {
        return new PlacardRenderState();
    }

    @Override
    public void extractRenderState(
        PlacardBlockEntity be,
        PlacardRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        ItemStack heldItem = be.getHeldItem();
        if (heldItem.isEmpty()) {
            return;
        }
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        Direction facing = state.blockState.getValue(PlacardBlock.FACING);
        AttachFace face = state.blockState.getValue(PlacardBlock.FACE);
        ItemStackRenderState item = state.item = new ItemStackRenderState();
        item.displayContext = ItemDisplayContext.FIXED;
        itemModelManager.appendItemLayers(item, heldItem, item.displayContext, be.getLevel(), null, 0);
        boolean isCeiling = face == AttachFace.CEILING;
        state.upAngle = (isCeiling ? Mth.PI : 0) + AngleHelper.rad(180 + AngleHelper.horizontalAngle(facing));
        state.eastAngle = isCeiling ? -Mth.PI / 2 : face == AttachFace.FLOOR ? Mth.PI / 2 : 0;
        state.scale = item.usesBlockLight() ? 0.5f : 0.375f;
    }

    @Override
    public void submit(PlacardRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.mulPose(new Quaternionf().setAngleAxis(state.upAngle, 0, 1, 0));
        matrices.mulPose(new Quaternionf().setAngleAxis(state.eastAngle, 1, 0, 0));
        matrices.translate(0, 0, 0.28125f);
        float scale = state.scale;
        matrices.scale(scale, scale, scale);
        state.item.submit(matrices, queue, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
    }

    public static class PlacardRenderState extends BlockEntityRenderState {
        public ItemStackRenderState item;
        public float upAngle;
        public float eastAngle;
        public float scale;
    }
}
