package com.zurrtum.create.client.flywheel.api.visualization;

import com.zurrtum.create.client.flywheel.api.backend.RenderContext;
import com.zurrtum.create.client.flywheel.api.internal.FlwApiLink;
import com.zurrtum.create.client.flywheel.api.visual.Effect;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.BlockBreakingInfo;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.SortedSet;

@ApiStatus.NonExtendable
public interface VisualizationManager {
    static boolean supportsVisualization(@Nullable WorldAccess level) {
        return FlwApiLink.INSTANCE.supportsVisualization(level);
    }

    static @Nullable VisualizationManager get(@Nullable WorldAccess level) {
        return FlwApiLink.INSTANCE.getVisualizationManager(level);
    }

    static VisualizationManager getOrThrow(@Nullable WorldAccess level) {
        return FlwApiLink.INSTANCE.getVisualizationManagerOrThrow(level);
    }

    Vec3i renderOrigin();

    VisualManager<BlockEntity> blockEntities();

    VisualManager<Entity> entities();

    VisualManager<Effect> effects();

    RenderDispatcher renderDispatcher();

    @ApiStatus.NonExtendable
    public interface RenderDispatcher {
        void onStartLevelRender(RenderContext var1);

        void afterEntities(RenderContext var1);

        void beforeCrumbling(RenderContext var1, Long2ObjectMap<SortedSet<BlockBreakingInfo>> var2);
    }
}
