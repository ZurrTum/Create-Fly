package com.zurrtum.create.client.flywheel.backend.engine;

import com.zurrtum.create.client.flywheel.api.backend.Engine;
import com.zurrtum.create.client.flywheel.api.backend.RenderContext;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import com.zurrtum.create.client.flywheel.api.instance.Instancer;
import com.zurrtum.create.client.flywheel.api.instance.InstancerProvider;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.api.task.Plan;
import com.zurrtum.create.client.flywheel.api.visualization.VisualEmbedding;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.backend.FlwBackend;
import com.zurrtum.create.client.flywheel.backend.engine.embed.EmbeddedEnvironment;
import com.zurrtum.create.client.flywheel.backend.engine.embed.Environment;
import com.zurrtum.create.client.flywheel.backend.engine.embed.EnvironmentStorage;
import com.zurrtum.create.client.flywheel.backend.engine.uniform.Uniforms;
import com.zurrtum.create.client.flywheel.backend.gl.GlStateTracker;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

public class EngineImpl implements Engine {
    private final DrawManager<? extends AbstractInstancer<?>> drawManager;
    private final int sqrMaxOriginDistance;
    private final EnvironmentStorage environmentStorage;
    private final LightStorage lightStorage;

    private BlockPos renderOrigin = BlockPos.ZERO;

    public EngineImpl(LevelAccessor level, DrawManager<? extends AbstractInstancer<?>> drawManager, int maxOriginDistance) {
        this.drawManager = drawManager;
        sqrMaxOriginDistance = maxOriginDistance * maxOriginDistance;
        environmentStorage = new EnvironmentStorage();
        lightStorage = new LightStorage(level);
    }

    @Override
    public VisualizationContext createVisualizationContext() {
        return new VisualizationContextImpl();
    }

    @Override
    public Plan<RenderContext> createFramePlan() {
        return drawManager.createFramePlan().and(lightStorage.createFramePlan());
    }

    @Override
    public Vec3i renderOrigin() {
        return renderOrigin;
    }

    @Override
    public boolean updateRenderOrigin(Camera camera) {
        Vec3 cameraPos = camera.getPosition();
        double dx = renderOrigin.getX() - cameraPos.x;
        double dy = renderOrigin.getY() - cameraPos.y;
        double dz = renderOrigin.getZ() - cameraPos.z;
        double distanceSqr = dx * dx + dy * dy + dz * dz;

        if (distanceSqr <= sqrMaxOriginDistance) {
            return false;
        }

        renderOrigin = BlockPos.containing(cameraPos);
        drawManager.onRenderOriginChanged();
        return true;
    }

    @Override
    public void lightSections(LongSet sections) {
        lightStorage.sections(sections);
    }

    @Override
    public void onLightUpdate(SectionPos sectionPos, LightLayer layer) {
        lightStorage.onLightUpdate(sectionPos.asLong());
    }

    @Override
    public void render(RenderContext context) {
        try (var state = GlStateTracker.getRestoreState()) {
            // Process the render queue for font updates
            Uniforms.update(context);
            environmentStorage.flush();
            drawManager.render(lightStorage, environmentStorage);
        } catch (Exception e) {
            FlwBackend.LOGGER.error("Falling back", e);
            triggerFallback();
        }
    }

    @Override
    public void renderCrumbling(RenderContext context, List<CrumblingBlock> crumblingBlocks) {
        try (var state = GlStateTracker.getRestoreState()) {
            drawManager.renderCrumbling(crumblingBlocks);
        } catch (Exception e) {
            FlwBackend.LOGGER.error("Falling back", e);
            triggerFallback();
        }
    }

    @Override
    public void delete() {
        drawManager.delete();
        lightStorage.delete();
        environmentStorage.delete();
    }

    private void triggerFallback() {
        drawManager.triggerFallback();
    }

    public <I extends Instance> Instancer<I> instancer(Environment environment, InstanceType<I> type, Model model, int bias) {
        return drawManager.getInstancer(environment, type, model, bias);
    }

    public EnvironmentStorage environmentStorage() {
        return environmentStorage;
    }

    public LightStorage lightStorage() {
        return lightStorage;
    }

    private class VisualizationContextImpl implements VisualizationContext {
        private final InstancerProviderImpl instancerProvider;

        public VisualizationContextImpl() {
            instancerProvider = new InstancerProviderImpl(EngineImpl.this);
        }

        @Override
        public InstancerProvider instancerProvider() {
            return instancerProvider;
        }

        @Override
        public Vec3i renderOrigin() {
            return EngineImpl.this.renderOrigin();
        }

        @Override
        public VisualEmbedding createEmbedding(Vec3i renderOrigin) {
            var out = new EmbeddedEnvironment(EngineImpl.this, renderOrigin);
            environmentStorage.track(out);
            return out;
        }
    }
}
