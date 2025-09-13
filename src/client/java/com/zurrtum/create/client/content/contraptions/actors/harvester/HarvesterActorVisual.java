package com.zurrtum.create.client.content.contraptions.actors.harvester;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.content.contraptions.render.ActorVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class HarvesterActorVisual extends ActorVisual {
    static float originOffset = 1 / 16f;
    static Vec3d rotOffset = new Vec3d(0.5f, -2 * originOffset + 0.5f, originOffset + 0.5f);

    protected TransformedInstance harvester;
    private final Direction facing;

    protected float horizontalAngle;

    private double rotation;
    private double previousRotation;

    public HarvesterActorVisual(VisualizationContext visualizationContext, VirtualRenderWorld simulationWorld, MovementContext movementContext) {
        super(visualizationContext, simulationWorld, movementContext);

        BlockState state = movementContext.state;

        facing = state.get(Properties.HORIZONTAL_FACING);

        harvester = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(getRollingPartial())).createInstance();

        horizontalAngle = facing.getPositiveHorizontalDegrees() + ((facing.getAxis() == Direction.Axis.X) ? 180 : 0);

        harvester.light(localBlockLight(), 0);
        harvester.setChanged();
    }

    protected PartialModel getRollingPartial() {
        return AllPartialModels.HARVESTER_BLADE;
    }

    protected Vec3d getRotationOffset() {
        return rotOffset;
    }

    protected double getRadius() {
        return 6.5;
    }

    @Override
    public void tick() {
        super.tick();

        previousRotation = rotation;

        if (context.contraption.stalled || context.disabled || VecHelper.isVecPointingTowards(context.relativeMotion, facing.getOpposite()))
            return;

        double arcLength = context.motion.length();

        double radians = arcLength * 16 / getRadius();

        float deg = AngleHelper.deg(radians);

        deg = (float) (((int) (deg * 3000)) / 3000);

        rotation += deg * 1.25;

        rotation %= 360;
    }

    @Override
    public void beginFrame() {
        harvester.setIdentityTransform().translate(context.localPos).center().rotateYDegrees(horizontalAngle).uncenter()
            .translate(getRotationOffset()).rotateXDegrees((float) getRotation()).translateBack(getRotationOffset()).setChanged();
    }

    @Override
    protected void _delete() {
        harvester.delete();
    }

    protected double getRotation() {
        return AngleHelper.angleLerp(AnimationTickHolder.getPartialTicks(), previousRotation, rotation);
    }
}
