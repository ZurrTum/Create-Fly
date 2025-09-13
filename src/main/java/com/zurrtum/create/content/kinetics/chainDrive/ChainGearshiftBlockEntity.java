package com.zurrtum.create.content.kinetics.chainDrive;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;

public class ChainGearshiftBlockEntity extends KineticBlockEntity {

    int signal;
    boolean signalChanged;

    public ChainGearshiftBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.ADJUSTABLE_CHAIN_GEARSHIFT, pos, state);
        signal = 0;
        setLazyTickRate(40);
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        view.putInt("Signal", signal);
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        signal = view.getInt("Signal", 0);
        super.read(view, clientPacket);
    }

    public float getModifier() {
        return getModifierForSignal(signal);
    }

    public void neighbourChanged() {
        if (!hasWorld())
            return;
        int power = world.getReceivedRedstonePower(pos);
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
        if (world.isClient)
            return;
        if (signalChanged) {
            signalChanged = false;
            analogSignalChanged(world.getReceivedRedstonePower(pos));
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
