package com.zurrtum.create.content.kinetics.flywheel;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.storage.ReadView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class FlywheelBlockEntity extends KineticBlockEntity {

    public LerpedFloat visualSpeed = LerpedFloat.linear();
    public float angle;

    public FlywheelBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.FLYWHEEL, pos, state);
    }

    @Override
    protected Box createRenderBoundingBox() {
        return super.createRenderBoundingBox().expand(2);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        if (clientPacket)
            visualSpeed.chase(getGeneratedSpeed(), 1 / 64f, Chaser.EXP);
    }

    @Override
    public void tick() {
        super.tick();

        if (!world.isClient())
            return;

        float targetSpeed = getSpeed();
        visualSpeed.updateChaseTarget(targetSpeed);
        visualSpeed.tickChaser();
        angle += visualSpeed.getValue() * 3 / 10f;
        angle %= 360;
    }
}
