package com.zurrtum.create.client.content.kinetics.deployer;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.content.contraptions.render.ActorVisual;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual;
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
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
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

    public DeployerActorVisual(VisualizationContext visualizationContext, VirtualRenderWorld simulationWorld, MovementContext context) {
        super(visualizationContext, simulationWorld, context);
        BlockState state = context.state;
        Mode mode = context.blockEntityData.get("Mode", Mode.CODEC).orElse(Mode.PUNCH);
        PartialModel handPose = DeployerRenderer.getHandPose(mode);

        stationaryTimer = context.data.contains("StationaryTimer");
        facing = state.get(FACING);

        boolean rotatePole = state.get(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Direction.Axis.Z;
        float yRot = AngleHelper.horizontalAngle(facing);
        float xRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;
        float zRot = rotatePole ? 90 : 0;

        pole = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.DEPLOYER_POLE)).createInstance();
        hand = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(handPose)).createInstance();

        Direction.Axis axis = KineticBlockEntityVisual.rotationAxis(state);
        shaft = instancerProvider.instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT)).createInstance().rotateToFace(axis);

        int blockLight = localBlockLight();

        shaft.setRotationAxis(axis).setRotationOffset(KineticBlockEntityVisual.rotationOffset(state, axis, context.localPos))
            .setPosition(context.localPos).light(blockLight, 0).setChanged();

        pole.translate(context.localPos).center().rotate(yRot * MathHelper.RADIANS_PER_DEGREE, Direction.UP)
            .rotate(xRot * MathHelper.RADIANS_PER_DEGREE, Direction.EAST).rotate(zRot * MathHelper.RADIANS_PER_DEGREE, Direction.SOUTH).uncenter()
            .light(blockLight, 0).setChanged();

        basePoleTransform = new Matrix4f(pole.pose);

        hand.translate(context.localPos).center().rotate(yRot * MathHelper.RADIANS_PER_DEGREE, Direction.UP)
            .rotate(xRot * MathHelper.RADIANS_PER_DEGREE, Direction.EAST).uncenter().light(blockLight, 0).setChanged();

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
            factor = MathHelper.sin(AnimationTickHolder.getRenderTime() * .5f) * .25f + .25f;
        } else {
            Vec3d center = VecHelper.getCenterOf(BlockPos.ofFloored(context.position));
            double distance = context.position.distanceTo(center);
            double nextDistance = context.position.add(context.motion).distanceTo(center);
            factor = .5f - MathHelper.clamp(MathHelper.lerp(AnimationTickHolder.getPartialTicks(), distance, nextDistance), 0, 1);
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
