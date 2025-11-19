package com.zurrtum.create.content.fluids;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.BlockFace;
import com.zurrtum.create.content.contraptions.actors.psi.PortableFluidInterfaceBlockEntity.InterfaceFluidHandler;
import com.zurrtum.create.content.fluids.PipeConnection.Flow;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Supplier;

public class FluidNetwork {

    private static final int CYCLES_PER_TICK = 16;

    Level world;
    BlockFace start;

    Supplier<FluidInventory> sourceSupplier;
    @Nullable FluidInventory source = null;
    int transferSpeed;

    int pauseBeforePropagation;
    List<BlockFace> queued;
    Set<Pair<BlockFace, PipeConnection>> frontier;
    Set<BlockPos> visited;
    FluidStack fluid;
    List<Pair<BlockFace, FlowSource>> targets;
    Map<BlockPos, WeakReference<FluidTransportBehaviour>> cache;

    public FluidNetwork(Level world, BlockFace location, Supplier<FluidInventory> sourceSupplier) {
        this.world = world;
        this.start = location;
        this.sourceSupplier = sourceSupplier;
        this.fluid = FluidStack.EMPTY;
        this.frontier = new HashSet<>();
        this.visited = new HashSet<>();
        this.targets = new ArrayList<>();
        this.cache = new HashMap<>();
        this.queued = new ArrayList<>();
        reset();
    }

    public void tick() {
        if (pauseBeforePropagation > 0) {
            pauseBeforePropagation--;
            return;
        }

        for (int cycle = 0; cycle < CYCLES_PER_TICK; cycle++) {
            boolean shouldContinue = false;
            for (Iterator<BlockFace> iterator = queued.iterator(); iterator.hasNext(); ) {
                BlockFace blockFace = iterator.next();
                if (!isPresent(blockFace))
                    continue;
                PipeConnection pipeConnection = get(blockFace);
                if (pipeConnection != null) {
                    if (blockFace.equals(start))
                        transferSpeed = (int) Math.max(1, pipeConnection.pressure.get(true) / 2f) * 81;
                    frontier.add(Pair.of(blockFace, pipeConnection));
                }
                iterator.remove();
            }

            //			drawDebugOutlines();

            for (Iterator<Pair<BlockFace, PipeConnection>> iterator = frontier.iterator(); iterator.hasNext(); ) {
                Pair<BlockFace, PipeConnection> pair = iterator.next();
                BlockFace blockFace = pair.getFirst();
                PipeConnection pipeConnection = pair.getSecond();

                if (!pipeConnection.hasFlow())
                    continue;

                Flow flow = pipeConnection.flow.get();
                if (!fluid.isEmpty() && !FluidStack.areFluidsAndComponentsEqualIgnoreCapacity(flow.fluid, fluid)) {
                    iterator.remove();
                    continue;
                }
                if (!flow.inbound) {
                    if (pipeConnection.comparePressure() >= 0)
                        iterator.remove();
                    continue;
                }
                if (!flow.complete)
                    continue;

                if (fluid.isEmpty())
                    fluid = flow.fluid;

                boolean canRemove = true;
                for (Direction side : Iterate.directions) {
                    if (side == blockFace.getFace())
                        continue;
                    BlockFace adjacentLocation = new BlockFace(blockFace.getPos(), side);
                    PipeConnection adjacent = get(adjacentLocation);
                    if (adjacent == null)
                        continue;
                    if (!adjacent.hasFlow()) {
                        // Branch could potentially still appear
                        if (adjacent.hasPressure() && adjacent.pressure.getSecond() > 0)
                            canRemove = false;
                        continue;
                    }
                    Flow outFlow = adjacent.flow.get();
                    if (outFlow.inbound) {
                        if (adjacent.comparePressure() > 0)
                            canRemove = false;
                        continue;
                    }
                    if (!outFlow.complete) {
                        canRemove = false;
                        continue;
                    }

                    // Give pipe end a chance to init connections
                    if (!adjacent.source.isPresent() && !adjacent.determineSource(world, blockFace.getPos())) {
                        canRemove = false;
                        continue;
                    }

                    if (adjacent.source.isPresent() && adjacent.source.get().isEndpoint()) {
                        targets.add(Pair.of(adjacentLocation, adjacent.source.get()));
                        continue;
                    }

                    if (visited.add(adjacentLocation.getConnectedPos())) {
                        queued.add(adjacentLocation.getOpposite());
                        shouldContinue = true;
                    }
                }
                if (canRemove)
                    iterator.remove();
            }
            if (!shouldContinue)
                break;
        }

        //		drawDebugOutlines();

        if (source == null)
            source = sourceSupplier.get();
        if (source == null)
            return;

        keepPortableFluidInterfaceEngaged();

        if (targets.isEmpty())
            return;
        for (Pair<BlockFace, FlowSource> pair : targets) {
            if (pair.getSecond() != null && world.getGameTime() % 40 != 0)
                continue;
            PipeConnection pipeConnection = get(pair.getFirst());
            if (pipeConnection == null)
                continue;
            pipeConnection.source.ifPresent(fs -> {
                if (fs.isEndpoint())
                    pair.setSecond(fs);
            });
        }

        int amount = source.count(fluid, transferSpeed, start.getFace().getOpposite());
        if (amount == 0) {
            return;
        }
        int transferredAmount = amount;
        List<Pair<BlockFace, FlowSource>> availableOutputs = new ArrayList<>(targets);
        while (!availableOutputs.isEmpty() && amount > 0) {
            int dividedTransfer = amount / availableOutputs.size();
            int remainder = amount % availableOutputs.size();
            for (Iterator<Pair<BlockFace, FlowSource>> iterator = availableOutputs.iterator(); amount > 0 && iterator.hasNext(); ) {
                Pair<BlockFace, FlowSource> pair = iterator.next();
                int toTransfer = dividedTransfer;
                if (remainder > 0) {
                    toTransfer++;
                    remainder--;
                }
                @Nullable FluidInventory targetHandlerProvider = pair.getSecond().provideHandler();
                if (targetHandlerProvider == null) {
                    iterator.remove();
                    continue;
                }
                int fill = targetHandlerProvider.insert(fluid, toTransfer, pair.getFirst().getFace().getOpposite());
                if (fill == toTransfer) {
                    amount -= fill;
                } else {
                    iterator.remove();
                    if (fill != 0) {
                        amount -= fill;
                    }
                }
            }
        }
        if (transferredAmount != amount) {
            source.extract(fluid, transferredAmount - amount, start.getFace().getOpposite());
        }
    }

    //	private void drawDebugOutlines() {
    //		FluidPropagator.showBlockFace(start)
    //			.lineWidth(1 / 8f)
    //			.colored(0xff0000);
    //		for (Pair<BlockFace, LazyOptional<IFluidHandler>> pair : targets)
    //			FluidPropagator.showBlockFace(pair.getFirst())
    //				.lineWidth(1 / 8f)
    //				.colored(0x00ff00);
    //		for (Pair<BlockFace, PipeConnection> pair : frontier)
    //			FluidPropagator.showBlockFace(pair.getFirst())
    //				.lineWidth(1 / 4f)
    //				.colored(0xfaaa33);
    //	}

    private void keepPortableFluidInterfaceEngaged() {
        if (!(source instanceof InterfaceFluidHandler))
            return;
        if (frontier.isEmpty())
            return;
        source.markDirty();
    }

    public void reset() {
        frontier.clear();
        visited.clear();
        targets.clear();
        queued.clear();
        fluid = FluidStack.EMPTY;
        queued.add(start);
        pauseBeforePropagation = 2;
    }

    @Nullable
    private PipeConnection get(BlockFace location) {
        BlockPos pos = location.getPos();
        FluidTransportBehaviour fluidTransfer = getFluidTransfer(pos);
        if (fluidTransfer == null)
            return null;
        return fluidTransfer.getConnection(location.getFace());
    }

    private boolean isPresent(BlockFace location) {
        return world.isLoaded(location.getPos());
    }

    @Nullable
    private FluidTransportBehaviour getFluidTransfer(BlockPos pos) {
        WeakReference<FluidTransportBehaviour> weakReference = cache.get(pos);
        FluidTransportBehaviour behaviour = weakReference != null ? weakReference.get() : null;
        if (behaviour != null && behaviour.blockEntity.isRemoved())
            behaviour = null;
        if (behaviour == null) {
            behaviour = BlockEntityBehaviour.get(world, pos, FluidTransportBehaviour.TYPE);
            if (behaviour != null)
                cache.put(pos, new WeakReference<>(behaviour));
        }
        return behaviour;
    }

}
