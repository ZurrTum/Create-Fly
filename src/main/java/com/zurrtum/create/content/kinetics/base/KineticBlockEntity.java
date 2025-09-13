package com.zurrtum.create.content.kinetics.base;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.Create;
import com.zurrtum.create.api.stress.BlockStressValues;
import com.zurrtum.create.content.kinetics.KineticNetwork;
import com.zurrtum.create.content.kinetics.RotationPropagator;
import com.zurrtum.create.content.kinetics.base.IRotate.SpeedLevel;
import com.zurrtum.create.content.kinetics.base.IRotate.StressImpact;
import com.zurrtum.create.content.kinetics.simpleRelays.ICogWheel;
import com.zurrtum.create.content.kinetics.transmission.sequencer.SequencedGearshiftBlockEntity.SequenceContext;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class KineticBlockEntity extends SmartBlockEntity {

    public @Nullable Long network;
    public @Nullable BlockPos source;
    public boolean networkDirty;
    public boolean updateSpeed;
    public int preventSpeedUpdate;
    public SequenceContext sequenceContext;
    public KineticEffectHandler effects;
    protected float speed;
    protected float capacity;
    protected float stress;
    protected boolean overStressed;
    protected boolean wasMoved;
    protected float lastStressApplied;
    protected float lastCapacityProvided;
    private int flickerTally;
    private int networkSize;
    private int validationCountdown;

    public KineticBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        effects = new KineticEffectHandler(this);
        updateSpeed = true;
    }

    public static KineticBlockEntity encased(BlockPos pos, BlockState state) {
        return new KineticBlockEntity(AllBlockEntityTypes.ENCASED_SHAFT, pos, state);
    }

    public static void switchToBlockState(World world, BlockPos pos, BlockState state) {
        if (world.isClient)
            return;

        BlockEntity blockEntity = world.getBlockEntity(pos);
        BlockState currentState = world.getBlockState(pos);
        boolean isKinetic = blockEntity instanceof KineticBlockEntity;

        if (currentState == state)
            return;
        if (blockEntity == null || !isKinetic) {
            world.setBlockState(pos, state, Block.NOTIFY_ALL);
            return;
        }

        KineticBlockEntity kineticBlockEntity = (KineticBlockEntity) blockEntity;
        if (state.getBlock() instanceof KineticBlock && !((KineticBlock) state.getBlock()).areStatesKineticallyEquivalent(currentState, state)) {
            if (kineticBlockEntity.hasNetwork())
                kineticBlockEntity.getOrCreateNetwork().remove(kineticBlockEntity);
            kineticBlockEntity.detachKinetics();
            kineticBlockEntity.removeSource();
        }

        if (blockEntity instanceof GeneratingKineticBlockEntity generatingBlockEntity) {
            generatingBlockEntity.reActivateSource = true;
        }

        world.setBlockState(pos, state, Block.NOTIFY_ALL);
    }

    public static float convertToDirection(float axisSpeed, Direction d) {
        return d.getDirection() == AxisDirection.POSITIVE ? axisSpeed : -axisSpeed;
    }

    public static float convertToLinear(float speed) {
        return speed / 512f;
    }

    public static float convertToAngular(float speed) {
        return speed * 3 / 10f;
    }

    @Override
    public void initialize() {
        if (hasNetwork() && !world.isClient) {
            KineticNetwork network = getOrCreateNetwork();
            if (!network.initialized)
                network.initFromTE(capacity, stress, networkSize);
            network.addSilently(this, lastCapacityProvided, lastStressApplied);
        }

        super.initialize();
    }

    @Override
    public void tick() {
        if (!world.isClient && needsSpeedUpdate())
            attachKinetics();

        super.tick();
        effects.tick();

        preventSpeedUpdate = 0;

        if (world.isClient) {
            return;
        }

        if (validationCountdown-- <= 0) {
            validationCountdown = AllConfigs.server().kinetics.kineticValidationFrequency.get();
            validateKinetics();
        }

        if (getFlickerScore() > 0)
            flickerTally = getFlickerScore() - 1;

        if (networkDirty) {
            if (hasNetwork())
                getOrCreateNetwork().updateNetwork();
            networkDirty = false;
        }
    }

    private void validateKinetics() {
        if (hasSource()) {
            if (!hasNetwork()) {
                removeSource();
                return;
            }

            if (!world.isChunkLoaded(source))
                return;

            BlockEntity blockEntity = world.getBlockEntity(source);
            KineticBlockEntity sourceBE = blockEntity instanceof KineticBlockEntity ? (KineticBlockEntity) blockEntity : null;
            if (sourceBE == null || sourceBE.speed == 0) {
                removeSource();
                detachKinetics();
                return;
            }

            return;
        }

        if (speed != 0) {
            if (getGeneratedSpeed() == 0)
                speed = 0;
        }
    }

    public void updateFromNetwork(float maxStress, float currentStress, int networkSize) {
        networkDirty = false;
        this.capacity = maxStress;
        this.stress = currentStress;
        this.networkSize = networkSize;
        boolean overStressed = maxStress < currentStress && StressImpact.isEnabled();
        markDirty();

        if (overStressed != this.overStressed) {
            float prevSpeed = getSpeed();
            this.overStressed = overStressed;
            onSpeedChanged(prevSpeed);
            sendData();
        }
    }

    protected Block getStressConfigKey() {
        return getCachedState().getBlock();
    }

    public float calculateStressApplied() {
        float impact = (float) BlockStressValues.getImpact(getStressConfigKey());
        this.lastStressApplied = impact;
        return impact;
    }

    public float calculateAddedStressCapacity() {
        float capacity = (float) BlockStressValues.getCapacity(getStressConfigKey());
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    public void onSpeedChanged(float previousSpeed) {
        boolean fromOrToZero = (previousSpeed == 0) != (getSpeed() == 0);
        boolean directionSwap = !fromOrToZero && Math.signum(previousSpeed) != Math.signum(getSpeed());
        if (fromOrToZero || directionSwap)
            flickerTally = getFlickerScore() + 5;
        markDirty();
    }

    @Override
    public void remove() {
        if (!world.isClient) {
            if (hasNetwork())
                getOrCreateNetwork().remove(this);
            detachKinetics();
        }
        super.remove();
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        view.putFloat("Speed", speed);
        if (sequenceContext != null && (!clientPacket || syncSequenceContext()))
            view.put("Sequence", SequenceContext.CODEC, sequenceContext);

        if (needsSpeedUpdate())
            view.putBoolean("NeedsSpeedUpdate", true);

        if (hasSource())
            view.put("Source", BlockPos.CODEC, source);

        if (hasNetwork()) {
            WriteView networkTag = view.get("Network");
            networkTag.putLong("Id", network);
            networkTag.putFloat("Stress", stress);
            networkTag.putFloat("Capacity", capacity);
            networkTag.putInt("Size", networkSize);

            if (lastStressApplied != 0)
                networkTag.putFloat("AddedStress", lastStressApplied);
            if (lastCapacityProvided != 0)
                networkTag.putFloat("AddedCapacity", lastCapacityProvided);
        }

        super.write(view, clientPacket);
    }

    public boolean needsSpeedUpdate() {
        return updateSpeed;
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        boolean overStressedBefore = overStressed;
        clearKineticInformation();

        // DO NOT READ kinetic information when placed after movement
        if (wasMoved) {
            super.read(view, clientPacket);
            return;
        }

        speed = view.getFloat("Speed", 0);
        sequenceContext = view.read("Sequence", SequenceContext.CODEC).orElse(null);

        source = view.read("Source", BlockPos.CODEC).orElse(null);

        view.getOptionalReadView("Network").ifPresent(networkTag -> {
            network = networkTag.getLong("Id", 0);
            stress = networkTag.getFloat("Stress", 0);
            capacity = networkTag.getFloat("Capacity", 0);
            networkSize = networkTag.getInt("Size", 0);
            lastStressApplied = networkTag.getFloat("AddedStress", 0);
            lastCapacityProvided = networkTag.getFloat("AddedCapacity", 0);
            overStressed = capacity < stress && StressImpact.isEnabled();
        });

        super.read(view, clientPacket);

        if (clientPacket && overStressedBefore != overStressed && speed != 0)
            effects.triggerOverStressedEffect();

        if (clientPacket)
            AllClientHandle.INSTANCE.queueUpdate(this);
    }

    public float getGeneratedSpeed() {
        return 0;
    }

    public boolean isSource() {
        return getGeneratedSpeed() != 0;
    }

    public void setSource(BlockPos source) {
        this.source = source;
        if (world == null || world.isClient)
            return;

        BlockEntity blockEntity = world.getBlockEntity(source);
        if (!(blockEntity instanceof KineticBlockEntity sourceBE)) {
            removeSource();
            return;
        }

        setNetwork(sourceBE.network);
        copySequenceContextFrom(sourceBE);
    }

    public float getSpeed() {
        if (overStressed || (world != null && world.getTickManager().isFrozen()))
            return 0;
        return getTheoreticalSpeed();
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getTheoreticalSpeed() {
        return speed;
    }

    public boolean hasSource() {
        return source != null;
    }

    protected void copySequenceContextFrom(KineticBlockEntity sourceBE) {
        sequenceContext = sourceBE.sequenceContext;
    }

    public void removeSource() {
        float prevSpeed = getSpeed();

        speed = 0;
        source = null;
        setNetwork(null);
        sequenceContext = null;

        onSpeedChanged(prevSpeed);
    }

    public void setNetwork(@Nullable Long networkIn) {
        if (Objects.equals(network, networkIn))
            return;
        if (network != null)
            getOrCreateNetwork().remove(this);

        network = networkIn;
        markDirty();

        if (networkIn == null)
            return;

        network = networkIn;
        KineticNetwork network = getOrCreateNetwork();
        network.initialized = true;
        network.add(this);
    }

    public KineticNetwork getOrCreateNetwork() {
        return Create.TORQUE_PROPAGATOR.getOrCreateNetworkFor(this);
    }

    public boolean hasNetwork() {
        return network != null;
    }

    public void attachKinetics() {
        updateSpeed = false;
        RotationPropagator.handleAdded(world, pos, this);
    }

    public void detachKinetics() {
        RotationPropagator.handleRemoved(world, pos, this);
    }

    public boolean isSpeedRequirementFulfilled() {
        BlockState state = getCachedState();
        if (!(state.getBlock() instanceof IRotate def))
            return true;
        SpeedLevel minimumRequiredSpeedLevel = def.getMinimumRequiredSpeedLevel();
        return Math.abs(getSpeed()) >= minimumRequiredSpeedLevel.getSpeedValue();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

    public void clearKineticInformation() {
        speed = 0;
        source = null;
        network = null;
        overStressed = false;
        stress = 0;
        capacity = 0;
        lastStressApplied = 0;
        lastCapacityProvided = 0;
    }

    public void warnOfMovement() {
        wasMoved = true;
    }

    public int getFlickerScore() {
        return flickerTally;
    }

    public boolean isOverStressed() {
        return overStressed;
    }

    // Custom Propagation

    /**
     * Specify ratio of transferred rotation from this kinetic component to a
     * specific other.
     *
     * @param target           other Kinetic BE to transfer to
     * @param stateFrom        this BE's blockstate
     * @param stateTo          other BE's blockstate
     * @param diff             difference in position (to.pos - from.pos)
     * @param connectedViaAxes whether these kinetic blocks are connected via mutual
     *                         IRotate.hasShaftTowards()
     * @param connectedViaCogs whether these kinetic blocks are connected via mutual
     *                         IRotate.hasIntegratedCogwheel()
     * @return factor of rotation speed from this BE to other. 0 if no rotation is
     * transferred, or the standard rules apply (integrated shafts/cogs)
     */
    public float propagateRotationTo(
        KineticBlockEntity target,
        BlockState stateFrom,
        BlockState stateTo,
        BlockPos diff,
        boolean connectedViaAxes,
        boolean connectedViaCogs
    ) {
        return 0;
    }

    /**
     * Specify additional locations the rotation propagator should look for
     * potentially connected components. Neighbour list contains offset positions in
     * all 6 directions by default.
     *
     * @param block
     * @param state
     * @param neighbours
     * @return
     */
    public List<BlockPos> addPropagationLocations(IRotate block, BlockState state, List<BlockPos> neighbours) {
        if (!canPropagateDiagonally(block, state))
            return neighbours;

        Axis axis = block.getRotationAxis(state);
        BlockPos.stream(new BlockPos(-1, -1, -1), new BlockPos(1, 1, 1)).forEach(offset -> {
            if (axis.choose(offset.getX(), offset.getY(), offset.getZ()) != 0)
                return;
            if (offset.getSquaredDistance(BlockPos.ZERO) != 2)
                return;
            neighbours.add(pos.add(offset));
        });
        return neighbours;
    }

    /**
     * Specify whether this component can propagate speed to the other in any
     * circumstance. Shaft and cogwheel connections are already handled by internal
     * logic. Does not have to be specified on both ends, it is assumed that this
     * relation is symmetrical.
     *
     * @param other
     * @param state
     * @param otherState
     * @return true if this and the other component should check their propagation
     * factor and are not already connected via integrated cogs or shafts
     */
    public boolean isCustomConnection(KineticBlockEntity other, BlockState state, BlockState otherState) {
        return false;
    }

    protected boolean canPropagateDiagonally(IRotate block, BlockState state) {
        return ICogWheel.isSmallCog(state);
    }

    public boolean isNoisy() {
        return true;
    }

    public int getRotationAngleOffset(Axis axis) {
        return 0;
    }

    protected boolean syncSequenceContext() {
        return false;
    }

}