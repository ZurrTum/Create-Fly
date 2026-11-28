package com.zurrtum.create.client.flywheel.backend.engine;

import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.zurrtum.create.client.flywheel.api.backend.RenderContext;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.api.task.Plan;
import com.zurrtum.create.client.flywheel.backend.FlwBackend;
import com.zurrtum.create.client.flywheel.backend.engine.embed.Environment;
import com.zurrtum.create.client.flywheel.backend.engine.embed.EnvironmentStorage;
import com.zurrtum.create.client.flywheel.lib.task.ForEachPlan;
import net.minecraft.client.renderer.oit.OitStage;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

public abstract class DrawManager<N extends AbstractInstancer<?>> {
    private static final boolean MODEL_WARNINGS = Boolean.getBoolean("flywheel.modelWarnings");

    /**
     * A map of instancer keys to instancers.
     *
     * <p>This map is populated as instancers are requested and contains both initialized and uninitialized instancers.
     */
    protected final Map<InstancerKey<?>, N> instancers = new ConcurrentHashMap<>();
    /**
     * A list of instancers that have not yet been initialized.
     *
     * <p>All new instancers land here before having resources allocated in {@link #render}.
     */
    protected final Queue<UninitializedInstancer<N, ?>> initializationQueue = new ConcurrentLinkedQueue<>();

    protected final GpuTextureManager textureManager = new GpuTextureManager();

    /**
     * Function object to pass into computeIfAbsent.
     * <p>Create once and cache to avoid allocating every time.
     */
    protected final Function<InstancerKey<?>, N> createAndDeferInit = this::createAndDeferInit;

    public <I extends Instance> AbstractInstancer<I> getInstancer(
        Environment environment,
        InstanceType<I> type,
        Model model,
        int bias
    ) {
        return getInstancer(new InstancerKey<>(environment, type, model, bias));
    }

    @SuppressWarnings("unchecked")
    public <I extends Instance> AbstractInstancer<I> getInstancer(InstancerKey<I> key) {
        return (AbstractInstancer<I>) instancers.computeIfAbsent(key, createAndDeferInit);
    }

    public Plan<RenderContext> createFramePlan() {
        // Go wide on instancers to process deletions in parallel.
        return ForEachPlan.of(() -> new ArrayList<>(instancers.values()), AbstractInstancer::parallelUpdate);
    }

    public void flush() {
        // Thread safety: flush is called from the render thread after all visual updates have been made,
        // so there are no:tm: threads we could be racing with.
        for (var init : initializationQueue) {
            var instancer = init.instancer();
            if (instancer.instanceCount() > 0) {
                initialize(init.key(), instancer, textureManager);
            } else {
                instancers.remove(init.key());
            }
        }
        initializationQueue.clear();
        textureManager.prepare();
    }

    public abstract void render(LightStorage lightStorage, EnvironmentStorage environmentStorage);

    public abstract void renderClassicTransparency();

    public abstract void renderOit(
        OitStage stage,
        GpuTextureView depthBoundsCulledTargetView,
        GpuTextureView[] transmittanceTargetViews,
        CrumblingBlockManager crumblingBlockManager
    );

    public void onRenderOriginChanged() {
        instancers.values().forEach(AbstractInstancer::clear);
    }

    @Nullable
    public abstract N cast(InstanceHandleImpl.State<?> state);

    public abstract void renderCrumbling(CrumblingBlockManager crumblingBlockManager, RenderContext context);

    protected abstract <I extends Instance> N create(InstancerKey<I> type);

    protected abstract <I extends Instance> void initialize(
        InstancerKey<I> key,
        N instancer,
        GpuTextureManager textureManager
    );

    private N createAndDeferInit(InstancerKey<?> key) {
        var out = create(key);

        // Only queue the instancer for initialization if it has anything to render.
        if (modelHasNoIssues(key.model())) {
            // Thread safety: this method is called atomically from within computeIfAbsent,
            // so you'd think we don't need extra synchronization to protect the queue, but
            // somehow threads can race here and wind up never initializing an instancer.
            initializationQueue.add(new UninitializedInstancer<>(key, out));
        }
        return out;
    }

    private static boolean modelHasNoIssues(Model model) {
        if (model.meshes().isEmpty()) {
            if (MODEL_WARNINGS) {
                StringBuilder builder = new StringBuilder();
                builder.append("Creating an instancer for a model with no meshes! Stack trace:");
                StackWalker.getInstance().forEach(f -> builder.append("\n\t").append(f.toString()));
                FlwBackend.LOGGER.warn(builder.toString());
            }

            return false;
        }
        List<Model.ConfiguredMesh> meshes = model.meshes();

        for (int i = 0; i < meshes.size(); ++i) {
            Model.ConfiguredMesh mesh = meshes.get(i);
            if (!MaterialRenderState.materialIsAllNonNull(mesh.material())) {
                if (MODEL_WARNINGS) {
                    StringBuilder builder = new StringBuilder();
                    builder.append("ConfiguredMesh at index ").append(i)
                        .append(" has null components in its material! Stack trace:");
                    StackWalker.getInstance().forEach(f -> builder.append("\n\t").append(f.toString()));
                    FlwBackend.LOGGER.warn(builder.toString());
                }

                return false;
            }
        }

        return true;
    }

    public void delete() {
        instancers.clear();
        initializationQueue.clear();
    }

    public abstract void triggerFallback();

    public abstract MeshPool meshPool();

    public Map<InstancerKey<?>, N> instancers() {
        return instancers;
    }

    protected record UninitializedInstancer<N, I extends Instance>(InstancerKey<I> key, N instancer) {
    }
}
