package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;

public class CrushWheelRenderer extends GuiBlockRenderer<CrushWheelRenderState> {
    @Override
    protected void renderToTexture(CrushWheelRenderState state, PoseStack matrices, SubmitNodeCollector queue) {
        Pose pose = matrices.last();
        pose.scale(1, 1, -1);
        pose.rotate(Axis.YP, Radians.DEG_337P5);
        pose.translate(-1.5f, -0.6f, -0.5f);
        pose.scale(1, -1, 1);
        SuperByteBuffer model = CachedBuffers.partial(AllPartialModels.CRUSHING_WHEEL, Blocks.AIR.defaultBlockState());

        float yRadians = Mth.DEG_TO_RAD * getCurrentAngle();
        matrices.pushPose();
        pose = matrices.last();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.rotate(Axis.XP, Radians.DEG_270);
        pose.rotate(Axis.YP, yRadians);
        pose.translate(-0.5f, -0.5f, -0.5f);
        model.submit(matrices, queue);
        matrices.popPose();

        pose = matrices.last();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.translate(2, 0, 0);
        pose.rotate(Axis.XP, Radians.DEG_270);
        pose.rotate(Axis.YP, -yRadians);
        pose.translate(-0.5f, -0.5f, -0.5f);
        model.submit(matrices, queue);
    }

    public static float getCurrentAngle() {
        return AnimationTickHolder.getRenderTime() * 4.0f % 360;
    }

    @Override
    public Class<CrushWheelRenderState> getRenderStateClass() {
        return CrushWheelRenderState.class;
    }

    @Override
    protected String getTextureLabel() {
        return "Crush Wheel";
    }
}
