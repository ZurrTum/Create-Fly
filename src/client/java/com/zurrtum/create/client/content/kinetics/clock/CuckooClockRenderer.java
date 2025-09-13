package com.zurrtum.create.client.content.kinetics.clock;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.animation.AnimationBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.animation.CuckooClockAnimationBehaviour;
import com.zurrtum.create.content.kinetics.clock.CuckooClockBlock;
import com.zurrtum.create.content.kinetics.clock.CuckooClockBlockEntity;
import com.zurrtum.create.content.kinetics.clock.CuckooClockBlockEntity.Animation;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class CuckooClockRenderer extends KineticBlockEntityRenderer<CuckooClockBlockEntity> {

    public CuckooClockRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(CuckooClockBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        BlockState blockState = be.getCachedState();
        Direction direction = blockState.get(CuckooClockBlock.HORIZONTAL_FACING);

        VertexConsumer vb = buffer.getBuffer(RenderLayer.getSolid());

        // Render Hands
        SuperByteBuffer hourHand = CachedBuffers.partial(AllPartialModels.CUCKOO_HOUR_HAND, blockState);
        SuperByteBuffer minuteHand = CachedBuffers.partial(AllPartialModels.CUCKOO_MINUTE_HAND, blockState);
        CuckooClockAnimationBehaviour behaviour = (CuckooClockAnimationBehaviour) be.getBehaviour(AnimationBehaviour.TYPE);
        float hourAngle = behaviour.hourHand.getValue(partialTicks);
        float minuteAngle = behaviour.minuteHand.getValue(partialTicks);
        rotateHand(hourHand, hourAngle, direction).light(light).renderInto(ms, vb);
        rotateHand(minuteHand, minuteAngle, direction).light(light).renderInto(ms, vb);

        // Doors
        SuperByteBuffer leftDoor = CachedBuffers.partial(AllPartialModels.CUCKOO_LEFT_DOOR, blockState);
        SuperByteBuffer rightDoor = CachedBuffers.partial(AllPartialModels.CUCKOO_RIGHT_DOOR, blockState);
        float angle = 0;
        float offset = 0;

        if (be.animationType != null) {
            float value = be.animationProgress.getValue(partialTicks);
            int step = be.animationType == Animation.SURPRISE ? 3 : 15;
            for (int phase = 30; phase <= 60; phase += step) {
                float local = value - phase;
                if (local < -step / 3)
                    continue;
                else if (local < 0)
                    angle = MathHelper.lerp(((value - (phase - 5)) / 5), 0, 135);
                else if (local < step / 3)
                    angle = 135;
                else if (local < 2 * step / 3)
                    angle = MathHelper.lerp(((value - (phase + 5)) / 5), 135, 0);

            }
        }

        rotateDoor(leftDoor, angle, true, direction).light(light).renderInto(ms, vb);
        rotateDoor(rightDoor, angle, false, direction).light(light).renderInto(ms, vb);

        // Figure
        if (be.animationType != Animation.NONE) {
            offset = -(angle / 135) * 1 / 2f + 10 / 16f;
            PartialModel partialModel = (be.animationType == Animation.PIG ? AllPartialModels.CUCKOO_PIG : AllPartialModels.CUCKOO_CREEPER);
            SuperByteBuffer figure = CachedBuffers.partial(partialModel, blockState);
            figure.rotateCentered(AngleHelper.rad(AngleHelper.horizontalAngle(direction.rotateYCounterclockwise())), Direction.UP);
            figure.translate(offset, 0, 0);
            figure.light(light).renderInto(ms, vb);
        }

    }

    @Override
    protected SuperByteBuffer getRotatedModel(CuckooClockBlockEntity be, BlockState state) {
        return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, state.get(CuckooClockBlock.HORIZONTAL_FACING).getOpposite());
    }

    private SuperByteBuffer rotateHand(SuperByteBuffer buffer, float angle, Direction facing) {
        float pivotX = 2 / 16f;
        float pivotY = 6 / 16f;
        float pivotZ = 8 / 16f;
        buffer.rotateCentered(AngleHelper.rad(AngleHelper.horizontalAngle(facing.rotateYCounterclockwise())), Direction.UP);
        buffer.translate(pivotX, pivotY, pivotZ);
        buffer.rotate(AngleHelper.rad(angle), Direction.EAST);
        buffer.translate(-pivotX, -pivotY, -pivotZ);
        return buffer;
    }

    private SuperByteBuffer rotateDoor(SuperByteBuffer buffer, float angle, boolean left, Direction facing) {
        float pivotX = 2 / 16f;
        float pivotY = 0;
        float pivotZ = (left ? 6 : 10) / 16f;
        buffer.rotateCentered(AngleHelper.rad(AngleHelper.horizontalAngle(facing.rotateYCounterclockwise())), Direction.UP);
        buffer.translate(pivotX, pivotY, pivotZ);
        buffer.rotate(AngleHelper.rad(angle) * (left ? -1 : 1), Direction.UP);
        buffer.translate(-pivotX, -pivotY, -pivotZ);
        return buffer;
    }

}
