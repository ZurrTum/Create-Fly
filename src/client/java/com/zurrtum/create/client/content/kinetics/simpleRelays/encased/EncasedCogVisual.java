package com.zurrtum.create.client.content.kinetics.simpleRelays.encased;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual;
import com.zurrtum.create.client.content.kinetics.base.RotatingInstance;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.InstancerProvider;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

import static com.zurrtum.create.client.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer.getShaftAngleOffset;

public class EncasedCogVisual extends KineticBlockEntityVisual<KineticBlockEntity> {
    protected final RotatingInstance rotatingModel;
    @Nullable
    protected final RotatingInstance rotatingShaft;

    public static EncasedCogVisual small(
        VisualizationContext modelManager,
        KineticBlockEntity blockEntity,
        float partialTick
    ) {
        return new EncasedCogVisual(
            modelManager,
            blockEntity,
            false,
            partialTick,
            Models.chunkPartial(AllPartialModels.SHAFTLESS_COGWHEEL_CULL)
        );
    }

    public static EncasedCogVisual large(
        VisualizationContext modelManager,
        KineticBlockEntity blockEntity,
        float partialTick
    ) {
        return new EncasedCogVisual(
            modelManager,
            blockEntity,
            true,
            partialTick,
            Models.chunkPartial(AllPartialModels.SHAFTLESS_LARGE_COGWHEEL)
        );
    }

    public EncasedCogVisual(
        VisualizationContext modelManager,
        KineticBlockEntity blockEntity,
        boolean large,
        float partialTick,
        Model model
    ) {
        super(modelManager, blockEntity, partialTick);
        InstancerProvider instancerProvider = instancerProvider();
        rotatingModel = instancerProvider.instancer(AllInstanceTypes.ROTATING, model).createInstance();
        Axis axis = rotationAxis();
        Direction direction = axis.getPositive();
        BlockPos visualPosition = getVisualPosition();
        rotatingModel.setup(blockEntity).setPosition(visualPosition).rotateToFace(direction).setChanged();
        boolean hasTop = blockState.getValue(EncasedCogwheelBlock.TOP_SHAFT);
        boolean hasBottom = blockState.getValue(EncasedCogwheelBlock.BOTTOM_SHAFT);
        if (hasTop) {
            if (hasBottom) {
                rotatingShaft = instancerProvider.instancer(
                    AllInstanceTypes.ROTATING,
                    Models.chunkPartial(AllPartialModels.ENCASED_SHAFT)
                ).createInstance();
                rotatingShaft.setup(blockEntity).setPosition(visualPosition).rotateToFace(direction).setChanged();
            } else {
                rotatingShaft = instancerProvider.instancer(
                    AllInstanceTypes.ROTATING,
                    Models.chunkPartial(AllPartialModels.ENCASED_SHAFT_HALF)
                ).createInstance();
                rotatingShaft.setup(blockEntity).setPosition(visualPosition).rotateToFace(Direction.SOUTH, direction)
                    .setChanged();
            }
            if (large) {
                rotatingShaft.setRotationOffset(getShaftAngleOffset(axis, pos));
            }
        } else if (hasBottom) {
            rotatingShaft = instancerProvider.instancer(
                AllInstanceTypes.ROTATING,
                Models.chunkPartial(AllPartialModels.ENCASED_SHAFT_HALF)
            ).createInstance();
            rotatingShaft.setup(blockEntity).setPosition(visualPosition)
                .rotateToFace(Direction.SOUTH, axis.getNegative()).setChanged();
            if (large) {
                rotatingShaft.setRotationOffset(getShaftAngleOffset(axis, pos));
            }
        } else {
            rotatingShaft = null;
        }
    }

    @Override
    public void setSectionCollector(SectionCollector sectionCollector) {
        switch (blockState.getValue(BlockStateProperties.AXIS)) {
            case X -> setSectionCollector(sectionCollector, 0, -1, -1, 0, 1, 1);
            case Y -> setSectionCollector(sectionCollector, -1, 0, -1, 1, 0, 1);
            default -> setSectionCollector(sectionCollector, -1, -1, 0, 1, 1, 0);
        }
    }

    @Override
    public void update(float pt) {
        rotatingModel.setup(blockEntity).setChanged();
        if (rotatingShaft != null) {
            rotatingShaft.setup(blockEntity).setChanged();
        }
    }

    @Override
    public void updateLight(float partialTick) {
        relight(rotatingModel, rotatingShaft);
    }

    @Override
    protected void _delete() {
        rotatingModel.delete();
        if (rotatingShaft != null) {
            rotatingShaft.delete();
        }
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
        consumer.accept(rotatingModel);
        consumer.accept(rotatingShaft);
    }
}
