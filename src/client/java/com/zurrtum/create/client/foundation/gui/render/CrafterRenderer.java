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

public class CrafterRenderer extends GuiBlockRenderer<CrafterRenderState> {
    @Override
    protected void renderToTexture(CrafterRenderState state, PoseStack matrices, SubmitNodeCollector queue) {
        Pose pose = matrices.last();
        pose.scale(1, 1, -1);
        pose.rotate(Axis.XP, Radians.DEG_344P5);
        pose.rotate(Axis.YP, Radians.DEG_337P5);
        pose.translate(-0.5f, -0.16f, -0.5f);
        pose.scale(1, -1, 1);
        matrices.pushPose();
        pose = matrices.last();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.rotate(Axis.ZP, Mth.DEG_TO_RAD * getCurrentAngle());
        pose.rotate(Axis.XP, Radians.DEG_90);
        pose.translate(-0.5f, -0.5f, -0.5f);
        CachedBuffers.partial(AllPartialModels.SHAFTLESS_COGWHEEL, Blocks.AIR.defaultBlockState())
            .submit(matrices, queue);
        matrices.popPose();

        matrices.pushPose();
        pose.rotateYCentered(Radians.DEG_180);
        CachedBuffers.block(AllBlocks.MECHANICAL_CRAFTER.defaultBlockState()).submit(matrices, queue);
        matrices.popPose();
    }

    public static float getCurrentAngle() {
        return AnimationTickHolder.getRenderTime() * 4.0f % 360;
    }

    @Override
    protected String getTextureLabel() {
        return "Crafter";
    }

    @Override
    public Class<CrafterRenderState> getRenderStateClass() {
        return CrafterRenderState.class;
    }
}
