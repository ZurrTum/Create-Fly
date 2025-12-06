package com.zurrtum.create.client.content.kinetics.crank;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.content.kinetics.crank.ValveHandleBlock;
import com.zurrtum.create.content.kinetics.crank.ValveHandleBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Direction;
import org.joml.Quaternionf;

import java.util.function.Consumer;

public class ValveHandleVisual extends KineticBlockEntityVisual<ValveHandleBlockEntity> implements SimpleDynamicVisual {
    private final TransformedInstance crank;

    public ValveHandleVisual(VisualizationContext modelManager, ValveHandleBlockEntity blockEntity, float partialTick) {
        super(modelManager, blockEntity, partialTick);

        BlockState state = blockEntity.getCachedState();
        DyeColor color = null;
        if (state != null && state.getBlock() instanceof ValveHandleBlock vhb)
            color = vhb.color;

        crank = instancerProvider().instancer(
            InstanceTypes.TRANSFORMED,
            Models.partial(color == null ? AllPartialModels.VALVE_HANDLE : AllPartialModels.DYED_VALVE_HANDLES.get(color))
        ).createInstance();

        rotateCrank(partialTick);
    }

    @Override
    public void beginFrame(Context ctx) {
        rotateCrank(ctx.partialTick());
    }

    private void rotateCrank(float pt) {
        var facing = blockState.get(Properties.FACING);
        float angle = AngleHelper.rad(ValveHandleRenderer.getValveHandleIndependentAngle(blockEntity, pt));

        crank.setIdentityTransform().translate(getVisualPosition()).center()
            .rotate(angle, Direction.get(Direction.AxisDirection.POSITIVE, facing.getAxis()))
            .rotate(new Quaternionf().rotateTo(0, 1, 0, facing.getOffsetX(), facing.getOffsetY(), facing.getOffsetZ())).uncenter().setChanged();
    }

    @Override
    protected void _delete() {
        crank.delete();
    }

    @Override
    public void updateLight(float partialTick) {
        relight(crank);
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        consumer.accept(crank);
    }
}
