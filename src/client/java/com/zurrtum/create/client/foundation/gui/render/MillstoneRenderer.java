package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;

public class MillstoneRenderer extends GuiBlockRenderer<MillstoneRenderState> {
    @Override
    protected void renderToTexture(MillstoneRenderState state, PoseStack matrices, SubmitNodeCollector queue) {
        Pose pose = matrices.last();
        pose.translate(-0.5f, -0.21f, -0.5f);
        pose.scale(1, -1, -1);
        matrices.pushPose();
        pose = matrices.last();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.rotate(Axis.XP, Radians.DEG_22P5);
        pose.rotate(Axis.YP, Mth.DEG_TO_RAD * getCurrentAngle());
        pose.translate(-0.5f, -0.5f, -0.5f);
        CachedBuffers.partial(AllPartialModels.MILLSTONE_COG, Blocks.AIR.defaultBlockState()).submit(matrices, queue);
        matrices.popPose();

        pose = matrices.last();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.rotate(Axis.XP, Radians.DEG_22P5);
        pose.rotate(Axis.YP, Radians.DEG_22P5);
        pose.translate(-0.5f, -0.5f, -0.5f);
        CachedBuffers.block(AllBlocks.MILLSTONE.defaultBlockState()).submit(matrices, queue);
    }

    private static float getCurrentAngle() {
        return AnimationTickHolder.getRenderTime() * 4.0f % 360 * 2;
    }

    @Override
    protected String getTextureLabel() {
        return "Millstone";
    }

    @Override
    public Class<MillstoneRenderState> getRenderStateClass() {
        return MillstoneRenderState.class;
    }
}
