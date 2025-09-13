package com.zurrtum.create.client.content.fluids.hosePulley;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.content.contraptions.pulley.AbstractPulleyVisual;
import com.zurrtum.create.client.content.processing.burner.ScrollInstance;
import com.zurrtum.create.client.flywheel.api.instance.Instancer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;
import com.zurrtum.create.content.fluids.hosePulley.HosePulleyBlockEntity;

public class HosePulleyVisual extends AbstractPulleyVisual<HosePulleyBlockEntity> {
    public HosePulleyVisual(VisualizationContext dispatcher, HosePulleyBlockEntity blockEntity, float partialTick) {
        super(dispatcher, blockEntity, partialTick);
    }

    @Override
    protected Instancer<TransformedInstance> getRopeModel() {
        return instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.HOSE));
    }

    @Override
    protected Instancer<TransformedInstance> getMagnetModel() {
        return instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.HOSE_MAGNET));
    }

    @Override
    protected Instancer<TransformedInstance> getHalfMagnetModel() {
        return instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.HOSE_HALF_MAGNET));
    }

    @Override
    protected Instancer<ScrollInstance> getCoilModel() {
        return instancerProvider().instancer(AllInstanceTypes.SCROLLING, Models.partial(AllPartialModels.HOSE_COIL));
    }

    @Override
    protected Instancer<TransformedInstance> getHalfRopeModel() {
        return instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.HOSE_HALF));
    }

    @Override
    protected float getOffset(float pt) {
        return blockEntity.getInterpolatedOffset(pt);
    }

    @Override
    protected boolean isRunning() {
        return true;
    }

    @Override
    protected SpriteShiftEntry getCoilAnimation() {
        return AllSpriteShifts.HOSE_PULLEY_COIL;
    }

}
