package com.zurrtum.create.content.kinetics.gauge;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.compat.computercraft.AbstractComputerBehaviour;
import com.zurrtum.create.content.kinetics.base.IRotate.SpeedLevel;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class SpeedGaugeBlockEntity extends GaugeBlockEntity {
    public SpeedGaugeBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.SPEEDOMETER, pos, state);
    }

    @Override
    public void onSpeedChanged(float prevSpeed) {
        super.onSpeedChanged(prevSpeed);
        AbstractComputerBehaviour computer = AbstractComputerBehaviour.get(this);
        if (computer != null) {
            computer.queueKineticsChange(speed, capacity, stress, overStressed);
        }
        float speed = Math.abs(getSpeed());

        dialTarget = getDialTarget(speed);
        color = Color.mixColors(SpeedLevel.of(speed).getColor(), 0xffffff, .25f);

        setChanged();
    }

    public static float getDialTarget(float speed) {
        speed = Math.abs(speed);
        float medium = AllConfigs.server().kinetics.mediumSpeed.get();
        float fast = AllConfigs.server().kinetics.fastSpeed.get();
        float max = AllConfigs.server().kinetics.maxRotationSpeed.get().floatValue();
        float target;
        if (speed == 0)
            target = 0;
        else if (speed < medium)
            target = Mth.lerp(speed / medium, 0, .45f);
        else if (speed < fast)
            target = Mth.lerp((speed - medium) / (fast - medium), .45f, .75f);
        else
            target = Mth.lerp((speed - fast) / (max - fast), .75f, 1.125f);
        return target;
    }
}
