package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.catnip.render.QuadRenderHelper;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class FanRenderer extends GuiBlockRenderer<FanRenderState> {
    private final ModelManager modelManager;

    public FanRenderer() {
        modelManager = Minecraft.getInstance().getModelManager();
    }

    @Override
    protected void renderToTexture(FanRenderState state, PoseStack matrices, SubmitNodeCollector queue) {
        Pose pose = matrices.last();
        pose.scale(1, 1, -1);
        pose.rotate(Axis.XP, Radians.DEG_344P5);
        pose.rotate(Axis.YP, Radians.DEG_22P5);
        pose.translate(-0.92f, -0.75f, -0.5f);
        pose.scale(1, -1, 1);
        matrices.pushPose();
        pose = matrices.last();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.rotate(Axis.ZP, getCurrentAngle() * Radians.DEG_16);
        pose.rotate(Axis.XP, Radians.DEG_180);
        pose.translate(-0.5f, -0.5f, -0.5f);
        CachedBuffers.partial(AllPartialModels.ENCASED_FAN_INNER, Blocks.AIR.defaultBlockState())
            .submit(matrices, queue);
        matrices.popPose();

        matrices.pushPose();
        matrices.rotateYCentered(Radians.DEG_180);
        CachedBuffers.block(AllBlocks.ENCASED_FAN.defaultBlockState()).submit(matrices, queue);
        matrices.popPose();

        matrices.translate(0, 0, 2);
        BlockState blockState = state.target();
        QuadRenderHelper.markFireSpriteActive(modelManager, blockState);
        FluidState fluidState = blockState.getFluidState();
        if (!fluidState.isEmpty()) {
            Fluid fluid = fluidState.getType();
            FluidStateModelSet fluidStateModelSet = Minecraft.getInstance().getModelManager().getFluidStateModelSet();
            FluidRenderHelper.extractFluidRenderState(
                null,
                null,
                fluidStateModelSet,
                fluid,
                DataComponentPatch.EMPTY,
                0,
                0,
                0,
                1,
                1,
                1,
                0,
                false,
                true
            ).submit(matrices, queue);
            return;
        }
        CachedBuffers.block(blockState).submit(matrices, queue);
    }

    public static float getCurrentAngle() {
        return AnimationTickHolder.getRenderTime() * 4.0f % 360;
    }

    @Override
    protected String getTextureLabel() {
        return "Fan";
    }

    @Override
    public Class<FanRenderState> getRenderStateClass() {
        return FanRenderState.class;
    }
}
