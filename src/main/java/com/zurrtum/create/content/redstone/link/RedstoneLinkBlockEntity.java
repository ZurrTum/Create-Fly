package com.zurrtum.create.content.redstone.link;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelSupportBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;

public class RedstoneLinkBlockEntity extends SmartBlockEntity {

    private boolean receivedSignalChanged;
    private int receivedSignal;
    private int transmittedSignal;
    private ServerLinkBehaviour link;
    private boolean transmitter;

    public FactoryPanelSupportBehaviour panelSupport;

    public RedstoneLinkBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.REDSTONE_LINK, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(panelSupport = new FactoryPanelSupportBehaviour(
            this,
            () -> link != null && link.isListening(),
            () -> receivedSignal > 0,
            () -> AllBlocks.REDSTONE_LINK.updateTransmittedSignal(getCachedState(), world, pos)
        ));
    }

    @Override
    public void addBehavioursDeferred(List<BlockEntityBehaviour<?>> behaviours) {
        createLink();
        behaviours.add(link);
    }

    protected void createLink() {
        link = transmitter ? ServerLinkBehaviour.transmitter(this, this::getSignal) : ServerLinkBehaviour.receiver(this, this::setSignal);
    }

    public int getSignal() {
        return transmittedSignal;
    }

    public void setSignal(int power) {
        if (receivedSignal != power)
            receivedSignalChanged = true;
        receivedSignal = power;
    }

    public void transmit(int strength) {
        transmittedSignal = strength;
        if (link != null)
            link.notifySignalChange();
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        view.putBoolean("Transmitter", transmitter);
        view.putInt("Receive", getReceivedSignal());
        view.putBoolean("ReceivedChanged", receivedSignalChanged);
        view.putInt("Transmit", transmittedSignal);
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        transmitter = view.getBoolean("Transmitter", false);
        super.read(view, clientPacket);

        receivedSignal = view.getInt("Receive", 0);
        receivedSignalChanged = view.getBoolean("ReceivedChanged", false);
        if (world == null || world.isClient() || !link.newPosition)
            transmittedSignal = view.getInt("Transmit", 0);
    }

    @Override
    public void tick() {
        super.tick();

        if (isTransmitterBlock() != transmitter) {
            transmitter = isTransmitterBlock();
            ServerLinkBehaviour prevlink = link;
            removeBehaviour(ServerLinkBehaviour.TYPE);
            createLink();
            link.copyItemsFrom(prevlink);
            attachBehaviourLate(link);
        }

        if (transmitter)
            return;
        if (world.isClient())
            return;

        BlockState blockState = getCachedState();
        if (!blockState.isOf(AllBlocks.REDSTONE_LINK))
            return;

        if ((getReceivedSignal() > 0) != blockState.get(RedstoneLinkBlock.POWERED)) {
            receivedSignalChanged = true;
            world.setBlockState(pos, blockState.cycle(RedstoneLinkBlock.POWERED));
        }

        if (receivedSignalChanged) {
            updateSelfAndAttached(blockState);
        }
    }

    @Override
    public void remove() {
        super.remove();

        updateSelfAndAttached(getCachedState());
    }

    public void updateSelfAndAttached(BlockState blockState) {
        Direction attachedFace = blockState.get(RedstoneLinkBlock.FACING).getOpposite();
        BlockPos attachedPos = pos.offset(attachedFace);
        world.updateNeighbors(pos, world.getBlockState(pos).getBlock());
        world.updateNeighbors(attachedPos, world.getBlockState(attachedPos).getBlock());
        receivedSignalChanged = false;
        panelSupport.notifyPanels();
    }

    protected Boolean isTransmitterBlock() {
        return !getCachedState().get(RedstoneLinkBlock.RECEIVER);
    }

    public int getReceivedSignal() {
        return receivedSignal;
    }

}
