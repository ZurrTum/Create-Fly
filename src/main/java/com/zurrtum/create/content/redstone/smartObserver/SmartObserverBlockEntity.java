package com.zurrtum.create.content.redstone.smartObserver;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.BlockFace;
import com.zurrtum.create.content.fluids.FluidTransportBehaviour;
import com.zurrtum.create.content.fluids.PipeConnection.Flow;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import com.zurrtum.create.content.redstone.DirectedDirectionalBlock;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.CapManipulationBehaviourBase.InterfaceProvider;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.TankManipulationBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.VersionedInventoryTrackerBehaviour;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;

public class SmartObserverBlockEntity extends SmartBlockEntity {

    private static final int DEFAULT_DELAY = 6;
    private ServerFilteringBehaviour filtering;
    private InvManipulationBehaviour observedInventory;
    private TankManipulationBehaviour observedTank;

    private VersionedInventoryTrackerBehaviour invVersionTracker;
    private boolean sustainSignal;

    public int turnOffTicks = 0;

    public SmartObserverBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.SMART_OBSERVER, pos, state);
        setLazyTickRate(20);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(filtering = new ServerFilteringBehaviour(this).withCallback($ -> invVersionTracker.reset()));
        behaviours.add(invVersionTracker = new VersionedInventoryTrackerBehaviour(this));

        InterfaceProvider towardBlockFacing = (w, p, s) -> new BlockFace(p, DirectedDirectionalBlock.getTargetDirection(s));

        behaviours.add(observedInventory = new InvManipulationBehaviour(this, towardBlockFacing).bypassSidedness());
        behaviours.add(observedTank = new TankManipulationBehaviour(this, towardBlockFacing).bypassSidedness());
    }

    @Override
    public void tick() {
        super.tick();

        if (world.isClient())
            return;

        BlockState state = getCachedState();
        if (turnOffTicks > 0) {
            turnOffTicks--;
            if (turnOffTicks == 0)
                world.scheduleBlockTick(pos, state.getBlock(), 1);
        }

        if (!isActive())
            return;

        BlockPos targetPos = pos.offset(SmartObserverBlock.getTargetDirection(state));
        Block block = world.getBlockState(targetPos).getBlock();

        if (!filtering.getFilter().isEmpty() && block.asItem() != null && filtering.test(new ItemStack(block))) {
            activate(3);
            return;
        }

        // Detect items on belt
        TransportedItemStackHandlerBehaviour behaviour = BlockEntityBehaviour.get(world, targetPos, TransportedItemStackHandlerBehaviour.TYPE);
        if (behaviour != null) {
            behaviour.handleCenteredProcessingOnAllItems(
                .45f, stack -> {
                    if (!filtering.test(stack.stack) || turnOffTicks == 6)
                        return TransportedResult.doNothing();
                    activate();
                    return TransportedResult.doNothing();
                }
            );
            return;
        }

        // Detect fluids in pipe
        FluidTransportBehaviour fluidBehaviour = BlockEntityBehaviour.get(world, targetPos, FluidTransportBehaviour.TYPE);
        if (fluidBehaviour != null) {
            for (Direction side : Iterate.directions) {
                Flow flow = fluidBehaviour.getFlow(side);
                if (flow == null || !flow.inbound || !flow.complete)
                    continue;
                if (!filtering.test(flow.fluid))
                    continue;
                activate();
                return;
            }
            return;
        }

        // Detect packages looping on a chain conveyor
        if (world.getBlockEntity(targetPos) instanceof ChainConveyorBlockEntity ccbe) {
            for (ChainConveyorPackage box : ccbe.getLoopingPackages())
                if (filtering.test(box.item)) {
                    activate();
                    return;
                }
            return;
        }

        if (observedInventory.hasInventory()) {
            boolean skipInv = invVersionTracker.stillWaiting(observedInventory);
            invVersionTracker.awaitNewVersion(observedInventory);

            if (skipInv && sustainSignal)
                turnOffTicks = DEFAULT_DELAY;

            if (!skipInv) {
                sustainSignal = false;
                if (!observedInventory.simulate().extract().isEmpty()) {
                    sustainSignal = true;
                    activate();
                    return;
                }
            }
        }

        if (!observedTank.simulate().extractAny().isEmpty()) {
            activate();
        }
    }

    public void activate() {
        activate(DEFAULT_DELAY);
    }

    public void activate(int ticks) {
        BlockState state = getCachedState();
        turnOffTicks = ticks;
        if (state.get(SmartObserverBlock.POWERED))
            return;
        world.setBlockState(pos, state.with(SmartObserverBlock.POWERED, true));
        world.updateNeighborsAlways(pos, state.getBlock(), null);
    }

    private boolean isActive() {
        return true;
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        view.putInt("TurnOff", turnOffTicks);
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        turnOffTicks = view.getInt("TurnOff", 0);
    }

}
