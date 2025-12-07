package com.zurrtum.create.compat.computercraft;

import com.zurrtum.create.compat.computercraft.events.ComputerEvent;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import org.jetbrains.annotations.NotNull;

public class AbstractComputerBehaviour extends BlockEntityBehaviour<SmartBlockEntity> {

    public static final BehaviourType<AbstractComputerBehaviour> TYPE = new BehaviourType<>();

    boolean hasAttachedComputer;

    public AbstractComputerBehaviour(SmartBlockEntity te) {
        super(te);
        this.hasAttachedComputer = false;
    }

    @Override
    public void read(ReadView view, boolean clientPacket) {
        hasAttachedComputer = view.getBoolean("HasAttachedComputer", false);
        super.read(view, clientPacket);
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        view.putBoolean("HasAttachedComputer", hasAttachedComputer);
        super.write(view, clientPacket);
    }

    public IPeripheral getPeripheralCapability() {
        return null;
    }

    public void setHasAttachedComputer(boolean hasAttachedComputer) {
        this.hasAttachedComputer = hasAttachedComputer;
    }

    public boolean hasAttachedComputer() {
        return hasAttachedComputer;
    }

    public void prepareComputerEvent(@NotNull ComputerEvent event) {
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

}
