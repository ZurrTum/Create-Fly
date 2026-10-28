package com.zurrtum.create.client.content.contraptions.bearing;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.content.contraptions.render.ActorVisual;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual;
import com.zurrtum.create.client.content.kinetics.base.RotatingInstance;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.OrientedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Quaternionf;
import org.joml.Vector3fc;

public class StabilizedBearingVisual extends ActorVisual {

    final OrientedInstance topInstance;
    final RotatingInstance shaft;

    final Direction facing;
    final Vector3fc rotationAxis;
    final Quaternionf blockOrientation;

    public StabilizedBearingVisual(
        VisualizationContext visualizationContext,
        VirtualRenderWorld simulationWorld,
        MovementContext movementContext
    ) {
        super(visualizationContext, simulationWorld, movementContext);

        BlockState blockState = movementContext.state;

        facing = blockState.getValue(BlockStateProperties.FACING);
        rotationAxis = facing.getAxis().getPositive().getUnitVec3f();

        blockOrientation = BearingVisual.getBlockStateOrientation(facing);

        topInstance = instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.BEARING_TOP))
            .createInstance();

        int blockLight = localBlockLight();
        topInstance.position(movementContext.localPos).rotation(blockOrientation).light(blockLight, 0).setChanged();

        shaft = instancerProvider.instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF))
            .createInstance();

        // not rotating so no need to set speed.
        var axis = KineticBlockEntityVisual.rotationAxis(blockState);
        shaft.setRotationAxis(axis)
            .setRotationOffset(KineticBlockEntityVisual.rotationOffset(blockState, axis, movementContext.localPos))
            .setPosition(movementContext.localPos)
            .rotateToFace(Direction.SOUTH, blockState.getValue(BlockStateProperties.FACING).getOpposite())
            .light(blockLight, 0).setChanged();
    }

    @Override
    public void beginFrame() {
        float counterRotationAngle = StabilizedBearingMovementRenderBehaviour.getCounterRotationAngle(
            context,
            facing,
            AnimationTickHolder.getPartialTicks()
        );
        topInstance.rotation(rotationAxis, Mth.DEG_TO_RAD * counterRotationAngle).rotate(blockOrientation).setChanged();
    }

    @Override
    protected void _delete() {
        topInstance.delete();
        shaft.delete();
    }
}