package com.zurrtum.create.client.content.contraptions.bearing;

import com.mojang.math.Axis;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.OrientedRotatingVisual;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.OrientedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.content.contraptions.bearing.IBearingBlockEntity;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Quaternionf;
import org.joml.Vector3fc;

import java.util.function.Consumer;

public class BearingVisual<B extends KineticBlockEntity & IBearingBlockEntity> extends OrientedRotatingVisual<B> implements SimpleDynamicVisual {
    final OrientedInstance topInstance;

    final Vector3fc rotationAxis;
    final Quaternionf blockOrientation;

    public BearingVisual(VisualizationContext context, B blockEntity, float partialTick) {
        super(
            context,
            blockEntity,
            partialTick,
            Direction.SOUTH,
            blockEntity.getBlockState().getValue(BlockStateProperties.FACING).getOpposite(),
            Models.chunkPartial(AllPartialModels.SHAFT_HALF)
        );

        Direction facing = blockState.getValue(BlockStateProperties.FACING);
        rotationAxis = rotationAxis().getPositive().getUnitVec3f();

        blockOrientation = getBlockStateOrientation(facing);

        PartialModel top =
            blockEntity.isWoodenTop() ? AllPartialModels.BEARING_TOP_WOODEN : AllPartialModels.BEARING_TOP;

        topInstance = instancerProvider().instancer(InstanceTypes.ORIENTED, Models.chunkPartial(top)).createInstance();

        topInstance.position(getVisualPosition()).rotation(blockOrientation).setChanged();
    }

    @Override
    public void setSectionCollector(SectionCollector sectionCollector) {
        switch (blockState.getValue(BlockStateProperties.FACING).getAxis()) {
            case X -> setSectionCollector(sectionCollector, 0, -1, -1, 0, 1, 1);
            case Y -> setSectionCollector(sectionCollector, -1, 0, -1, 1, 0, 1);
            case Z -> setSectionCollector(sectionCollector, -1, -1, 0, 1, 1, 0);
        }
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        float interpolatedAngle = blockEntity.getInterpolatedAngle(ctx.partialTick() - 1);
        topInstance.rotation(rotationAxis, Mth.DEG_TO_RAD * interpolatedAngle).rotate(blockOrientation).setChanged();
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);
        relight(topInstance);
    }

    @Override
    protected void _delete() {
        super._delete();
        topInstance.delete();
    }

    static Quaternionf getBlockStateOrientation(Direction facing) {
        if (facing.getAxis().isHorizontal()) {
            return Axis.YP.rotation(Radians.horizontal(facing.getOpposite()))
                .mul(Axis.XP.rotation(Radians.negatedVerticalPlus90(facing)));
        }
        return Axis.XP.rotation(Radians.negatedVerticalPlus90(facing));
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(topInstance);
    }
}
