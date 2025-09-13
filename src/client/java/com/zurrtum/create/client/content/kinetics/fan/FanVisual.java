package com.zurrtum.create.client.content.kinetics.fan;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual;
import com.zurrtum.create.client.content.kinetics.base.RotatingInstance;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;
import com.zurrtum.create.content.kinetics.fan.EncasedFanBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

import static net.minecraft.state.property.Properties.FACING;

public class FanVisual extends KineticBlockEntityVisual<EncasedFanBlockEntity> {

    protected final RotatingInstance shaft;
    protected final RotatingInstance fan;
    final Direction direction;
    private final Direction opposite;

    public FanVisual(VisualizationContext context, EncasedFanBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        direction = blockState.get(FACING);

        opposite = direction.getOpposite();
        shaft = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF)).createInstance();
        fan = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.ENCASED_FAN_INNER)).createInstance();

        shaft.setup(blockEntity).setPosition(getVisualPosition()).rotateToFace(Direction.SOUTH, opposite).setChanged();

        fan.setup(blockEntity, getFanSpeed()).setPosition(getVisualPosition()).rotateToFace(Direction.SOUTH, opposite).setChanged();
    }

    private float getFanSpeed() {
        float speed = blockEntity.getSpeed() * 5;
        if (speed > 0)
            speed = MathHelper.clamp(speed, 80, 64 * 20);
        if (speed < 0)
            speed = MathHelper.clamp(speed, -64 * 20, -80);
        return speed;
    }

    @Override
    public void update(float pt) {
        shaft.setup(blockEntity).setChanged();
        fan.setup(blockEntity, getFanSpeed()).setChanged();
    }

    @Override
    public void updateLight(float partialTick) {
        BlockPos behind = pos.offset(opposite);
        relight(behind, shaft);

        BlockPos inFront = pos.offset(direction);
        relight(inFront, fan);
    }

    @Override
    protected void _delete() {
        shaft.delete();
        fan.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        consumer.accept(shaft);
        consumer.accept(fan);
    }
}
