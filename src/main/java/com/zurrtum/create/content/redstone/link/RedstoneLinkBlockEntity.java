package com.zurrtum.create.content.redstone.link;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelSupportBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
            () -> AllBlocks.REDSTONE_LINK.updateTransmittedSignal(getBlockState(), level, worldPosition)
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
    public void write(ValueOutput view, boolean clientPacket) {
        view.putBoolean("Transmitter", transmitter);
        view.putInt("Receive", getReceivedSignal());
        view.putBoolean("ReceivedChanged", receivedSignalChanged);
        view.putInt("Transmit", transmittedSignal);
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        transmitter = view.getBooleanOr("Transmitter", false);
        super.read(view, clientPacket);

        receivedSignal = view.getIntOr("Receive", 0);
        receivedSignalChanged = view.getBooleanOr("ReceivedChanged", false);
        if (level == null || level.isClientSide() || !link.newPosition)
            transmittedSignal = view.getIntOr("Transmit", 0);
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
        if (level.isClientSide())
            return;

        BlockState blockState = getBlockState();
        if (!blockState.is(AllBlocks.REDSTONE_LINK))
            return;

        if ((getReceivedSignal() > 0) != blockState.getValue(RedstoneLinkBlock.POWERED)) {
            receivedSignalChanged = true;
            level.setBlockAndUpdate(worldPosition, blockState.cycle(RedstoneLinkBlock.POWERED));
        }

        if (receivedSignalChanged) {
            updateSelfAndAttached(blockState);
        }
    }

    @Override
    public void remove() {
        super.remove();

        updateSelfAndAttached(getBlockState());
    }

    public void updateSelfAndAttached(BlockState blockState) {
        Direction attachedFace = blockState.getValue(RedstoneLinkBlock.FACING).getOpposite();
        BlockPos attachedPos = worldPosition.relative(attachedFace);
        level.updateNeighborsAt(worldPosition, level.getBlockState(worldPosition).getBlock());
        level.updateNeighborsAt(attachedPos, level.getBlockState(attachedPos).getBlock());
        receivedSignalChanged = false;
        panelSupport.notifyPanels();
    }

    protected Boolean isTransmitterBlock() {
        return !getBlockState().getValue(RedstoneLinkBlock.RECEIVER);
    }

    public int getReceivedSignal() {
        return receivedSignal;
    }

}
