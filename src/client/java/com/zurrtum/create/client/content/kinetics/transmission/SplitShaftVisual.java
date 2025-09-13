package com.zurrtum.create.client.content.kinetics.transmission;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual;
import com.zurrtum.create.client.content.kinetics.base.RotatingInstance;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.AbstractInstance;
import com.zurrtum.create.client.flywheel.lib.instance.FlatLit;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.transmission.SplitShaftBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.function.Consumer;

public class SplitShaftVisual extends KineticBlockEntityVisual<SplitShaftBlockEntity> {

    protected final ArrayList<RotatingInstance> keys;

    public SplitShaftVisual(VisualizationContext modelManager, SplitShaftBlockEntity blockEntity, float partialTick) {
        super(modelManager, blockEntity, partialTick);

        keys = new ArrayList<>(2);

        float speed = blockEntity.getSpeed();

        for (Direction dir : Iterate.directionsInAxis(rotationAxis())) {

            float splitSpeed = speed * blockEntity.getRotationSpeedModifier(dir);

            var instance = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF)).createInstance();

            instance.setup(blockEntity, splitSpeed).setPosition(getVisualPosition()).rotateToFace(Direction.SOUTH, dir).setChanged();

            keys.add(instance);
        }
    }

    @Override
    public void update(float pt) {
        Block block = blockState.getBlock();
        final Direction.Axis boxAxis = ((IRotate) block).getRotationAxis(blockState);

        Direction[] directions = Iterate.directionsInAxis(boxAxis);

        for (int i : Iterate.zeroAndOne) {
            keys.get(i).setup(blockEntity, blockEntity.getSpeed() * blockEntity.getRotationSpeedModifier(directions[i])).setChanged();
        }
    }

    @Override
    public void updateLight(float partialTick) {
        relight(keys.toArray(FlatLit[]::new));
    }

    @Override
    protected void _delete() {
        keys.forEach(AbstractInstance::delete);
        keys.clear();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        keys.forEach(consumer);
    }
}
