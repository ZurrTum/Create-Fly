package com.zurrtum.create.client.content.contraptions.actors.roller;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.contraptions.actors.harvester.HarvesterActorVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.world.phys.Vec3;

public class RollerActorVisual extends HarvesterActorVisual {

    TransformedInstance frame;

    public RollerActorVisual(VisualizationContext visualizationContext, VirtualRenderWorld simulationWorld, MovementContext movementContext) {
        super(visualizationContext, simulationWorld, movementContext);

        frame = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ROLLER_FRAME)).createInstance();
        frame.light(localBlockLight(), 0);
    }

    @Override
    public void beginFrame() {
        harvester.setIdentityTransform().translate(context.localPos).center().rotateYDegrees(horizontalAngle).uncenter().translate(0, -.25, 17 / 16f)
            .rotateXDegrees((float) getRotation()).translate(0, -.5, .5).rotateYDegrees(90).setChanged();

        frame.setIdentityTransform().translate(context.localPos).center().rotateYDegrees(horizontalAngle + 180).uncenter().setChanged();
    }

    @Override
    protected PartialModel getRollingPartial() {
        return AllPartialModels.ROLLER_WHEEL;
    }

    @Override
    protected Vec3 getRotationOffset() {
        return Vec3.ZERO;
    }

    @Override
    protected double getRadius() {
        return 16.5;
    }

    @Override
    protected void _delete() {
        super._delete();

        frame.delete();
    }
}
