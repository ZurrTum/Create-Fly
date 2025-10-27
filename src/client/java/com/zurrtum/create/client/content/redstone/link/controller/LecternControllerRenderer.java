package com.zurrtum.create.client.content.redstone.link.controller;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.infrastructure.model.LinkedControllerModel;
import com.zurrtum.create.content.redstone.link.controller.LecternControllerBlock;
import com.zurrtum.create.content.redstone.link.controller.LecternControllerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class LecternControllerRenderer implements BlockEntityRenderer<LecternControllerBlockEntity, LecternControllerRenderer.LecternControllerRenderState> {
    public LecternControllerRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public LecternControllerRenderState createRenderState() {
        return new LecternControllerRenderState();
    }

    @Override
    public void updateRenderState(
        LecternControllerBlockEntity be,
        LecternControllerRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        MinecraftClient mc = MinecraftClient.getInstance();
        state.model = (LinkedControllerModel) mc.getBakedModelManager()
            .getItemModel(AllItems.LINKED_CONTROLLER.getComponents().get(DataComponentTypes.ITEM_MODEL));
        state.active = be.hasUser();
        state.renderDepression = be.isUsedBy(mc.player);
        Direction facing = state.blockState.get(LecternControllerBlock.FACING);
        state.yRot = MathHelper.RADIANS_PER_DEGREE * (AngleHelper.horizontalAngle(facing) - 90);
        state.zRot = MathHelper.RADIANS_PER_DEGREE * -22;
    }

    @Override
    public void render(LecternControllerRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        matrices.translate(0.5f, 1.45f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(state.yRot));
        matrices.translate(0.28f, 0, 0);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotation(state.zRot));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        state.model.renderInLectern(
            ItemDisplayContext.NONE,
            matrices,
            queue,
            state.lightmapCoordinates,
            OverlayTexture.DEFAULT_UV,
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
