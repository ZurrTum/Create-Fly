package com.zurrtum.create.client.content.contraptions.actors.contraptionControls;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.content.contraptions.render.ContraptionMatrices;
import com.zurrtum.create.client.content.redstone.nixieTube.NixieTubeRenderer;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.utility.DyeHelper;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsBlock;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsBlockEntity;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsMovement;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Direction;

import java.util.Random;

public class ContraptionControlsMovementRender implements MovementRenderBehaviour {
    private static final ThreadLocal<Random> RANDOM = ThreadLocal.withInitial(Random::new);

    @Override
    public void renderInContraption(
        MovementContext ctx,
        VirtualRenderWorld renderWorld,
        ContraptionMatrices matrices,
        VertexConsumerProvider buffer
    ) {

        if (!(ctx.temporaryData instanceof ContraptionControlsMovement.ElevatorFloorSelection efs))
            return;
        if (!ctx.state.isOf(AllBlocks.CONTRAPTION_CONTROLS))
            return;

        MinecraftClient mc = MinecraftClient.getInstance();
        Entity cameraEntity = mc.getCameraEntity();
        float playerDistance = (float) (ctx.position == null || cameraEntity == null ? 0 : ctx.position.squaredDistanceTo(cameraEntity.getEyePos()));

        float flicker = RANDOM.get().nextFloat();
        Couple<Integer> couple = DyeHelper.getDyeColors(efs.targetYEqualsSelection ? DyeColor.WHITE : DyeColor.ORANGE);
        int brightColor = couple.getFirst();
        int darkColor = couple.getSecond();
        int flickeringBrightColor = Color.mixColors(brightColor, darkColor, flicker / 4) | 0xFF000000;
        TextRenderer fontRenderer = mc.textRenderer;
        float shadowOffset = .5f;

        String text = efs.currentShortName;
        String description = efs.currentLongName;
        MatrixStack ms = matrices.getViewProjection();
        var msr = TransformStack.of(ms);

        float buttondepth = 0;
        if (ctx.contraption.presentBlockEntities.get(ctx.localPos) instanceof ContraptionControlsBlockEntity cbe)
            buttondepth = -1 / 24f * cbe.button.getValue(AnimationTickHolder.getPartialTicks(renderWorld));

        ms.push();
        msr.translate(ctx.localPos);
        ms.translate(0, buttondepth, 0);
        VertexConsumer vc = buffer.getBuffer(RenderLayer.getSolid());
        CachedBuffers.partialFacing(
            AllPartialModels.CONTRAPTION_CONTROLS_BUTTON,
            ctx.state,
            ctx.state.get(ContraptionControlsBlock.FACING).getOpposite()
        ).light(WorldRenderer.getLightmapCoordinates(renderWorld, ctx.localPos)).useLevelLight(ctx.world, matrices.getWorld()).renderInto(ms, vc);
        ms.pop();

        ms.push();
        msr.translate(ctx.localPos);
        msr.rotateCentered(AngleHelper.rad(AngleHelper.horizontalAngle(ctx.state.get(ContraptionControlsBlock.FACING))), Direction.UP);
        ms.translate(0.275f + 0.125f, 1 + 2 / 16f, 0.5f);
        msr.rotate(AngleHelper.rad(67.5f), Direction.WEST);

        if (!text.isBlank() && playerDistance < 100) {
            int actualWidth = fontRenderer.getWidth(text);
            int width = Math.max(actualWidth, 12);
            float scale = 1 / (5f * (width - .5f));
            float heightCentering = (width - 8f) / 2;

            ms.push();
            ms.translate(0, .15f, buttondepth - .25f);
            ms.scale(scale, -scale, scale);
            ms.translate((float) Math.max(0, width - actualWidth) / 2, heightCentering, 0);
            NixieTubeRenderer.drawInWorldString(fontRenderer, ms, buffer, text, flickeringBrightColor);
            ms.translate(shadowOffset, shadowOffset, -1 / 16f);
            NixieTubeRenderer.drawInWorldString(fontRenderer, ms, buffer, text, Color.mixColors(darkColor, 0, .35f) | 0xFF000000);
            ms.pop();
        }

        if (!description.isBlank() && playerDistance < 20) {
            int actualWidth = fontRenderer.getWidth(description);
            int width = Math.max(actualWidth, 55);
            float scale = 1 / (3f * (width - .5f));
            float heightCentering = (width - 8f) / 2;

            ms.push();
            ms.translate(-.0635f, 0.06f, buttondepth - .25f);
            ms.scale(scale, -scale, scale);
            ms.translate((float) Math.max(0, width - actualWidth) / 2, heightCentering, 0);
            NixieTubeRenderer.drawInWorldString(fontRenderer, ms, buffer, description, flickeringBrightColor);
            ms.pop();
        }

        ms.pop();

    }
}
