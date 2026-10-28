package com.zurrtum.create.client.content.kinetics.clock;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.OrientedRotatingVisual;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.InstancerProvider;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.animation.AnimationBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.animation.CuckooClockAnimationBehaviour;
import com.zurrtum.create.content.kinetics.clock.CuckooClockBlockEntity;
import com.zurrtum.create.content.kinetics.clock.CuckooClockBlockEntity.Animation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class CuckooClockVisual extends OrientedRotatingVisual<CuckooClockBlockEntity> implements SimpleDynamicVisual {
    private final TransformedInstance hourHand;
    private final TransformedInstance minuteHand;
    private final TransformedInstance leftDoor;
    private final TransformedInstance rightDoor;
    private @Nullable TransformedInstance figure;
    private final Matrix4f transform;

    public CuckooClockVisual(VisualizationContext context, CuckooClockBlockEntity blockEntity, float partialTick) {
        Direction facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        super(
            context,
            blockEntity,
            partialTick,
            Direction.SOUTH,
            facing.getOpposite(),
            Models.chunkPartial(AllPartialModels.SHAFT_HALF)
        );
        InstancerProvider instancerProvider = instancerProvider();
        hourHand = instancerProvider.instancer(
            InstanceTypes.TRANSFORMED,
            Models.chunkPartial(AllPartialModels.CUCKOO_HOUR_HAND)
        ).createInstance();
        minuteHand = instancerProvider.instancer(
            InstanceTypes.TRANSFORMED,
            Models.chunkPartial(AllPartialModels.CUCKOO_MINUTE_HAND)
        ).createInstance();
        leftDoor = instancerProvider.instancer(
            InstanceTypes.TRANSFORMED,
            Models.chunkPartial(AllPartialModels.CUCKOO_LEFT_DOOR)
        ).createInstance();
        rightDoor = instancerProvider.instancer(
            InstanceTypes.TRANSFORMED,
            Models.chunkPartial(AllPartialModels.CUCKOO_RIGHT_DOOR)
        ).createInstance();
        BlockPos visualPos = getVisualPosition();
        transform = new Matrix4f().translate(visualPos.getX(), visualPos.getY(), visualPos.getZ());
        float yRadians = Radians.horizontal(facing.getCounterClockWise());
        if (yRadians != 0) {
            transform.translate(0.5f, 0.5f, 0.5f);
            transform.rotateY(yRadians);
            transform.translate(-0.5f, -0.5f, -0.5f);
        }
        transformHandModels(partialTick);
        transformFigureModels(partialTick);
    }

    @Override
    public void setSectionCollector(SectionCollector sectionCollector) {
        switch (blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case NORTH -> setSectionCollector(sectionCollector, 0, 0, -1, 0, 0, 0);
            case SOUTH -> setSectionCollector(sectionCollector, 0, 0, 0, 0, 0, 1);
            case WEST -> setSectionCollector(sectionCollector, -1, 0, 0, 0, 0, 0);
            case EAST -> setSectionCollector(sectionCollector, 0, 0, 0, 1, 0, 0);
        }
    }

    private void transformHandModels(float partialTick) {
        hourHand.setTransform(transform);
        minuteHand.setTransform(transform);
        CuckooClockAnimationBehaviour behaviour = (CuckooClockAnimationBehaviour) blockEntity.getBehaviour(
            AnimationBehaviour.TYPE);
        if (behaviour != null) {
            float hourAngle = behaviour.hourHand.getValue(partialTick);
            if (hourAngle != 0) {
                hourHand.translate(0.125f, 0.375f, 0.5f).rotateX(hourAngle).translate(-0.125f, -0.375f, -0.5f);
            }
            float minuteAngle = behaviour.minuteHand.getValue(partialTick);
            if (minuteAngle != 0) {
                minuteHand.translate(0.125f, 0.375f, 0.5f).rotateX(minuteAngle).translate(-0.125f, -0.375f, -0.5f);
            }
        }
        hourHand.setChanged();
        minuteHand.setChanged();
    }

    private void transformFigureModels(float partialTick) {
        rightDoor.setTransform(transform);
        leftDoor.setTransform(transform);
        float doorAngle = CuckooClockRenderer.getDoorAngle(blockEntity, partialTick);
        if (doorAngle != 0) {
            float doorRadians = Mth.DEG_TO_RAD * doorAngle;
            rightDoor.translate(0.125f, 0, 0.625f).rotateY(doorRadians).translate(-0.125f, 0, -0.625f);
            leftDoor.translate(0.125f, 0, 0.375f).rotateY(-doorRadians).translate(-0.125f, 0, -0.375f);
            if (blockEntity.animationType != Animation.NONE) {
                float offset = -(doorAngle / 135) * 0.5f + 0.625f;
                if (offset <= 0.4f) {
                    if (figure == null) {
                        figure = instancerProvider().instancer(
                            InstanceTypes.TRANSFORMED,
                            Models.chunkPartial(
                                blockEntity.animationType == Animation.PIG ? AllPartialModels.CUCKOO_PIG :
                                    AllPartialModels.CUCKOO_CREEPER)
                        ).createInstance();
                    }
                    figure.setTransform(transform).translate(offset, 0, 0).setChanged();
                }
            }
        }
        rightDoor.setChanged();
        leftDoor.setChanged();
    }

    @Override
    public void beginFrame(Context ctx) {
        float partialTick = ctx.partialTick();
        transformHandModels(partialTick);
        if (blockEntity.animationProgress.settled()) {
            if (figure != null) {
                figure.delete();
                figure = null;
            }
        } else {
            transformFigureModels(partialTick);
        }
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);
        relight(hourHand, minuteHand, leftDoor, rightDoor);
    }

    @Override
    protected void _delete() {
        super._delete();
        hourHand.delete();
        minuteHand.delete();
        leftDoor.delete();
        rightDoor.delete();
        if (figure != null) {
            figure.delete();
        }
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(hourHand);
        consumer.accept(minuteHand);
        consumer.accept(leftDoor);
        consumer.accept(rightDoor);
    }
}
