package com.zurrtum.create.client.content.redstone.analogLever;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.transform.Rotate;
import com.zurrtum.create.client.flywheel.lib.transform.Translate;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.content.redstone.analogLever.AnalogLeverBlock;
import com.zurrtum.create.content.redstone.analogLever.AnalogLeverBlockEntity;

import java.util.function.Consumer;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.AttachFace;

public class AnalogLeverVisual extends AbstractBlockEntityVisual<AnalogLeverBlockEntity> implements SimpleDynamicVisual {

    protected final TransformedInstance handle;
    protected final TransformedInstance indicator;

    final float rX;
    final float rY;

    public AnalogLeverVisual(VisualizationContext context, AnalogLeverBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        handle = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ANALOG_LEVER_HANDLE)).createInstance();
        indicator = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ANALOG_LEVER_INDICATOR))
            .createInstance();

        AttachFace face = blockState.getValue(AnalogLeverBlock.FACE);
        rX = face == AttachFace.FLOOR ? 0 : face == AttachFace.WALL ? 90 : 180;
        rY = AngleHelper.horizontalAngle(blockState.getValue(AnalogLeverBlock.FACING));

        transform(indicator.setIdentityTransform());

        animateLever(partialTick);
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        if (!blockEntity.clientState.settled())
            animateLever(ctx.partialTick());
    }

    protected void animateLever(float pt) {
        float state = blockEntity.clientState.getValue(pt);

        indicator.colorRgb(Color.mixColors(0x2C0300, 0xCD0000, state / 15f));
        indicator.setChanged();

        float angle = (float) ((state / 15) * 90 / 180 * Math.PI);

        transform(handle.setIdentityTransform()).translate(1 / 2f, 1 / 16f, 1 / 2f).rotate(angle, Direction.EAST)
            .translate(-1 / 2f, -1 / 16f, -1 / 2f).setChanged();
    }

    @Override
    protected void _delete() {
        handle.delete();
        indicator.delete();
    }

    @Override
    public void updateLight(float partialTick) {
        relight(handle, indicator);
    }

    private <T extends Translate<T> & Rotate<T>> T transform(T msr) {
        return msr.translate(getVisualPosition()).center().rotate((float) (rY / 180 * Math.PI), Direction.UP)
            .rotate((float) (rX / 180 * Math.PI), Direction.EAST).uncenter();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        consumer.accept(handle);
        consumer.accept(indicator);
    }
}
