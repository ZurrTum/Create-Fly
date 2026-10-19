package com.zurrtum.create.client.flywheel.api.visualization;

import com.zurrtum.create.client.flywheel.api.backend.Engine.CrumblingBlock;
import com.zurrtum.create.client.flywheel.api.backend.RenderContext;
import com.zurrtum.create.client.flywheel.api.internal.FlwApiLink;
import com.zurrtum.create.client.flywheel.api.visual.Effect;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.List;

@ApiStatus.NonExtendable
public interface VisualizationManager {
    static boolean supportsVisualization(@Nullable LevelAccessor level) {
        return FlwApiLink.INSTANCE.supportsVisualization(level);
    }

    static @Nullable VisualizationManager get(@Nullable LevelAccessor level) {
        return FlwApiLink.INSTANCE.getVisualizationManager(level);
    }

    static VisualizationManager getOrThrow(@Nullable LevelAccessor level) {
        return FlwApiLink.INSTANCE.getVisualizationManagerOrThrow(level);
    }

    Vec3i renderOrigin();

    VisualManager<BlockEntity> blockEntities();

    VisualManager<Entity> entities();

    VisualManager<Effect> effects();

    /**
     * Get the render dispatcher, which can be used to invoke rendering.
     * <b>This should only be used by mods which heavily rewrite rendering to restore compatibility with Flywheel
     * without mixins.</b>
     */
    RenderDispatcher renderDispatcher();

    void collectCrumblingBlocks(
        List<BlockBreakingRenderState> destructionProgress,
        List<CrumblingBlock> crumblingBlocks
    );

    @ApiStatus.NonExtendable
    interface RenderDispatcher {
        /**
         * Prepare visuals for render.
         *
         * <p>Guaranteed to be called before {@link #afterSolid}.
         * <br>Guaranteed to be called after the render thread has processed all light updates.
         * <br>The caller is otherwise free to choose an invocation site, but it is recommended to call
         * this as early as possible to give the VisualizationManager time to process things off-thread.
         */
        void onStartLevelRender(RenderContext var1);

        /**
         * Render instances.
         *
         * <p>Guaranteed to be called after {@link #onStartLevelRender}.
         * <br>The caller is otherwise free to choose an invocation site, but it is recommended to call
         * this between rendering entities and block entities.
         */
        void afterSolid(RenderContext var1);
    }
}
