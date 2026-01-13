package com.zurrtum.create.content.fluids.pump;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.BlockFace;
import com.zurrtum.create.content.fluids.FluidPropagator;
import com.zurrtum.create.content.fluids.FluidTransportBehaviour;
import com.zurrtum.create.content.fluids.PipeConnection;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.WorldAccess;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PumpBlockEntity extends KineticBlockEntity {

    Couple<MutableBoolean> sidesToUpdate;
    boolean pressureUpdate;

    // Backcompat- flips any pump blockstate that loads with reversed=true
    boolean scheduleFlip;

    public PumpBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.MECHANICAL_PUMP, pos, state);
        sidesToUpdate = Couple.create(MutableBoolean::new);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(new PumpFluidTransferBehaviour(this));
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        List<CreateTrigger> list = FluidPropagator.getSharedTriggers();
        list.add(AllAdvancements.PUMP);
        return list;
    }

    @Override
    public void tick() {
        super.tick();

        if (world.isClient() && !isVirtual())
            return;

        if (scheduleFlip) {
            world.setBlockState(pos, getCachedState().with(PumpBlock.FACING, getCachedState().get(PumpBlock.FACING).getOpposite()));
            scheduleFlip = false;
        }

        sidesToUpdate.forEachWithContext((update, isFront) -> {
            if (update.isFalse())
                return;
            update.setFalse();
            distributePressureTo(isFront ? getFront() : getFront().getOpposite());
        });
    }

    @Override
    public void onSpeedChanged(float previousSpeed) {
        super.onSpeedChanged(previousSpeed);

        if (Math.abs(previousSpeed) == Math.abs(getSpeed()))
            return;
        if (speed != 0)
            award(AllAdvancements.PUMP);
        if (world.isClient() && !isVirtual())
            return;

        updatePressureChange();
    }

    public void updatePressureChange() {
        pressureUpdate = false;
        BlockPos frontPos = pos.offset(getFront());
        BlockPos backPos = pos.offset(getFront().getOpposite());
        FluidPropagator.propagateChangedPipe(world, frontPos, world.getBlockState(frontPos));
        FluidPropagator.propagateChangedPipe(world, backPos, world.getBlockState(backPos));

        FluidTransportBehaviour behaviour = getBehaviour(FluidTransportBehaviour.TYPE);
        if (behaviour != null)
            behaviour.wipePressure();
        sidesToUpdate.forEach(MutableBoolean::setTrue);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        if (view.getBoolean("Reversed", false))
            scheduleFlip = true;
    }

    protected void distributePressureTo(Direction side) {
        if (getSpeed() == 0)
            return;

        BlockFace start = new BlockFace(pos, side);
        boolean pull = isPullingOnSide(isFront(side));
        Set<BlockFace> targets = new HashSet<>();
        Map<BlockPos, Pair<Integer, Map<Direction, Boolean>>> pipeGraph = new HashMap<>();

        if (!pull)
            FluidPropagator.resetAffectedFluidNetworks(world, pos, side.getOpposite());

        if (!hasReachedValidEndpoint(world, start, pull)) {

            pipeGraph.computeIfAbsent(pos, $ -> Pair.of(0, new IdentityHashMap<>())).getSecond().put(side, pull);
            pipeGraph.computeIfAbsent(start.getConnectedPos(), $ -> Pair.of(1, new IdentityHashMap<>())).getSecond().put(side.getOpposite(), !pull);

            List<Pair<Integer, BlockPos>> frontier = new ArrayList<>();
            Set<BlockPos> visited = new HashSet<>();
            int maxDistance = FluidPropagator.getPumpRange();
            frontier.add(Pair.of(1, start.getConnectedPos()));

            while (!frontier.isEmpty()) {
                Pair<Integer, BlockPos> entry = frontier.remove(0);
                int distance = entry.getFirst();
                BlockPos currentPos = entry.getSecond();

                if (!world.isPosLoaded(currentPos))
                    continue;
                if (visited.contains(currentPos))
                    continue;
                visited.add(currentPos);
                BlockState currentState = world.getBlockState(currentPos);
                FluidTransportBehaviour pipe = FluidPropagator.getPipe(world, currentPos);
                if (pipe == null)
                    continue;

                for (Direction face : FluidPropagator.getPipeConnections(currentState, pipe)) {
                    BlockFace blockFace = new BlockFace(currentPos, face);
                    BlockPos connectedPos = blockFace.getConnectedPos();

                    if (!world.isPosLoaded(connectedPos))
                        continue;
                    if (blockFace.isEquivalent(start))
                        continue;
                    if (hasReachedValidEndpoint(world, blockFace, pull)) {
                        pipeGraph.computeIfAbsent(currentPos, $ -> Pair.of(distance, new IdentityHashMap<>())).getSecond().put(face, pull);
                        targets.add(blockFace);
                        continue;
                    }

                    FluidTransportBehaviour pipeBehaviour = FluidPropagator.getPipe(world, connectedPos);
                    if (pipeBehaviour == null)
                        continue;
                    if (pipeBehaviour instanceof PumpFluidTransferBehaviour)
                        continue;
                    if (visited.contains(connectedPos))
                        continue;
                    if (distance + 1 >= maxDistance) {
                        pipeGraph.computeIfAbsent(currentPos, $ -> Pair.of(distance, new IdentityHashMap<>())).getSecond().put(face, pull);
                        targets.add(blockFace);
                        continue;
                    }

                    pipeGraph.computeIfAbsent(currentPos, $ -> Pair.of(distance, new IdentityHashMap<>())).getSecond().put(face, pull);
                    pipeGraph.computeIfAbsent(connectedPos, $ -> Pair.of(distance + 1, new IdentityHashMap<>())).getSecond()
                        .put(face.getOpposite(), !pull);
                    frontier.add(Pair.of(distance + 1, connectedPos));
                }
            }
        }

        // DFS
        Map<Integer, Set<BlockFace>> validFaces = new HashMap<>();
        searchForEndpointRecursively(pipeGraph, targets, validFaces, new BlockFace(start.getPos(), start.getOppositeFace()), pull);

        float pressure = Math.abs(getSpeed());
        for (Set<BlockFace> set : validFaces.values()) {
            int parallelBranches = Math.max(1, set.size() - 1);
            for (BlockFace face : set) {
                BlockPos pipePos = face.getPos();
                Direction pipeSide = face.getFace();

                if (pipePos.equals(pos))
                    continue;

                boolean inbound = pipeGraph.get(pipePos).getSecond().get(pipeSide);
                FluidTransportBehaviour pipeBehaviour = FluidPropagator.getPipe(world, pipePos);
                if (pipeBehaviour == null)
                    continue;

                pipeBehaviour.addPressure(pipeSide, inbound, pressure / parallelBranches);
            }
        }

    }

    protected boolean searchForEndpointRecursively(
        Map<BlockPos, Pair<Integer, Map<Direction, Boolean>>> pipeGraph,
        Set<BlockFace> targets,
        Map<Integer, Set<BlockFace>> validFaces,
        BlockFace currentFace,
        boolean pull
    ) {
        BlockPos currentPos = currentFace.getPos();
        if (!pipeGraph.containsKey(currentPos))
            return false;
        Pair<Integer, Map<Direction, Boolean>> pair = pipeGraph.get(currentPos);
        int distance = pair.getFirst();

        boolean atLeastOneBranchSuccessful = false;
        for (Direction nextFacing : Iterate.directions) {
            if (nextFacing == currentFace.getFace())
                continue;
            Map<Direction, Boolean> map = pair.getSecond();
            if (!map.containsKey(nextFacing))
                continue;

            BlockFace localTarget = new BlockFace(currentPos, nextFacing);
            if (targets.contains(localTarget)) {
                validFaces.computeIfAbsent(distance, $ -> new HashSet<>()).add(localTarget);
                atLeastOneBranchSuccessful = true;
                continue;
            }

            if (map.get(nextFacing) != pull)
                continue;
            if (!searchForEndpointRecursively(
                pipeGraph,
                targets,
                validFaces,
                new BlockFace(currentPos.offset(nextFacing), nextFacing.getOpposite()),
                pull
            ))
                continue;

            validFaces.computeIfAbsent(distance, $ -> new HashSet<>()).add(localTarget);
            atLeastOneBranchSuccessful = true;
        }

        if (atLeastOneBranchSuccessful)
            validFaces.computeIfAbsent(distance, $ -> new HashSet<>()).add(currentFace);

        return atLeastOneBranchSuccessful;
    }

    private boolean hasReachedValidEndpoint(WorldAccess world, BlockFace blockFace, boolean pull) {
        BlockPos connectedPos = blockFace.getConnectedPos();
        BlockState connectedState = world.getBlockState(connectedPos);
        BlockEntity blockEntity = world.getBlockEntity(connectedPos);
        Direction face = blockFace.getFace();

        // facing a pump
        if (PumpBlock.isPump(connectedState) && connectedState.get(PumpBlock.FACING)
            .getAxis() == face.getAxis() && blockEntity instanceof PumpBlockEntity pumpBE) {
            return pumpBE.isPullingOnSide(pumpBE.isFront(blockFace.getOppositeFace())) != pull;
        }

        // other pipe, no endpoint
        FluidTransportBehaviour pipe = FluidPropagator.getPipe(world, connectedPos);
        if (pipe != null && pipe.canHaveFlowToward(connectedState, blockFace.getOppositeFace()))
            return false;

        // fluid handler endpoint
        if (blockEntity != null) {
            boolean hasCapability = FluidHelper.hasFluidInventory(
                blockEntity.getWorld(),
                connectedPos,
                connectedState,
                blockEntity,
                face.getOpposite()
            );
            if (hasCapability)
                return true;
        }

        // open endpoint
        return FluidPropagator.isOpenEnd(world, blockFace.getPos(), face);
    }

    public void updatePipesOnSide(Direction side) {
        if (!isSideAccessible(side))
            return;
        updatePipeNetwork(isFront(side));
        getBehaviour(FluidTransportBehaviour.TYPE).wipePressure();
    }

    protected boolean isFront(Direction side) {
        BlockState blockState = getCachedState();
        if (!(blockState.getBlock() instanceof PumpBlock))
            return false;
        Direction front = blockState.get(PumpBlock.FACING);
        boolean isFront = side == front;
        return isFront;
    }

    @Nullable
    protected Direction getFront() {
        BlockState blockState = getCachedState();
        if (!(blockState.getBlock() instanceof PumpBlock))
            return null;
        return blockState.get(PumpBlock.FACING);
    }

    protected void updatePipeNetwork(boolean front) {
        sidesToUpdate.get(front).setTrue();
    }

    public boolean isSideAccessible(Direction side) {
        BlockState blockState = getCachedState();
        if (!(blockState.getBlock() instanceof PumpBlock))
            return false;
        return blockState.get(PumpBlock.FACING).getAxis() == side.getAxis();
    }

    public boolean isPullingOnSide(boolean front) {
        return !front;
    }

    class PumpFluidTransferBehaviour extends FluidTransportBehaviour {

        public PumpFluidTransferBehaviour(SmartBlockEntity be) {
            super(be);
        }

        @Override
        public void tick() {
            super.tick();
            for (Map.Entry<Direction, PipeConnection> entry : interfaces.entrySet()) {
                boolean pull = isPullingOnSide(isFront(entry.getKey()));
                Couple<Float> pressure = entry.getValue().getPressure();
                pressure.set(pull, Math.abs(getSpeed()));
                pressure.set(!pull, 0f);
            }
        }

        @Override
        public boolean canHaveFlowToward(BlockState state, Direction direction) {
            return isSideAccessible(direction);
        }

        @Override
        public AttachmentTypes getRenderedRimAttachment(BlockRenderView world, BlockPos pos, BlockState state, Direction direction) {
            AttachmentTypes attachment = super.getRenderedRimAttachment(world, pos, state, direction);
            if (attachment == AttachmentTypes.RIM)
                return AttachmentTypes.NONE;
            return attachment;
        }

    }
}
