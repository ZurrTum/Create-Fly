package com.zurrtum.create.client.flywheel.impl.event;

import com.zurrtum.create.client.flywheel.api.backend.Engine.CrumblingBlock;
import com.zurrtum.create.client.flywheel.api.backend.RenderContext;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class RenderContextImpl implements RenderContext {
    public LevelRenderState levelRenderState;
    public Matrix4fc projection;
    public Matrix4f viewProjection;
    public float partialTick;
    public @Nullable VisualizationManager visualizationManager;
    public ObjectArrayList<CrumblingBlock> crumblingBlocks;

    public RenderContextImpl(LevelRenderState levelRenderState) {
        this.levelRenderState = levelRenderState;
        projection = levelRenderState.cameraRenderState.projectionMatrix;
        viewProjection = new Matrix4f();
        crumblingBlocks = new ObjectArrayList<>();
    }

    public void update(@Nullable ClientLevel level, float partialTick) {
        visualizationManager = VisualizationManager.get(level);
        if (visualizationManager != null) {
            crumblingBlocks.clear();
            visualizationManager.collectCrumblingBlocks(levelRenderState.blockBreakingRenderStates, crumblingBlocks);
            this.partialTick = partialTick;
        }
    }

    public void updateProjection(Matrix4fc projection) {
        this.projection = projection;
        viewProjection.set(projection).mul(levelRenderState.cameraRenderState.viewRotationMatrix);
    }

    public void onStartLevelRender() {
        if (visualizationManager != null) {
            visualizationManager.renderDispatcher().onStartLevelRender(this);
        }
    }

    public void afterSolid() {
        if (visualizationManager != null) {
            visualizationManager.renderDispatcher().afterSolid(this);
        }
    }

    @Override
    public LevelRenderState levelRenderState() {
        return levelRenderState;
    }

    @Override
    public Matrix4fc projection() {
        return projection;
    }

    @Override
    public Matrix4fc viewProjection() {
        return viewProjection;
    }

    @Override
    public float partialTick() {
        return partialTick;
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public VisualizationManager visualizationManager() {
        return visualizationManager;
    }

    @Override
    public List<CrumblingBlock> crumblingBlocks() {
        return crumblingBlocks;
    }
}
