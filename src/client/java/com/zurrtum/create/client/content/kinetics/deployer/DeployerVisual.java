package com.zurrtum.create.client.content.kinetics.deployer;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.Radians;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

import static com.zurrtum.create.content.kinetics.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock.FACING;

public class DeployerVisual extends ShaftVisual<DeployerBlockEntity> implements SimpleDynamicVisual, SimpleTickableVisual {

    final Direction facing;

    protected final OrientedInstance pole;

    protected OrientedInstance hand;

    PartialModel currentHand;
    float progress;

    public DeployerVisual(VisualizationContext context, DeployerBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        facing = blockState.getValue(FACING);

        pole = instancerProvider().instancer(
            InstanceTypes.ORIENTED,
            Models.chunkPartial(AllPartialModels.DEPLOYER_POLE)
        ).createInstance();

        currentHand = DeployerRenderer.getHandPose(blockEntity);

        hand = instancerProvider().instancer(InstanceTypes.ORIENTED, Models.chunkPartial(currentHand)).createInstance();

        hand.rotationY(Radians.horizontal(facing));
        if (facing == Direction.UP) {
            hand.rotateX(Radians.DEG_270);
        } else if (facing == Direction.DOWN) {
            hand.rotateX(Radians.DEG_90);
        }
        hand.setChanged();
        pole.rotation(hand);
        if (blockState.getValue(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Direction.Axis.Z) {
            pole.rotateZ(Radians.DEG_90);
        }
        pole.setChanged();

        progress = getProgress(partialTick);
        updatePosition();
    }

    @Override
    public void setSectionCollector(SectionCollector sectionCollector) {
        switch (facing) {
            case UP -> setSectionCollector(sectionCollector, 0, -1, 0, 0, 2, 0);
            case DOWN -> setSectionCollector(sectionCollector, 0, -2, 0, 0, 1, 0);
            case NORTH -> setSectionCollector(sectionCollector, 0, 0, -2, 0, 0, 1);
            case SOUTH -> setSectionCollector(sectionCollector, 0, 0, -1, 0, 0, 2);
            case WEST -> setSectionCollector(sectionCollector, -2, 0, 0, 1, 0, 0);
            case EAST -> setSectionCollector(sectionCollector, -1, 0, 0, 2, 0, 0);
        }
    }

    @Override
    public void tick(TickableVisual.Context context) {
        PartialModel handPose = DeployerRenderer.getHandPose(blockEntity);

        if (currentHand != handPose) {
            currentHand = handPose;
            instancerProvider().instancer(InstanceTypes.ORIENTED, Models.chunkPartial(currentHand)).stealInstance(hand);
        }
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        float newProgress = getProgress(ctx.partialTick());

        if (Mth.equal(newProgress, progress)) {
            return;
        }

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
            float f = 1 - (blockEntity.timer - partialTicks * blockEntity.getTimerSpeed()) / 1000.0f;
            if (blockEntity.fistBump) {
                f *= f;
            }
            return f;
        }
        if (blockEntity.state == DeployerBlockEntity.State.RETRACTING) {
            return (blockEntity.timer - partialTicks * blockEntity.getTimerSpeed()) / 1000.0f;
        }
        return 0;
    }

    private void updatePosition() {
        float handLength = currentHand == AllPartialModels.DEPLOYER_HAND_POINTING ? 0 :
            currentHand == AllPartialModels.DEPLOYER_HAND_HOLDING ? 4 / 16.0f : 3 / 16.0f;
        float distance = Math.min(Mth.clamp(progress, 0, 1) * (blockEntity.reach + handLength), 21 / 16.0f);
        Vec3i facingVec = facing.getUnitVec3i();
        BlockPos blockPos = getVisualPosition();

        float x = blockPos.getX() + facingVec.getX() * distance;
        float y = blockPos.getY() + facingVec.getY() * distance;
        float z = blockPos.getZ() + facingVec.getZ() * distance;

        pole.position(x, y, z).setChanged();
        hand.position(x, y, z).setChanged();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(pole);
        consumer.accept(hand);
    }
}
