package com.zurrtum.create.client.content.redstone.displayLink;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.PonderRenderTypes;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.zurrtum.create.client.foundation.render.RenderTypes;
import com.zurrtum.create.content.redstone.displayLink.LinkWithBulbBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class LinkBulbRenderer extends SafeBlockEntityRenderer<LinkWithBulbBlockEntity> {

    public LinkBulbRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(LinkWithBulbBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        BlockState blockState = be.getCachedState();
        var msr = TransformStack.of(ms);

        Direction face = be.getBulbFacing(blockState);

        ms.push();

        msr.center().rotateYDegrees(AngleHelper.horizontalAngle(face) + 180).rotateXDegrees(-AngleHelper.verticalAngle(face) - 90).uncenter();

        RenderLayer translucent;
        float glow = be.getGlow(partialTicks);
        if (glow >= .125f) {
            translucent = RenderTypes.translucent();
            glow = (float) (1 - (2 * Math.pow(glow - .75f, 2)));
            glow = MathHelper.clamp(glow, -1, 1);
            int color = (int) (200 * glow);
            CachedBuffers.partial(AllPartialModels.DISPLAY_LINK_GLOW, blockState).translate(be.getBulbOffset(blockState))
                .light(LightmapTextureManager.MAX_LIGHT_COORDINATE).color(color, color, color, 255).disableDiffuse()
                .renderInto(ms, buffer.getBuffer(RenderTypes.additive()));
        } else {
            translucent = PonderRenderTypes.translucent();
        }
        CachedBuffers.partial(AllPartialModels.DISPLAY_LINK_TUBE, blockState).translate(be.getBulbOffset(blockState))
            .light(LightmapTextureManager.MAX_LIGHT_COORDINATE).renderInto(ms, buffer.getBuffer(translucent));

        ms.pop();
    }

}
