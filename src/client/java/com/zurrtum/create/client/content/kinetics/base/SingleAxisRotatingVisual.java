package com.zurrtum.create.client.content.kinetics.base;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.equipment.armor.BacktankRenderer;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleTickableVisual;
import com.zurrtum.create.client.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.Direction;

import java.util.function.Consumer;

public class SingleAxisRotatingVisual<T extends KineticBlockEntity> extends KineticBlockEntityVisual<T> implements SimpleTickableVisual {
    public static boolean rainbowMode = false;

    protected final RotatingInstance rotatingModel;

    public SingleAxisRotatingVisual(VisualizationContext context, T blockEntity, float partialTick, Model model) {
        this(context, blockEntity, partialTick, Direction.UP, model);
    }

    /**
     * @param from  The source model orientation to rotate away from.
     * @param model The model to spin.
     */
    public SingleAxisRotatingVisual(VisualizationContext context, T blockEntity, float partialTick, Direction from, Model model) {
        super(context, blockEntity, partialTick);
        rotatingModel = instancerProvider().instancer(AllInstanceTypes.ROTATING, model).createInstance().rotateToFace(from, rotationAxis())
            .setup(blockEntity).setPosition(getVisualPosition());

        rotatingModel.setChanged();
    }

    public static <T extends KineticBlockEntity> SimpleBlockEntityVisualizer.Factory<T> of(PartialModel partial) {
        return (context, blockEntity, partialTick) -> new SingleAxisRotatingVisual<>(context, blockEntity, partialTick, Models.partial(partial));
    }

    /**
     * For partial models whose source model is aligned with the Z axis instead of Y
     */
    public static <T extends KineticBlockEntity> SimpleBlockEntityVisualizer.Factory<T> ofZ(PartialModel partial) {
        return (context, blockEntity, partialTick) -> new SingleAxisRotatingVisual<>(
            context,
            blockEntity,
            partialTick,
            Direction.SOUTH,
            Models.partial(partial)
        );
    }

    public static <T extends KineticBlockEntity> SingleAxisRotatingVisual<T> shaft(VisualizationContext context, T blockEntity, float partialTick) {
        return new SingleAxisRotatingVisual<>(context, blockEntity, partialTick, Models.partial(AllPartialModels.SHAFT));
    }

    public static <T extends KineticBlockEntity> SingleAxisRotatingVisual<T> backtank(
        VisualizationContext context,
        T blockEntity,
        float partialTick
    ) {
        var model = Models.partial(BacktankRenderer.getShaftModel(blockEntity.getBlockState()));
        return new SingleAxisRotatingVisual<>(context, blockEntity, partialTick, model);
    }

    @Override
    public void update(float pt) {
        rotatingModel.setup(blockEntity).setChanged();
    }

    @Override
    public void tick(Context context) {
        applyOverstressEffect(blockEntity, rotatingModel);
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
