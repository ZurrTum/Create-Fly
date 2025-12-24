package com.zurrtum.create.compat.computercraft.implementation.peripherals;

import com.zurrtum.create.compat.computercraft.events.ComputerEvent;
import com.zurrtum.create.compat.computercraft.implementation.ComputerBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.infrastructure.packet.s2c.AttachedComputerPacket;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class SyncedPeripheral<T extends SmartBlockEntity> implements IPeripheral {

    protected final T blockEntity;
    private final List<@NotNull IComputerAccess> computers = new ArrayList<>();

    public SyncedPeripheral(T blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public void attach(@NotNull IComputerAccess computer) {
        synchronized (computers) {
            computers.add(computer);
            if (computers.size() == 1)
                onFirstAttach();
            updateBlockEntity();
        }
    }

    protected void onFirstAttach() {
    }

    @Override
    public void detach(@NotNull IComputerAccess computer) {
        synchronized (computers) {
            computers.remove(computer);
            updateBlockEntity();
            if (computers.isEmpty())
                onLastDetach();
        }
    }

    protected void onLastDetach() {
    }

    private void updateBlockEntity() {
        boolean hasAttachedComputer = !computers.isEmpty();

        blockEntity.getBehaviour(ComputerBehaviour.TYPE).setHasAttachedComputer(hasAttachedComputer);
        blockEntity.getLevel().getServer().getPlayerList().broadcastAll(new AttachedComputerPacket(blockEntity.getBlockPos(), hasAttachedComputer));
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return this == other;
    }

    public void prepareComputerEvent(@NotNull ComputerEvent event) {
    }

    /**
     * Queue an event to all attached computers. Adds the peripheral attachment name as 1st event argument, followed by
     * any optional arguments passed to this method.
     */
    protected void queueEvent(@NotNull String event, @Nullable Object... arguments) {
        Object[] sourceAndArgs = new Object[arguments.length + 1];
        System.arraycopy(arguments, 0, sourceAndArgs, 1, arguments.length);
        synchronized (computers) {
            for (IComputerAccess computer : computers) {
                sourceAndArgs[0] = computer.getAttachmentName();
                computer.queueEvent(event, sourceAndArgs);
            }
        }
    }

}
