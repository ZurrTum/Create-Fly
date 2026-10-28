package com.zurrtum.create.client.content.contraptions.actors.contraptionControls;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.InstancerProvider;
import com.zurrtum.create.client.flywheel.api.visual.ShaderLightVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.OrientedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsBlock;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class ContraptionControlsVisual extends AbstractBlockEntityVisual<ContraptionControlsBlockEntity> implements SimpleDynamicVisual, ShaderLightVisual {
    private final OrientedInstance button;
    private final OrientedInstance indicator;
    private final Vec3 buttonMovementAxis;
    private boolean needUpdateIndicator;

    public ContraptionControlsVisual(
        VisualizationContext ctx,
        ContraptionControlsBlockEntity blockEntity,
        float partialTick
    ) {
        super(ctx, blockEntity, partialTick);
        InstancerProvider instancerProvider = instancerProvider();
        Direction facing = blockState.getValue(ContraptionControlsBlock.FACING).getOpposite();
        float horizontalAngle = AngleHelper.horizontalAngle(facing);
        button = instancerProvider.instancer(
            InstanceTypes.ORIENTED,
            Models.chunkPartial(AllPartialModels.CONTRAPTION_CONTROLS_BUTTON)
        ).createInstance().rotateYDegrees(horizontalAngle).rotateX(Radians.vertical(facing));
        buttonMovementAxis = VecHelper.rotate(new Vec3(0, 1, -0.325), horizontalAngle, Direction.Axis.Y);
        transformButton(partialTick);
        int i = (int) blockEntity.indicator.getValue(partialTick) / 45 % 8 + 8;
        indicator = instancerProvider.instancer(
            InstanceTypes.ORIENTED,
            Models.partial(AllPartialModels.CONTRAPTION_CONTROLS_INDICATOR.get(i % 8))
        ).createInstance();
        indicator.position(getVisualPosition()).rotation(button.rotation).setChanged();
        needUpdateIndicator = !blockEntity.indicator.settled();
    }

    private void transformButton(float partialTick) {
        BlockPos pos = getVisualPosition();
        float scale = -blockEntity.button.getValue(partialTick) / 24.0f;
        button.position(
            (float) (pos.getX() + buttonMovementAxis.x * scale),
            (float) (pos.getY() + buttonMovementAxis.y * scale),
            (float) (pos.getZ() + buttonMovementAxis.z * scale)
        ).setChanged();
    }

    private void updateIndicator(float partialTick) {
        int i = (int) blockEntity.indicator.getValue(partialTick) / 45 % 8 + 8;
        instancerProvider().instancer(
            InstanceTypes.ORIENTED,
            Models.partial(AllPartialModels.CONTRAPTION_CONTROLS_INDICATOR.get(i % 8))
        ).stealInstance(indicator);
    }

    @Override
    public void beginFrame(Context ctx) {
        if (!blockEntity.button.settled()) {
            transformButton(ctx.partialTick());
        }
        if (!blockEntity.indicator.settled()) {
            updateIndicator(ctx.partialTick());
            needUpdateIndicator = true;
        } else if (needUpdateIndicator) {
            updateIndicator(ctx.partialTick());
            needUpdateIndicator = false;
        }
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
        consumer.accept(button);
        consumer.accept(indicator);
    }

    @Override
    public void updateLight(float partialTick) {
        relight(button, indicator);
    }

    @Override
    protected void _delete() {
        button.delete();
        indicator.delete();
    }
}
