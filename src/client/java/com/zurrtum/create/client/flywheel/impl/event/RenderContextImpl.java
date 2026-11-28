package com.zurrtum.create.client.flywheel.impl.event;

import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.zurrtum.create.client.flywheel.api.backend.RenderContext;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.oit.OitStage;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

public class RenderContextImpl implements RenderContext {
    public LevelRenderState levelRenderState;
    public Matrix4fc projection;
    public Matrix4f viewProjection;
    public float partialTick;
    public @Nullable VisualizationManager visualizationManager;
    private boolean useClassicTransparency;

    public RenderContextImpl(LevelRenderState levelRenderState) {
        this.levelRenderState = levelRenderState;
        projection = levelRenderState.cameraRenderState.projectionMatrix;
        viewProjection = new Matrix4f();
    }

    public void update(@Nullable ClientLevel level, float partialTick) {
        visualizationManager = VisualizationManager.get(level);
        if (visualizationManager != null) {
            this.partialTick = partialTick;
        }
    }

    public void updateProjection(Matrix4fc projection) {
        if (visualizationManager != null) {
            this.projection = projection;
            viewProjection.set(projection).mul(levelRenderState.cameraRenderState.viewRotationMatrix);
        }
    }

    public void onStartLevelRender(boolean useImprovedTransparency) {
        if (visualizationManager != null) {
            useClassicTransparency = !useImprovedTransparency;
            visualizationManager.renderDispatcher().onStartLevelRender(this);
        }
    }

    public void afterSolid() {
        if (visualizationManager != null) {
            visualizationManager.renderDispatcher().afterSolid(this);
        }
    }

    public void executeOit(
        OitStage stage,
        GpuTextureView depthBoundsCulledTargetView,
        GpuTextureView[] transmittanceTargetViews
    ) {
        if (visualizationManager != null) {
            visualizationManager.renderDispatcher()
                .executeOit(stage, depthBoundsCulledTargetView, transmittanceTargetViews);
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
    public VisualizationManager visualizationManager() {
        assert visualizationManager != null;
        return visualizationManager;
    }

    @Override
    public boolean useClassicTransparency() {
        return useClassicTransparency;
    }
}
