package com.zurrtum.create.client.content.kinetics.saw;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual;
import com.zurrtum.create.client.content.kinetics.base.RotatingInstance;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.InstancerProvider;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;
import com.zurrtum.create.content.kinetics.saw.SawBlock;
import com.zurrtum.create.content.kinetics.saw.SawBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

import java.util.function.Consumer;

public class SawVisual extends KineticBlockEntityVisual<SawBlockEntity> {

    protected final RotatingInstance rotatingModel;

    public SawVisual(VisualizationContext context, SawBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);
        rotatingModel = shaft(instancerProvider(), blockState).setup(blockEntity).setPosition(getVisualPosition());
        rotatingModel.setChanged();
    }

    public static RotatingInstance shaft(InstancerProvider instancerProvider, BlockState state) {
        var facing = state.get(Properties.FACING);
        var axis = facing.getAxis();
        // We could change this to return either an Oriented- or SingleAxisRotatingVisual
        if (axis.isHorizontal()) {
            Direction align = facing.getOpposite();
            return instancerProvider.instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF)).createInstance()
                .rotateTo(0, 0, 1, align.getOffsetX(), align.getOffsetY(), align.getOffsetZ());
        } else {
            return instancerProvider.instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT)).createInstance()
                .rotateToFace(state.get(SawBlock.AXIS_ALONG_FIRST_COORDINATE) ? Axis.X : Axis.Z);
        }
    }

    @Override
    public void update(float pt) {
        rotatingModel.setup(blockEntity).setChanged();
    }

    @Override
    public void updateLight(float partialTick) {
        relight(rotatingModel);
    }

    @Override
    protected void _delete() {
        rotatingModel.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        consumer.accept(rotatingModel);
    }
}
