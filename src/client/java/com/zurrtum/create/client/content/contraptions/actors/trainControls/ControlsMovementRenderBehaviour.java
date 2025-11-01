package com.zurrtum.create.client.content.contraptions.actors.trainControls;

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
import net.minecraft.block.BlockState;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;

import java.util.Collection;

public class ControlsMovementRenderBehaviour implements MovementRenderBehaviour {
    @Override
    public MovementRenderState getRenderState(
        Vec3d camera,
        TextRenderer textRenderer,
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
        Direction facing = blockState.get(ControlsBlock.FACING);
        if (ControlsHandler.getContraption() == entity && ControlsHandler.getControlsPos() != null && ControlsHandler.getControlsPos()
            .equals(context.localPos)) {
            Collection<Integer> pressed = ControlsHandler.currentlyPressed;
            angles.equipAnimation.chase(1, .2f, Chaser.EXP);
            angles.steering.chase((pressed.contains(3) ? 1 : 0) + (pressed.contains(2) ? -1 : 0), 0.2f, Chaser.EXP);
            Direction initialOrientation = cce.getInitialOrientation().rotateYCounterclockwise();
            float f = cce.movingBackwards ^ !facing.equals(initialOrientation) ? -1 : 1;
            angles.speed.chase(Math.min(context.motion.length(), 0.5f) * f, 0.2f, Chaser.EXP);
        } else {
            angles.equipAnimation.chase(0, .2f, Chaser.EXP);
            angles.steering.chase(0, 0, Chaser.EXP);
            angles.speed.chase(0, 0, Chaser.EXP);
        }
        float pt = AnimationTickHolder.getPartialTicks(context.world);
        ControlsMovementRenderState state = new ControlsMovementRenderState(context.localPos);
        state.layer = RenderLayer.getCutoutMipped();
        state.cover = CachedBuffers.partial(AllPartialModels.TRAIN_CONTROLS_COVER, blockState);
        state.lever = CachedBuffers.partial(AllPartialModels.TRAIN_CONTROLS_LEVER, blockState);
        state.yRot = MathHelper.RADIANS_PER_DEGREE * (180 + AngleHelper.horizontalAngle(facing));
        state.light = WorldRenderer.getLightmapCoordinates(renderWorld, context.localPos);
        state.world = context.world;
        state.worldMatrix4f = worldMatrix4f;
        float equipAnimation = angles.equipAnimation.getValue(pt);
        float firstLever = angles.speed.getValue(pt);
        float secondLever = angles.steering.getValue(pt);
        state.offsetY = MathHelper.lerp(equipAnimation * equipAnimation, -0.15f, 0.05f);
        state.firstAngle = MathHelper.RADIANS_PER_DEGREE * (MathHelper.clamp(firstLever * 70 - 25, -45, 45) - 45);
        state.secondAngle = MathHelper.RADIANS_PER_DEGREE * (MathHelper.clamp(secondLever * 15, -45, 45) - 45);
        state.xRot = MathHelper.RADIANS_PER_DEGREE * 45;
        return state;
    }

    public static class ControlsMovementRenderState extends MovementRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public SuperByteBuffer cover;
        public SuperByteBuffer lever;
        public float yRot;
        public int light;
        public World world;
        public Matrix4f worldMatrix4f;
        public float offsetY;
        public float firstAngle;
        public float secondAngle;
        public float xRot;

        public ControlsMovementRenderState(BlockPos pos) {
            super(pos);
        }

        @Override
        public void render(MatrixStack matrices, OrderedRenderCommandQueue queue) {
            queue.submitCustom(matrices, layer, this);
        }

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            cover.center().rotateY(yRot).uncenter().light(light).useLevelLight(world, worldMatrix4f).renderInto(matricesEntry, vertexConsumer);
            lever.center().rotateY(yRot).translate(0, 0.25f, 0.25f).rotateX(firstAngle).translate(0, offsetY, 0).rotateX(xRot).uncenter()
                .translate(0, -0.375f, -0.1875f).light(light).useLevelLight(world, worldMatrix4f).renderInto(matricesEntry, vertexConsumer);
            lever.center().rotateY(yRot).translate(0, 0.25f, 0.25f).rotateX(secondAngle).translate(0, offsetY, 0).rotateX(xRot).uncenter()
                .translate(0.375f, -0.375f, -0.1875f).light(light).useLevelLight(world, worldMatrix4f).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
