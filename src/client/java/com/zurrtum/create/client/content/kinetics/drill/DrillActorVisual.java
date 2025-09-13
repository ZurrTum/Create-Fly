package com.zurrtum.create.client.content.kinetics.drill;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.content.contraptions.render.ActorVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.kinetics.drill.DrillBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;

public class DrillActorVisual extends ActorVisual {
    TransformedInstance drillHead;
    private final Direction facing;

    private double rotation;
    private double previousRotation;

    public DrillActorVisual(VisualizationContext visualizationContext, VirtualRenderWorld contraption, MovementContext context) {
        super(visualizationContext, contraption, context);

        BlockState state = context.state;

        facing = state.get(DrillBlock.FACING);

        drillHead = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.DRILL_HEAD)).createInstance();
    }

    @Override
    public void tick() {
        previousRotation = rotation;

        if (context.disabled || VecHelper.isVecPointingTowards(context.relativeMotion, facing.getOpposite()))
            return;

        float deg = context.getAnimationSpeed();

        rotation += deg / 20;

        rotation %= 360;
    }

    @Override
    public void beginFrame() {
        drillHead.setIdentityTransform().translate(context.localPos).center().rotateToFace(facing.getOpposite()).rotateZDegrees((float) getRotation())
            .uncenter().setChanged();
    }

    protected double getRotation() {
        return AngleHelper.angleLerp(AnimationTickHolder.getPartialTicks(), previousRotation, rotation);
    }

    @Override
    protected void _delete() {
        drillHead.delete();
    }
}
