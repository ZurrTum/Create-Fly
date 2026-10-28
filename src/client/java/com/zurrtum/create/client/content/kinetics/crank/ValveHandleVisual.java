package com.zurrtum.create.client.content.kinetics.crank;

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
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Quaternionf;

import java.util.function.Consumer;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getDegreesWithoutBeOffset;

public class ValveHandleVisual extends KineticBlockEntityVisual<ValveHandleBlockEntity> implements SimpleDynamicVisual {
    private final TransformedInstance crank;

    public ValveHandleVisual(VisualizationContext modelManager, ValveHandleBlockEntity blockEntity, float partialTick) {
        super(modelManager, blockEntity, partialTick);

        BlockState state = blockEntity.getBlockState();
        DyeColor color = null;
        if (state.getBlock() instanceof ValveHandleBlock vhb) {
            color = vhb.color;
        }

        crank = instancerProvider().instancer(
            InstanceTypes.TRANSFORMED,
            Models.chunkPartial(
                color == null ? AllPartialModels.VALVE_HANDLE : AllPartialModels.DYED_VALVE_HANDLES.get(color))
        ).createInstance();

        rotateCrank(partialTick);
    }

    @Override
    public void beginFrame(Context ctx) {
        rotateCrank(ctx.partialTick());
    }

    private void rotateCrank(float pt) {
        var facing = blockState.getValue(BlockStateProperties.FACING);
        Axis axis = facing.getAxis();
        float angle;
        if (blockEntity.inUse == 0 && blockEntity.source != null && blockEntity.getSpeed() != 0) {
            angle = getDegreesWithoutBeOffset(axis, blockEntity);
        } else {
            angle = ValveHandleRenderer.getValveHandleIndependentAngle(blockEntity, facing, pt);
        }

        crank.setIdentityTransform().translate(getVisualPosition()).center().rotate(Mth.DEG_TO_RAD * angle, axis)
            .rotate(new Quaternionf().rotateTo(0, 1, 0, facing.getStepX(), facing.getStepY(), facing.getStepZ()))
            .uncenter().setChanged();
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
