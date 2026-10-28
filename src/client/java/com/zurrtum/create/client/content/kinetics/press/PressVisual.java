package com.zurrtum.create.client.content.kinetics.press;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.kinetics.base.ShaftVisual;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.OrientedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.content.kinetics.press.MechanicalPressBlock;
import com.zurrtum.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.zurrtum.create.content.kinetics.press.PressingBehaviour;

import java.util.function.Consumer;

public class PressVisual extends ShaftVisual<MechanicalPressBlockEntity> implements SimpleDynamicVisual {

    private final OrientedInstance pressHead;

    public PressVisual(VisualizationContext context, MechanicalPressBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        pressHead = instancerProvider().instancer(
            InstanceTypes.ORIENTED,
            Models.chunkPartial(AllPartialModels.MECHANICAL_PRESS_HEAD)
        ).createInstance();

        float yRadians = Radians.horizontal(blockState.getValue(MechanicalPressBlock.HORIZONTAL_FACING));
        pressHead.rotationY(yRadians);

        transformModels(partialTick);
    }

    @Override
    public void setSectionCollector(SectionCollector sectionCollector) {
        setSectionCollector(sectionCollector, 0, -2, 0, 0, 1, 0);
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        transformModels(ctx.partialTick());
    }

    private void transformModels(float pt) {
        float renderedHeadOffset = getRenderedHeadOffset(pt);

        pressHead.position(getVisualPosition()).translatePosition(0, -renderedHeadOffset, 0).setChanged();
    }

    private float getRenderedHeadOffset(float pt) {
        PressingBehaviour pressingBehaviour = blockEntity.getPressingBehaviour();
        return pressingBehaviour.getRenderedHeadOffset(pt) * pressingBehaviour.mode.headOffset;
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);
        relight(pressHead);
    }

    @Override
    protected void _delete() {
        super._delete();
        pressHead.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(pressHead);
    }
}
