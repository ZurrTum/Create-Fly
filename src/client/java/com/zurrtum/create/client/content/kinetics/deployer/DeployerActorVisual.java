package com.zurrtum.create.client.content.kinetics.deployer;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.content.contraptions.render.ActorVisual;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.kinetics.base.RotatingInstance;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity.Mode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import static com.zurrtum.create.content.kinetics.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock.FACING;

public class DeployerActorVisual extends ActorVisual {

    Direction facing;
    boolean stationaryTimer;

    TransformedInstance pole;
    TransformedInstance hand;
    RotatingInstance shaft;

    Matrix4fc baseHandTransform;
    Matrix4fc basePoleTransform;

    public DeployerActorVisual(
        VisualizationContext visualizationContext,
        VirtualRenderWorld simulationWorld,
        MovementContext context
    ) {
        super(visualizationContext, simulationWorld, context);
        BlockState state = context.state;
        Mode mode = context.blockEntityData.read("Mode", Mode.CODEC).orElse(Mode.PUNCH);
        PartialModel handPose = DeployerRenderer.getHandPose(mode);

        stationaryTimer = context.data.contains("StationaryTimer");
        facing = state.getValue(FACING);

        boolean rotatePole = state.getValue(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Direction.Axis.Z;
        float yRadians = Radians.horizontal(facing);
        float xRadians = facing == Direction.UP ? Radians.DEG_270 : facing == Direction.DOWN ? Radians.DEG_90 : 0;
        float zRadians = rotatePole ? Radians.DEG_90 : 0;

        pole = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.DEPLOYER_POLE))
            .createInstance();
        hand = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(handPose)).createInstance();

        Direction.Axis axis = KineticBlockEntityVisual.rotationAxis(state);
        shaft = instancerProvider.instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT))
            .createInstance().rotateToFace(axis);

        int blockLight = localBlockLight();

        shaft.setRotationAxis(axis)
            .setRotationOffset(KineticBlockEntityVisual.rotationOffset(state, axis, context.localPos))
            .setPosition(context.localPos).light(blockLight, 0).setChanged();

        pole.translate(context.localPos).center().rotateY(yRadians).rotateX(xRadians).rotateZ(zRadians).uncenter()
            .light(blockLight, 0).setChanged();

        basePoleTransform = new Matrix4f(pole.pose);

        hand.translate(context.localPos).center().rotateY(yRadians).rotateX(xRadians).uncenter().light(blockLight, 0)
            .setChanged();

        baseHandTransform = new Matrix4f(hand.pose);
    }

    @Override
    public void beginFrame() {
        float distance = deploymentDistance();

        pole.setTransform(basePoleTransform).translateZ(distance).setChanged();

        hand.setTransform(baseHandTransform).translateZ(distance).setChanged();
    }

    private float deploymentDistance() {
        double factor;
        if (context.disabled) {
            factor = 0;
        } else if (context.contraption.stalled || context.position == null || context.data.contains("StationaryTimer")) {
            factor = Mth.sin(AnimationTickHolder.getRenderTime() * 0.5f) * 0.25f + 0.25f;
        } else {
            Vec3 center = VecHelper.getCenterOf(BlockPos.containing(context.position));
            double distance = context.position.distanceTo(center);
            double nextDistance = context.position.add(context.motion).distanceTo(center);
            factor = 0.5f - Mth.clamp(Mth.lerp(AnimationTickHolder.getPartialTicks(), distance, nextDistance), 0, 1);
        }
        return (float) factor;
    }

    @Override
    protected void _delete() {
        pole.delete();
        hand.delete();
        shaft.delete();
    }
}
