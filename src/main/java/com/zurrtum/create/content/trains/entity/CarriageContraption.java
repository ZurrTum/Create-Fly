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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

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
    public boolean assemble(World world, BlockPos pos) throws AssemblyException {
        if (!searchMovedStructure(world, pos, null))
            return false;
        if (blocks.size() <= 1)
            return false;
        if (bogeys == 0)
            return false;
        if (bogeys > 2)
            throw new AssemblyException(Text.translatable("create.train_assembly.too_many_bogeys", bogeys));
        if (sidewaysControls)
            throw new AssemblyException(Text.translatable("create.train_assembly.sideways_controls"));

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
        BlockPos controlsPos = pos.offset(direction);
        if (!blocks.containsKey(controlsPos))
            return false;
        StructureBlockInfo info = blocks.get(controlsPos);
        if (!info.state().isOf(AllBlocks.TRAIN_CONTROLS))
            return false;
        return info.state().get(ControlsBlock.FACING) == direction.getOpposite();
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
    protected Pair<StructureBlockInfo, BlockEntity> capture(World world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);

        if (ArrivalSoundQueue.isPlayable(blockState)) {
            int anchorCoord = VecHelper.getCoordinate(anchor, assemblyDirection.getAxis());
            int posCoord = VecHelper.getCoordinate(pos, assemblyDirection.getAxis());
            soundQueue.add((posCoord - anchorCoord) * assemblyDirection.getDirection().offset(), toLocalPos(pos));
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

        if (blockState.isOf(AllBlocks.TRAIN_CONTROLS)) {
            Direction facing = blockState.get(ControlsBlock.FACING);
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
    protected BlockEntity readBlockEntity(World level, StructureBlockInfo info, ReadView view) {
        if (info.state().getBlock() instanceof AbstractBogeyBlock<?> bogey && !bogey.captureBlockEntityForTrain())
            return null; // Bogeys are typically rendered by the carriage contraption, not the BE

        return super.readBlockEntity(level, info, view);
    }

    @Override
    public void write(WriteView view, boolean spawnPacket) {
        super.write(view, spawnPacket);
        view.put("AssemblyDirection", Direction.CODEC, getAssemblyDirection());
        view.putBoolean("FrontControls", forwardControls);
        view.putBoolean("BackControls", backwardControls);
        view.putBoolean("FrontBlazeConductor", blockConductors.getFirst());
        view.putBoolean("BackBlazeConductor", blockConductors.getSecond());
        WriteView.ListView list = view.getList("ConductorSeats");
        for (Map.Entry<BlockPos, Couple<Boolean>> entry : conductorSeats.entrySet()) {
            WriteView item = list.add();
            item.put("Pos", BlockPos.CODEC, entry.getKey());
            Couple<Boolean> couple = entry.getValue();
            item.putBoolean("Forward", couple.getFirst());
            item.putBoolean("Backward", couple.getSecond());
        }
        soundQueue.write(view);
    }

    @Override
    public void read(World world, ReadView view, boolean spawnData) {
        assemblyDirection = view.read("AssemblyDirection", Direction.CODEC).orElse(Direction.DOWN);
        forwardControls = view.getBoolean("FrontControls", false);
        backwardControls = view.getBoolean("BackControls", false);
        blockConductors = Couple.create(view.getBoolean("FrontBlazeConductor", false), view.getBoolean("BackBlazeConductor", false));
        conductorSeats.clear();
        view.getListReadView("ConductorSeats").forEach(item -> {
            conductorSeats.put(
                item.read("Pos", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
                Couple.create(item.getBoolean("Forward", false), item.getBoolean("Backward", false))
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

    private Collection<BlockEntity> renderedBEsOutsidePortal = new ArrayList<>();

    @Override
    public RenderedBlocks getRenderedBlocks() {
        if (notInPortal())
            return super.getRenderedBlocks();

        renderedBEsOutsidePortal = new ArrayList<>();
        renderedBlockEntities.stream().filter(be -> !isHiddenInPortal(be.getPos())).forEach(renderedBEsOutsidePortal::add);

        Map<BlockPos, BlockState> values = new HashMap<>();
        blocks.forEach((pos, info) -> {
            if (withinVisible(pos)) {
                values.put(pos, info.state());
            } else if (atSeam(pos)) {
                values.put(pos, Blocks.PURPLE_STAINED_GLASS.getDefaultState());
            }
        });
        return new RenderedBlocks(pos -> values.getOrDefault(pos, Blocks.AIR.getDefaultState()), values.keySet());
    }

    @Override
    public Collection<BlockEntity> getRenderedBEs() {
        if (notInPortal())
            return super.getRenderedBEs();
        return renderedBEsOutsidePortal;
    }

    @Override
    public Optional<List<Box>> getSimplifiedEntityColliders() {
        if (notInPortal())
            return super.getSimplifiedEntityColliders();
        return Optional.empty();
    }

    @Override
    public boolean isHiddenInPortal(BlockPos localPos) {
        if (notInPortal())
            return super.isHiddenInPortal(localPos);
        return !withinVisible(localPos) || atSeam(localPos);
    }

    public boolean notInPortal() {
        return portalCutoffMin == Integer.MIN_VALUE && portalCutoffMax == Integer.MAX_VALUE;
    }

    public boolean atSeam(BlockPos localPos) {
        Direction facing = assemblyDirection;
        Axis axis = facing.rotateYClockwise().getAxis();
        int coord = axis.choose(localPos.getZ(), localPos.getY(), localPos.getX()) * -facing.getDirection().offset();
        return coord == portalCutoffMin || coord == portalCutoffMax;
    }

    public boolean withinVisible(BlockPos localPos) {
        Direction facing = assemblyDirection;
        Axis axis = facing.rotateYClockwise().getAxis();
        int coord = axis.choose(localPos.getZ(), localPos.getY(), localPos.getX()) * -facing.getDirection().offset();
        return coord > portalCutoffMin && coord < portalCutoffMax;
    }

    @Override
    public MountedStorageManager getStorage() {
        return storageProxy == null ? fallbackStorage : storageProxy;
    }

    @Override
    public void writeStorage(WriteView view, boolean spawnPacket) {
        if (!spawnPacket)
            return;
        if (storageProxy != null)
            storageProxy.write(view, spawnPacket);
    }

}