package com.zurrtum.create.client.content.contraptions.pulley;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.content.processing.burner.ScrollInstance;
import com.zurrtum.create.client.flywheel.api.instance.Instancer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;
import com.zurrtum.create.content.contraptions.pulley.PulleyBlockEntity;

public class RopePulleyVisual extends AbstractPulleyVisual<PulleyBlockEntity> {
    public RopePulleyVisual(VisualizationContext context, PulleyBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);
    }

    @Override
    protected Instancer<TransformedInstance> getRopeModel() {
        return instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ROPE));
    }

    @Override
    protected Instancer<TransformedInstance> getMagnetModel() {
        return instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.PULLEY_MAGNET));
    }

    @Override
    protected Instancer<TransformedInstance> getHalfMagnetModel() {
        return instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ROPE_HALF_MAGNET));
    }

    @Override
    protected Instancer<ScrollInstance> getCoilModel() {
        return instancerProvider().instancer(AllInstanceTypes.SCROLLING, Models.partial(AllPartialModels.ROPE_COIL));
    }

    @Override
    protected Instancer<TransformedInstance> getHalfRopeModel() {
        return instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ROPE_HALF));
    }

    @Override
    protected float getOffset(float pt) {
        return PulleyRenderer.getBlockEntityOffset(pt, blockEntity);
    }

    @Override
    protected boolean isRunning() {
        return PulleyRenderer.isPulleyRunning(blockEntity);
    }

    @Override
    protected SpriteShiftEntry getCoilAnimation() {
        return AllSpriteShifts.ROPE_PULLEY_COIL;
    }

}