package com.zurrtum.create.client.content.kinetics.gauge;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.ShaftRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.kinetics.gauge.GaugeBlock;
import com.zurrtum.create.content.kinetics.gauge.GaugeBlock.Type;
import com.zurrtum.create.content.kinetics.gauge.GaugeBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class GaugeRenderer extends ShaftRenderer<GaugeBlockEntity> {

    protected Type type;

    public static GaugeRenderer speed(BlockEntityRendererFactory.Context context) {
        return new GaugeRenderer(context, Type.SPEED);
    }

    public static GaugeRenderer stress(BlockEntityRendererFactory.Context context) {
        return new GaugeRenderer(context, Type.STRESS);
    }

    protected GaugeRenderer(BlockEntityRendererFactory.Context context, Type type) {
        super(context);
        this.type = type;
    }

    @Override
    protected void renderSafe(GaugeBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        if (VisualizationManager.supportsVisualization(be.getWorld()))
            return;

        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        BlockState gaugeState = be.getCachedState();

        PartialModel partialModel = (type == Type.SPEED ? AllPartialModels.GAUGE_HEAD_SPEED : AllPartialModels.GAUGE_HEAD_STRESS);
        SuperByteBuffer headBuffer = CachedBuffers.partial(partialModel, gaugeState);
        SuperByteBuffer dialBuffer = CachedBuffers.partial(AllPartialModels.GAUGE_DIAL, gaugeState);

        float dialPivot = 5.75f / 16;
        float progress = MathHelper.lerp(partialTicks, be.prevDialState, be.dialState);

        for (Direction facing : Iterate.directions) {
            if (!((GaugeBlock) gaugeState.getBlock()).shouldRenderHeadOnFace(be.getWorld(), be.getPos(), gaugeState, facing))
                continue;

            VertexConsumer vb = buffer.getBuffer(RenderLayer.getSolid());
            rotateBufferTowards(dialBuffer, facing).translate(0, dialPivot, dialPivot).rotate((float) (Math.PI / 2 * -progress), Direction.EAST)
                .translate(0, -dialPivot, -dialPivot).light(light).renderInto(ms, vb);
            rotateBufferTowards(headBuffer, facing).light(light).renderInto(ms, vb);
        }
    }

    protected SuperByteBuffer rotateBufferTowards(SuperByteBuffer buffer, Direction target) {
        return buffer.rotateCentered((float) ((-target.getPositiveHorizontalDegrees() - 90) / 180 * Math.PI), Direction.UP);
    }

}
