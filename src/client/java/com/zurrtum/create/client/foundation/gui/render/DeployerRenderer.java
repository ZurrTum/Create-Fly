package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.render.GpuTexture;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlock;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class DeployerRenderer extends GuiBlockRenderer<DeployerRenderState> {
    private static final Int2ObjectMap<GpuTexture> TEXTURES = new Int2ObjectArrayMap<>();
    private final PoseStack matrices = new PoseStack();
    private int windowScaleFactor;

    @Override
    public void prepare(
        DeployerRenderState item,
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
        int height = 75 * windowScaleFactor;
        GpuTexture texture = TEXTURES.get(item.id());
        if (texture == null) {
            texture = GpuTexture.create(width, height);
            TEXTURES.put(item.id(), texture);
        }
        texture.prepare(projection, projectionMatrixBuffer);
        matrices.pushPose();
        matrices.translate(width / 2.0F, height, 0.0F);
        float scale = 20 * windowScaleFactor;
        matrices.scale(scale, scale, scale);

        matrices.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrices.mulPose(Axis.YP.rotationDegrees(22.5f));
        matrices.translate(-0.5f, -2.24f, -0.5f);
        matrices.scale(1, -1, 1);

        float time = AnimationTickHolder.getRenderTime();
        float cycle = (time - item.offset() * 8) % 30;
        float offset = cycle < 10 ? cycle / 10.0f : cycle < 20 ? (20 - cycle) / 10.0f : 0;

        matrices.pushPose();
        BlockState blockState = AllBlocks.SHAFT.defaultBlockState()
            .setValue(BlockStateProperties.AXIS, Direction.Axis.Z);
        matrices.rotateAround(Axis.ZP.rotationDegrees(getCurrentAngle(time)), 0.5f, 0.5f, 0.5f);
        CachedBuffers.block(blockState).submit(matrices, submitNodeStorage);
        matrices.popPose();

        blockState = AllBlocks.DEPLOYER.defaultBlockState().setValue(DeployerBlock.FACING, Direction.DOWN)
            .setValue(DeployerBlock.AXIS_ALONG_FIRST_COORDINATE, false);
        CachedBuffers.block(blockState).submit(matrices, submitNodeStorage);

        matrices.pushPose();
        blockState = Blocks.AIR.defaultBlockState();
        matrices.translate(0, -offset, 0);
        matrices.rotateAround(Axis.XP.rotationDegrees(90), 0.5f, 0.5f, 0.5f);
        CachedBuffers.partial(AllPartialModels.DEPLOYER_POLE, blockState).submit(matrices, submitNodeStorage);
        CachedBuffers.partial(AllPartialModels.DEPLOYER_HAND_HOLDING, blockState).submit(matrices, submitNodeStorage);
        matrices.popPose();

        matrices.translate(0, -2.06f, 0);
        CachedBuffers.block(AllBlocks.DEPOT.defaultBlockState()).submit(matrices, submitNodeStorage);

        matrices.popPose();
        renderAllFeatures(featureRenderDispatcher);
        texture.clear();
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

    public static float getCurrentAngle(float time) {
        return time * 4.0f % 360;
    }

    @Override
    protected void renderToTexture(
        DeployerRenderState state,
        PoseStack matrices,
        SubmitNodeCollector submitNodeCollector
    ) {
    }

    @Override
    protected String getTextureLabel() {
        return "Deployer";
    }

    @Override
    public Class<DeployerRenderState> getRenderStateClass() {
        return DeployerRenderState.class;
    }
}
