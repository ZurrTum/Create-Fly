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
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;

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
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        if (clientPacket)
            currentlySelectedRange = getRange();
    }

    public int getRange() {
        return range.getValue();
    }

    public List<BlockPos> getIncludedBlockPositions(Direction forcedMovement, boolean visualize) {
        if (!(getBlockState().getBlock() instanceof AbstractChassisBlock))
            return Collections.emptyList();
        return isRadial() ? getIncludedBlockPositionsRadial(forcedMovement, visualize) : getIncludedBlockPositionsLinear(forcedMovement, visualize);
    }

    protected boolean isRadial() {
        return level.getBlockState(worldPosition).getBlock() instanceof RadialChassisBlock;
    }

    public List<ChassisBlockEntity> collectChassisGroup() {
        Queue<BlockPos> frontier = new LinkedList<>();
        List<ChassisBlockEntity> collected = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        frontier.add(worldPosition);
        while (!frontier.isEmpty()) {
            BlockPos current = frontier.poll();
            if (visited.contains(current))
                continue;
            visited.add(current);
            BlockEntity blockEntity = level.getBlockEntity(current);
            if (blockEntity instanceof ChassisBlockEntity chassis) {
                collected.add(chassis);
                visited.add(current);
                chassis.addAttachedChasses(frontier, visited);
            }
        }
        return collected;
    }

    public boolean addAttachedChasses(Queue<BlockPos> frontier, Set<BlockPos> visited) {
        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof AbstractChassisBlock))
            return false;
        Axis axis = state.getValue(AbstractChassisBlock.AXIS);
        if (isRadial()) {

            // Collect chain of radial chassis
            for (int offset : new int[]{-1, 1}) {
                Direction direction = Direction.get(AxisDirection.POSITIVE, axis);
                BlockPos currentPos = worldPosition.relative(direction, offset);
                if (!level.isLoaded(currentPos))
                    return false;

                BlockState neighbourState = level.getBlockState(currentPos);
                if (!neighbourState.is(AllBlocks.RADIAL_CHASSIS))
                    continue;
                if (axis != neighbourState.getValue(BlockStateProperties.AXIS))
                    continue;
                if (!visited.contains(currentPos))
                    frontier.add(currentPos);
            }

            return true;
        }

        // Collect group of connected linear chassis
        for (Direction offset : Iterate.directions) {
            BlockPos current = worldPosition.relative(offset);
            if (visited.contains(current))
                continue;
            if (!level.isLoaded(current))
                return false;

            BlockState neighbourState = level.getBlockState(current);
            if (!LinearChassisBlock.isChassis(neighbourState))
                continue;
            if (!LinearChassisBlock.sameKind(state, neighbourState))
                continue;
            if (neighbourState.getValue(LinearChassisBlock.AXIS) != axis)
                continue;

            frontier.add(current);
        }

        return true;
    }

    private List<BlockPos> getIncludedBlockPositionsLinear(Direction forcedMovement, boolean visualize) {
        List<BlockPos> positions = new ArrayList<>();
        BlockState state = getBlockState();
        AbstractChassisBlock block = (AbstractChassisBlock) state.getBlock();
        Axis axis = state.getValue(AbstractChassisBlock.AXIS);
        Direction facing = Direction.get(AxisDirection.POSITIVE, axis);
        int chassisRange = visualize ? currentlySelectedRange : getRange();

        for (int offset : new int[]{1, -1}) {
            if (offset == -1)
                facing = facing.getOpposite();
            boolean sticky = state.getValue(block.getGlueableSide(state, facing));
            for (int i = 1; i <= chassisRange; i++) {
                BlockPos current = worldPosition.relative(facing, i);
                BlockState currentState = level.getBlockState(current);

                if (forcedMovement != facing && !sticky)
                    break;

                // Ignore replaceable Blocks and Air-like
                if (!BlockMovementChecks.isMovementNecessary(currentState, level, current))
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
        BlockState state = level.getBlockState(worldPosition);
        Axis axis = state.getValue(AbstractChassisBlock.AXIS);
        AbstractChassisBlock block = (AbstractChassisBlock) state.getBlock();
        int chassisRange = visualize ? currentlySelectedRange : getRange();

        for (Direction facing : Iterate.directions) {
            if (facing.getAxis() == axis)
                continue;
            if (!state.getValue(block.getGlueableSide(state, facing)))
                continue;

            BlockPos startPos = worldPosition.relative(facing);
            List<BlockPos> localFrontier = new LinkedList<>();
            Set<BlockPos> localVisited = new HashSet<>();
            localFrontier.add(startPos);

            while (!localFrontier.isEmpty()) {
                BlockPos searchPos = localFrontier.remove(0);
                BlockState searchedState = level.getBlockState(searchPos);

                if (localVisited.contains(searchPos))
                    continue;
                if (!searchPos.closerThan(worldPosition, chassisRange + .5f))
                    continue;
                if (!BlockMovementChecks.isMovementNecessary(searchedState, level, searchPos))
                    continue;
                if (BlockMovementChecks.isBrittle(searchedState))
                    continue;

                localVisited.add(searchPos);
                if (!searchPos.equals(worldPosition))
                    positions.add(searchPos);

                for (Direction offset : Iterate.directions) {
                    if (offset.getAxis() == axis)
                        continue;
                    if (searchPos.equals(worldPosition) && offset != facing)
                        continue;
                    if (BlockMovementChecks.isNotSupportive(searchedState, offset))
                        continue;

                    localFrontier.add(searchPos.relative(offset));
                }
            }
        }

        return positions;
    }
}
