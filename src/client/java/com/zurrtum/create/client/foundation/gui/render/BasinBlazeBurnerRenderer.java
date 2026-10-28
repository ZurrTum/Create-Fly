package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;

public class BasinBlazeBurnerRenderer extends GuiBlockRenderer<BasinBlazeBurnerRenderState> {
    @Override
    protected void renderToTexture(BasinBlazeBurnerRenderState state, PoseStack matrices, SubmitNodeCollector queue) {
        Minecraft mc = Minecraft.getInstance();
        Pose pose = matrices.last();
        pose.scale(1, 1, -1);
        pose.rotate(Axis.XP, Radians.DEG_344P5);
        pose.rotate(Axis.YP, Radians.DEG_22P5);
        pose.translate(-0.5f, -0.5f, -0.5f);
        pose.scale(1, -1, 1);
        float offset = -(Mth.sin(AnimationTickHolder.getRenderTime() / 16.0f) + 0.5f) / 16.0f;
        CachedBuffers.block(AllBlocks.BLAZE_BURNER.defaultBlockState()).submit(matrices, queue);

        matrices.pushPose();
        pose = matrices.last();
        pose.rotateYCentered(Radians.DEG_180);
        boolean seething = state.heat() == HeatLevel.SEETHING;
        CachedBuffers.partial(
            seething ? AllPartialModels.BLAZE_SUPER : AllPartialModels.BLAZE_ACTIVE,
            Blocks.AIR.defaultBlockState()
        ).submit(matrices, queue);
        pose.translate(0, offset, 0);
        CachedBuffers.partial(
            seething ? AllPartialModels.BLAZE_BURNER_SUPER_RODS_2 : AllPartialModels.BLAZE_BURNER_RODS_2,
            Blocks.AIR.defaultBlockState()
        ).submit(matrices, queue);
        matrices.popPose();

        SpriteShiftEntry spriteShift = seething ? AllSpriteShifts.SUPER_BURNER_FLAME : AllSpriteShifts.BURNER_FLAME;

        float spriteWidth = spriteShift.getTarget().getU1() - spriteShift.getTarget().getU0();

        float spriteHeight = spriteShift.getTarget().getV1() - spriteShift.getTarget().getV0();

        float time = AnimationTickHolder.getRenderTime(mc.level);
        float speed = 1 / 32.0f + 1 / 64.0f * state.heat().ordinal();

        float progress = speed * time;
        double vScroll = progress - Math.floor(progress);
        vScroll = vScroll * spriteHeight / 2;

        double uScroll = progress / 2;
        uScroll = uScroll - Math.floor(uScroll);
        uScroll = uScroll * spriteWidth / 2;

        CachedBuffers.partial(AllPartialModels.BLAZE_BURNER_FLAME, Blocks.AIR.defaultBlockState())
            .shiftUVScrolling(spriteShift, (float) uScroll, (float) vScroll).submit(matrices, queue);
    }

    @Override
    protected String getTextureLabel() {
        return "Blaze Burner";
    }

    @Override
    public Class<BasinBlazeBurnerRenderState> getRenderStateClass() {
        return BasinBlazeBurnerRenderState.class;
    }
}
