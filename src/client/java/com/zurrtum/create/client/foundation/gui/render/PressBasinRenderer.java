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
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class PressBasinRenderer extends GuiBlockRenderer<PressBasinRenderState> {
    @Override
    protected void renderToTexture(PressBasinRenderState state, PoseStack matrices, SubmitNodeCollector queue) {
        Pose pose = matrices.last();
        pose.scale(1, 1, -1);
        pose.rotate(Axis.XP, Radians.DEG_344P5);
        pose.rotate(Axis.YP, Radians.DEG_22P5);
        pose.translate(-0.5f, -1.8f, -0.5f);
        pose.scale(1, -1, 1);

        float time = AnimationTickHolder.getRenderTime();
        CachedBuffers.block(AllBlocks.MECHANICAL_PRESS.defaultBlockState()).submit(matrices, queue);

        matrices.pushPose();
        matrices.rotateZCentered(Mth.DEG_TO_RAD * getShaftAngle(time));
        CachedBuffers.block(AllBlocks.SHAFT.defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.Z))
            .submit(matrices, queue);
        matrices.popPose();

        matrices.pushPose();
        matrices.translate(0, getAnimatedHeadOffset(time), 0);
        CachedBuffers.partial(AllPartialModels.MECHANICAL_PRESS_HEAD, Blocks.AIR.defaultBlockState())
            .submit(matrices, queue);
        matrices.popPose();

        matrices.translate(0, -1.65f, 0);
        CachedBuffers.block(AllBlocks.BASIN.defaultBlockState()).submit(matrices, queue);
    }

    private static float getShaftAngle(float time) {
        return time * 4.0f % 360;
    }

    private static float getAnimatedHeadOffset(float time) {
        float cycle = time % 30;
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
    protected String getTextureLabel() {
        return "Press Basin";
    }

    @Override
    public Class<PressBasinRenderState> getRenderStateClass() {
        return PressBasinRenderState.class;
    }
}