package com.zurrtum.create.client.content.contraptions.actors.contraptionControls;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderState;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.contraptions.render.ClientContraption;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.foundation.utility.DyeHelper;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsBlock;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsBlockEntity;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsMovement;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

import java.util.Random;

public class ContraptionControlsMovementRender implements MovementRenderBehaviour {
    private static final ThreadLocal<Random> RANDOM = ThreadLocal.withInitial(Random::new);

    @Override
    @Nullable
    public MovementRenderState getRenderState(
        Vec3 camera,
        Font textRenderer,
        MovementContext context,
        VirtualRenderWorld renderWorld,
        Pose transform,
        Matrix4f worldMatrix4f
    ) {
        if (!(context.temporaryData instanceof ContraptionControlsMovement.ElevatorFloorSelection efs)) {
            return null;
        }
        BlockState blockState = context.state;
        if (!blockState.is(AllBlocks.CONTRAPTION_CONTROLS)) {
            return null;
        }
        BlockPos pos = context.localPos;
        ContraptionControlsMovementRenderState state = new ContraptionControlsMovementRenderState();
        state.pos = pos;
        state.pose = transform;
        float flicker = RANDOM.get().nextFloat();
        float buttondepth;
        if (ClientContraption.getBlockEntityClientSide(
            context.contraption,
            pos
        ) instanceof ContraptionControlsBlockEntity cbe) {
            buttondepth = -1 / 24.0f * cbe.button.getValue(AnimationTickHolder.getPartialTicks(renderWorld));
        } else {
            buttondepth = 0;
        }
        Direction facing = blockState.getValue(ContraptionControlsBlock.FACING);
        state.button = CachedBuffers.partialFacing(
                AllPartialModels.CONTRAPTION_CONTROLS_BUTTON,
                blockState,
                facing.getOpposite()
            ).transform(transform).translate(pos).translate(0, buttondepth, 0)
            .light(LightCoordsUtil.getLightCoords(renderWorld, pos)).useLevelLight(context.world, worldMatrix4f)
            .extractRenderState();
        String text = efs.currentShortName;
        String description = efs.currentLongName;
        Vec3 position = context.position;
        float playerDistance = (float) (position == null ? 0 : camera.distanceToSqr(position));
        boolean hideText = text.isBlank() || playerDistance > 100;
        boolean hideDescription = description.isBlank() || playerDistance > 20;
        if (hideText && hideDescription) {
            return state;
        }
        state.yRadians = Radians.horizontal(facing);
        Couple<Integer> couple = DyeHelper.getDyeColors(efs.targetYEqualsSelection ? DyeColor.WHITE : DyeColor.ORANGE);
        int brightColor = couple.getFirst();
        int darkColor = couple.getSecond();
        state.color = Color.mixColors(brightColor, darkColor, flicker / 4) | 0xFF000000;
        state.offsetZ = buttondepth - 0.25f;
        if (!hideText) {
            state.shadowColor = Color.mixColors(darkColor, 0, 0.35f) | 0xFF000000;
            int actualWidth = textRenderer.width(text);
            int width = Math.max(actualWidth, 12);
            state.textScale = 1 / (5.0f * (width - 0.5f));
            state.textX = (float) Math.max(0, width - actualWidth) / 2;
            state.textY = (width - 8.0f) / 2;
            state.text = Component.literal(text).getVisualOrderText();
        }
        if (!hideDescription) {
            int actualWidth = textRenderer.width(description);
            int width = Math.max(actualWidth, 55);
            state.descriptionScale = 1 / (3.0f * (width - 0.5f));
            state.descriptionX = (float) Math.max(0, width - actualWidth) / 2;
            state.descriptionY = (width - 8.0f) / 2;
            state.description = Component.literal(description).getVisualOrderText();
        }
        return state;
    }

    public static class ContraptionControlsMovementRenderState implements MovementRenderState {
        public @UnknownNullability SuperByteBufferRenderState button;
        public float yRadians;
        public float offsetZ;
        public @Nullable FormattedCharSequence text;
        public @Nullable FormattedCharSequence description;
        public float textScale;
        public float textX;
        public float textY;
        public float descriptionScale;
        public float descriptionX;
        public float descriptionY;
        public int color;
        public int shadowColor;
        public @UnknownNullability Pose pose;
        public @UnknownNullability BlockPos pos;

        @Override
        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            button.submit(matrices, queue);
            if (text != null || description != null) {
                matrices.pushPose();
                transform(matrices, pose, pos);
                Pose last = matrices.last();
                if (yRadians != 0) {
                    last.rotateYCentered(yRadians);
                }
                last.translate(0.4f, 1.125f, 0.5f);
                last.rotate(Axis.XP, Radians.DEG_292P5);
                if (text != null) {
                    matrices.pushPose();
                    last = matrices.last();
                    last.translate(0, 0.15f, offsetZ);
                    last.scale(textScale, -textScale, textScale);
                    queue.submitText(
                        matrices,
                        textX,
                        textY,
                        text,
                        false,
                        Font.DisplayMode.NORMAL,
                        LightCoordsUtil.FULL_BRIGHT,
                        color,
                        0,
                        0
                    );
                    last.translate(0.5f, 0.5f, -0.0625f);
                    queue.submitText(
                        matrices,
                        textX,
                        textY,
                        text,
                        false,
                        Font.DisplayMode.NORMAL,
                        LightCoordsUtil.FULL_BRIGHT,
                        shadowColor,
                        0,
                        0
                    );
                    matrices.popPose();
                }
                if (description != null) {
                    matrices.pushPose();
                    last = matrices.last();
                    last.translate(-0.0635f, 0.06f, offsetZ);
                    last.scale(descriptionScale, -descriptionScale, descriptionScale);
                    queue.submitText(
                        matrices,
                        descriptionX,
                        descriptionY,
                        description,
                        false,
                        Font.DisplayMode.NORMAL,
                        LightCoordsUtil.FULL_BRIGHT,
                        color,
                        0,
                        0
                    );
                    matrices.popPose();
                }
                matrices.popPose();
            }
        }
    }
}
