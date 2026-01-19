package com.zurrtum.create.client.content.redstone.link.controller;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.infrastructure.model.LinkedControllerModel;
import com.zurrtum.create.content.redstone.link.controller.LecternControllerBlock;
import com.zurrtum.create.content.redstone.link.controller.LecternControllerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class LecternControllerRenderer implements BlockEntityRenderer<LecternControllerBlockEntity, LecternControllerRenderer.LecternControllerRenderState> {
    public LecternControllerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public LecternControllerRenderState createRenderState() {
        return new LecternControllerRenderState();
    }

    @Override
    public void extractRenderState(
        LecternControllerBlockEntity be,
        LecternControllerRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        Minecraft mc = Minecraft.getInstance();
        state.model = (LinkedControllerModel) mc.getModelManager()
            .getItemModel(AllItems.LINKED_CONTROLLER.components().get(DataComponents.ITEM_MODEL));
        state.active = be.hasUser();
        state.renderDepression = be.isUsedBy(mc.player);
        Direction facing = state.blockState.getValue(LecternControllerBlock.FACING);
        state.yRot = Mth.DEG_TO_RAD * (AngleHelper.horizontalAngle(facing) - 90);
        state.zRot = Mth.DEG_TO_RAD * -22;
    }

    @Override
    public void submit(LecternControllerRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        matrices.translate(0.5f, 1.45f, 0.5f);
        matrices.mulPose(Axis.YP.rotation(state.yRot));
        matrices.translate(0.28f, 0, 0);
        matrices.mulPose(Axis.ZP.rotation(state.zRot));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        state.model.renderInLectern(
            ItemDisplayContext.NONE,
            matrices,
            queue,
            state.lightCoords,
            OverlayTexture.NO_OVERLAY,
            state.active,
            state.renderDepression
        );
    }

    public static class LecternControllerRenderState extends BlockEntityRenderState {
        public LinkedControllerModel model;
        public boolean active;
        public boolean renderDepression;
        public float yRot;
        public float zRot;
    }
}
