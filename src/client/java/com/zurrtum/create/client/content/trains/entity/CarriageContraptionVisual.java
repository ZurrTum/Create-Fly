package com.zurrtum.create.client.content.trains.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllBogeyStyleRenders;
import com.zurrtum.create.client.content.contraptions.render.ClientContraption;
import com.zurrtum.create.client.content.contraptions.render.OrientedContraptionVisual;
import com.zurrtum.create.client.content.trains.bogey.BogeyVisual;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.trains.entity.CarriageBogey;
import com.zurrtum.create.content.trains.entity.CarriageContraption;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.apache.commons.lang3.tuple.MutablePair;
import org.joml.Vector3f;

public class CarriageContraptionVisual extends OrientedContraptionVisual<CarriageContraptionEntity> {
    public static final int MAX_NUM_BOGEYS = 2;

    private final PoseStack poseStack = new PoseStack();

    private final CarriageContraption contraption;

    // The number of bogeys actually populated in the below arrays.
    private int numBogeys;
    private final CarriageBogey[] bogeys = new CarriageBogey[MAX_NUM_BOGEYS];
    private final BogeyVisual[] visuals = new BogeyVisual[MAX_NUM_BOGEYS];
    // The position (in blocks) of each bogey along the carriage, relative to the carriage's origin.
    // Used to check if a bogey is hidden in a portal.
    private final int[] bogeyPos = new int[MAX_NUM_BOGEYS];

    public CarriageContraptionVisual(VisualizationContext context, CarriageContraptionEntity entity, float partialTick) {
        super(context, entity, partialTick);

        // An extra block because bogeys are always slightly outside the contraption bounds.
        this.lightPaddingBlocks = DEFAULT_LIGHT_PADDING + 1;

        this.contraption = (CarriageContraption) entity.getContraption();

        animate(partialTick);
    }

    @Override
    protected ClientContraption createClientContraption(Contraption contraption) {
        return new CarriageClientContraption((CarriageContraption) contraption);
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        super.beginFrame(ctx);

        animate(ctx.partialTick());
    }

    @Override
    protected <T extends BlockEntity> void setupVisualizer(T be, float partialTicks) {
        if (entity.getContraption() instanceof CarriageContraption cc && cc.isHiddenInPortal(be.getBlockPos())) {
            return;
        }
        super.setupVisualizer(be, partialTicks);
    }

    @Override
    protected void setupActor(MutablePair<StructureBlockInfo, MovementContext> actor, VirtualRenderWorld renderLevel) {
        if (entity.getContraption() instanceof CarriageContraption cc && cc.isHiddenInPortal(actor.left.pos())) {
            return;
        }
        super.setupActor(actor, renderLevel);
    }

    /**
     * @return True if we're ready to actually animate.
     */
    private boolean checkCarriage(float pt) {
        if (numBogeys > 0) {
            return true;
        }

        var carriage = entity.getCarriage();

        if (entity.validForRender && carriage != null) {
            numBogeys = 0;

            for (var bogey : carriage.bogeys) {
                if (bogey != null) {
                    visuals[numBogeys] = AllBogeyStyleRenders.createVisual(bogey.getStyle(), bogey.getSize(), visualizationContext, pt, true);
                    bogeys[numBogeys] = bogey;
                    bogeyPos[numBogeys] = bogey.isLeading ? 0 : carriage.bogeySpacing * contraption.getAssemblyDirection().getCounterClockWise()
                        .getAxisDirection().getStep();
                    numBogeys++;
                }
            }

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
        var carriage = entity.getCarriage();
        int bogeySpacing = carriage.bogeySpacing;

        poseStack.pushPose();

        Vector3f visualPosition = getVisualPosition(partialTick);
        TransformStack.of(poseStack).translate(visualPosition);

        for (int bogeyIdx = 0; bogeyIdx < numBogeys; bogeyIdx++) {
            if (contraption.isHiddenInPortal(bogeyPos[bogeyIdx])) {
                visuals[bogeyIdx].hide();
                continue;
            }

            poseStack.pushPose();

            CarriageBogey bogey = bogeys[bogeyIdx];

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

            CompoundTag bogeyData = bogey.bogeyData;
            if (bogeyData == null) {
                bogeyData = new CompoundTag();
            }
            visuals[bogeyIdx].update(bogeyData, bogey.wheelAngle.getValue(partialTick), poseStack);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    @Override
    public void _delete() {
        super._delete();

        for (var visual : visuals) {
            if (visual != null) {
                visual.delete();
            }
        }
    }
}
