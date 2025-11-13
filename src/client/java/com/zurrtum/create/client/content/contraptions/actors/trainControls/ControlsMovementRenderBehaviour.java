package com.zurrtum.create.client.content.contraptions.actors.trainControls;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderState;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.actors.trainControls.ControlsBlock;
import com.zurrtum.create.content.contraptions.actors.trainControls.ControlsMovementBehaviour.LeverAngles;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.Collection;

public class ControlsMovementRenderBehaviour implements MovementRenderBehaviour {
    @Override
    public MovementRenderState getRenderState(
        Vec3 camera,
        Font textRenderer,
        MovementContext context,
        VirtualRenderWorld renderWorld,
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
        if (ControlsHandler.getContraption() == entity && ControlsHandler.getControlsPos() != null && ControlsHandler.getControlsPos()
            .equals(context.localPos)) {
            Collection<Integer> pressed = ControlsHandler.currentlyPressed;
            angles.equipAnimation.chase(1, .2f, Chaser.EXP);
            angles.steering.chase((pressed.contains(3) ? 1 : 0) + (pressed.contains(2) ? -1 : 0), 0.2f, Chaser.EXP);
            Direction initialOrientation = cce.getInitialOrientation().getCounterClockWise();
            float f = cce.movingBackwards ^ !facing.equals(initialOrientation) ? -1 : 1;
            angles.speed.chase(Math.min(context.motion.length(), 0.5f) * f, 0.2f, Chaser.EXP);
        } else {
            angles.equipAnimation.chase(0, .2f, Chaser.EXP);
            angles.steering.chase(0, 0, Chaser.EXP);
            angles.speed.chase(0, 0, Chaser.EXP);
        }
        float pt = AnimationTickHolder.getPartialTicks(context.world);
        ControlsMovementRenderState state = new ControlsMovementRenderState(context.localPos);
        state.layer = RenderType.cutoutMipped();
        state.cover = CachedBuffers.partial(AllPartialModels.TRAIN_CONTROLS_COVER, blockState);
        state.lever = CachedBuffers.partial(AllPartialModels.TRAIN_CONTROLS_LEVER, blockState);
        state.yRot = Mth.DEG_TO_RAD * (180 + AngleHelper.horizontalAngle(facing));
        state.light = LevelRenderer.getLightColor(renderWorld, context.localPos);
        state.world = context.world;
        state.worldMatrix4f = worldMatrix4f;
        float equipAnimation = angles.equipAnimation.getValue(pt);
        float firstLever = angles.speed.getValue(pt);
        float secondLever = angles.steering.getValue(pt);
        state.offsetY = Mth.lerp(equipAnimation * equipAnimation, -0.15f, 0.05f);
        state.firstAngle = Mth.DEG_TO_RAD * (Mth.clamp(firstLever * 70 - 25, -45, 45) - 45);
        state.secondAngle = Mth.DEG_TO_RAD * (Mth.clamp(secondLever * 15, -45, 45) - 45);
        state.xRot = Mth.DEG_TO_RAD * 45;
        return state;
    }

    public static class ControlsMovementRenderState extends MovementRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public SuperByteBuffer cover;
        public SuperByteBuffer lever;
        public float yRot;
        public int light;
        public Level world;
        public Matrix4f worldMatrix4f;
        public float offsetY;
        public float firstAngle;
        public float secondAngle;
        public float xRot;

        public ControlsMovementRenderState(BlockPos pos) {
            super(pos);
        }

        @Override
        public void render(PoseStack matrices, SubmitNodeCollector queue) {
            queue.submitCustomGeometry(matrices, layer, this);
        }

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            cover.center().rotateY(yRot).uncenter().light(light).useLevelLight(world, worldMatrix4f).renderInto(matricesEntry, vertexConsumer);
            lever.center().rotateY(yRot).translate(0, 0.25f, 0.25f).rotateX(firstAngle).translate(0, offsetY, 0).rotateX(xRot).uncenter()
                .translate(0, -0.375f, -0.1875f).light(light).useLevelLight(world, worldMatrix4f).renderInto(matricesEntry, vertexConsumer);
            lever.center().rotateY(yRot).translate(0, 0.25f, 0.25f).rotateX(secondAngle).translate(0, offsetY, 0).rotateX(xRot).uncenter()
                .translate(0.375f, -0.375f, -0.1875f).light(light).useLevelLight(world, worldMatrix4f).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
