package com.zurrtum.create.client.flywheel.api.backend;

import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.task.Plan;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.LightType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Range;

import java.util.List;

@BackendImplemented
public interface Engine {
    VisualizationContext createVisualizationContext();

    Plan<RenderContext> createFramePlan();

    Vec3i renderOrigin();

    boolean updateRenderOrigin(Camera var1);

    void lightSections(LongSet var1);

    void onLightUpdate(ChunkSectionPos var1, LightType var2);

    void render(RenderContext var1);

    void renderCrumbling(RenderContext var1, List<CrumblingBlock> var2);

    void delete();

    @ApiStatus.NonExtendable
    public interface CrumblingBlock {
        BlockPos pos();

        @Range(from = 0L, to = 9L)
        int progress();

        List<Instance> instances();
    }
}
