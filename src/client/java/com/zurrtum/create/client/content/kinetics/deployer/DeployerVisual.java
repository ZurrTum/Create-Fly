package com.zurrtum.create.client.content.kinetics.deployer;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.ShaftVisual;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visual.TickableVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.OrientedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleTickableVisual;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity;
import net.minecraft.util.math.*;
import org.joml.Quaternionf;

import java.util.function.Consumer;

import static com.zurrtum.create.content.kinetics.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock.FACING;

public class DeployerVisual extends ShaftVisual<DeployerBlockEntity> implements SimpleDynamicVisual, SimpleTickableVisual {

    final Direction facing;
    final float yRot;
    final float xRot;
    final float zRot;

    protected final OrientedInstance pole;

    protected OrientedInstance hand;

    PartialModel currentHand;
    float progress;

    public DeployerVisual(VisualizationContext context, DeployerBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        facing = blockState.get(FACING);

        boolean rotatePole = blockState.get(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Direction.Axis.Z;

        yRot = AngleHelper.horizontalAngle(facing);
        xRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;
        zRot = rotatePole ? 90 : 0;

        pole = instancerProvider().instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.DEPLOYER_POLE)).createInstance();

        currentHand = DeployerRenderer.getHandPose(blockEntity);

        hand = instancerProvider().instancer(InstanceTypes.ORIENTED, Models.partial(currentHand)).createInstance();

        progress = getProgress(partialTick);
        updateRotation(pole, hand, yRot, xRot, zRot);
        updatePosition();
    }

    @Override
    public void tick(TickableVisual.Context context) {
        PartialModel handPose = DeployerRenderer.getHandPose(blockEntity);

        if (currentHand != handPose) {
            currentHand = handPose;
            instancerProvider().instancer(InstanceTypes.ORIENTED, Models.partial(currentHand)).stealInstance(hand);
        }
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        float newProgress = getProgress(ctx.partialTick());

        if (MathHelper.approximatelyEquals(newProgress, progress))
            return;

        progress = newProgress;

        updatePosition();
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);
        relight(hand, pole);
    }

    @Override
    protected void _delete() {
        super._delete();
        hand.delete();
        pole.delete();
    }

    private float getProgress(float partialTicks) {
        if (blockEntity.state == DeployerBlockEntity.State.EXPANDING) {
            float f = 1 - (blockEntity.timer - partialTicks * blockEntity.getTimerSpeed()) / 1000f;
            if (blockEntity.fistBump)
                f *= f;
            return f;
        }
        if (blockEntity.state == DeployerBlockEntity.State.RETRACTING)
            return (blockEntity.timer - partialTicks * blockEntity.getTimerSpeed()) / 1000f;
        return 0;
    }

    private void updatePosition() {
        float handLength = currentHand == AllPartialModels.DEPLOYER_HAND_POINTING ? 0 : currentHand == AllPartialModels.DEPLOYER_HAND_HOLDING ? 4 / 16f : 3 / 16f;
        float distance = Math.min(MathHelper.clamp(progress, 0, 1) * (blockEntity.reach + handLength), 21 / 16f);
        Vec3i facingVec = facing.getVector();
        BlockPos blockPos = getVisualPosition();

        float x = blockPos.getX() + ((float) facingVec.getX()) * distance;
        float y = blockPos.getY() + ((float) facingVec.getY()) * distance;
        float z = blockPos.getZ() + ((float) facingVec.getZ()) * distance;

        pole.position(x, y, z).setChanged();
        hand.position(x, y, z).setChanged();
    }

    static void updateRotation(OrientedInstance pole, OrientedInstance hand, float yRot, float xRot, float zRot) {

        Quaternionf q = RotationAxis.POSITIVE_Y.rotationDegrees(yRot);
        q.mul(RotationAxis.POSITIVE_X.rotationDegrees(xRot));

        hand.rotation(q).setChanged();

        q.mul(RotationAxis.POSITIVE_Z.rotationDegrees(zRot));

        pole.rotation(q).setChanged();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(pole);
        consumer.accept(hand);
    }
}
