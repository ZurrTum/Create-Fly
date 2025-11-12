package com.zurrtum.create.client.content.equipment.toolbox;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.Instancer;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlock;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlockEntity;

import java.util.function.Consumer;

import net.minecraft.core.Direction;

public class ToolBoxVisual extends AbstractBlockEntityVisual<ToolboxBlockEntity> implements SimpleDynamicVisual {

    private final Direction facing;
    private final TransformedInstance lid;
    private final TransformedInstance[] drawers;

    private float lastLidAngle = Float.NaN;
    private float lastDrawerOffset = Float.NaN;

    public ToolBoxVisual(VisualizationContext context, ToolboxBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        facing = blockState.getValue(ToolboxBlock.FACING).getOpposite();

        Instancer<TransformedInstance> drawerModel = instancerProvider().instancer(
            InstanceTypes.TRANSFORMED,
            Models.partial(AllPartialModels.TOOLBOX_DRAWER)
        );

        drawers = new TransformedInstance[]{drawerModel.createInstance(), drawerModel.createInstance()};
        lid = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.TOOLBOX_LIDS.get(blockEntity.getColor())))
            .createInstance();

        animate(partialTick);
    }

    @Override
    protected void _delete() {
        lid.delete();

        for (var drawer : drawers) {
            drawer.delete();
        }
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        animate(ctx.partialTick());
    }

    private void animate(float partialTicks) {
        float lidAngle = blockEntity.lid.getValue(partialTicks);
        float drawerOffset = blockEntity.drawers.getValue(partialTicks);

        if (lidAngle != lastLidAngle) {
            lid.setIdentityTransform().translate(getVisualPosition()).center().rotateYDegrees(-facing.toYRot()).uncenter()
                .translate(0, 6 / 16f, 12 / 16f).rotateXDegrees(135 * lidAngle).translateBack(0, 6 / 16f, 12 / 16f).setChanged();
        }

        if (drawerOffset != lastDrawerOffset) {
            for (int offset : Iterate.zeroAndOne) {
                drawers[offset].setIdentityTransform().translate(getVisualPosition()).center().rotateYDegrees(-facing.toYRot()).uncenter()
                    .translate(0, offset * 1 / 8f, -drawerOffset * .175f * (2 - offset)).setChanged();
            }
        }

        lastLidAngle = lidAngle;
        lastDrawerOffset = drawerOffset;
    }

    @Override
    public void updateLight(float partialTick) {
        relight(drawers);
        relight(lid);
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        consumer.accept(lid);
        for (var drawer : drawers) {
            consumer.accept(drawer);
        }
    }
}