package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.render.GpuTexture;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class PressRenderer extends GuiBlockRenderer<PressRenderState> {
    private static final Int2ObjectMap<GpuTexture> TEXTURES = new Int2ObjectArrayMap<>();
    private final PoseStack matrices = new PoseStack();
    private int windowScaleFactor;

    @Override
    public void prepare(
        PressRenderState item,
        GuiRenderState state,
        FeatureRenderDispatcher featureRenderDispatcher,
        int windowScaleFactor
    ) {
        if (this.windowScaleFactor != windowScaleFactor) {
            this.windowScaleFactor = windowScaleFactor;
            TEXTURES.values().forEach(GpuTexture::close);
            TEXTURES.clear();
        }
        int width = 30 * windowScaleFactor;
        int height = 64 * windowScaleFactor;
        GpuTexture texture = TEXTURES.get(item.id());
        if (texture == null) {
            texture = GpuTexture.create(width, height);
            TEXTURES.put(item.id(), texture);
        }
        texture.prepare(projection, projectionMatrixBuffer);
        matrices.pushPose();
        Pose pose = matrices.last();
        pose.translate(width / 2.0F, height, 0.0F);
        float scale = 23 * windowScaleFactor;
        pose.scale(scale, scale, scale);

        pose.rotate(Axis.XP, Radians.DEG_344P5);
        pose.rotate(Axis.YP, Radians.DEG_22P5);
        pose.translate(-0.5f, -1.14f, -0.5f);
        pose.scale(1, -1, 1);

        float time = AnimationTickHolder.getRenderTime();
        CachedBuffers.block(AllBlocks.MECHANICAL_PRESS.defaultBlockState()).submit(matrices, submitNodeStorage);

        matrices.pushPose();
        matrices.rotateZCentered(Mth.DEG_TO_RAD * getShaftAngle(time));
        CachedBuffers.block(AllBlocks.SHAFT.defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.Z))
            .submit(matrices, submitNodeStorage);
        matrices.popPose();

        matrices.pushPose();
        matrices.translate(0, getAnimatedHeadOffset(time, item.offset()), 0);
        CachedBuffers.partial(AllPartialModels.MECHANICAL_PRESS_HEAD, Blocks.AIR.defaultBlockState())
            .submit(matrices, submitNodeStorage);
        matrices.popPose();
        matrices.popPose();
        renderAllFeatures(texture, featureRenderDispatcher);
        state.addBlitToCurrentLayer(new BlitRenderState(
            RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
            TextureSetup.singleTexture(
                texture.textureView(),
                RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST)
            ),
            item.pose(),
            item.x0(),
            item.y0(),
            item.x1(),
            item.y1(),
            0.0F,
            1.0F,
            1.0F,
            0.0F,
            -1,
            null,
            null
        ));
    }

    private static float getShaftAngle(float time) {
        return time * 4.0f % 360;
    }

    private static float getAnimatedHeadOffset(float time, float offset) {
        float cycle = (time - offset * 8) % 30;
        if (cycle < 10) {
            float progress = cycle / 10;
            return -(progress * progress * progress);
        }
        if (cycle < 15) {
            return -1;
        }
        if (cycle < 20) {
            return -1 + (1 - (20 - cycle) / 5);
        }
        return 0;
    }

    @Override
    protected void renderToTexture(
        PressRenderState state,
        PoseStack matrices,
        SubmitNodeCollector submitNodeCollector
    ) {
    }

    @Override
    protected String getTextureLabel() {
        return "Press";
    }

    @Override
    public Class<PressRenderState> getRenderStateClass() {
        return PressRenderState.class;
    }
}
