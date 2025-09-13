package com.zurrtum.create.client.foundation.entity.behaviour;

import com.zurrtum.create.client.content.trains.entity.CarriageContraptionVisual;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageContraption;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.entity.behaviour.EntityBehaviour;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

public class PortalCutoffBehaviour extends EntityBehaviour<CarriageContraptionEntity> {
    public static final BehaviourType<PortalCutoffBehaviour> TYPE = new BehaviourType<>();
    private CarriageContraptionVisual visual;

    public PortalCutoffBehaviour(CarriageContraptionEntity entity) {
        super(entity);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    public void setVisual(CarriageContraptionVisual visual) {
        this.visual = visual;
    }

    public void updateRenderedPortalCutoff() {
        Carriage carriage = entity.getCarriage();
        if (carriage == null)
            return;

        // update portal slice
        entity.particleSlice.clear();
        entity.particleAvgY = 0;

        if (entity.getContraption() instanceof CarriageContraption cc) {
            Direction forward = cc.getAssemblyDirection().rotateYClockwise();
            Axis axis = forward.getAxis();
            boolean x = axis == Axis.X;
            for (BlockPos pos : entity.getContraption().getBlocks().keySet()) {
                if (!cc.atSeam(pos))
                    continue;
                int pX = x ? pos.getX() : pos.getZ();
                pX *= forward.getDirection().offset();
                pos = new BlockPos(pX, pos.getY(), 0);
                entity.particleSlice.add(pos);
                entity.particleAvgY += pos.getY();
            }

        }
        if (!entity.particleSlice.isEmpty())
            entity.particleAvgY /= entity.particleSlice.size();

        // update hidden bogeys (if instanced)
        if (visual == null)
            return;
        int bogeySpacing = carriage.bogeySpacing;

        carriage.bogeys.forEachWithContext((bogey, first) -> {
            if (bogey == null)
                return;

            BlockPos bogeyPos = bogey.isLeading ? BlockPos.ORIGIN : BlockPos.ORIGIN.offset(
                entity.getInitialOrientation().rotateYCounterclockwise(),
                bogeySpacing
            );
            visual.setBogeyVisibility(first, !entity.getContraption().isHiddenInPortal(bogeyPos));
        });
    }
}
