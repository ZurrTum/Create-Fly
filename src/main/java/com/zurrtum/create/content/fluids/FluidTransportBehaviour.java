package com.zurrtum.create.content.fluids;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.WorldAttached;
import com.zurrtum.create.content.fluids.pipes.EncasedPipeBlock;
import com.zurrtum.create.content.fluids.pump.PumpBlock;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.block.BlockState;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Predicate;

public abstract class FluidTransportBehaviour extends BlockEntityBehaviour<SmartBlockEntity> {

    public static final BehaviourType<FluidTransportBehaviour> TYPE = new BehaviourType<>();

    public enum UpdatePhase {
        WAIT_FOR_PUMPS, // Do not run Layer II logic while pumps could still be distributing pressure
        FLIP_FLOWS, // Do not cut any flows until all pipes had a chance to reverse them
        IDLE; // Operate normally
    }

    public Map<Direction, PipeConnection> interfaces;
    public UpdatePhase phase;

    public FluidTransportBehaviour(SmartBlockEntity be) {
        super(be);
        phase = UpdatePhase.WAIT_FOR_PUMPS;
    }

    public boolean canPullFluidFrom(FluidStack fluid, BlockState state, Direction direction) {
        return true;
    }

    public abstract boolean canHaveFlowToward(BlockState state, Direction direction);

    @Override
    public void initialize() {
        super.initialize();
        createConnectionData();
    }

    @Override
    public void tick() {
        super.tick();
        World world = getWorld();
        BlockPos pos = getPos();
        boolean onServer = !world.isClient || blockEntity.isVirtual();

        if (interfaces == null)
            return;
        Collection<PipeConnection> connections = interfaces.values();

        // Do not provide a lone pipe connection with its own flow input
        PipeConnection singleSource = null;

        //		if (onClient) {
        //			connections.forEach(connection -> {
        //				connection.visualizeFlow(pos);
        //				connection.visualizePressure(pos);
        //			});
        //		}

        if (phase == UpdatePhase.WAIT_FOR_PUMPS) {
            phase = UpdatePhase.FLIP_FLOWS;
            return;
        }

        if (onServer) {
            boolean sendUpdate = false;
            for (PipeConnection connection : connections) {
                sendUpdate |= connection.flipFlowsIfPressureReversed();
                connection.manageSource(world, pos, blockEntity);
            }
            if (sendUpdate)
                blockEntity.notifyUpdate();
        }

        if (phase == UpdatePhase.FLIP_FLOWS) {
            phase = UpdatePhase.IDLE;
            return;
        }

        if (onServer) {
            FluidStack availableFlow = FluidStack.EMPTY;
            FluidStack collidingFlow = FluidStack.EMPTY;

            for (PipeConnection connection : connections) {
                FluidStack fluidInFlow = connection.getProvidedFluid();
                if (fluidInFlow.isEmpty())
                    continue;
                if (availableFlow.isEmpty()) {
                    singleSource = connection;
                    availableFlow = fluidInFlow;
                    continue;
                }
                if (FluidStack.areFluidsAndComponentsEqualIgnoreCapacity(availableFlow, fluidInFlow)) {
                    singleSource = null;
                    availableFlow = fluidInFlow;
                    continue;
                }
                collidingFlow = fluidInFlow;
                break;
            }

            if (!collidingFlow.isEmpty()) {
                FluidReactions.handlePipeFlowCollision(world, pos, availableFlow, collidingFlow);
                return;
            }

            boolean sendUpdate = false;
            for (PipeConnection connection : connections) {
                FluidStack internalFluid = singleSource != connection ? availableFlow : FluidStack.EMPTY;
                Predicate<FluidStack> extractionPredicate = extracted -> canPullFluidFrom(extracted, blockEntity.getCachedState(), connection.side);
                sendUpdate |= connection.manageFlows(world, pos, internalFluid, extractionPredicate);
            }

            if (sendUpdate)
                blockEntity.notifyUpdate();
        }

        for (PipeConnection connection : connections)
            connection.tickFlowProgress(world, pos);
    }

    @Override
    public void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        BlockPos pos = blockEntity.getPos();
        for (Direction face : Iterate.directions) {
            view.getOptionalReadView(face.getId()).ifPresent(data -> {
                if (interfaces == null)
                    interfaces = new IdentityHashMap<>();
                interfaces.computeIfAbsent(face, PipeConnection::new).read(data, pos, clientPacket);
            });
        }
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        if (clientPacket)
            createConnectionData();
        if (interfaces == null)
            return;

        BlockPos pos = blockEntity.getPos();
        interfaces.values().forEach(connection -> connection.write(view.get(connection.side.getId()), pos, clientPacket));
    }

    public FluidStack getProvidedOutwardFluid(Direction side) {
        createConnectionData();
        if (!interfaces.containsKey(side))
            return FluidStack.EMPTY;
        return interfaces.get(side).provideOutboundFlow();
    }

    @Nullable
    public PipeConnection getConnection(Direction side) {
        createConnectionData();
        return interfaces.get(side);
    }

    public boolean hasAnyPressure() {
        createConnectionData();
        for (PipeConnection pipeConnection : interfaces.values())
            if (pipeConnection.hasPressure())
                return true;
        return false;
    }

    @Nullable
    public PipeConnection.Flow getFlow(Direction side) {
        createConnectionData();
        if (!interfaces.containsKey(side))
            return null;
        return interfaces.get(side).flow.orElse(null);
    }

    public void addPressure(Direction side, boolean inbound, float pressure) {
        createConnectionData();
        if (!interfaces.containsKey(side))
            return;
        interfaces.get(side).addPressure(inbound, pressure);
        blockEntity.sendData();
    }

    public void wipePressure() {
        if (interfaces != null)
            for (Direction d : Iterate.directions) {
                if (!canHaveFlowToward(blockEntity.getCachedState(), d))
                    interfaces.remove(d);
                else
                    interfaces.computeIfAbsent(d, PipeConnection::new);
            }
        phase = UpdatePhase.WAIT_FOR_PUMPS;
        createConnectionData();
        interfaces.values().forEach(PipeConnection::wipePressure);
        blockEntity.sendData();
    }

    private void createConnectionData() {
        if (interfaces != null)
            return;
        interfaces = new IdentityHashMap<>();
        for (Direction d : Iterate.directions)
            if (canHaveFlowToward(blockEntity.getCachedState(), d))
                interfaces.put(d, new PipeConnection(d));
    }

    public AttachmentTypes getRenderedRimAttachment(BlockRenderView world, BlockPos pos, BlockState state, Direction direction) {
        if (!canHaveFlowToward(state, direction))
            return AttachmentTypes.NONE;

        BlockPos offsetPos = pos.offset(direction);
        BlockState facingState = world.getBlockState(offsetPos);

        if (facingState.getBlock() instanceof PumpBlock && facingState.get(PumpBlock.FACING) == direction.getOpposite())
            return AttachmentTypes.NONE;

        if (facingState.isOf(AllBlocks.ENCASED_FLUID_PIPE) && facingState.get(EncasedPipeBlock.FACING_TO_PROPERTY_MAP.get(direction.getOpposite())))
            return AttachmentTypes.RIM;

        if (FluidPropagator.hasFluidCapability(world, offsetPos, direction.getOpposite()) && !facingState.isOf(AllBlocks.HOSE_PULLEY))
            return AttachmentTypes.DRAIN;

        return AttachmentTypes.RIM;
    }

    public enum AttachmentTypes {
        NONE,
        CONNECTION(ComponentPartials.CONNECTION),
        DETAILED_CONNECTION(ComponentPartials.RIM_CONNECTOR),
        RIM(ComponentPartials.RIM_CONNECTOR, ComponentPartials.RIM),
        PARTIAL_RIM(ComponentPartials.RIM),
        DRAIN(ComponentPartials.RIM_CONNECTOR, ComponentPartials.DRAIN),
        PARTIAL_DRAIN(ComponentPartials.DRAIN);

        public final ComponentPartials[] partials;

        AttachmentTypes(ComponentPartials... partials) {
            this.partials = partials;
        }

        public AttachmentTypes withoutConnector() {
            if (this == AttachmentTypes.RIM)
                return AttachmentTypes.PARTIAL_RIM;
            if (this == AttachmentTypes.DRAIN)
                return AttachmentTypes.PARTIAL_DRAIN;
            return this;
        }

        public enum ComponentPartials {
            CONNECTION,
            RIM_CONNECTOR,
            RIM,
            DRAIN;
        }
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    // for switching TEs, but retaining flows

    public static final WorldAttached<Map<BlockPos, Map<Direction, PipeConnection>>> interfaceTransfer = new WorldAttached<>($ -> new HashMap<>());

    public static void cacheFlows(WorldAccess world, BlockPos pos) {
        FluidTransportBehaviour pipe = BlockEntityBehaviour.get(world, pos, FluidTransportBehaviour.TYPE);
        if (pipe != null)
            interfaceTransfer.get(world).put(pos, pipe.interfaces);
    }

    public static void loadFlows(WorldAccess world, BlockPos pos) {
        FluidTransportBehaviour newPipe = BlockEntityBehaviour.get(world, pos, FluidTransportBehaviour.TYPE);
        if (newPipe != null)
            newPipe.interfaces = interfaceTransfer.get(world).remove(pos);
    }

}
