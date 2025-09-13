package com.zurrtum.create.client.content.contraptions.actors.psi;

import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visual.TickableVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleTickableVisual;
import com.zurrtum.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;

import java.util.function.Consumer;

public class PSIVisual extends AbstractBlockEntityVisual<PortableStorageInterfaceBlockEntity> implements SimpleDynamicVisual, SimpleTickableVisual {

    private final PIInstance instance;

    public PSIVisual(VisualizationContext visualizationContext, PortableStorageInterfaceBlockEntity blockEntity, float partialTick) {
        super(visualizationContext, blockEntity, partialTick);

        instance = new PIInstance(visualizationContext.instancerProvider(), blockState, getVisualPosition(), isLit());
        instance.beginFrame(blockEntity.getExtensionDistance(partialTick));
    }

    @Override
    public void tick(TickableVisual.Context ctx) {
        instance.tick(isLit());
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        instance.beginFrame(blockEntity.getExtensionDistance(ctx.partialTick()));
    }

    @Override
    public void updateLight(float partialTick) {
        relight(instance.middle, instance.top);
    }

    @Override
    protected void _delete() {
        instance.remove();
    }

    private boolean isLit() {
        return blockEntity.isConnected();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        instance.collectCrumblingInstances(consumer);
    }
}
