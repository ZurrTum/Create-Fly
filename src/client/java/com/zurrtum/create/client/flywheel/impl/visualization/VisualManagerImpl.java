package com.zurrtum.create.client.flywheel.impl.visualization;

import com.zurrtum.create.client.flywheel.api.task.Plan;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visual.TickableVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualManager;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.impl.visualization.storage.Storage;
import com.zurrtum.create.client.flywheel.impl.visualization.storage.Transaction;
import com.zurrtum.create.client.flywheel.lib.task.SimplePlan;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class VisualManagerImpl<T, S extends Storage<T>> implements VisualManager<T> {
    private final Queue<Transaction<T>> queue = new ConcurrentLinkedQueue<>();

    private final S storage;

    public VisualManagerImpl(S storage) {
        this.storage = storage;
    }

    public S getStorage() {
        return storage;
    }

    @Override
    public int visualCount() {
        return getStorage().getAllVisuals().size();
    }

    @Override
    public void queueAdd(T obj) {
        if (!getStorage().willAccept(obj)) {
            return;
        }

        queue.add(Transaction.add(obj));
    }

    @Override
    public void queueRemove(T obj) {
        queue.add(Transaction.remove(obj));
    }

    @Override
    public void queueUpdate(T obj) {
        if (!getStorage().willAccept(obj)) {
            return;
        }

        queue.add(Transaction.update(obj));
    }

    public void processQueue(VisualizationContext visualizationContext, float partialTick) {
        var storage = getStorage();
        Transaction<T> transaction;
        while ((transaction = queue.poll()) != null) {
            switch (transaction.action()) {
                case ADD -> storage.add(visualizationContext, transaction.obj(), partialTick);
                case REMOVE -> storage.remove(transaction.obj());
                case UPDATE -> storage.update(transaction.obj(), partialTick);
            }
        }
    }

    public Plan<DynamicVisual.Context> framePlan(VisualizationContext visualizationContext) {
        return SimplePlan.<DynamicVisual.Context>of(context -> processQueue(visualizationContext, context.partialTick())).then(storage.framePlan());
    }

    public Plan<TickableVisual.Context> tickPlan(VisualizationContext visualizationContext) {
        return SimplePlan.<TickableVisual.Context>of(context -> processQueue(visualizationContext, 1)).then(storage.tickPlan());
    }

    public void onLightUpdate(long section) {
        getStorage().lightUpdatedVisuals().onLightUpdate(section);
    }

    public boolean areGpuLightSectionsDirty() {
        return getStorage().shaderLightVisuals().isDirty();
    }

    public LongSet gpuLightSections() {
        return getStorage().shaderLightVisuals().sections();
    }

    public void invalidate() {
        getStorage().invalidate();
    }
}
