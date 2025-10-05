package com.zurrtum.create.client.content.trains.entity;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllBogeyStyleRenders;
import com.zurrtum.create.client.content.contraptions.render.OrientedContraptionVisual;
import com.zurrtum.create.client.content.trains.bogey.BogeyVisual;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.entity.behaviour.PortalCutoffBehaviour;
import com.zurrtum.create.content.trains.entity.CarriageBogey;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class CarriageContraptionVisual extends OrientedContraptionVisual<CarriageContraptionEntity> {
    private final MatrixStack poseStack = new MatrixStack();

    @Nullable
    private Couple<@Nullable VisualizedBogey> bogeys;
    private final Couple<Boolean> bogeyHidden = Couple.create(() -> false);

    public CarriageContraptionVisual(VisualizationContext context, CarriageContraptionEntity entity, float partialTick) {
        super(context, entity, partialTick);
        PortalCutoffBehaviour behaviour = entity.getBehaviour(PortalCutoffBehaviour.TYPE);
        if (behaviour != null) {
            behaviour.setVisual(this);
            behaviour.updateRenderedPortalCutoff();
        }

        animate(partialTick);
    }

    public void setBogeyVisibility(boolean first, boolean visible) {
        bogeyHidden.set(first, !visible);
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        super.beginFrame(ctx);

        animate(ctx.partialTick());
    }

    /**
     * @return True if we're ready to actually animate.
     */
    private boolean checkCarriage(float pt) {
        if (bogeys != null) {
            return true;
        }

        var carriage = entity.getCarriage();

        if (entity.validForRender && carriage != null) {
            bogeys = carriage.bogeys.mapNotNull(bogey -> VisualizedBogey.of(visualizationContext, bogey, pt));
            updateLight(pt);
            return true;
        }

        return false;
    }

    private void animate(float partialTick) {
        if (!checkCarriage(partialTick)) {
            return;
        }

        float viewYRot = entity.getViewYRot(partialTick);
        float viewXRot = entity.getViewXRot(partialTick);
        int bogeySpacing = entity.getCarriage().bogeySpacing;

        poseStack.push();

        Vector3f visualPosition = getVisualPosition(partialTick);
        TransformStack.of(poseStack).translate(visualPosition);

        for (boolean current : Iterate.trueAndFalse) {
            VisualizedBogey visualizedBogey = bogeys.get(current);
            if (visualizedBogey == null)
                continue;

            if (bogeyHidden.get(current)) {
                visualizedBogey.visual.hide();
                continue;
            }

            poseStack.push();
            CarriageBogey bogey = visualizedBogey.bogey;

            CarriageContraptionEntityRenderer.translateBogey(
                poseStack,
                bogey,
                bogeySpacing,
                viewYRot,
                viewXRot,
                bogey.yaw.getValue(partialTick),
                bogey.pitch.getValue(partialTick)
            );
            poseStack.translate(0, -1.5 - 1 / 128f, 0);

            NbtCompound bogeyData = bogey.bogeyData;
            if (bogeyData == null) {
                bogeyData = new NbtCompound();
            }
            visualizedBogey.visual.update(bogeyData, bogey.wheelAngle.getValue(partialTick), poseStack);
            poseStack.pop();
        }

        poseStack.pop();
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);

        if (bogeys == null)
            return;

        bogeys.forEach(bogey -> {
            if (bogey != null) {
                int packedLight = CarriageContraptionEntityRenderer.getBogeyLightCoords(
                    entity.getEntityWorld(),
                    bogey.bogey,
                    () -> entity.getClientCameraPosVec(partialTick)
                );
                bogey.visual.updateLight(packedLight);
            }
        });
    }

    @Override
    public void _delete() {
        super._delete();

        if (bogeys == null)
            return;

        bogeys.forEach(bogey -> {
            if (bogey != null) {
                bogey.visual.delete();
            }
        });
    }

    private record VisualizedBogey(CarriageBogey bogey, BogeyVisual visual) {
        @Nullable
        static VisualizedBogey of(VisualizationContext ctx, CarriageBogey bogey, float partialTick) {
            BogeyVisual visual = AllBogeyStyleRenders.createVisual(bogey.getStyle(), bogey.getSize(), ctx, partialTick, true);
            if (visual == null) {
                return null;
            }
            return new VisualizedBogey(bogey, visual);
        }
    }
}
