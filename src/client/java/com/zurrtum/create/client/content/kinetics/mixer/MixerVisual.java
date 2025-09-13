package com.zurrtum.create.client.content.kinetics.mixer;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.RotatingInstance;
import com.zurrtum.create.client.content.kinetics.base.SingleAxisRotatingVisual;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.OrientedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;
import com.zurrtum.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import net.minecraft.util.math.Direction;

import java.util.function.Consumer;

public class MixerVisual extends SingleAxisRotatingVisual<MechanicalMixerBlockEntity> implements SimpleDynamicVisual {

    private final RotatingInstance mixerHead;
    private final OrientedInstance mixerPole;
    private final MechanicalMixerBlockEntity mixer;

    public MixerVisual(VisualizationContext context, MechanicalMixerBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick, Models.partial(AllPartialModels.SHAFTLESS_COGWHEEL));
        this.mixer = blockEntity;

        mixerHead = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.MECHANICAL_MIXER_HEAD)).createInstance();

        mixerHead.setRotationAxis(Direction.Axis.Y);

        mixerPole = instancerProvider().instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.MECHANICAL_MIXER_POLE)).createInstance();

        animate(partialTick);
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        animate(ctx.partialTick());
    }

    private void animate(float pt) {
        float renderedHeadOffset = mixer.getRenderedHeadOffset(pt);

        transformPole(renderedHeadOffset);
        transformHead(renderedHeadOffset, pt);
    }

    private void transformHead(float renderedHeadOffset, float pt) {
        float speed = mixer.getRenderedHeadRotationSpeed(pt);

        mixerHead.setPosition(getVisualPosition()).nudge(0, -renderedHeadOffset, 0).setRotationalSpeed(speed * 2 * RotatingInstance.SPEED_MULTIPLIER)
            .setChanged();
    }

    private void transformPole(float renderedHeadOffset) {
        mixerPole.position(getVisualPosition()).translatePosition(0, -renderedHeadOffset, 0).setChanged();
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);

        relight(pos.down(), mixerHead);
        relight(mixerPole);
    }

    @Override
    protected void _delete() {
        super._delete();
        mixerHead.delete();
        mixerPole.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(mixerHead);
        consumer.accept(mixerPole);
    }
}
