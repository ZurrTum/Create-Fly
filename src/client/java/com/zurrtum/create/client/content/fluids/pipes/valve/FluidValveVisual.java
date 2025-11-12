package com.zurrtum.create.client.content.fluids.pipes.valve;

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
import com.zurrtum.create.content.fluids.pipes.valve.FluidValveBlock;
import com.zurrtum.create.content.fluids.pipes.valve.FluidValveBlockEntity;

import java.util.function.Consumer;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class FluidValveVisual extends ShaftVisual<FluidValveBlockEntity> implements SimpleDynamicVisual {

    protected TransformedInstance pointer;
    protected boolean settled;

    protected final double xRot;
    protected final double yRot;
    protected final int pointerRotationOffset;

    public FluidValveVisual(VisualizationContext dispatcher, FluidValveBlockEntity blockEntity, float partialTick) {
        super(dispatcher, blockEntity, partialTick);

        Direction facing = blockState.getValue(FluidValveBlock.FACING);

        yRot = AngleHelper.horizontalAngle(facing);
        xRot = facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90;

        Direction.Axis pipeAxis = FluidValveBlock.getPipeAxis(blockState);
        Direction.Axis shaftAxis = KineticBlockEntityRenderer.getRotationAxisOf(blockEntity);

        boolean twist = pipeAxis.isHorizontal() && shaftAxis == Direction.Axis.X || pipeAxis.isVertical();
        pointerRotationOffset = twist ? 90 : 0;
        settled = false;

        pointer = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.FLUID_VALVE_POINTER)).createInstance();

        transformPointer(partialTick);
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        if (blockEntity.pointer.settled() && settled)
            return;

        transformPointer(ctx.partialTick());
    }

    private void transformPointer(float partialTick) {
        float value = blockEntity.pointer.getValue(partialTick);
        float pointerRotation = Mth.lerpInt(value, 0, -90);
        settled = (value == 0 || value == 1) && blockEntity.pointer.settled();

        pointer.setIdentityTransform().translate(getVisualPosition()).center().rotateYDegrees((float) yRot).rotateXDegrees((float) xRot)
            .rotateYDegrees(pointerRotationOffset + pointerRotation).uncenter().setChanged();
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);
        relight(pointer);
    }

    @Override
    protected void _delete() {
        super._delete();
        pointer.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(pointer);
    }
}
