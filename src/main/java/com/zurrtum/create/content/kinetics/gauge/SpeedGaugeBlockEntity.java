package com.zurrtum.create.content.kinetics.gauge;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.compat.computercraft.AbstractComputerBehaviour;
import com.zurrtum.create.compat.computercraft.ComputerCraftProxy;
import com.zurrtum.create.content.kinetics.base.IRotate.SpeedLevel;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class SpeedGaugeBlockEntity extends GaugeBlockEntity {

    public AbstractComputerBehaviour computerBehaviour;

    public SpeedGaugeBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.SPEEDOMETER, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(computerBehaviour = ComputerCraftProxy.behaviour(this));
    }


    @Override
    public void onSpeedChanged(float prevSpeed) {
        super.onSpeedChanged(prevSpeed);
        if (computerBehaviour.hasAttachedComputer())
            computerBehaviour.prepareComputerEvent(makeComputerKineticsChangeEvent());
        float speed = Math.abs(getSpeed());

        dialTarget = getDialTarget(speed);
        color = Color.mixColors(SpeedLevel.of(speed).getColor(), 0xffffff, .25f);

        markDirty();
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
            target = MathHelper.lerp(speed / medium, 0, .45f);
        else if (speed < fast)
            target = MathHelper.lerp((speed - medium) / (fast - medium), .45f, .75f);
        else
            target = MathHelper.lerp((speed - fast) / (max - fast), .75f, 1.125f);
        return target;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        computerBehaviour.removePeripheral();
    }
}
