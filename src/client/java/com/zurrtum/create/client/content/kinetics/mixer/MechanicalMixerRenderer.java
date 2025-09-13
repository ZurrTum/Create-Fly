package com.zurrtum.create.client.content.kinetics.mixer;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

public class MechanicalMixerRenderer extends KineticBlockEntityRenderer<MechanicalMixerBlockEntity> {

    public MechanicalMixerRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public boolean rendersOutsideBoundingBox() {
        return true;
    }

    @Override
    protected void renderSafe(
        MechanicalMixerBlockEntity be,
        float partialTicks,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay
    ) {

        if (VisualizationManager.supportsVisualization(be.getWorld()))
            return;

        BlockState blockState = be.getCachedState();

        VertexConsumer vb = buffer.getBuffer(RenderLayer.getSolid());

        SuperByteBuffer superBuffer = CachedBuffers.partial(AllPartialModels.SHAFTLESS_COGWHEEL, blockState);
        standardKineticRotationTransform(superBuffer, be, light).renderInto(ms, vb);

        float renderedHeadOffset = be.getRenderedHeadOffset(partialTicks);
        float speed = be.getRenderedHeadRotationSpeed(partialTicks);
        float time = AnimationTickHolder.getRenderTime(be.getWorld());
        float angle = ((time * speed * 6 / 10f) % 360) / 180 * (float) Math.PI;

        SuperByteBuffer poleRender = CachedBuffers.partial(AllPartialModels.MECHANICAL_MIXER_POLE, blockState);
        poleRender.translate(0, -renderedHeadOffset, 0).light(light).renderInto(ms, vb);

        VertexConsumer vbCutout = buffer.getBuffer(RenderLayer.getCutoutMipped());
        SuperByteBuffer headRender = CachedBuffers.partial(AllPartialModels.MECHANICAL_MIXER_HEAD, blockState);
        headRender.rotateCentered(angle, Direction.UP).translate(0, -renderedHeadOffset, 0).light(light).renderInto(ms, vbCutout);
    }

}
