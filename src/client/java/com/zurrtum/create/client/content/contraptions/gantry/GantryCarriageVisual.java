package com.zurrtum.create.client.content.contraptions.gantry;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.base.ShaftVisual;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.content.contraptions.gantry.GantryCarriageBlock;
import com.zurrtum.create.content.contraptions.gantry.GantryCarriageBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

public class GantryCarriageVisual extends ShaftVisual<GantryCarriageBlockEntity> implements SimpleDynamicVisual {

    private final TransformedInstance gantryCogs;

    final Direction facing;
    final Boolean alongFirst;
    final Direction.Axis rotationAxis;
    final float rotationMult;
    final BlockPos visualPos;

    private float lastAngle = Float.NaN;

    public GantryCarriageVisual(VisualizationContext context, GantryCarriageBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        gantryCogs = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.GANTRY_COGS)).createInstance();

        facing = blockState.get(GantryCarriageBlock.FACING);
        alongFirst = blockState.get(GantryCarriageBlock.AXIS_ALONG_FIRST_COORDINATE);
        rotationAxis = KineticBlockEntityRenderer.getRotationAxisOf(blockEntity);

        rotationMult = getRotationMultiplier(getGantryAxis(), facing);

        visualPos = facing.getDirection() == Direction.AxisDirection.POSITIVE ? blockEntity.getPos() : blockEntity.getPos()
            .offset(facing.getOpposite());

        animateCogs(getCogAngle());
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        float cogAngle = getCogAngle();

        if (MathHelper.approximatelyEquals(cogAngle, lastAngle))
            return;

        animateCogs(cogAngle);
    }

    private float getCogAngle() {
        return GantryCarriageRenderer.getAngleForBE(blockEntity, visualPos, rotationAxis) * rotationMult;
    }

    private void animateCogs(float cogAngle) {
        gantryCogs.setIdentityTransform().translate(getVisualPosition()).center().rotateYDegrees(AngleHelper.horizontalAngle(facing))
            .rotateXDegrees(facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90)
            .rotateYDegrees(alongFirst ^ facing.getAxis() == Direction.Axis.X ? 0 : 90).translate(0, -9 / 16f, 0).rotateXDegrees(-cogAngle)
            .translate(0, 9 / 16f, 0).uncenter().setChanged();
    }

    static float getRotationMultiplier(Direction.Axis gantryAxis, Direction facing) {
        float multiplier = 1;
        if (gantryAxis == Direction.Axis.X)
            if (facing == Direction.UP)
                multiplier *= -1;
        if (gantryAxis == Direction.Axis.Y)
            if (facing == Direction.NORTH || facing == Direction.EAST)
                multiplier *= -1;

        return multiplier;
    }

    private Direction.Axis getGantryAxis() {
        Direction.Axis gantryAxis = Direction.Axis.X;
        for (Direction.Axis axis : Iterate.axes)
            if (axis != rotationAxis && axis != facing.getAxis())
                gantryAxis = axis;
        return gantryAxis;
    }

    @Override
    public void updateLight(float partialTick) {
        relight(gantryCogs, rotatingModel);
    }

    @Override
    protected void _delete() {
        super._delete();
        gantryCogs.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(gantryCogs);
    }
}
