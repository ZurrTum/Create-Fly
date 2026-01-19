package com.zurrtum.create.client.flywheel.impl.visualization;

import com.zurrtum.create.client.flywheel.api.backend.BackendManager;
import com.zurrtum.create.client.flywheel.api.backend.Engine;
import com.zurrtum.create.client.flywheel.api.backend.Engine.CrumblingBlock;
import com.zurrtum.create.client.flywheel.api.backend.RenderContext;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.task.Plan;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visual.Effect;
import com.zurrtum.create.client.flywheel.api.visual.TickableVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualManager;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationLevel;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.backend.engine.EngineImpl;
import com.zurrtum.create.client.flywheel.impl.FlwConfig;
import com.zurrtum.create.client.flywheel.impl.task.Flag;
import com.zurrtum.create.client.flywheel.impl.task.FlwTaskExecutor;
import com.zurrtum.create.client.flywheel.impl.task.RaisePlan;
import com.zurrtum.create.client.flywheel.impl.task.TaskExecutorImpl;
import com.zurrtum.create.client.flywheel.impl.visual.*;
import com.zurrtum.create.client.flywheel.impl.visualization.storage.BlockEntityStorage;
import com.zurrtum.create.client.flywheel.impl.visualization.storage.EffectStorage;
import com.zurrtum.create.client.flywheel.impl.visualization.storage.EntityStorage;
import com.zurrtum.create.client.flywheel.lib.task.IfElsePlan;
import com.zurrtum.create.client.flywheel.lib.task.MapContextPlan;
import com.zurrtum.create.client.flywheel.lib.task.NestedPlan;
import com.zurrtum.create.client.flywheel.lib.task.SimplePlan;
import com.zurrtum.create.client.flywheel.lib.util.LevelAttached;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Contract;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

/**
 * A manager class for a single level where visualization is supported.
 */
public class VisualizationManagerImpl implements VisualizationManager {
    private static final LevelAttached<VisualizationManagerImpl> MANAGERS = new LevelAttached<>(
        VisualizationManagerImpl::new,
        VisualizationManagerImpl::delete
    );

    private final TaskExecutorImpl taskExecutor;
    private final DistanceUpdateLimiterImpl frameLimiter;
    private final RenderDispatcherImpl renderDispatcher = new RenderDispatcherImpl();
    private final LevelAccessor level;

    // VisualizationManagerImpl can (and should!) be constructed off of the main thread, but it may be
    // difficult for engines to avoid OpenGL calls which would not be safe. Shove all the init logic
    // that depends on engine construction into here, and defer until we get invoked on the main thread.
    @Nullable
    private LateInit lateInit;

    private final VisualManagerImpl<BlockEntity, BlockEntityStorage> blockEntities;
    private final VisualManagerImpl<Entity, EntityStorage> entities;
    private final VisualManagerImpl<Effect, EffectStorage> effects;

    private final Flag frameFlag = new Flag("frame");
    private final Flag tickFlag = new Flag("tick");

    private VisualizationManagerImpl(LevelAccessor level) {
        this.level = level;
        taskExecutor = FlwTaskExecutor.get();
        frameLimiter = createUpdateLimiter();

        blockEntities = new VisualManagerImpl<>(new BlockEntityStorage());
        entities = new VisualManagerImpl<>(new EntityStorage());
        effects = new VisualManagerImpl<>(new EffectStorage());

        if (level instanceof Level l) {
            l.getEntities().getAll().forEach(entities::queueAdd);
        }
    }

    private class LateInit {
        private final Engine engine;

        private final Plan<RenderContext> framePlan;
        private final Plan<TickableVisual.Context> tickPlan;

        private LateInit(LevelAccessor level) {
            engine = BackendManager.currentBackend().createEngine(level);

            var visualizationContext = engine.createVisualizationContext();

            var recreate = SimplePlan.<RenderContext>of(
                context -> blockEntities.getStorage().recreateAll(visualizationContext, context.partialTick()),
                context -> entities.getStorage().recreateAll(visualizationContext, context.partialTick()),
                context -> effects.getStorage().recreateAll(visualizationContext, context.partialTick())
            );

            var update = MapContextPlan.map(this::createVisualFrameContext).to(NestedPlan.of(
                blockEntities.framePlan(visualizationContext),
                entities.framePlan(visualizationContext),
                effects.framePlan(visualizationContext)
            ));

            framePlan = IfElsePlan.on((RenderContext ctx) -> engine.updateRenderOrigin(ctx.camera())).ifTrue(recreate).ifFalse(update).plan()
                .then(SimplePlan.of(() -> {
                    if (blockEntities.areGpuLightSectionsDirty() || entities.areGpuLightSectionsDirty() || effects.areGpuLightSectionsDirty()) {
                        var out = new LongOpenHashSet();
                        out.addAll(blockEntities.gpuLightSections());
                        out.addAll(entities.gpuLightSections());
                        out.addAll(effects.gpuLightSections());
                        engine.lightSections(out);
                    }
                })).then(engine.createFramePlan()).then(RaisePlan.raise(frameFlag));

            tickPlan = NestedPlan.of(
                blockEntities.tickPlan(visualizationContext),
                entities.tickPlan(visualizationContext),
                effects.tickPlan(visualizationContext)
            ).then(RaisePlan.raise(tickFlag));
        }

        private DynamicVisual.Context createVisualFrameContext(RenderContext ctx) {
            Vec3i renderOrigin = engine.renderOrigin();
            var cameraPos = ctx.camera().position();

            Matrix4f viewProjection = new Matrix4f(ctx.viewProjection());
            viewProjection.translate(
                (float) (renderOrigin.getX() - cameraPos.x),
                (float) (renderOrigin.getY() - cameraPos.y),
                (float) (renderOrigin.getZ() - cameraPos.z)
            );
            FrustumIntersection frustum = new FrustumIntersection(viewProjection);

            return new DynamicVisualContextImpl(ctx.camera(), frustum, ctx.partialTick(), frameLimiter);
        }
    }

    private LateInit lateInit() {
        if (lateInit == null) {
            lateInit = new LateInit(level);
        }

        return lateInit;
    }

    private DistanceUpdateLimiterImpl createUpdateLimiter() {
        if (FlwConfig.INSTANCE.limitUpdates()) {
            return new BandedPrimeLimiter();
        } else {
            return new NonLimiter();
        }
    }

    @Contract("null -> false")
    public static boolean supportsVisualization(@Nullable LevelAccessor level) {
        if (!BackendManager.isBackendOn()) {
            return false;
        }

        if (level == null) {
            return false;
        }

        if (!level.isClientSide()) {
            return false;
        }

        if (level instanceof VisualizationLevel flywheelLevel && flywheelLevel.supportsVisualization()) {
            return true;
        }

        return level == Minecraft.getInstance().level;
    }

    @Nullable
    public static VisualizationManagerImpl get(@Nullable LevelAccessor level) {
        if (!supportsVisualization(level)) {
            return null;
        }

        return MANAGERS.get(level);
    }

    public static VisualizationManagerImpl getOrThrow(@Nullable LevelAccessor level) {
        if (!supportsVisualization(level)) {
            throw new IllegalStateException("Cannot retrieve visualization manager when visualization is not supported by level '" + level + "'!");
        }

        return MANAGERS.get(level);
    }

    // TODO: Consider making these reset actions reuse the existing game objects instead of re-adding them
    //  potentially by keeping the same VisualizationManagerImpl and deleting the engine and visuals but not the game objects
    public static void reset(LevelAccessor level) {
        MANAGERS.remove(level);
    }

    public static void resetAll() {
        MANAGERS.reset();
    }

    @Override
    public Vec3i renderOrigin() {
        if (lateInit == null) {
            return Vec3i.ZERO;
        } else {
            return lateInit.engine.renderOrigin();
        }
    }

    @Override
    public VisualManager<BlockEntity> blockEntities() {
        return blockEntities;
    }

    @Override
    public VisualManager<Entity> entities() {
        return entities;
    }

    @Override
    public VisualManager<Effect> effects() {
        return effects;
    }

    @Override
    public RenderDispatcher renderDispatcher() {
        return renderDispatcher;
    }

    /**
     * Begin execution of the tick plan.
     */
    public void tick() {
        // Make sure we're done with any prior frame or tick to avoid racing.
        taskExecutor.syncUntil(frameFlag::isRaised);
        frameFlag.lower();

        taskExecutor.syncUntil(tickFlag::isRaised);
        tickFlag.lower();

        lateInit().tickPlan.execute(taskExecutor, TickableVisualContextImpl.INSTANCE);
    }

    /**
     * Begin execution of the frame plan.
     */
    private void beginFrame(RenderContext context) {
        // Make sure we're done with the last tick.
        // Note we don't lower here because many frames may happen per tick.
        taskExecutor.syncUntil(tickFlag::isRaised);

        frameFlag.lower();

        frameLimiter.tick();

        lateInit().framePlan.execute(taskExecutor, context);
    }

    /**
     * Draw all visuals of the given type.
     */
    private void render(RenderContext context) {
        taskExecutor.syncUntil(frameFlag::isRaised);
        lateInit().engine.render(context);
    }

    private void renderCrumbling(RenderContext context, Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress) {
        if (destructionProgress.isEmpty()) {
            return;
        }

        List<CrumblingBlock> crumblingBlocks = new ArrayList<>();

        for (var entry : destructionProgress.long2ObjectEntrySet()) {
            var set = entry.getValue();
            if (set == null || set.isEmpty()) {
                // Nothing to do if there's no crumbling.
                continue;
            }

            var visual = blockEntities.getStorage().visualAtPos(entry.getLongKey());

            if (visual == null) {
                // The block doesn't have a visual, this is probably the common case.
                continue;
            }

            List<Instance> instances = new ArrayList<>();

            visual.collectCrumblingInstances(instance -> {
                if (instance != null) {
                    instances.add(instance);
                }
            });

            if (instances.isEmpty()) {
                // The visual doesn't want to render anything crumbling.
                continue;
            }

            var maxDestruction = set.last();

            crumblingBlocks.add(new CrumblingBlockImpl(maxDestruction.getPos(), maxDestruction.getProgress(), instances));
        }

        if (!crumblingBlocks.isEmpty()) {
            lateInit().engine.renderCrumbling(context, crumblingBlocks);
        }
    }

    public void onLightUpdate(SectionPos sectionPos, LightLayer layer) {
        lateInit().engine.onLightUpdate(sectionPos, layer);
        long longPos = sectionPos.asLong();
        blockEntities.onLightUpdate(longPos);
        entities.onLightUpdate(longPos);
        effects.onLightUpdate(longPos);
    }

    /**
     * Free all acquired resources and delete this manager.
     */
    private void delete() {
        // Just finish everything. This may include the work of others but that's okay.
        taskExecutor.syncPoint();

        // Now clean up.
        blockEntities.invalidate();
        entities.invalidate();
        effects.invalidate();
        if (lateInit != null) {
            lateInit.engine.delete();
        }
    }

    /**
     * Expose the raw engine, iff it has been initialized and is a default Flywheel engine.
     * <p>For debug information gathering only.
     */
    @Nullable
    public EngineImpl getEngineImpl() {
        if (lateInit == null) {
            return null;
        }
        var engine = lateInit.engine;
        if (engine instanceof EngineImpl engineImpl) {
            return engineImpl;
        }
        return null;
    }

    private class RenderDispatcherImpl implements RenderDispatcher {
        @Override
        public void onStartLevelRender(RenderContext ctx) {
            beginFrame(ctx);
        }

        @Override
        public void afterEntities(RenderContext ctx) {
            render(ctx);
        }

        @Override
        public void beforeCrumbling(RenderContext ctx, Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress) {
            renderCrumbling(ctx, destructionProgress);
        }
    }

    private record CrumblingBlockImpl(
        BlockPos pos, int progress, List<Instance> instances
    ) implements CrumblingBlock {
    }
}
