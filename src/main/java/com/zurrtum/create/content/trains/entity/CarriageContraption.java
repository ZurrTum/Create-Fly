package com.zurrtum.create.content.trains.entity;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllContraptionTypes;
import com.zurrtum.create.api.behaviour.interaction.ConductorBlockInteractionBehavior;
import com.zurrtum.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.zurrtum.create.api.contraption.ContraptionType;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.MountedStorageManager;
import com.zurrtum.create.content.contraptions.actors.trainControls.ControlsBlock;
import com.zurrtum.create.content.contraptions.minecart.TrainCargoManager;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlock;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

public class CarriageContraption extends Contraption {

    private Direction assemblyDirection;
    private boolean forwardControls;
    private boolean backwardControls;

    public Couple<Boolean> blockConductors;
    public Map<BlockPos, Couple<Boolean>> conductorSeats;
    public ArrivalSoundQueue soundQueue;

    protected MountedStorageManager storageProxy;

    // during assembly only
    private int bogeys;
    private boolean sidewaysControls;
    private BlockPos secondBogeyPos;
    private List<BlockPos> assembledBlockConductors;

    // render
    public int portalCutoffMin;
    public int portalCutoffMax;

    static final MountedStorageManager fallbackStorage;

    static {
        fallbackStorage = new MountedStorageManager();
        fallbackStorage.initialize();
    }

    public CarriageContraption() {
        conductorSeats = new HashMap<>();
        assembledBlockConductors = new ArrayList<>();
        blockConductors = Couple.create(false, false);
        soundQueue = new ArrivalSoundQueue();
        portalCutoffMin = Integer.MIN_VALUE;
        portalCutoffMax = Integer.MAX_VALUE;
        storage = new TrainCargoManager();
    }

    public void setSoundQueueOffset(int offset) {
        soundQueue.offset = offset;
    }

    public CarriageContraption(Direction assemblyDirection) {
        this();
        this.assemblyDirection = assemblyDirection;
        this.bogeys = 0;
    }

    @Override
    public boolean assemble(Level world, BlockPos pos) throws AssemblyException {
        if (!searchMovedStructure(world, pos, null))
            return false;
        if (blocks.size() <= 1)
            return false;
        if (bogeys == 0)
            return false;
        if (bogeys > 2)
            throw new AssemblyException(Component.translatable("create.train_assembly.too_many_bogeys", bogeys));
        if (sidewaysControls)
            throw new AssemblyException(Component.translatable("create.train_assembly.sideways_controls"));

        for (BlockPos blazePos : assembledBlockConductors)
            for (Direction direction : Iterate.directionsInAxis(assemblyDirection.getAxis()))
                if (inControl(blazePos, direction))
                    blockConductors.set(direction != assemblyDirection, true);
        for (BlockPos seatPos : getSeats())
            for (Direction direction : Iterate.directionsInAxis(assemblyDirection.getAxis()))
                if (inControl(seatPos, direction))
                    conductorSeats.computeIfAbsent(seatPos, p -> Couple.create(false, false)).set(direction != assemblyDirection, true);

        return true;
    }

    public boolean inControl(BlockPos pos, Direction direction) {
        BlockPos controlsPos = pos.relative(direction);
        if (!blocks.containsKey(controlsPos))
            return false;
        StructureBlockInfo info = blocks.get(controlsPos);
        if (!info.state().is(AllBlocks.TRAIN_CONTROLS))
            return false;
        return info.state().getValue(ControlsBlock.FACING) == direction.getOpposite();
    }

    public void swapStorageAfterAssembly(CarriageContraptionEntity cce) {
        // Ensure that the entity does not hold its inventory data, because the global
        // carriage manages it instead
        Carriage carriage = cce.getCarriage();
        if (carriage.storage == null) {
            carriage.storage = (TrainCargoManager) storage;
            storage = new MountedStorageManager();
        }
        storageProxy = carriage.storage;
    }

    public void returnStorageForDisassembly(MountedStorageManager storage) {
        this.storage = storage;
    }

    @Override
    protected boolean isAnchoringBlockAt(BlockPos pos) {
        return false;
    }

    @Override
    protected Pair<StructureBlockInfo, BlockEntity> capture(Level world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);

        if (ArrivalSoundQueue.isPlayable(blockState)) {
            int anchorCoord = VecHelper.getCoordinate(anchor, assemblyDirection.getAxis());
            int posCoord = VecHelper.getCoordinate(pos, assemblyDirection.getAxis());
            soundQueue.add((posCoord - anchorCoord) * assemblyDirection.getAxisDirection().getStep(), toLocalPos(pos));
        }

        if (blockState.getBlock() instanceof AbstractBogeyBlock<?>) {
            bogeys++;
            if (bogeys == 2)
                secondBogeyPos = pos;
        }

        MovingInteractionBehaviour behaviour = MovingInteractionBehaviour.REGISTRY.get(blockState);
        if (behaviour instanceof ConductorBlockInteractionBehavior conductor && conductor.isValidConductor(blockState)) {
            assembledBlockConductors.add(toLocalPos(pos));
        }

        if (blockState.is(AllBlocks.TRAIN_CONTROLS)) {
            Direction facing = blockState.getValue(ControlsBlock.FACING);
            if (facing.getAxis() != assemblyDirection.getAxis())
                sidewaysControls = true;
            else {
                boolean forwards = facing == assemblyDirection;
                if (forwards)
                    forwardControls = true;
                else
                    backwardControls = true;
            }
        }

        return super.capture(world, pos);
    }

    @Override
    public void write(ValueOutput view, boolean spawnPacket) {
        super.write(view, spawnPacket);
        view.store("AssemblyDirection", Direction.CODEC, getAssemblyDirection());
        view.putBoolean("FrontControls", forwardControls);
        view.putBoolean("BackControls", backwardControls);
        view.putBoolean("FrontBlazeConductor", blockConductors.getFirst());
        view.putBoolean("BackBlazeConductor", blockConductors.getSecond());
        ValueOutput.ValueOutputList list = view.childrenList("ConductorSeats");
        for (Map.Entry<BlockPos, Couple<Boolean>> entry : conductorSeats.entrySet()) {
            ValueOutput item = list.addChild();
            item.store("Pos", BlockPos.CODEC, entry.getKey());
            Couple<Boolean> couple = entry.getValue();
            item.putBoolean("Forward", couple.getFirst());
            item.putBoolean("Backward", couple.getSecond());
        }
        soundQueue.write(view);
    }

    @Override
    public void read(Level world, ValueInput view, boolean spawnData) {
        assemblyDirection = view.read("AssemblyDirection", Direction.CODEC).orElse(Direction.DOWN);
        forwardControls = view.getBooleanOr("FrontControls", false);
        backwardControls = view.getBooleanOr("BackControls", false);
        blockConductors = Couple.create(view.getBooleanOr("FrontBlazeConductor", false), view.getBooleanOr("BackBlazeConductor", false));
        conductorSeats.clear();
        view.childrenListOrEmpty("ConductorSeats").forEach(item -> {
            conductorSeats.put(
                item.read("Pos", BlockPos.CODEC).orElse(BlockPos.ZERO),
                Couple.create(item.getBooleanOr("Forward", false), item.getBooleanOr("Backward", false))
            );
        });
        soundQueue.read(view);
        super.read(world, view, spawnData);
    }

    @Override
    public boolean canBeStabilized(Direction facing, BlockPos localPos) {
        return false;
    }

    @Override
    public ContraptionType getType() {
        return AllContraptionTypes.CARRIAGE;
    }

    public Direction getAssemblyDirection() {
        return assemblyDirection;
    }

    public boolean hasForwardControls() {
        return forwardControls;
    }

    public boolean hasBackwardControls() {
        return backwardControls;
    }

    public BlockPos getSecondBogeyPos() {
        return secondBogeyPos;
    }

    @Override
    public Optional<List<AABB>> getSimplifiedEntityColliders() {
        if (notInPortal())
            return super.getSimplifiedEntityColliders();
        return Optional.empty();
    }

    @Override
    public boolean isHiddenInPortal(BlockPos localPos) {
        if (notInPortal())
            return super.isHiddenInPortal(localPos);
        Direction facing = assemblyDirection;
        Axis axis = facing.getClockWise().getAxis();
        int coord = axis.choose(localPos.getZ(), localPos.getY(), localPos.getX()) * -facing.getAxisDirection().getStep();
        return !withinVisible(coord) || atSeam(coord);
    }

    public boolean isHiddenInPortal(int posAlongMovementAxis) {
        if (notInPortal())
            return false;
        return !withinVisible(posAlongMovementAxis) || atSeam(posAlongMovementAxis);
    }

    public boolean notInPortal() {
        return portalCutoffMin == Integer.MIN_VALUE && portalCutoffMax == Integer.MAX_VALUE;
    }

    public boolean atSeam(BlockPos localPos) {
        Direction facing = assemblyDirection;
        Axis axis = facing.getClockWise().getAxis();
        int coord = axis.choose(localPos.getZ(), localPos.getY(), localPos.getX()) * -facing.getAxisDirection().getStep();
        return coord == portalCutoffMin || coord == portalCutoffMax;
    }

    public boolean withinVisible(BlockPos localPos) {
        Direction facing = assemblyDirection;
        Axis axis = facing.getClockWise().getAxis();
        int coord = axis.choose(localPos.getZ(), localPos.getY(), localPos.getX()) * -facing.getAxisDirection().getStep();
        return withinVisible(coord);
    }

    public boolean atSeam(int posAlongMovementAxis) {
        return posAlongMovementAxis == portalCutoffMin || posAlongMovementAxis == portalCutoffMax;
    }

    public boolean withinVisible(int posAlongMovementAxis) {
        return posAlongMovementAxis > portalCutoffMin && posAlongMovementAxis < portalCutoffMax;
    }

    @Override
    public MountedStorageManager getStorage() {
        return storageProxy == null ? fallbackStorage : storageProxy;
    }

    @Override
    public void writeStorage(ValueOutput view, boolean spawnPacket) {
        if (!spawnPacket)
            return;
        if (storageProxy != null)
            storageProxy.write(view, spawnPacket);
    }

}