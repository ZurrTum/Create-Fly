package com.zurrtum.create.client.content.redstone.diodes;

import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visual.TickableVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleTickableVisual;
import com.zurrtum.create.content.redstone.diodes.BrassDiodeBlockEntity;

import java.util.function.Consumer;

public class BrassDiodeVisual extends AbstractBlockEntityVisual<BrassDiodeBlockEntity> implements SimpleTickableVisual {

    protected final TransformedInstance indicator;

    protected int previousState;

    public BrassDiodeVisual(VisualizationContext context, BrassDiodeBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        indicator = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.FLEXPEATER_INDICATOR)).createInstance();

        indicator.setIdentityTransform().translate(getVisualPosition()).colorRgb(getColor()).setChanged();

        previousState = blockEntity.state;
    }

    @Override
    public void tick(TickableVisual.Context context) {
        if (previousState == blockEntity.state)
            return;

        indicator.colorRgb(getColor());
        indicator.setChanged();

        previousState = blockEntity.state;
    }

    @Override
    public void updateLight(float partialTick) {
        relight(indicator);
    }

    @Override
    protected void _delete() {
        indicator.delete();
    }

    protected int getColor() {
        return Color.mixColors(0x2c0300, 0xcd0000, blockEntity.getProgress());
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        consumer.accept(indicator);
    }
}
