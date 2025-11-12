package com.zurrtum.create.client.content.kinetics.crank;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual;
import com.zurrtum.create.client.content.kinetics.base.RotatingInstance;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;
import com.zurrtum.create.content.kinetics.crank.HandCrankBlockEntity;
import org.joml.Quaternionf;

import java.util.function.Consumer;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class HandCrankVisual extends KineticBlockEntityVisual<HandCrankBlockEntity> implements SimpleDynamicVisual {
    private final RotatingInstance rotatingModel;
    private final TransformedInstance crank;

    public HandCrankVisual(VisualizationContext modelManager, HandCrankBlockEntity blockEntity, float partialTick) {
        super(modelManager, blockEntity, partialTick);

        crank = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.HAND_CRANK_HANDLE)).createInstance();

        rotateCrank(partialTick);

        rotatingModel = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.HAND_CRANK_BASE)).createInstance();

        rotatingModel.setup(HandCrankVisual.this.blockEntity).setPosition(getVisualPosition())
            .rotateToFace(blockState.getValue(BlockStateProperties.FACING)).setChanged();
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        rotateCrank(ctx.partialTick());
    }

    private void rotateCrank(float pt) {
        var facing = blockState.getValue(BlockStateProperties.FACING);
        float angle = HandCrankRenderer.getHandCrankIndependentAngle(blockEntity, pt);

        crank.setIdentityTransform().translate(getVisualPosition()).center()
            .rotate(angle, Direction.get(Direction.AxisDirection.POSITIVE, facing.getAxis()))
            .rotate(new Quaternionf().rotateTo(0, 0, -1, facing.getStepX(), facing.getStepY(), facing.getStepZ())).uncenter().setChanged();
    }

    @Override
    protected void _delete() {
        crank.delete();
        rotatingModel.delete();
    }

    @Override
    public void update(float pt) {
        rotatingModel.setup(blockEntity).setChanged();
    }

    @Override
    public void updateLight(float partialTick) {
        relight(crank, rotatingModel);
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        consumer.accept(crank);
        consumer.accept(rotatingModel);
    }
}
