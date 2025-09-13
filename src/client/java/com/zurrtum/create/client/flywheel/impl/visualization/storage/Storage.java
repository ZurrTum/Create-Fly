package com.zurrtum.create.client.flywheel.impl.visualization.storage;

import com.zurrtum.create.client.flywheel.api.task.Plan;
import com.zurrtum.create.client.flywheel.api.visual.*;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.impl.ImplDebugFlags;
import com.zurrtum.create.client.flywheel.lib.task.ConditionalPlan;
import com.zurrtum.create.client.flywheel.lib.task.ForEachPlan;
import com.zurrtum.create.client.flywheel.lib.task.NestedPlan;
import com.zurrtum.create.client.flywheel.lib.task.PlanMap;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleTickableVisual;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class Storage<T> {
    private final Map<T, Visual> visuals = new Reference2ObjectOpenHashMap<>();
    protected final PlanMap<DynamicVisual, DynamicVisual.Context> dynamicVisuals = new PlanMap<>();
    protected final PlanMap<TickableVisual, TickableVisual.Context> tickableVisuals = new PlanMap<>();
    protected final List<SimpleDynamicVisual> simpleDynamicVisuals = new ArrayList<>();
    protected final List<SimpleTickableVisual> simpleTickableVisuals = new ArrayList<>();
    protected final LightUpdatedVisualStorage lightUpdatedVisuals = new LightUpdatedVisualStorage();
    protected final ShaderLightVisualStorage shaderLightVisuals = new ShaderLightVisualStorage();

    public Collection<Visual> getAllVisuals() {
        return visuals.values();
    }

    public Plan<DynamicVisual.Context> framePlan() {
        var update = ConditionalPlan.<DynamicVisual.Context>on(() -> !ImplDebugFlags.PAUSE_UPDATES)
            .then(NestedPlan.of(dynamicVisuals, ForEachPlan.of(() -> simpleDynamicVisuals, SimpleDynamicVisual::beginFrame)));

        // Do light updates regardless.
        return NestedPlan.of(lightUpdatedVisuals.plan(), update);
    }

    public Plan<TickableVisual.Context> tickPlan() {
        return ConditionalPlan.<TickableVisual.Context>on(() -> !ImplDebugFlags.PAUSE_UPDATES)
            .then(NestedPlan.of(tickableVisuals, ForEachPlan.of(() -> simpleTickableVisuals, SimpleTickableVisual::tick)));
    }

    public LightUpdatedVisualStorage lightUpdatedVisuals() {
        return lightUpdatedVisuals;
    }

    public ShaderLightVisualStorage shaderLightVisuals() {
        return shaderLightVisuals;
    }

    /**
     * Is the given object currently capable of being added?
     *
     * @return true if the object is currently capable of being visualized.
     */
    public abstract boolean willAccept(T obj);

    public void add(VisualizationContext visualizationContext, T obj, float partialTick) {
        Visual visual = visuals.get(obj);

        if (visual == null) {
            visual = createRaw(visualizationContext, obj, partialTick);

            if (visual != null) {
                setup(visual, partialTick);
                visuals.put(obj, visual);
            }
        }
    }

    public void remove(T obj) {
        Visual visual = visuals.remove(obj);

        if (visual == null) {
            return;
        }

        if (visual instanceof DynamicVisual dynamic) {
            if (visual instanceof SimpleDynamicVisual simpleDynamic) {
                simpleDynamicVisuals.remove(simpleDynamic);
            } else {
                dynamicVisuals.remove(dynamic);
            }
        }
        if (visual instanceof TickableVisual tickable) {
            if (visual instanceof SimpleTickableVisual simpleTickable) {
                simpleTickableVisuals.remove(simpleTickable);
            } else {
                tickableVisuals.remove(tickable);
            }
        }
        if (visual instanceof LightUpdatedVisual lightUpdated) {
            lightUpdatedVisuals.remove(lightUpdated);
        }
        if (visual instanceof ShaderLightVisual shaderLight) {
            shaderLightVisuals.remove(shaderLight);
        }

        visual.delete();
    }

    public void update(T obj, float partialTick) {
        Visual visual = visuals.get(obj);

        if (visual == null) {
            return;
        }

        visual.update(partialTick);
    }

    public void recreateAll(VisualizationContext visualizationContext, float partialTick) {
        dynamicVisuals.clear();
        tickableVisuals.clear();
        simpleDynamicVisuals.clear();
        simpleTickableVisuals.clear();
        lightUpdatedVisuals.clear();
        shaderLightVisuals.clear();

        visuals.replaceAll((obj, visual) -> {
            visual.delete();

            var out = createRaw(visualizationContext, obj, partialTick);

            if (out != null) {
                setup(out, partialTick);
            }

            return out;
        });
    }

    @Nullable
    protected abstract Visual createRaw(VisualizationContext visualizationContext, T obj, float partialTick);

    private void setup(Visual visual, float partialTick) {
        if (visual instanceof DynamicVisual dynamic) {
            if (visual instanceof SimpleDynamicVisual simpleDynamic) {
                simpleDynamicVisuals.add(simpleDynamic);
            } else {
                dynamicVisuals.add(dynamic, dynamic.planFrame());
            }
        }

        if (visual instanceof TickableVisual tickable) {
            if (visual instanceof SimpleTickableVisual simpleTickable) {
                simpleTickableVisuals.add(simpleTickable);
            } else {
                tickableVisuals.add(tickable, tickable.planTick());
            }
        }

        if (visual instanceof SectionTrackedVisual tracked) {
            SectionTracker tracker = new SectionTracker();

            // Give the visual a chance to invoke the collector.
            tracked.setSectionCollector(tracker);

            if (visual instanceof LightUpdatedVisual lightUpdated) {
                lightUpdatedVisuals.add(lightUpdated, tracker);
                lightUpdated.updateLight(partialTick);
            }

            if (visual instanceof ShaderLightVisual shaderLight) {
                shaderLightVisuals.add(shaderLight, tracker);
            }
        }
    }

    public void invalidate() {
        dynamicVisuals.clear();
        tickableVisuals.clear();
        simpleDynamicVisuals.clear();
        simpleTickableVisuals.clear();
        lightUpdatedVisuals.clear();
        shaderLightVisuals.clear();
        visuals.values().forEach(Visual::delete);
        visuals.clear();
    }
}
