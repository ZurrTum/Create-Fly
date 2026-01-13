package com.zurrtum.create.content.kinetics.gauge;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.content.kinetics.base.IRotate.StressImpact;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import net.minecraft.block.BlockState;
import net.minecraft.storage.ReadView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class StressGaugeBlockEntity extends GaugeBlockEntity {

    public static BlockPos lastSent;

    public StressGaugeBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.STRESSOMETER, pos, state);
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.STRESSOMETER, AllAdvancements.STRESSOMETER_MAXED);
    }

    @Override
    public void updateFromNetwork(float maxStress, float currentStress, int networkSize) {
        super.updateFromNetwork(maxStress, currentStress, networkSize);

        if (!StressImpact.isEnabled())
            dialTarget = 0;
        else if (isOverStressed())
            dialTarget = 1.125f;
        else if (maxStress == 0)
            dialTarget = 0;
        else
            dialTarget = currentStress / maxStress;

        if (dialTarget > 0) {
            if (dialTarget < .5f)
                color = Color.mixColors(0x00FF00, 0xFFFF00, dialTarget * 2);
            else if (dialTarget < 1)
                color = Color.mixColors(0xFFFF00, 0xFF0000, (dialTarget) * 2 - 1);
            else
                color = 0xFF0000;
        }

        sendData();
        markDirty();
    }

    @Override
    public void onSpeedChanged(float prevSpeed) {
        super.onSpeedChanged(prevSpeed);
        if (getSpeed() == 0) {
            dialTarget = 0;
            markDirty();
            return;
        }

        updateFromNetwork(capacity, stress, getOrCreateNetwork().getSize());
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        if (clientPacket && pos != null && pos.equals(lastSent))
            lastSent = null;
    }

    public float getNetworkStress() {
        return stress;
    }

    public float getNetworkCapacity() {
        return capacity;
    }

    public void onObserved() {
        award(AllAdvancements.STRESSOMETER);
        if (MathHelper.approximatelyEquals(dialTarget, 1))
            award(AllAdvancements.STRESSOMETER_MAXED);
    }
}
