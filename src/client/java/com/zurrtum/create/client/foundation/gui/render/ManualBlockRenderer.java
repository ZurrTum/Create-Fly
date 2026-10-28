package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.zurrtum.create.client.catnip.gui.render.GpuTexture;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;

import java.util.ArrayDeque;
import java.util.Deque;

public class ManualBlockRenderer extends GuiBlockRenderer<ManualBlockRenderState> {
    public static int MAX = 6;
    private int allocate = MAX;
    private static final Deque<GpuTexture> TEXTURES = new ArrayDeque<>(MAX);
    private final PoseStack matrices = new PoseStack();
    private int windowScaleFactor;

    @Override
    public void prepare(
        ManualBlockRenderState block,
        GuiRenderState state,
        FeatureRenderDispatcher featureRenderDispatcher,
        int windowScaleFactor
    ) {
        if (this.windowScaleFactor != windowScaleFactor) {
            this.windowScaleFactor = windowScaleFactor;
            TEXTURES.forEach(GpuTexture::close);
            TEXTURES.clear();
            allocate = MAX;
        }
        int size = 27 * windowScaleFactor;
        GpuTexture texture;
        if (allocate > 0) {
            allocate--;
            texture = GpuTexture.create(size);
        } else {
            texture = TEXTURES.poll();
            assert texture != null;
        }
        texture.prepare(projection, projectionMatrixBuffer);
        matrices.pushPose();
        Pose pose = matrices.last();
        pose.translate(size / 2.0F, size, 0.0F);
        float scale = 20 * windowScaleFactor;
        pose.scale(scale, scale, scale);
        pose.rotate(Axis.XP, Radians.DEG_344P5);
        pose.rotate(Axis.YP, Radians.DEG_22P5);
        pose.translate(-0.5f, -0.2f, -0.5f);
        pose.scale(1, -1, 1);
        CachedBuffers.block(block.state()).submit(matrices, submitNodeStorage);
        matrices.popPose();
        renderAllFeatures(featureRenderDispatcher);
        texture.clear();
        state.addBlitToCurrentLayer(new BlitRenderState(
            RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
            TextureSetup.singleTexture(
                texture.textureView(),
                RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST)
            ),
            block.pose(),
            block.x0(),
            block.y0(),
            block.x1(),
            block.y1(),
            0.0F,
            1.0F,
            1.0F,
            0.0F,
            -1,
            null,
            null
        ));
        TEXTURES.add(texture);
    }

    @Override
    protected void renderToTexture(
        ManualBlockRenderState state,
        PoseStack matrices,
        SubmitNodeCollector submitNodeCollector
    ) {
    }

    @Override
    protected String getTextureLabel() {
        return "Manual Block";
    }

    @Override
    public Class<ManualBlockRenderState> getRenderStateClass() {
        return ManualBlockRenderState.class;
    }
}
