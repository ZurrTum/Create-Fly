package com.zurrtum.create.client.content.contraptions.bearing;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.OrientedRotatingVisual;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.OrientedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.content.contraptions.bearing.IBearingBlockEntity;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;

import java.util.function.Consumer;

public class BearingVisual<B extends KineticBlockEntity & IBearingBlockEntity> extends OrientedRotatingVisual<B> implements SimpleDynamicVisual {
    final OrientedInstance topInstance;

    final RotationAxis rotationAxis;
    final Quaternionf blockOrientation;

    public BearingVisual(VisualizationContext context, B blockEntity, float partialTick) {
        super(
            context,
            blockEntity,
            partialTick,
            Direction.SOUTH,
            blockEntity.getCachedState().get(Properties.FACING).getOpposite(),
            Models.partial(AllPartialModels.SHAFT_HALF)
        );

        Direction facing = blockState.get(Properties.FACING);
        rotationAxis = RotationAxis.of(Direction.get(Direction.AxisDirection.POSITIVE, rotationAxis()).getUnitVector());

        blockOrientation = getBlockStateOrientation(facing);

        PartialModel top = blockEntity.isWoodenTop() ? AllPartialModels.BEARING_TOP_WOODEN : AllPartialModels.BEARING_TOP;

        topInstance = instancerProvider().instancer(InstanceTypes.ORIENTED, Models.partial(top)).createInstance();

        topInstance.position(getVisualPosition()).rotation(blockOrientation).setChanged();
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        float interpolatedAngle = blockEntity.getInterpolatedAngle(ctx.partialTick() - 1);
        Quaternionf rot = rotationAxis.rotationDegrees(interpolatedAngle);

        rot.mul(blockOrientation);

        topInstance.rotation(rot).setChanged();
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);
        relight(topInstance);
    }

    @Override
    protected void _delete() {
        super._delete();
        topInstance.delete();
    }

    static Quaternionf getBlockStateOrientation(Direction facing) {
        Quaternionf orientation;

        if (facing.getAxis().isHorizontal()) {
            orientation = RotationAxis.POSITIVE_Y.rotationDegrees(AngleHelper.horizontalAngle(facing.getOpposite()));
        } else {
            orientation = new Quaternionf();
        }

        orientation.mul(RotationAxis.POSITIVE_X.rotationDegrees(-90 - AngleHelper.verticalAngle(facing)));
        return orientation;
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(topInstance);
    }
}
