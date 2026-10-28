package com.zurrtum.create.client.content.decoration.steamWhistle;

import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visual.ShaderLightVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.OrientedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.animation.AnimationBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.animation.WhistleAnimationBehaviour;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleBlock;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleBlock.WhistleSize;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleBlockEntity;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class WhistleVisual extends AbstractBlockEntityVisual<WhistleBlockEntity> implements SimpleDynamicVisual, ShaderLightVisual {
    private final OrientedInstance mouth;
    private final boolean powered;

    public WhistleVisual(VisualizationContext ctx, WhistleBlockEntity blockEntity, float partialTick) {
        super(ctx, blockEntity, partialTick);
        WhistleSize size = blockState.getValue(WhistleBlock.SIZE);
        mouth = instancerProvider().instancer(
            InstanceTypes.ORIENTED,
            Models.chunkPartial(WhistleRenderer.getMouthModel(size))
        ).createInstance();
        mouth.rotateY(Radians.horizontal(blockState.getValue(WhistleBlock.FACING)));
        powered = blockState.getValue(WhistleBlock.POWERED);
        WhistleAnimationBehaviour behaviour = (WhistleAnimationBehaviour) blockEntity.getBehaviour(AnimationBehaviour.TYPE);
        if (behaviour != null) {
            transformModels(behaviour, size, partialTick);
        } else {
            mouth.position(getVisualPosition()).setChanged();
        }
    }

    private void transformModels(WhistleAnimationBehaviour behaviour, WhistleSize size, float partialTick) {
        float offset = behaviour.animation.getValue(partialTick);
        mouth.position(getVisualPosition());
        if (behaviour.animation.getChaseTarget() > 0 && behaviour.animation.getValue() > 0.5f) {
            float wiggleProgress = (AnimationTickHolder.getTicks(level) + partialTick) / 8.0f;
            offset = (float) (offset - Math.sin(wiggleProgress * (2 * Mth.PI) * (4 - size.ordinal())) / 16.0f);
        }
        mouth.translatePosition(0, offset * 0.25f, 0);
        mouth.setChanged();
    }

    @Override
    public void beginFrame(Context ctx) {
        WhistleAnimationBehaviour behaviour = (WhistleAnimationBehaviour) blockEntity.getBehaviour(AnimationBehaviour.TYPE);
        if (behaviour != null && (!behaviour.animation.settled() || powered)) {
            transformModels(behaviour, blockState.getValue(WhistleBlock.SIZE), ctx.partialTick());
        }
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
        consumer.accept(mouth);
    }

    @Override
    public void updateLight(float partialTick) {
        relight(mouth);
    }

    @Override
    protected void _delete() {
        mouth.delete();
    }
}
