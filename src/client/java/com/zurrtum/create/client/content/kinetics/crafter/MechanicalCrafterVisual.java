package com.zurrtum.create.client.content.kinetics.crafter;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.kinetics.base.SingleAxisRotatingVisual;
import com.zurrtum.create.client.content.processing.burner.ScrollInstance;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.InstancerProvider;
import com.zurrtum.create.client.flywheel.api.visual.TickableVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.OrientedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlock;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlockEntity.Phase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

import static com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;

public class MechanicalCrafterVisual extends SingleAxisRotatingVisual<MechanicalCrafterBlockEntity> {
    private final OrientedInstance lid;
    private final ScrollInstance belt;
    private final OrientedInstance frame;
    private final OrientedInstance arrow;
    private final BlockPos targetPos;

    public MechanicalCrafterVisual(
        VisualizationContext context,
        MechanicalCrafterBlockEntity blockEntity,
        float partialTick
    ) {
        super(context, blockEntity, partialTick, Models.chunkPartial(AllPartialModels.SHAFTLESS_COGWHEEL));
        Direction facing = blockState.getValue(HORIZONTAL_FACING);
        targetPos = pos.relative(MechanicalCrafterBlock.getTargetDirection(blockState));
        BlockPos visualPos = getVisualPosition();
        InstancerProvider instancerProvider = instancerProvider();
        lid = instancerProvider.instancer(
                InstanceTypes.ORIENTED,
                Models.chunkPartial(AllPartialModels.MECHANICAL_CRAFTER_LID)
            ).createInstance().position(visualPos).rotateY(Radians.horizontalPlus90(facing))
            .rotateXDegrees(blockState.getValue(MechanicalCrafterBlock.POINTING).getXRotation());
        belt = instancerProvider.instancer(
                AllInstanceTypes.SCROLLING_STEP,
                Models.chunkPartial(AllPartialModels.MECHANICAL_CRAFTER_BELT)
            ).createInstance().setSpriteShift(AllSpriteShifts.CRAFTER_THINGIES, 1, 1, 0.25f, 1).position(visualPos)
            .rotation(lid.rotation);
        frame = instancerProvider.instancer(
            InstanceTypes.ORIENTED,
            Models.chunkPartial(AllPartialModels.MECHANICAL_CRAFTER_BELT_FRAME)
        ).createInstance().position(visualPos).rotation(lid.rotation);
        arrow = instancerProvider.instancer(
            InstanceTypes.ORIENTED,
            Models.chunkPartial(AllPartialModels.MECHANICAL_CRAFTER_ARROW)
        ).createInstance().position(visualPos).rotation(lid.rotation);
        updateVisible();
        lid.setChanged();
        frame.setChanged();
        arrow.setChanged();
        updateBeltSpeed();
    }

    @Override
    public void setSectionCollector(SectionCollector sectionCollector) {
        switch (blockState.getValue(HORIZONTAL_FACING)) {
            case NORTH -> setSectionCollector(sectionCollector, -1, -1, -1, 1, 1, 0);
            case SOUTH -> setSectionCollector(sectionCollector, -1, -1, 0, 1, 1, 1);
            case WEST -> setSectionCollector(sectionCollector, -1, -1, -1, 0, 1, 1);
            case EAST -> setSectionCollector(sectionCollector, 0, -1, -1, 1, 1, 1);
        }
    }

    private void updateVisible() {
        lid.setVisible((blockEntity.covered || blockEntity.phase != Phase.IDLE) && blockEntity.phase.ordinal() < MechanicalCrafterRenderer.CRAFTING_PHASE_ORDINAL);
        boolean valid = MechanicalCrafterBlock.isValidTarget(level, targetPos, blockState);
        belt.setVisible(valid);
        frame.setVisible(valid);
        arrow.setVisible(!valid);
    }

    private void updateBeltSpeed() {
        belt.speed(blockEntity.phase == Phase.EXPORTING ? blockEntity.getCountDownSpeed() / 512.0f : 0, 0).setChanged();
    }

    @Override
    public void tick(TickableVisual.Context context) {
        super.tick(context);
        updateVisible();
    }

    @Override
    public void update(float pt) {
        super.update(pt);
        updateBeltSpeed();
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(lid);
        consumer.accept(belt);
        consumer.accept(frame);
        consumer.accept(arrow);
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);
        relight(lid, belt, frame, arrow);
    }

    @Override
    protected void _delete() {
        super._delete();
        lid.delete();
        belt.delete();
        frame.delete();
        arrow.delete();
    }
}
