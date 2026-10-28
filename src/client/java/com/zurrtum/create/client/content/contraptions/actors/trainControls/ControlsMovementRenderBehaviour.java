package com.zurrtum.create.client.content.contraptions.actors.trainControls;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderState;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.actors.trainControls.ControlsBlock;
import com.zurrtum.create.content.contraptions.actors.trainControls.ControlsMovementBehaviour.LeverAngles;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

public class ControlsMovementRenderBehaviour implements MovementRenderBehaviour {
    @Override
    @Nullable
    public MovementRenderState getRenderState(
        Vec3 camera,
        Font textRenderer,
        MovementContext context,
        VirtualRenderWorld renderWorld,
        PoseStack.Pose transform,
        Matrix4f worldMatrix4f
    ) {
        if (!(context.temporaryData instanceof LeverAngles angles)) {
            return null;
        }
        AbstractContraptionEntity entity = context.contraption.entity;
        if (!(entity instanceof CarriageContraptionEntity cce)) {
            return null;
        }
        BlockState blockState = context.state;
        Direction facing = blockState.getValue(ControlsBlock.FACING);
        BlockPos pos = context.localPos;
        if (ControlsHandler.getContraption() == entity && ControlsHandler.getControlsPos() != null && ControlsHandler.getControlsPos()
            .equals(pos)) {
            Collection<Integer> pressed = ControlsHandler.currentlyPressed;
            angles.equipAnimation.chase(1, 0.2f, Chaser.EXP);
            angles.steering.chase((pressed.contains(3) ? 1 : 0) + (pressed.contains(2) ? -1 : 0), 0.2f, Chaser.EXP);
            Direction initialOrientation = cce.getInitialOrientation().getCounterClockWise();
            float f = cce.movingBackwards ^ !facing.equals(initialOrientation) ? -1 : 1;
            angles.speed.chase(Math.min(context.motion.length(), 0.5f) * f, 0.2f, Chaser.EXP);
        } else {
            angles.equipAnimation.chase(0, 0.2f, Chaser.EXP);
            angles.steering.chase(0, 0, Chaser.EXP);
            angles.speed.chase(0, 0, Chaser.EXP);
        }
        float pt = AnimationTickHolder.getPartialTicks(context.world);
        ControlsMovementRenderState state = new ControlsMovementRenderState();
        int light = LightCoordsUtil.getLightCoords(renderWorld, pos);
        float yRadians = Radians.horizontalPlus180(facing);
        float equipAnimation = angles.equipAnimation.getValue(pt);
        float firstLever = angles.speed.getValue(pt);
        float secondLever = angles.steering.getValue(pt);
        float offsetY = Mth.lerp(equipAnimation * equipAnimation, -0.15f, 0.05f);
        float firstAngle = Mth.DEG_TO_RAD * (Mth.clamp(firstLever * 70 - 25, -45, 45) - 45);
        float secondAngle = Mth.DEG_TO_RAD * (Mth.clamp(secondLever * 15, -45, 45) - 45);
        SuperByteBuffer cover = CachedBuffers.partial(AllPartialModels.TRAIN_CONTROLS_COVER, blockState)
            .transform(transform).translate(pos).center().rotateY(yRadians);
        SuperByteBuffer lever = CachedBuffers.partial(AllPartialModels.TRAIN_CONTROLS_LEVER, blockState);
        SuperByteBuffer.copyTransform(cover, lever);
        state.firstLever = lever.translate(0, 0.25f, 0.25f).rotateX(firstAngle).translate(0, offsetY, 0)
            .rotateX(Radians.DEG_45).uncenter().translate(0, -0.375f, -0.1875f).light(light)
            .useLevelLight(context.world, worldMatrix4f).extractRenderState();
        SuperByteBuffer.copyTransform(cover, lever);
        state.secondLever = lever.translate(0, 0.25f, 0.25f).rotateX(secondAngle).translate(0, offsetY, 0)
            .rotateX(Radians.DEG_45).uncenter().translate(0.375f, -0.375f, -0.1875f).light(light)
            .useLevelLight(context.world, worldMatrix4f).extractRenderState();
        state.cover = cover.uncenter().light(light).useLevelLight(context.world, worldMatrix4f).extractRenderState();
        return state;
    }

    public static class ControlsMovementRenderState implements MovementRenderState {
        public @UnknownNullability SuperByteBufferRenderState cover;
        public @UnknownNullability SuperByteBufferRenderState firstLever;
        public @UnknownNullability SuperByteBufferRenderState secondLever;

        @Override
        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            cover.submit(matrices, queue);
            firstLever.submit(matrices, queue);
            secondLever.submit(matrices, queue);
        }
    }
}
