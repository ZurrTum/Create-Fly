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
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class SpoutRenderer extends GuiBlockRenderer<SpoutRenderState> {
    private static final Int2ObjectMap<GpuTexture> TEXTURES = new Int2ObjectArrayMap<>();
    private final PoseStack matrices = new PoseStack();
    private int windowScaleFactor;

    @Override
    public void prepare(
        SpoutRenderState item,
        GuiRenderState state,
        FeatureRenderDispatcher featureRenderDispatcher,
        int windowScaleFactor
    ) {
        if (this.windowScaleFactor != windowScaleFactor) {
            this.windowScaleFactor = windowScaleFactor;
            TEXTURES.values().forEach(GpuTexture::close);
            TEXTURES.clear();
        }
        int width = 26 * windowScaleFactor;
        int height = 65 * windowScaleFactor;
        GpuTexture texture = TEXTURES.get(item.id());
        if (texture == null) {
            texture = GpuTexture.create(width, height);
            TEXTURES.put(item.id(), texture);
        }
        texture.prepare(projection, projectionMatrixBuffer);
        matrices.pushPose();
        Pose pose = matrices.last();
        pose.translate(width / 2.0F, height / 2.0F, 0.0F);
        float scale = 20 * windowScaleFactor;
        pose.scale(scale, scale, scale);

        pose.rotate(Axis.XP, Radians.DEG_344P5);
        pose.rotate(Axis.YP, Radians.DEG_22P5);
        pose.translate(-0.5f, -0.5f, -0.5f);
        pose.scale(1, -1, 1);

        float time = AnimationTickHolder.getRenderTime();
        CachedBuffers.block(AllBlocks.SPOUT.defaultBlockState()).submit(matrices, submitNodeStorage);

        float cycle = (time - item.offset() * 8) % 30;
        float squeeze = cycle < 20 ? -Mth.sin((float) (cycle / 20.0f * Math.PI)) : 0;
        float move = -3 * squeeze / 32.0f;

        matrices.pushPose();
        pose = matrices.last();
        pose.translate(0, move, 0);
        CachedBuffers.partial(AllPartialModels.SPOUT_MIDDLE, Blocks.AIR.defaultBlockState())
            .submit(matrices, submitNodeStorage);
        pose.translate(0, move, 0);
        CachedBuffers.partial(AllPartialModels.SPOUT_BOTTOM, Blocks.AIR.defaultBlockState())
            .submit(matrices, submitNodeStorage);
        matrices.popPose();

        matrices.pushPose();
        pose.translate(0.07f, -2, -0.14f);
        CachedBuffers.block(AllBlocks.DEPOT.defaultBlockState()).submit(matrices, submitNodeStorage);
        matrices.popPose();
        matrices.popPose();

        Fluid fluid = item.fluid();
        if (fluid != Fluids.EMPTY) {
            matrices.pushPose();
            pose = matrices.last();
            pose.rotate(Axis.XP, Radians.DEG_344P5);
            pose.rotate(Axis.YP, Radians.DEG_22P5);

            DataComponentPatch components = item.components();
            matrices.pushPose();
            float fluidScale = 16 * windowScaleFactor;
            pose = matrices.last();
            pose.scale(fluidScale, -fluidScale, fluidScale);
            pose.translate(0, -1.4f, 0);
            float from = 3.0f / 16.0f;
            float to = 17.0f / 16.0f;
            FluidStateModelSet fluidStateModelSet = Minecraft.getInstance().getModelManager().getFluidStateModelSet();
            FluidRenderHelper.extractFluidRenderState(
                null,
                null,
                fluidStateModelSet,
                fluid,
                components,
                from,
                from,
                from,
                to,
                to,
                to,
                0,
                false,
                true
            ).submit(matrices, submitNodeStorage);
            matrices.popPose();

            pose = matrices.last();
            pose.translate(scale / 2.0f, scale * 1.5f, scale / 2.0f);
            pose.scale(fluidScale, -fluidScale, fluidScale);
            pose.translate(-0.5f, -1.0f, -0.5f);
            float fluidWidth = 1 / 128.0f * -squeeze * 16;
            from = -fluidWidth / 2 + 0.5f;
            to = fluidWidth / 2 + 0.5f;
            FluidRenderHelper.extractFluidRenderState(
                null,
                null,
                fluidStateModelSet,
                fluid,
                components,
                from,
                0,
                from,
                to,
                2,
                to,
                0,
                false,
                true
            ).submit(matrices, submitNodeStorage);
            matrices.popPose();
        }

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

    @Override
    protected void renderToTexture(
        SpoutRenderState state,
        PoseStack matrices,
        SubmitNodeCollector submitNodeCollector
    ) {
    }

    @Override
    protected String getTextureLabel() {
        return "Spout";
    }

    @Override
    public Class<SpoutRenderState> getRenderStateClass() {
        return SpoutRenderState.class;
    }
}
