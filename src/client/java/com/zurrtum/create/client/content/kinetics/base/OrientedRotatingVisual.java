package com.zurrtum.create.client.content.kinetics.base;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllPartialModels.GantryShaftKey;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.api.visual.BlockEntityVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.gantry.GantryShaftBlock;
import com.zurrtum.create.content.kinetics.gantry.GantryShaftBlock.Part;
import com.zurrtum.create.content.kinetics.gantry.GantryShaftBlockEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.AxisDirection;

import java.util.function.Consumer;

public class OrientedRotatingVisual<T extends KineticBlockEntity> extends KineticBlockEntityVisual<T> {
    protected final RotatingInstance rotatingModel;

    /**
     * @param from  The source model orientation to rotate away from.
     * @param to    The orientation to rotate to.
     * @param model The model to spin.
     */
    public OrientedRotatingVisual(VisualizationContext context, T blockEntity, float partialTick, Direction from, Direction to, Model model) {
        super(context, blockEntity, partialTick);

        rotatingModel = instancerProvider().instancer(AllInstanceTypes.ROTATING, model).createInstance().rotateToFace(from, to).setup(blockEntity)
            .setPosition(getVisualPosition());

        rotatingModel.setChanged();
    }

    public static <T extends KineticBlockEntity> SimpleBlockEntityVisualizer.Factory<T> of(PartialModel partial) {
        return (context, blockEntity, partialTick) -> {
            Direction facing = blockEntity.getCachedState().get(Properties.FACING);
            return new OrientedRotatingVisual<>(context, blockEntity, partialTick, Direction.SOUTH, facing, Models.partial(partial));
        };
    }

    public static <T extends KineticBlockEntity> SimpleBlockEntityVisualizer.Factory<T> backHorizontal(PartialModel partial) {
        return (context, blockEntity, partialTick) -> {
            Direction facing = blockEntity.getCachedState().get(Properties.HORIZONTAL_FACING).getOpposite();
            return new OrientedRotatingVisual<>(context, blockEntity, partialTick, Direction.SOUTH, facing, Models.partial(partial));
        };
    }

    public static BlockEntityVisual<? super GantryShaftBlockEntity> gantryShaft(
        VisualizationContext visualizationContext,
        GantryShaftBlockEntity gantryShaftBlockEntity,
        float partialTick
    ) {
        var blockState = gantryShaftBlockEntity.getCachedState();

        Part part = blockState.get(GantryShaftBlock.PART);

        boolean isPowered = blockState.get(GantryShaftBlock.POWERED);
        boolean isFlipped = blockState.get(GantryShaftBlock.FACING).getDirection() == AxisDirection.NEGATIVE;

        var model = Models.partial(AllPartialModels.GANTRY_SHAFTS.get(new GantryShaftKey(part, isPowered, isFlipped)));

        return new OrientedRotatingVisual<>(
            visualizationContext,
            gantryShaftBlockEntity,
            partialTick,
            Direction.UP,
            blockState.get(GantryShaftBlock.FACING),
            model
        );
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
