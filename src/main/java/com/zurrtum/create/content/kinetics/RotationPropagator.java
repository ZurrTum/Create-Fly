package com.zurrtum.create.content.kinetics;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.kinetics.base.DirectionalShaftHalvesBlockEntity;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.chainDrive.ChainDriveBlock;
import com.zurrtum.create.content.kinetics.gearbox.GearboxBlockEntity;
import com.zurrtum.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.zurrtum.create.content.kinetics.simpleRelays.ICogWheel;
import com.zurrtum.create.content.kinetics.speedController.SpeedControllerBlock;
import com.zurrtum.create.content.kinetics.speedController.SpeedControllerBlockEntity;
import com.zurrtum.create.content.kinetics.transmission.SplitShaftBlockEntity;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;

import java.util.LinkedList;
import java.util.List;

import static net.minecraft.state.property.Properties.AXIS;

public class RotationPropagator {

    private static final int MAX_FLICKER_SCORE = 128;

    /**
     * Determines the change in rotation between two attached kinetic entities. For
     * instance, an axis connection returns 1 while a 1-to-1 gear connection
     * reverses the rotation and therefore returns -1.
     *
     * @param from
     * @param to
     * @return
     */
    private static float getRotationSpeedModifier(KineticBlockEntity from, KineticBlockEntity to) {
        final BlockState stateFrom = from.getCachedState();
        final BlockState stateTo = to.getCachedState();

        Block fromBlock = stateFrom.getBlock();
        Block toBlock = stateTo.getBlock();
        if (!(fromBlock instanceof IRotate definitionFrom && toBlock instanceof IRotate definitionTo))
            return 0;

        final BlockPos diff = to.getPos().subtract(from.getPos());
        final Direction direction = Direction.getFacing(diff.getX(), diff.getY(), diff.getZ());
        final World world = from.getWorld();

        boolean alignedAxes = true;
        for (Axis axis : Axis.values())
            if (axis != direction.getAxis())
                if (axis.choose(diff.getX(), diff.getY(), diff.getZ()) != 0)
                    alignedAxes = false;

        boolean connectedByAxis = alignedAxes && definitionFrom.hasShaftTowards(
            world,
            from.getPos(),
            stateFrom,
            direction
        ) && definitionTo.hasShaftTowards(world, to.getPos(), stateTo, direction.getOpposite());

        boolean connectedByGears = ICogWheel.isSmallCog(stateFrom) && ICogWheel.isSmallCog(stateTo);

        float custom = from.propagateRotationTo(to, stateFrom, stateTo, diff, connectedByAxis, connectedByGears);
        if (custom != 0)
            return custom;

        // Axis <-> Axis
        if (connectedByAxis) {
            float axisModifier = getAxisModifier(to, direction.getOpposite());
            if (axisModifier != 0)
                axisModifier = 1 / axisModifier;
            return getAxisModifier(from, direction) * axisModifier;
        }

        // Attached Encased Belts
        if (fromBlock instanceof ChainDriveBlock && toBlock instanceof ChainDriveBlock) {
            boolean connected = ChainDriveBlock.areBlocksConnected(stateFrom, stateTo, direction);
            return connected ? ChainDriveBlock.getRotationSpeedModifier(from, to) : 0;
        }

        // Large Gear <-> Large Gear
        if (isLargeToLargeGear(stateFrom, stateTo, diff)) {
            Axis sourceAxis = stateFrom.get(AXIS);
            Axis targetAxis = stateTo.get(AXIS);
            int sourceAxisDiff = sourceAxis.choose(diff.getX(), diff.getY(), diff.getZ());
            int targetAxisDiff = targetAxis.choose(diff.getX(), diff.getY(), diff.getZ());

            return sourceAxisDiff > 0 ^ targetAxisDiff > 0 ? -1 : 1;
        }

        // Gear <-> Large Gear
        if (ICogWheel.isLargeCog(stateFrom) && ICogWheel.isSmallCog(stateTo))
            if (isLargeToSmallCog(stateFrom, stateTo, definitionTo, diff))
                return -2f;
        if (ICogWheel.isLargeCog(stateTo) && ICogWheel.isSmallCog(stateFrom))
            if (isLargeToSmallCog(stateTo, stateFrom, definitionFrom, diff))
                return -.5f;

        // Gear <-> Gear
        if (connectedByGears) {
            if (diff.getManhattanDistance(BlockPos.ZERO) != 1)
                return 0;
            if (ICogWheel.isLargeCog(stateTo))
                return 0;
            if (direction.getAxis() == definitionFrom.getRotationAxis(stateFrom))
                return 0;
            if (definitionFrom.getRotationAxis(stateFrom) == definitionTo.getRotationAxis(stateTo))
                return -1;
        }

        return 0;
    }

    private static float getConveyedSpeed(KineticBlockEntity from, KineticBlockEntity to) {
        final BlockState stateFrom = from.getCachedState();
        final BlockState stateTo = to.getCachedState();

        // Rotation Speed Controller <-> Large Gear
        if (isLargeCogToSpeedController(stateFrom, stateTo, to.getPos().subtract(from.getPos())))
            return SpeedControllerBlockEntity.getConveyedSpeed(from, to, true);
        if (isLargeCogToSpeedController(stateTo, stateFrom, from.getPos().subtract(to.getPos())))
            return SpeedControllerBlockEntity.getConveyedSpeed(to, from, false);

        float rotationSpeedModifier = getRotationSpeedModifier(from, to);
        return from.getTheoreticalSpeed() * rotationSpeedModifier;
    }

    private static boolean isLargeToLargeGear(BlockState from, BlockState to, BlockPos diff) {
        if (!ICogWheel.isLargeCog(from) || !ICogWheel.isLargeCog(to))
            return false;
        Axis fromAxis = from.get(AXIS);
        Axis toAxis = to.get(AXIS);
        if (fromAxis == toAxis)
            return false;
        for (Axis axis : Axis.values()) {
            int axisDiff = axis.choose(diff.getX(), diff.getY(), diff.getZ());
            if (axis == fromAxis || axis == toAxis) {
                if (axisDiff == 0)
                    return false;

            } else if (axisDiff != 0)
                return false;
        }
        return true;
    }

    private static float getAxisModifier(KineticBlockEntity be, Direction direction) {
        if (!(be.hasSource() || be.isSource()) || !(be instanceof DirectionalShaftHalvesBlockEntity))
            return 1;
        Direction source = ((DirectionalShaftHalvesBlockEntity) be).getSourceFacing();

        if (be instanceof GearboxBlockEntity)
            return direction.getAxis() == source.getAxis() ? direction == source ? 1 : -1 : direction.getDirection() == source.getDirection() ? -1 : 1;

        if (be instanceof SplitShaftBlockEntity)
            return ((SplitShaftBlockEntity) be).getRotationSpeedModifier(direction);

        return 1;
    }

    private static boolean isLargeToSmallCog(BlockState from, BlockState to, IRotate defTo, BlockPos diff) {
        Axis axisFrom = from.get(AXIS);
        if (axisFrom != defTo.getRotationAxis(to))
            return false;
        if (axisFrom.choose(diff.getX(), diff.getY(), diff.getZ()) != 0)
            return false;
        for (Axis axis : Axis.values()) {
            if (axis == axisFrom)
                continue;
            if (Math.abs(axis.choose(diff.getX(), diff.getY(), diff.getZ())) != 1)
                return false;
        }
        return true;
    }

    private static boolean isLargeCogToSpeedController(BlockState from, BlockState to, BlockPos diff) {
        if (!ICogWheel.isLargeCog(from) || !to.isOf(AllBlocks.ROTATION_SPEED_CONTROLLER))
            return false;
        if (!diff.equals(BlockPos.ORIGIN.down()))
            return false;
        Axis axis = from.get(CogWheelBlock.AXIS);
        if (axis.isVertical())
            return false;
        if (to.get(SpeedControllerBlock.HORIZONTAL_AXIS) == axis)
            return false;
        return true;
    }

    /**
     * Insert the added position to the kinetic network.
     *
     * @param worldIn
     * @param pos
     */
    public static void handleAdded(World worldIn, BlockPos pos, KineticBlockEntity addedTE) {
        if (worldIn.isClient)
            return;
        if (!worldIn.isPosLoaded(pos))
            return;
        propagateNewSource(addedTE);
    }

    /**
     * Search for sourceless networks attached to the given entity and update them.
     *
     * @param currentTE
     */
    private static void propagateNewSource(KineticBlockEntity currentTE) {
        BlockPos pos = currentTE.getPos();
        World world = currentTE.getWorld();

        for (KineticBlockEntity neighbourTE : getConnectedNeighbours(currentTE)) {
            float speedOfCurrent = currentTE.getTheoreticalSpeed();
            float speedOfNeighbour = neighbourTE.getTheoreticalSpeed();
            float newSpeed = getConveyedSpeed(currentTE, neighbourTE);
            float oppositeSpeed = getConveyedSpeed(neighbourTE, currentTE);

            if (newSpeed == 0 && oppositeSpeed == 0)
                continue;

            boolean incompatible = Math.signum(newSpeed) != Math.signum(speedOfNeighbour) && (newSpeed != 0 && speedOfNeighbour != 0);

            boolean tooFast = Math.abs(newSpeed) > AllConfigs.server().kinetics.maxRotationSpeed.get() || Math.abs(oppositeSpeed) > AllConfigs.server().kinetics.maxRotationSpeed.get();
            // Check for both the new speed and the opposite speed, just in case

            boolean speedChangedTooOften = currentTE.getFlickerScore() > MAX_FLICKER_SCORE;
            if (tooFast || speedChangedTooOften) {
                world.breakBlock(pos, true);
                return;
            }

            // Opposite directions
            if (incompatible) {
                world.breakBlock(pos, true);
                return;

                // Same direction: overpower the slower speed
            } else {

                // Neighbour faster, overpower the incoming tree
                if (Math.abs(oppositeSpeed) > Math.abs(speedOfCurrent)) {
                    float prevSpeed = currentTE.getSpeed();
                    currentTE.setSource(neighbourTE.getPos());
                    currentTE.setSpeed(getConveyedSpeed(neighbourTE, currentTE));
                    currentTE.onSpeedChanged(prevSpeed);
                    currentTE.sendData();

                    propagateNewSource(currentTE);
                    return;
                }

                // Current faster, overpower the neighbours' tree
                if (Math.abs(newSpeed) >= Math.abs(speedOfNeighbour)) {

                    // Do not overpower you own network -> cycle
                    if (!currentTE.hasNetwork() || currentTE.network.equals(neighbourTE.network)) {
                        float epsilon = Math.abs(speedOfNeighbour) / 256f / 256f;
                        if (Math.abs(newSpeed) > Math.abs(speedOfNeighbour) + epsilon)
                            world.breakBlock(pos, true);
                        continue;
                    }

                    if (currentTE.hasSource() && currentTE.source.equals(neighbourTE.getPos()))
                        currentTE.removeSource();

                    float prevSpeed = neighbourTE.getSpeed();
                    neighbourTE.setSource(currentTE.getPos());
                    neighbourTE.setSpeed(getConveyedSpeed(currentTE, neighbourTE));
                    neighbourTE.onSpeedChanged(prevSpeed);
                    neighbourTE.sendData();
                    propagateNewSource(neighbourTE);
                    continue;
                }
            }

            if (Math.abs(neighbourTE.getTheoreticalSpeed() - newSpeed) <= 1e-5f)
                continue;

            float prevSpeed = neighbourTE.getSpeed();
            neighbourTE.setSpeed(newSpeed);
            neighbourTE.setSource(currentTE.getPos());
            neighbourTE.onSpeedChanged(prevSpeed);
            neighbourTE.sendData();
            propagateNewSource(neighbourTE);

        }
    }

    /**
     * Remove the given entity from the network.
     *
     * @param worldIn
     * @param pos
     * @param removedBE
     */
    public static void handleRemoved(World worldIn, BlockPos pos, KineticBlockEntity removedBE) {
        if (worldIn.isClient)
            return;
        if (removedBE == null)
            return;
        if (removedBE.getTheoreticalSpeed() == 0)
            return;

        for (BlockPos neighbourPos : getPotentialNeighbourLocations(removedBE)) {
            BlockState neighbourState = worldIn.getBlockState(neighbourPos);
            if (!(neighbourState.getBlock() instanceof IRotate))
                continue;
            BlockEntity blockEntity = worldIn.getBlockEntity(neighbourPos);
            if (!(blockEntity instanceof KineticBlockEntity neighbourBE))
                continue;

            if (!neighbourBE.hasSource() || !neighbourBE.source.equals(pos))
                continue;

            propagateMissingSource(neighbourBE);
        }

    }

    /**
     * Clear the entire subnetwork depending on the given entity and find a new
     * source
     *
     * @param updateTE
     */
    private static void propagateMissingSource(KineticBlockEntity updateTE) {
        final World world = updateTE.getWorld();

        List<KineticBlockEntity> potentialNewSources = new LinkedList<>();
        List<BlockPos> frontier = new LinkedList<>();
        frontier.add(updateTE.getPos());
        BlockPos missingSource = updateTE.hasSource() ? updateTE.source : null;

        while (!frontier.isEmpty()) {
            final BlockPos pos = frontier.remove(0);
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (!(blockEntity instanceof KineticBlockEntity currentBE))
                continue;

            currentBE.removeSource();
            currentBE.sendData();

            for (KineticBlockEntity neighbourBE : getConnectedNeighbours(currentBE)) {
                if (neighbourBE.getPos().equals(missingSource))
                    continue;
                if (!neighbourBE.hasSource())
                    continue;

                if (!neighbourBE.source.equals(pos)) {
                    potentialNewSources.add(neighbourBE);
                    continue;
                }

                if (neighbourBE.isSource())
                    potentialNewSources.add(neighbourBE);

                frontier.add(neighbourBE.getPos());
            }
        }

        for (KineticBlockEntity newSource : potentialNewSources) {
            if (newSource.hasSource() || newSource.isSource()) {
                propagateNewSource(newSource);
                return;
            }
        }
    }

    private static KineticBlockEntity findConnectedNeighbour(KineticBlockEntity currentTE, BlockPos neighbourPos) {
        BlockState neighbourState = currentTE.getWorld().getBlockState(neighbourPos);
        if (!(neighbourState.getBlock() instanceof IRotate))
            return null;
        if (!neighbourState.hasBlockEntity())
            return null;
        BlockEntity neighbourBE = currentTE.getWorld().getBlockEntity(neighbourPos);
        if (!(neighbourBE instanceof KineticBlockEntity neighbourKBE))
            return null;
        if (!(neighbourKBE.getCachedState().getBlock() instanceof IRotate))
            return null;
        if (!isConnected(currentTE, neighbourKBE) && !isConnected(neighbourKBE, currentTE))
            return null;
        return neighbourKBE;
    }

    public static boolean isConnected(KineticBlockEntity from, KineticBlockEntity to) {
        final BlockState stateFrom = from.getCachedState();
        final BlockState stateTo = to.getCachedState();
        return isLargeCogToSpeedController(stateFrom, stateTo, to.getPos().subtract(from.getPos())) || getRotationSpeedModifier(
            from,
            to
        ) != 0 || from.isCustomConnection(to, stateFrom, stateTo);
    }

    private static List<KineticBlockEntity> getConnectedNeighbours(KineticBlockEntity be) {
        List<KineticBlockEntity> neighbours = new LinkedList<>();
        for (BlockPos neighbourPos : getPotentialNeighbourLocations(be)) {
            final KineticBlockEntity neighbourBE = findConnectedNeighbour(be, neighbourPos);
            if (neighbourBE == null)
                continue;

            neighbours.add(neighbourBE);
        }
        return neighbours;
    }

    private static List<BlockPos> getPotentialNeighbourLocations(KineticBlockEntity be) {
        List<BlockPos> neighbours = new LinkedList<>();
        BlockPos blockPos = be.getPos();
        World level = be.getWorld();

        if (!level.isPosLoaded(blockPos))
            return neighbours;

        for (Direction facing : Iterate.directions) {
            BlockPos relative = blockPos.offset(facing);
            if (level.isPosLoaded(relative))
                neighbours.add(relative);
        }

        BlockState blockState = be.getCachedState();
        if (!(blockState.getBlock() instanceof IRotate block))
            return neighbours;
        return be.addPropagationLocations(block, blockState, neighbours);
    }

}