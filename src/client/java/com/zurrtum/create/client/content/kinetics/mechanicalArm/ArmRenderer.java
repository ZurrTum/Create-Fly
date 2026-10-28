package com.zurrtum.create.client.content.kinetics.mechanicalArm;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.kinetics.mechanicalArm.ArmRenderer.ArmRenderState;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlock;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity.Phase;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.*;

public class ArmRenderer implements BlockEntityRenderer<ArmBlockEntity, ArmRenderState> {
    protected ItemModelResolver itemModelManager;

    public ArmRenderer(Context context) {
        itemModelManager = context.itemModelResolver();
    }

    @Override
    public ArmRenderState createRenderState() {
        return new ArmRenderState();
    }

    @Override
    public void extractRenderState(
        ArmBlockEntity be,
        ArmRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = be.getLevel();
        ItemStack heldItem = be.heldItem;
        if (VisualizationManager.supportsVisualization(level)) {
            if (heldItem.isEmpty()) {
                return;
            }
            SmartBlockEntityRenderer.extractBase(level, be, state, crumblingOverlay);
            ArmItemData item = state.item = new ArmItemData();
            ItemStackRenderState renderState = new ItemStackRenderState();
            renderState.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.appendItemLayers(renderState, heldItem, renderState.displayContext, level, null, 0);
            item.state = renderState;
            if (heldItem.getItem() instanceof BlockItem && renderState.usesBlockLight()) {
                item.offset = -0.5625f;
                item.scale = 0.5f;
            } else {
                item.offset = -0.625f;
                item.scale = 0.625f;
            }
            state.rotate = state.blockState.getValue(ArmBlock.CEILING);
            if (be.phase != Phase.DANCING || be.getSpeed() == 0) {
                state.baseAngle = be.baseAngle.getValue(tickProgress);
                state.lowerArmAngle = be.lowerArmAngle.getValue(tickProgress);
                state.upperArmAngle = be.upperArmAngle.getValue(tickProgress) - 180;
                state.headAngle = be.headAngle.getValue(tickProgress) - 45;
            } else {
                float renderTick = AnimationTickHolder.getRenderTime(level) + be.hashCode() % 64;
                state.baseAngle = renderTick * 10 % 360;
                float lowerArmAngle = Mth.lerp((Mth.sin(renderTick / 4) + 1) / 2, -45, 15);
                state.lowerArmAngle = lowerArmAngle + 135;
                state.upperArmAngle = Mth.lerp((Mth.sin(renderTick / 8) + 1) / 4, -45, 95) - 90;
                state.headAngle = -lowerArmAngle - 45;
            }
            return;
        }
        SmartBlockEntityRenderer.extractBase(level, be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        state.cog = CachedBuffers.partial(AllPartialModels.ARM_COG, state.blockState).cardinalLighting(cardinalLighting)
            .light(state.lightCoords).color(getTintColor(be)).extractRenderState();
        float time = AnimationTickHolder.getRenderTime(level);
        Direction.Axis axis = getRotationAxisOf(state.blockState);
        float offset = KineticBlockEntityVisual.rotationOffset(state.blockState, axis, state.blockPos);
        float speed = be.getSpeed();
        float progress = getProgress(speed, time);
        state.axis = axis;
        state.angle = (progress + offset) % 360;
        ArmRenderData arm = state.arm = new ArmRenderData();
        if (heldItem.isEmpty()) {
            arm.clawOffset = 0.0625f;
        } else {
            ArmItemData item = state.item = new ArmItemData();
            ItemStackRenderState renderState = new ItemStackRenderState();
            renderState.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.appendItemLayers(renderState, heldItem, renderState.displayContext, level, null, 0);
            item.state = renderState;
            if (heldItem.getItem() instanceof BlockItem && renderState.usesBlockLight()) {
                item.offset = -0.5625f;
                item.scale = 0.5f;
                arm.clawOffset = 0.1875f;
            } else {
                item.offset = -0.625f;
                item.scale = 0.625f;
                arm.clawOffset = 0.078125f;
            }
        }
        if (state.blockState.getValue(ArmBlock.CEILING)) {
            state.rotate = true;
            arm.inverted = be.goggles;
        }
        SuperByteBuffer lower = CachedBuffers.partial(AllPartialModels.ARM_LOWER_BODY, state.blockState);
        SuperByteBuffer upper = CachedBuffers.partial(AllPartialModels.ARM_UPPER_BODY, state.blockState);
        if (be.phase != Phase.DANCING || speed == 0) {
            state.baseAngle = be.baseAngle.getValue(tickProgress);
            state.lowerArmAngle = be.lowerArmAngle.getValue(tickProgress);
            state.upperArmAngle = be.upperArmAngle.getValue(tickProgress) - 180;
            state.headAngle = be.headAngle.getValue(tickProgress) - 45;
        } else {
            Color color = Color.rainbowColor(AnimationTickHolder.getTicks() * 100);
            lower.color(color);
            upper.color(color);
            float renderTick = time + be.hashCode() % 64;
            state.baseAngle = renderTick * 10 % 360;
            float lowerArmAngle = Mth.lerp((Mth.sin(renderTick / 4) + 1) / 2, -45, 15);
            state.lowerArmAngle = lowerArmAngle + 135;
            state.upperArmAngle = Mth.lerp((Mth.sin(renderTick / 8) + 1) / 4, -45, 95) - 90;
            state.headAngle = -lowerArmAngle - 45;
        }
        arm.base = CachedBuffers.partial(AllPartialModels.ARM_BASE, state.blockState).cardinalLighting(cardinalLighting)
            .light(state.lightCoords).extractRenderState();
        arm.lower = lower.cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        arm.upper = upper.cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        arm.claw = CachedBuffers.partial(
            be.goggles ? AllPartialModels.ARM_CLAW_BASE_GOGGLES : AllPartialModels.ARM_CLAW_BASE,
            state.blockState
        ).cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        arm.clawUpper = CachedBuffers.partial(AllPartialModels.ARM_CLAW_GRIP_UPPER, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        arm.clawLower = CachedBuffers.partial(AllPartialModels.ARM_CLAW_GRIP_LOWER, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
    }

    @Override
    public void submit(
        ArmRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        Pose pose = matrices.last();
        if (state.cog == null) {
            pose.translate(0.5f, 0.5f, 0.5f);
            if (state.rotate) {
                pose.rotate(Axis.XP, Radians.DEG_180);
            }
            pose.translate(0, 0.25f, 0);
            if (state.baseAngle != 0) {
                pose.rotate(Axis.YP, Mth.DEG_TO_RAD * state.baseAngle);
            }
            pose.translate(0, 0.125f, 0);
            if (state.lowerArmAngle != 0) {
                pose.rotate(Axis.XP, Mth.DEG_TO_RAD * state.lowerArmAngle);
            }
            pose.translate(0, 0, -0.875f);
            if (state.upperArmAngle != 0) {
                pose.rotate(Axis.XP, Mth.DEG_TO_RAD * state.upperArmAngle);
            }
            pose.translate(0, 0, -0.9375f);
            if (state.headAngle != 0) {
                pose.rotate(Axis.XP, Mth.DEG_TO_RAD * state.headAngle);
            }
            state.item.submit(matrices, queue, state.lightCoords);
            return;
        }
        if (state.angle != 0) {
            matrices.pushPose();
            matrices.rotateCentered(state.axis, Mth.DEG_TO_RAD * state.angle);
            state.cog.submit(matrices, queue);
            matrices.popPose();
        } else {
            state.cog.submit(matrices, queue);
        }
        pose.translate(0.5f, 0.5f, 0.5f);
        if (state.rotate) {
            pose.rotate(Axis.XP, Radians.DEG_180);
        }
        pose.translate(0, 0.25f, 0);
        if (state.baseAngle != 0) {
            pose.rotate(Axis.YP, Mth.DEG_TO_RAD * state.baseAngle);
        }
        ArmRenderData arm = state.arm;
        arm.base.submit(matrices, queue);
        pose.translate(0, 0.125f, 0);
        if (state.lowerArmAngle != 0) {
            pose.rotate(Axis.XP, Mth.DEG_TO_RAD * state.lowerArmAngle);
        }
        arm.lower.submit(matrices, queue);
        pose.translate(0, 0, -0.875f);
        if (state.upperArmAngle != 0) {
            pose.rotate(Axis.XP, Mth.DEG_TO_RAD * state.upperArmAngle);
        }
        arm.upper.submit(matrices, queue);
        pose.translate(0, 0, -0.9375f);
        if (state.headAngle != 0) {
            pose.rotate(Axis.XP, Mth.DEG_TO_RAD * state.headAngle);
        }
        if (arm.inverted) {
            pose.rotate(Axis.ZP, Radians.DEG_180);
            arm.claw.submit(matrices, queue);
            pose.rotate(Axis.ZP, Radians.DEG_180);
        } else {
            arm.claw.submit(matrices, queue);
        }
        matrices.pushPose();
        matrices.translate(0, -state.arm.clawOffset, -0.375f);
        arm.clawLower.submit(matrices, queue);
        matrices.popPose();
        matrices.pushPose();
        matrices.translate(0, state.arm.clawOffset, -0.375f);
        arm.clawUpper.submit(matrices, queue);
        matrices.popPose();
        if (state.item != null) {
            state.item.submit(matrices, queue, state.lightCoords);
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    public static class ArmRenderState extends BlockEntityRenderState {
        public @Nullable SuperByteBufferRenderState cog;
        public Direction.@UnknownNullability Axis axis;
        public float angle;
        public boolean rotate;
        public float baseAngle;
        public float lowerArmAngle;
        public float upperArmAngle;
        public float headAngle;
        public @UnknownNullability ArmRenderData arm;
        public @Nullable ArmItemData item;
    }

    public static class ArmItemData {
        public @UnknownNullability ItemStackRenderState state;
        public float offset;
        public float scale;

        public void submit(PoseStack matrices, SubmitNodeCollector queue, int light) {
            Pose pose = matrices.last();
            pose.rotate(Axis.XP, Radians.DEG_90);
            pose.translate(0, offset, 0);
            pose.scale(scale, scale, scale);
            state.submit(matrices, queue, light, OverlayTexture.NO_OVERLAY, 0);
        }
    }

    public static class ArmRenderData {
        public @UnknownNullability SuperByteBufferRenderState base;
        public @UnknownNullability SuperByteBufferRenderState lower;
        public @UnknownNullability SuperByteBufferRenderState upper;
        public @UnknownNullability SuperByteBufferRenderState claw;
        public @UnknownNullability SuperByteBufferRenderState clawUpper;
        public @UnknownNullability SuperByteBufferRenderState clawLower;
        public boolean inverted;
        public float clawOffset;
    }
}
