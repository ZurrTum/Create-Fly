package com.zurrtum.create.client.content.redstone.link.controller;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.zurrtum.create.client.infrastructure.model.LinkedControllerModel;
import com.zurrtum.create.content.redstone.link.controller.LecternControllerBlock;
import com.zurrtum.create.content.redstone.link.controller.LecternControllerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.math.Direction;

public class LecternControllerRenderer extends SafeBlockEntityRenderer<LecternControllerBlockEntity> {

    public LecternControllerRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(
        LecternControllerBlockEntity be,
        float partialTicks,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay
    ) {
        MinecraftClient mc = MinecraftClient.getInstance();
        LinkedControllerModel model = (LinkedControllerModel) mc.getBakedModelManager()
            .getItemModel(AllItems.LINKED_CONTROLLER.getComponents().get(DataComponentTypes.ITEM_MODEL));
        boolean active = be.hasUser();
        boolean renderDepression = be.isUsedBy(mc.player);

        Direction facing = be.getCachedState().get(LecternControllerBlock.FACING);
        var msr = TransformStack.of(ms);

        msr.pushPose();
        msr.translate(0.5, 1.45, 0.5);
        msr.rotateYDegrees(AngleHelper.horizontalAngle(facing) - 90);
        msr.translate(0.28, 0, 0);
        msr.rotateZDegrees(-22.0f);
        msr.translate(-0.5, -0.5, -0.5);
        model.renderInLectern(ItemDisplayContext.NONE, ms, buffer, light, overlay, active, renderDepression);
        msr.popPose();
    }

}
