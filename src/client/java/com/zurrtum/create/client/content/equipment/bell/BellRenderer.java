package com.zurrtum.create.client.content.equipment.bell;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.zurrtum.create.content.equipment.bell.AbstractBellBlockEntity;
import com.zurrtum.create.content.equipment.bell.PeculiarBellBlockEntity;
import net.minecraft.block.BellBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.Attachment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class BellRenderer<BE extends AbstractBellBlockEntity> extends SafeBlockEntityRenderer<BE> {

    public BellRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(BE be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        BlockState state = be.getCachedState();
        Direction facing = state.get(BellBlock.FACING);
        Attachment attachment = state.get(BellBlock.ATTACHMENT);

        SuperByteBuffer bell = CachedBuffers.partial(
            be instanceof PeculiarBellBlockEntity ? AllPartialModels.PECULIAR_BELL : AllPartialModels.HAUNTED_BELL,
            state
        );

        if (be.isRinging)
            bell.rotateCentered(getSwingAngle(be.ringingTicks + partialTicks), be.ringDirection.rotateYCounterclockwise());

        float rY = AngleHelper.horizontalAngle(facing);
        if (attachment == Attachment.SINGLE_WALL || attachment == Attachment.DOUBLE_WALL)
            rY += 90;
        bell.rotateCentered(AngleHelper.rad(rY), Direction.UP);

        bell.light(light).renderInto(ms, buffer.getBuffer(RenderLayer.getCutout()));
    }

    public static float getSwingAngle(float time) {
        float t = time / 1.5f;
        return 1.2f * MathHelper.sin(t / (float) Math.PI) / (2.5f + t / 3.0f);
    }

}
