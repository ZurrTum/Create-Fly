package com.zurrtum.create.client.content.decoration.steamWhistle;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.animation.AnimationBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.animation.WhistleAnimationBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleBlock;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleBlock.WhistleSize;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class WhistleRenderer extends SafeBlockEntityRenderer<WhistleBlockEntity> {

    public WhistleRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(WhistleBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        BlockState blockState = be.getCachedState();
        if (!(blockState.getBlock() instanceof WhistleBlock))
            return;

        Direction direction = blockState.get(WhistleBlock.FACING);
        WhistleSize size = blockState.get(WhistleBlock.SIZE);

        PartialModel mouth = size == WhistleSize.LARGE ? AllPartialModels.WHISTLE_MOUTH_LARGE : size == WhistleSize.MEDIUM ? AllPartialModels.WHISTLE_MOUTH_MEDIUM : AllPartialModels.WHISTLE_MOUTH_SMALL;

        WhistleAnimationBehaviour behaviour = (WhistleAnimationBehaviour) be.getBehaviour(AnimationBehaviour.TYPE);
        float offset = behaviour.animation.getValue(partialTicks);
        if (behaviour.animation.getChaseTarget() > 0 && behaviour.animation.getValue() > 0.5f) {
            float wiggleProgress = (AnimationTickHolder.getTicks(be.getWorld()) + partialTicks) / 8f;
            offset -= Math.sin(wiggleProgress * (2 * MathHelper.PI) * (4 - size.ordinal())) / 16f;
        }

        CachedBuffers.partial(mouth, blockState).center().rotateYDegrees(AngleHelper.horizontalAngle(direction)).uncenter()
            .translate(0, offset * 4 / 16f, 0).light(light).renderInto(ms, buffer.getBuffer(RenderLayer.getSolid()));
    }

}
