package com.zurrtum.create.content.kinetics.chainDrive;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ChainGearshiftBlockEntity extends KineticBlockEntity {

    int signal;
    boolean signalChanged;

    public ChainGearshiftBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.ADJUSTABLE_CHAIN_GEARSHIFT, pos, state);
        signal = 0;
        setLazyTickRate(40);
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        view.putInt("Signal", signal);
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        signal = view.getIntOr("Signal", 0);
        super.read(view, clientPacket);
    }

    public float getModifier() {
        return getModifierForSignal(signal);
    }

    public void neighbourChanged() {
        if (!hasLevel())
            return;
        int power = level.getBestNeighborSignal(worldPosition);
        if (power != signal)
            signalChanged = true;
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        neighbourChanged();
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide())
            return;
        if (signalChanged) {
            signalChanged = false;
            analogSignalChanged(level.getBestNeighborSignal(worldPosition));
        }
    }

    protected void analogSignalChanged(int newSignal) {
        detachKinetics();
        removeSource();
        signal = newSignal;
        attachKinetics();
    }

    protected float getModifierForSignal(int newPower) {
        if (newPower == 0)
            return 1;
        return 1 + ((newPower + 1) / 16f);
    }

}
