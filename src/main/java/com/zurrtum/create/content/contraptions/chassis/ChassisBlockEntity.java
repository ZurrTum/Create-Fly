package com.zurrtum.create.content.contraptions.chassis;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.contraption.BlockMovementChecks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerChassisScrollValueBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.ReadView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;

import java.util.*;

public class ChassisBlockEntity extends SmartBlockEntity {

    ServerScrollValueBehaviour range;

    public int currentlySelectedRange;

    public ChassisBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.CHASSIS, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        int max = AllConfigs.server().kinetics.maxChassisRange.get();
        range = new ServerChassisScrollValueBehaviour(this, be -> ((ChassisBlockEntity) be).collectChassisGroup());
        range.between(1, max);
        range.setValue(max / 2);
        behaviours.add(range);
        currentlySelectedRange = range.getValue();
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        if (clientPacket)
            currentlySelectedRange = getRange();
    }

    public int getRange() {
        return range.getValue();
    }

    public List<BlockPos> getIncludedBlockPositions(Direction forcedMovement, boolean visualize) {
        if (!(getCachedState().getBlock() instanceof AbstractChassisBlock))
            return Collections.emptyList();
        return isRadial() ? getIncludedBlockPositionsRadial(forcedMovement, visualize) : getIncludedBlockPositionsLinear(forcedMovement, visualize);
    }

    protected boolean isRadial() {
        return world.getBlockState(pos).getBlock() instanceof RadialChassisBlock;
    }

    public List<ChassisBlockEntity> collectChassisGroup() {
        Queue<BlockPos> frontier = new LinkedList<>();
        List<ChassisBlockEntity> collected = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        frontier.add(pos);
        while (!frontier.isEmpty()) {
            BlockPos current = frontier.poll();
            if (visited.contains(current))
                continue;
            visited.add(current);
            BlockEntity blockEntity = world.getBlockEntity(current);
            if (blockEntity instanceof ChassisBlockEntity chassis) {
                collected.add(chassis);
                visited.add(current);
                chassis.addAttachedChasses(frontier, visited);
            }
        }
        return collected;
    }

    public boolean addAttachedChasses(Queue<BlockPos> frontier, Set<BlockPos> visited) {
        BlockState state = getCachedState();
        if (!(state.getBlock() instanceof AbstractChassisBlock))
            return false;
        Axis axis = state.get(AbstractChassisBlock.AXIS);
        if (isRadial()) {

            // Collect chain of radial chassis
            for (int offset : new int[]{-1, 1}) {
                Direction direction = Direction.get(AxisDirection.POSITIVE, axis);
                BlockPos currentPos = pos.offset(direction, offset);
                if (!world.isPosLoaded(currentPos))
                    return false;

                BlockState neighbourState = world.getBlockState(currentPos);
                if (!neighbourState.isOf(AllBlocks.RADIAL_CHASSIS))
                    continue;
                if (axis != neighbourState.get(Properties.AXIS))
                    continue;
                if (!visited.contains(currentPos))
                    frontier.add(currentPos);
            }

            return true;
        }

        // Collect group of connected linear chassis
        for (Direction offset : Iterate.directions) {
            BlockPos current = pos.offset(offset);
            if (visited.contains(current))
                continue;
            if (!world.isPosLoaded(current))
                return false;

            BlockState neighbourState = world.getBlockState(current);
            if (!LinearChassisBlock.isChassis(neighbourState))
                continue;
            if (!LinearChassisBlock.sameKind(state, neighbourState))
                continue;
            if (neighbourState.get(LinearChassisBlock.AXIS) != axis)
                continue;

            frontier.add(current);
        }

        return true;
    }

    private List<BlockPos> getIncludedBlockPositionsLinear(Direction forcedMovement, boolean visualize) {
        List<BlockPos> positions = new ArrayList<>();
        BlockState state = getCachedState();
        AbstractChassisBlock block = (AbstractChassisBlock) state.getBlock();
        Axis axis = state.get(AbstractChassisBlock.AXIS);
        Direction facing = Direction.get(AxisDirection.POSITIVE, axis);
        int chassisRange = visualize ? currentlySelectedRange : getRange();

        for (int offset : new int[]{1, -1}) {
            if (offset == -1)
                facing = facing.getOpposite();
            boolean sticky = state.get(block.getGlueableSide(state, facing));
            for (int i = 1; i <= chassisRange; i++) {
                BlockPos current = pos.offset(facing, i);
                BlockState currentState = world.getBlockState(current);

                if (forcedMovement != facing && !sticky)
                    break;

                // Ignore replaceable Blocks and Air-like
                if (!BlockMovementChecks.isMovementNecessary(currentState, world, current))
                    break;
                if (BlockMovementChecks.isBrittle(currentState))
                    break;

                positions.add(current);

                if (BlockMovementChecks.isNotSupportive(currentState, facing))
                    break;
            }
        }

        return positions;
    }

    private List<BlockPos> getIncludedBlockPositionsRadial(Direction forcedMovement, boolean visualize) {
        List<BlockPos> positions = new ArrayList<>();
        BlockState state = world.getBlockState(pos);
        Axis axis = state.get(AbstractChassisBlock.AXIS);
        AbstractChassisBlock block = (AbstractChassisBlock) state.getBlock();
        int chassisRange = visualize ? currentlySelectedRange : getRange();

        for (Direction facing : Iterate.directions) {
            if (facing.getAxis() == axis)
                continue;
            if (!state.get(block.getGlueableSide(state, facing)))
                continue;

            BlockPos startPos = pos.offset(facing);
            List<BlockPos> localFrontier = new LinkedList<>();
            Set<BlockPos> localVisited = new HashSet<>();
            localFrontier.add(startPos);

            while (!localFrontier.isEmpty()) {
                BlockPos searchPos = localFrontier.remove(0);
                BlockState searchedState = world.getBlockState(searchPos);

                if (localVisited.contains(searchPos))
                    continue;
                if (!searchPos.isWithinDistance(pos, chassisRange + .5f))
                    continue;
                if (!BlockMovementChecks.isMovementNecessary(searchedState, world, searchPos))
                    continue;
                if (BlockMovementChecks.isBrittle(searchedState))
                    continue;

                localVisited.add(searchPos);
                if (!searchPos.equals(pos))
                    positions.add(searchPos);

                for (Direction offset : Iterate.directions) {
                    if (offset.getAxis() == axis)
                        continue;
                    if (searchPos.equals(pos) && offset != facing)
                        continue;
                    if (BlockMovementChecks.isNotSupportive(searchedState, offset))
                        continue;

                    localFrontier.add(searchPos.offset(offset));
                }
            }
        }

        return positions;
    }
}
