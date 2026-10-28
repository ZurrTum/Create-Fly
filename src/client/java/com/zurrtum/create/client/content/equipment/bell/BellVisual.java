package com.zurrtum.create.client.content.equipment.bell;

import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.api.visual.ShaderLightVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.client.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import com.zurrtum.create.content.equipment.bell.AbstractBellBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class BellVisual<T extends AbstractBellBlockEntity> extends AbstractBlockEntityVisual<T> implements SimpleDynamicVisual, ShaderLightVisual {
    private final TransformedInstance bell;
    private final float yRadians;

    public BellVisual(VisualizationContext ctx, T blockEntity, float partialTick, Model model) {
        super(ctx, blockEntity, partialTick);
        bell = instancerProvider().instancer(InstanceTypes.TRANSFORMED, model).createInstance();
        Direction facing = blockState.getValue(BellBlock.FACING);
        BellAttachType attachment = blockState.getValue(BellBlock.ATTACHMENT);
        if (attachment == BellAttachType.SINGLE_WALL || attachment == BellAttachType.DOUBLE_WALL) {
            yRadians = Radians.horizontalPlus90(facing);
        } else {
            yRadians = Radians.horizontal(facing);
        }
        transformModels(partialTick);
    }

    private void transformModels(float partialTick) {
        bell.setIdentityTransform().translate(getVisualPosition());
        if (blockEntity.isRinging) {
            bell.rotateCentered(
                BellRenderer.getSwingAngle(blockEntity.ringingTicks + partialTick),
                blockEntity.ringDirection.getCounterClockWise()
            );
        }
        if (yRadians != 0) {
            bell.rotateYCentered(yRadians);
        }
        bell.setChanged();
    }

    public static <T extends AbstractBellBlockEntity> SimpleBlockEntityVisualizer.Factory<T> of(PartialModel partial) {
        return (context, blockEntity, partialTick) -> new BellVisual<>(
            context,
            blockEntity,
            partialTick,
            Models.chunkPartial(partial)
        );
    }

    @Override
    public void beginFrame(Context ctx) {
        if (blockEntity.isRinging) {
            transformModels(ctx.partialTick());
        }
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
        consumer.accept(bell);
    }

    @Override
    public void updateLight(float partialTick) {
        relight(bell);
    }

    @Override
    protected void _delete() {
        bell.delete();
    }
}
