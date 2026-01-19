package com.zurrtum.create.client.content.trains.signal;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllTrackRenders;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.content.trains.track.TrackBlockRenderer;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleTickableVisual;
import com.zurrtum.create.content.trains.signal.SignalBlockEntity;
import com.zurrtum.create.content.trains.signal.SignalBlockEntity.OverlayState;
import com.zurrtum.create.content.trains.signal.SignalBlockEntity.SignalState;
import com.zurrtum.create.content.trains.signal.SignalBoundary;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class SignalVisual extends AbstractBlockEntityVisual<SignalBlockEntity> implements SimpleTickableVisual {
    private final TransformedInstance signalLight;
    private final TransformedInstance signalOverlay;

    private boolean previousIsRedLight;
    private OverlayState previousOverlayState;

    public SignalVisual(VisualizationContext ctx, SignalBlockEntity blockEntity, float partialTick) {
        super(ctx, blockEntity, partialTick);

        signalLight = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.SIGNAL_OFF)).createInstance();

        signalOverlay = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.TRACK_SIGNAL_OVERLAY))
            .createInstance();

        setupVisual();
    }

    @Override
    public void tick(Context context) {
        setupVisual();
    }

    @Override
    public void updateLight(float partialTick) {
        relight(signalLight, signalOverlay);
    }

    @Override
    protected void _delete() {
        signalLight.delete();
        signalOverlay.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
        consumer.accept(signalLight);
    }

    private void setupVisual() {
        {
            SignalState signalState = blockEntity.getState();

            float renderTime = AnimationTickHolder.getRenderTime(blockEntity.getLevel());
            boolean isRedLight = signalState.isRedLight(renderTime);

            if (isRedLight != previousIsRedLight) {
                PartialModel partial = isRedLight ? AllPartialModels.SIGNAL_ON : AllPartialModels.SIGNAL_OFF;
                instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(partial)).stealInstance(signalLight);
            }

            signalLight.setIdentityTransform().translate(getVisualPosition());

            if (isRedLight)
                signalLight.light(LightCoordsUtil.MAX_SMOOTH_LIGHT_LEVEL);

            signalLight.setChanged();

            previousIsRedLight = isRedLight;
        }

        {
            OverlayState overlayState = blockEntity.getOverlay();

            TrackTargetingBehaviour<SignalBoundary> target = blockEntity.edgePoint;
            BlockPos targetPosition = target.getGlobalPosition();
            Level level = blockEntity.getLevel();
            BlockState trackState = level.getBlockState(targetPosition);
            Block block = trackState.getBlock();

            if (!(block instanceof ITrackBlock trackBlock) || overlayState == OverlayState.SKIP) {
                previousOverlayState = null;
                signalOverlay.setZeroTransform().setChanged();
                return;
            }

            if (overlayState != previousOverlayState) {
                previousOverlayState = overlayState;

                PartialModel partial;
                RenderedTrackOverlayType type;
                if (overlayState == OverlayState.DUAL) {
                    type = RenderedTrackOverlayType.DUAL_SIGNAL;
                    partial = AllPartialModels.TRACK_SIGNAL_DUAL_OVERLAY;
                } else {
                    type = RenderedTrackOverlayType.SIGNAL;
                    partial = AllPartialModels.TRACK_SIGNAL_OVERLAY;
                }

                instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(partial)).stealInstance(signalOverlay);

                signalOverlay.setIdentityTransform().translate(targetPosition.subtract(renderOrigin()));

                TrackBlockRenderer renderer = AllTrackRenders.get(trackBlock);
                if (renderer != null) {
                    renderer.prepareTrackOverlay(
                        signalOverlay,
                        level,
                        targetPosition,
                        trackState,
                        target.getTargetBezier(),
                        target.getTargetDirection(),
                        type
                    );
                }

                signalOverlay.setChanged();
            }
        }
    }
}
