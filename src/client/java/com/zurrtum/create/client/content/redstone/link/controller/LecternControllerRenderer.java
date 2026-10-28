package com.zurrtum.create.client.content.redstone.link.controller;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.redstone.link.controller.LecternControllerRenderer.LecternControllerRenderState;
import com.zurrtum.create.client.infrastructure.model.LinkedControllerModel;
import com.zurrtum.create.content.redstone.link.controller.LecternControllerBlock;
import com.zurrtum.create.content.redstone.link.controller.LecternControllerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.Create.MOD_ID;

public class LecternControllerRenderer implements BlockEntityRenderer<LecternControllerBlockEntity, LecternControllerRenderState> {
    Identifier MODEL_ID = Identifier.fromNamespaceAndPath(MOD_ID, "linked_controller");
    private final LinkedControllerModel model;

    public LecternControllerRenderer(Context context) {
        model = (LinkedControllerModel) context.blockModelResolver().modelManager.getItemModel(MODEL_ID);
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
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.active = be.hasUser();
        state.renderDepression = be.isUsedBy(Minecraft.getInstance().player);
        state.yRadians = Radians.horizontalPlus270(state.blockState.getValue(LecternControllerBlock.FACING));
    }

    @Override
    public void submit(
        LecternControllerRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        Pose pose = matrices.last();
        pose.translate(0.5f, 1.45f, 0.5f);
        if (state.yRadians != 0) {
            pose.rotate(Axis.YP, state.yRadians);
        }
        pose.translate(0.28f, 0, 0);
        pose.rotate(Axis.ZP, Radians.DEG_338);
        pose.translate(-0.5f, -0.5f, -0.5f);
        model.submitInLectern(
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
        public boolean active;
        public boolean renderDepression;
        public float yRadians;
    }
}
