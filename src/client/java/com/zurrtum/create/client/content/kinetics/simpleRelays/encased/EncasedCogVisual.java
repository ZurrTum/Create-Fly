package com.zurrtum.create.client.content.kinetics.simpleRelays.encased;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual;
import com.zurrtum.create.client.content.kinetics.base.RotatingInstance;
import com.zurrtum.create.client.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.AxisDirection;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class EncasedCogVisual extends KineticBlockEntityVisual<KineticBlockEntity> {

    private final boolean large;

    protected final RotatingInstance rotatingModel;
    @Nullable
    protected final RotatingInstance rotatingTopShaft;
    @Nullable
    protected final RotatingInstance rotatingBottomShaft;

    public static EncasedCogVisual small(VisualizationContext modelManager, KineticBlockEntity blockEntity, float partialTick) {
        return new EncasedCogVisual(modelManager, blockEntity, false, partialTick, Models.partial(AllPartialModels.SHAFTLESS_COGWHEEL));
    }

    public static EncasedCogVisual large(VisualizationContext modelManager, KineticBlockEntity blockEntity, float partialTick) {
        return new EncasedCogVisual(modelManager, blockEntity, true, partialTick, Models.partial(AllPartialModels.SHAFTLESS_LARGE_COGWHEEL));
    }

    public EncasedCogVisual(VisualizationContext modelManager, KineticBlockEntity blockEntity, boolean large, float partialTick, Model model) {
        super(modelManager, blockEntity, partialTick);
        this.large = large;

        rotatingModel = instancerProvider().instancer(AllInstanceTypes.ROTATING, model).createInstance();

        rotatingModel.setup(blockEntity).setPosition(getVisualPosition()).rotateToFace(rotationAxis()).setChanged();

        RotatingInstance rotatingTopShaft = null;
        RotatingInstance rotatingBottomShaft = null;

        Block block = blockState.getBlock();
        if (block instanceof IRotate def) {
            for (Direction d : Iterate.directionsInAxis(rotationAxis())) {
                if (!def.hasShaftTowards(blockEntity.getWorld(), blockEntity.getPos(), blockState, d))
                    continue;
                RotatingInstance instance = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF))
                    .createInstance();
                instance.setup(blockEntity).setPosition(getVisualPosition()).rotateToFace(Direction.SOUTH, d).setChanged();

                if (large) {
                    instance.setRotationOffset(BracketedKineticBlockEntityRenderer.getShaftAngleOffset(rotationAxis(), pos));
                }

                if (d.getDirection() == AxisDirection.POSITIVE) {
                    rotatingTopShaft = instance;
                } else {
                    rotatingBottomShaft = instance;
                }
            }
        }

        this.rotatingTopShaft = rotatingTopShaft;
        this.rotatingBottomShaft = rotatingBottomShaft;
    }

    @Override
    public void update(float pt) {
        rotatingModel.setup(blockEntity).setChanged();
        if (rotatingTopShaft != null)
            rotatingTopShaft.setup(blockEntity).setChanged();
        if (rotatingBottomShaft != null)
            rotatingBottomShaft.setup(blockEntity).setChanged();
    }

    @Override
    public void updateLight(float partialTick) {
        relight(rotatingModel, rotatingTopShaft, rotatingBottomShaft);
    }

    @Override
    protected void _delete() {
        rotatingModel.delete();
        if (rotatingTopShaft != null)
            rotatingTopShaft.delete();
        if (rotatingBottomShaft != null)
            rotatingBottomShaft.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
        consumer.accept(rotatingModel);
        consumer.accept(rotatingTopShaft);
        consumer.accept(rotatingBottomShaft);
    }
}
