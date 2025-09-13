package com.zurrtum.create.client.content.logistics.funnel;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.logistics.FlapStuffs;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.content.logistics.funnel.FunnelBlock;
import com.zurrtum.create.content.logistics.funnel.FunnelBlockEntity;

import java.util.function.Consumer;

public class FunnelVisual extends AbstractBlockEntityVisual<FunnelBlockEntity> implements SimpleDynamicVisual {

    private final FlapStuffs.Visual flaps;

    public FunnelVisual(VisualizationContext context, FunnelBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        if (!blockEntity.hasFlap()) {
            flaps = null;
            return;
        }

        var funnelFacing = FunnelBlock.getFunnelFacing(blockState);
        PartialModel flapPartial = (blockState.getBlock() instanceof FunnelBlock ? AllPartialModels.FUNNEL_FLAP : AllPartialModels.BELT_FUNNEL_FLAP);

        var commonTransform = FlapStuffs.commonTransform(getVisualPosition(), funnelFacing, -blockEntity.getFlapOffset());
        flaps = new FlapStuffs.Visual(instancerProvider(), commonTransform, FlapStuffs.FUNNEL_PIVOT, Models.partial(flapPartial));

        flaps.update(blockEntity.flap.getValue(partialTick));
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        if (flaps == null)
            return;

        flaps.update(blockEntity.flap.getValue(ctx.partialTick()));
    }

    @Override
    public void updateLight(float partialTick) {
        if (flaps != null)
            flaps.updateLight(computePackedLight());
    }

    @Override
    protected void _delete() {
        if (flaps == null)
            return;

        flaps.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        if (flaps == null)
            return;

        flaps.collectCrumblingInstances(consumer);
    }

}
