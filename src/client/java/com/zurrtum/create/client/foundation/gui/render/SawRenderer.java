package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.content.kinetics.saw.SawBlock;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class SawRenderer extends GuiBlockRenderer<SawRenderState> {
    @Override
    protected void renderToTexture(SawRenderState state, PoseStack matrices, SubmitNodeCollector queue) {
        Pose pose = matrices.last();
        pose.scale(1, 1, -1);
        pose.rotate(Axis.XP, Radians.DEG_344P5);
        pose.rotate(Axis.YP, Radians.DEG_112P5);
        pose.translate(-0.5f, -0.2f, -0.5f);
        pose.scale(1, -1, 1);

        matrices.pushPose();
        matrices.rotateXCentered(Mth.DEG_TO_RAD * getCurrentAngle());
        CachedBuffers.block(AllBlocks.SHAFT.defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.X))
            .submit(matrices, queue);
        matrices.popPose();
        CachedBuffers.block(AllBlocks.MECHANICAL_SAW.defaultBlockState().setValue(SawBlock.FACING, Direction.UP))
            .submit(matrices, queue);
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.rotate(Axis.ZP, Radians.DEG_270);
        pose.rotate(Axis.YP, Radians.DEG_270);
        pose.translate(-0.5f, -0.5f, -0.5f);
        CachedBuffers.partial(AllPartialModels.SAW_BLADE_VERTICAL_ACTIVE, Blocks.AIR.defaultBlockState())
            .submit(matrices, queue);
    }

    public static float getCurrentAngle() {
        return -(AnimationTickHolder.getRenderTime() * 4.0f) % 360;
    }

    @Override
    protected String getTextureLabel() {
        return "Saw";
    }

    @Override
    public Class<SawRenderState> getRenderStateClass() {
        return SawRenderState.class;
    }

}
