package com.zurrtum.create.content.contraptions;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllContraptionTypeTags;
import com.zurrtum.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.api.contraption.BlockMovementChecks;
import com.zurrtum.create.api.contraption.ContraptionType;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.UniqueLinkedList;
import com.zurrtum.create.catnip.math.BBHelper;
import com.zurrtum.create.catnip.math.BlockFace;
import com.zurrtum.create.catnip.nbt.NBTHelper;
import com.zurrtum.create.catnip.nbt.NBTProcessors;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsMovement;
import com.zurrtum.create.content.contraptions.actors.harvester.HarvesterMovementBehaviour;
import com.zurrtum.create.content.contraptions.actors.seat.SeatBlock;
import com.zurrtum.create.content.contraptions.actors.seat.SeatEntity;
import com.zurrtum.create.content.contraptions.actors.trainControls.ControlsBlock;
import com.zurrtum.create.content.contraptions.bearing.MechanicalBearingBlock;
import com.zurrtum.create.content.contraptions.bearing.StabilizedContraption;
import com.zurrtum.create.content.contraptions.bearing.WindmillBearingBlock;
import com.zurrtum.create.content.contraptions.bearing.WindmillBearingBlockEntity;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.contraptions.chassis.AbstractChassisBlock;
import com.zurrtum.create.content.contraptions.chassis.ChassisBlockEntity;
import com.zurrtum.create.content.contraptions.chassis.StickerBlock;
import com.zurrtum.create.content.contraptions.gantry.GantryCarriageBlock;
import com.zurrtum.create.content.contraptions.glue.SuperGlueEntity;
import com.zurrtum.create.content.contraptions.piston.MechanicalPistonBlock;
import com.zurrtum.create.content.contraptions.piston.MechanicalPistonBlock.PistonState;
import com.zurrtum.create.content.contraptions.piston.MechanicalPistonHeadBlock;
import com.zurrtum.create.content.contraptions.piston.PistonExtensionPoleBlock;
import com.zurrtum.create.content.contraptions.pulley.PulleyBlock;
import com.zurrtum.create.content.contraptions.pulley.PulleyBlock.MagnetBlock;
import com.zurrtum.create.content.contraptions.pulley.PulleyBlock.RopeBlock;
import com.zurrtum.create.content.contraptions.pulley.PulleyBlockEntity;
import com.zurrtum.create.content.decoration.slidingDoor.SlidingDoorBlock;
import com.zurrtum.create.content.kinetics.base.BlockBreakingMovementBehaviour;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltBlock;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.zurrtum.create.content.kinetics.gantry.GantryShaftBlock;
import com.zurrtum.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import com.zurrtum.create.content.kinetics.simpleRelays.ShaftBlock;
import com.zurrtum.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import com.zurrtum.create.content.logistics.crate.CreativeCrateBlockEntity;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.zurrtum.create.content.redstone.contact.RedstoneContactBlock;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlock;
import com.zurrtum.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.foundation.utility.BlockHelper;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.block.enums.PistonType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Uuids;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.chunk.BiMapPalette;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.zurrtum.create.Create.LOGGER;
import static com.zurrtum.create.content.contraptions.piston.MechanicalPistonBlock.isExtensionPole;
import static com.zurrtum.create.content.contraptions.piston.MechanicalPistonBlock.isPistonHead;

public abstract class Contraption {
    public static final Codec<Map<UUID, Integer>> SEAT_MAP_CODEC = Codec.unboundedMap(Uuids.STRING_CODEC, Codec.INT);
    public static final Codec<Map<UUID, BlockFace>> SUB_CONTRAPTIONS_CODEC = Codec.unboundedMap(Uuids.STRING_CODEC, BlockFace.CODEC);

    public Optional<List<Box>> simplifiedEntityColliders;
    public AbstractContraptionEntity entity;

    public Box bounds;
    public BlockPos anchor;
    public boolean stalled;
    public boolean hasUniversalCreativeCrate;
    public boolean disassembled;

    protected Map<BlockPos, StructureBlockInfo> blocks;
    protected Map<BlockPos, NbtCompound> updateTags;
    protected List<MutablePair<StructureBlockInfo, MovementContext>> actors;
    protected Map<BlockPos, MovingInteractionBehaviour> interactors;
    protected List<ItemStack> disabledActors;

    protected List<Box> superglue;
    protected List<BlockPos> seats;
    protected Map<UUID, Integer> seatMapping;
    protected Map<UUID, BlockFace> stabilizedSubContraptions;
    protected MountedStorageManager storage;
    protected Multimap<BlockPos, StructureBlockInfo> capturedMultiblocks;

    private Set<SuperGlueEntity> glueToRemove;
    private Map<BlockPos, Entity> initialPassengers;
    private List<BlockFace> pendingSubContraptions;

    private CompletableFuture<Void> simplifiedEntityColliderProvider;

    // Client
    public Map<BlockPos, BlockEntity> presentBlockEntities;
    public List<BlockEntity> renderedBlockEntities;
    // Must be atomic as this is accessed from both the render thread and flywheel executors.
    public final AtomicReference<?> renderInfo = new AtomicReference<>();

    protected ContraptionWorld world;
    public boolean deferInvalidate;

    public Contraption() {
        blocks = new HashMap<>();
        updateTags = new HashMap<>();
        seats = new ArrayList<>();
        actors = new ArrayList<>();
        disabledActors = new ArrayList<>();
        interactors = new HashMap<>();
        superglue = new ArrayList<>();
        seatMapping = new HashMap<>();
        glueToRemove = new HashSet<>();
        initialPassengers = new HashMap<>();
        presentBlockEntities = new HashMap<>();
        renderedBlockEntities = new ArrayList<>();
        pendingSubContraptions = new ArrayList<>();
        stabilizedSubContraptions = new HashMap<>();
        simplifiedEntityColliders = Optional.empty();
        storage = new MountedStorageManager();
        capturedMultiblocks = ArrayListMultimap.create();
    }

    public ContraptionWorld getContraptionWorld() {
        if (world == null)
            world = new ContraptionWorld(entity.getEntityWorld(), this);
        return world;
    }

    public abstract boolean assemble(World world, BlockPos pos) throws AssemblyException;

    public abstract boolean canBeStabilized(Direction facing, BlockPos localPos);

    public abstract ContraptionType getType();

    protected boolean customBlockPlacement(WorldAccess world, BlockPos pos, BlockState state) {
        return false;
    }

    protected boolean customBlockRemoval(WorldAccess world, BlockPos pos, BlockState state) {
        return false;
    }

    protected boolean addToInitialFrontier(World world, BlockPos pos, Direction forcedDirection, Queue<BlockPos> frontier) throws AssemblyException {
        return true;
    }

    public static Contraption fromData(World world, ReadView view, boolean spawnData) {
        ContraptionType type = view.read("Type", ContraptionType.CODEC).orElseThrow();
        Contraption contraption = type.factory.get();
        contraption.read(world, view, spawnData);
        contraption.world = new ContraptionWorld(world, contraption);
        contraption.gatherBBsOffThread();
        return contraption;
    }

    public boolean searchMovedStructure(World world, BlockPos pos, @Nullable Direction forcedDirection) throws AssemblyException {
        initialPassengers.clear();
        Queue<BlockPos> frontier = new UniqueLinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        anchor = pos;

        if (bounds == null)
            bounds = new Box(BlockPos.ORIGIN);

        if (!BlockMovementChecks.isBrittle(world.getBlockState(pos)))
            frontier.add(pos);
        if (!addToInitialFrontier(world, pos, forcedDirection, frontier))
            return false;
        for (int limit = 100000; limit > 0; limit--) {
            if (frontier.isEmpty())
                return true;
            if (!moveBlock(world, forcedDirection, frontier, visited))
                return false;
        }
        throw AssemblyException.structureTooLarge();
    }

    public void onEntityCreated(AbstractContraptionEntity entity) {
        this.entity = entity;

        // Create subcontraptions
        for (BlockFace blockFace : pendingSubContraptions) {
            Direction face = blockFace.getFace();
            StabilizedContraption subContraption = new StabilizedContraption(face);
            World world = entity.getEntityWorld();
            BlockPos pos = blockFace.getPos();
            try {
                if (!subContraption.assemble(world, pos))
                    continue;
            } catch (AssemblyException e) {
                continue;
            }
            subContraption.removeBlocksFromWorld(world, BlockPos.ORIGIN);
            OrientedContraptionEntity movedContraption = OrientedContraptionEntity.create(world, subContraption, face);
            BlockPos anchor = blockFace.getConnectedPos();
            movedContraption.setPosition(anchor.getX() + .5f, anchor.getY(), anchor.getZ() + .5f);
            world.spawnEntity(movedContraption);
            stabilizedSubContraptions.put(movedContraption.getUuid(), new BlockFace(toLocalPos(pos), face));
        }

        storage.initialize();
        gatherBBsOffThread();
    }

    public void onEntityRemoved(AbstractContraptionEntity entity) {
        if (simplifiedEntityColliderProvider != null) {
            simplifiedEntityColliderProvider.cancel(false);
            simplifiedEntityColliderProvider = null;
        }
    }

    public void onEntityInitialize(World world, AbstractContraptionEntity contraptionEntity) {
        if (world.isClient())
            return;

        for (OrientedContraptionEntity orientedCE : world.getNonSpectatingEntities(
            OrientedContraptionEntity.class,
            contraptionEntity.getBoundingBox().expand(1)
        ))
            if (stabilizedSubContraptions.containsKey(orientedCE.getUuid()))
                orientedCE.startRiding(contraptionEntity);

        for (BlockPos seatPos : getSeats()) {
            Entity passenger = initialPassengers.get(seatPos);
            if (passenger == null)
                continue;
            int seatIndex = getSeats().indexOf(seatPos);
            if (seatIndex == -1)
                continue;
            contraptionEntity.addSittingPassenger(passenger, seatIndex);
        }
    }

    private boolean canStickTo(BlockState state, BlockState other) {
        Block stateBlock = state.getBlock();
        if (stateBlock == Blocks.SLIME_BLOCK) {
            return other.getBlock() != Blocks.HONEY_BLOCK;
        } else if (stateBlock == Blocks.HONEY_BLOCK) {
            return other.getBlock() != Blocks.SLIME_BLOCK;
        } else {
            Block otherBlock = other.getBlock();
            return otherBlock == Blocks.SLIME_BLOCK || otherBlock == Blocks.HONEY_BLOCK;
        }
    }

    /**
     * move the first block in frontier queue
     */
    protected boolean moveBlock(
        World world,
        @Nullable Direction forcedDirection,
        Queue<BlockPos> frontier,
        Set<BlockPos> visited
    ) throws AssemblyException {
        BlockPos pos = frontier.poll();
        if (pos == null)
            return false;
        visited.add(pos);

        if (world.isOutOfHeightLimit(pos))
            return true;
        if (!world.isPosLoaded(pos))
            throw AssemblyException.unloadedChunk(pos);
        if (isAnchoringBlockAt(pos))
            return true;
        BlockState state = world.getBlockState(pos);
        if (!BlockMovementChecks.isMovementNecessary(state, world, pos))
            return true;
        if (!movementAllowed(state, world, pos))
            throw AssemblyException.unmovableBlock(pos, state);
        if (state.getBlock() instanceof AbstractChassisBlock && !moveChassis(world, pos, forcedDirection, frontier, visited))
            return false;

        if (state.isOf(AllBlocks.BELT))
            moveBelt(pos, frontier, visited, state);

        if (state.isOf(AllBlocks.WINDMILL_BEARING) && world.getBlockEntity(pos) instanceof WindmillBearingBlockEntity wbbe)
            wbbe.disassembleForMovement();

        if (state.isOf(AllBlocks.GANTRY_CARRIAGE))
            moveGantryPinion(world, pos, frontier, visited, state);

        if (state.isOf(AllBlocks.GANTRY_SHAFT))
            moveGantryShaft(world, pos, frontier, visited, state);

        if (state.isOf(AllBlocks.STICKER) && state.get(StickerBlock.EXTENDED)) {
            Direction offset = state.get(StickerBlock.FACING);
            BlockPos attached = pos.offset(offset);
            if (!visited.contains(attached) && !BlockMovementChecks.isNotSupportive(world.getBlockState(attached), offset.getOpposite()))
                frontier.add(attached);
        }

        if (world.getBlockEntity(pos) instanceof ChainConveyorBlockEntity ccbe)
            ccbe.notifyConnectedToValidate();

        // Double Chest halves stick together
        if (state.contains(ChestBlock.CHEST_TYPE) && state.contains(ChestBlock.FACING) && state.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
            Direction offset = ChestBlock.getFacing(state);
            BlockPos attached = pos.offset(offset);
            if (!visited.contains(attached))
                frontier.add(attached);
        }

        // Bogeys tend to have sticky sides
        if (state.getBlock() instanceof AbstractBogeyBlock<?> bogey)
            for (Direction d : bogey.getStickySurfaces(world, pos, state))
                if (!visited.contains(pos.offset(d)))
                    frontier.add(pos.offset(d));

        // Bearings potentially create stabilized sub-contraptions
        if (state.isOf(AllBlocks.MECHANICAL_BEARING))
            moveBearing(pos, frontier, visited, state);

        // WM Bearings attach their structure when moved
        if (state.isOf(AllBlocks.WINDMILL_BEARING))
            moveWindmillBearing(pos, frontier, visited, state);

        // Seats transfer their passenger to the contraption
        if (state.getBlock() instanceof SeatBlock)
            moveSeat(world, pos);

        // Pulleys drag their rope and their attached structure
        if (state.getBlock() instanceof PulleyBlock)
            movePulley(world, pos, frontier, visited);

        // Pistons drag their attaches poles and extension
        if (state.getBlock() instanceof MechanicalPistonBlock)
            if (!moveMechanicalPiston(world, pos, frontier, visited, state))
                return false;
        if (isExtensionPole(state))
            movePistonPole(world, pos, frontier, visited, state);
        if (isPistonHead(state))
            movePistonHead(world, pos, frontier, visited, state);

        // Cart assemblers attach themselves
        BlockPos posDown = pos.down();
        BlockState stateBelow = world.getBlockState(posDown);
        if (!visited.contains(posDown) && stateBelow.isOf(AllBlocks.CART_ASSEMBLER))
            frontier.add(posDown);

        // Slime blocks and super glue drag adjacent blocks if possible
        for (Direction offset : Iterate.directions) {
            BlockPos offsetPos = pos.offset(offset);
            BlockState blockState = world.getBlockState(offsetPos);
            if (isAnchoringBlockAt(offsetPos))
                continue;
            if (!movementAllowed(blockState, world, offsetPos)) {
                if (offset == forcedDirection)
                    throw AssemblyException.unmovableBlock(pos, state);
                continue;
            }

            boolean wasVisited = visited.contains(offsetPos);
            boolean faceHasGlue = SuperGlueEntity.isGlued(world, pos, offset, glueToRemove);
            boolean blockAttachedTowardsFace = BlockMovementChecks.isBlockAttachedTowards(blockState, world, offsetPos, offset.getOpposite());
            boolean brittle = BlockMovementChecks.isBrittle(blockState);
            boolean canStick = !brittle && canStickTo(state, blockState);
            if (canStick) {
                if (state.getPistonBehavior() == PistonBehavior.PUSH_ONLY || blockState.getPistonBehavior() == PistonBehavior.PUSH_ONLY) {
                    canStick = false;
                }
                if (BlockMovementChecks.isNotSupportive(state, offset)) {
                    canStick = false;
                }
                if (BlockMovementChecks.isNotSupportive(blockState, offset.getOpposite())) {
                    canStick = false;
                }
            }

            if (!wasVisited && (canStick || blockAttachedTowardsFace || faceHasGlue || (offset == forcedDirection && !BlockMovementChecks.isNotSupportive(state,
                forcedDirection
            ))))
                frontier.add(offsetPos);
        }

        addBlock(world, pos, capture(world, pos));
        if (blocks.size() <= AllConfigs.server().kinetics.maxBlocksMoved.get())
            return true;
        else
            throw AssemblyException.structureTooLarge();
    }

    protected void movePistonHead(World world, BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited, BlockState state) {
        Direction direction = state.get(MechanicalPistonHeadBlock.FACING);
        BlockPos offset = pos.offset(direction.getOpposite());
        if (!visited.contains(offset)) {
            BlockState blockState = world.getBlockState(offset);
            if (isExtensionPole(blockState) && blockState.get(PistonExtensionPoleBlock.FACING).getAxis() == direction.getAxis())
                frontier.add(offset);
            if (blockState.getBlock() instanceof MechanicalPistonBlock) {
                Direction pistonFacing = blockState.get(MechanicalPistonBlock.FACING);
                if (pistonFacing == direction && blockState.get(MechanicalPistonBlock.STATE) == PistonState.EXTENDED)
                    frontier.add(offset);
            }
        }
        if (state.get(MechanicalPistonHeadBlock.TYPE) == PistonType.STICKY) {
            BlockPos attached = pos.offset(direction);
            if (!visited.contains(attached))
                frontier.add(attached);
        }
    }

    protected void movePistonPole(World world, BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited, BlockState state) {
        for (Direction d : Iterate.directionsInAxis(state.get(PistonExtensionPoleBlock.FACING).getAxis())) {
            BlockPos offset = pos.offset(d);
            if (!visited.contains(offset)) {
                BlockState blockState = world.getBlockState(offset);
                if (isExtensionPole(blockState) && blockState.get(PistonExtensionPoleBlock.FACING).getAxis() == d.getAxis())
                    frontier.add(offset);
                if (isPistonHead(blockState) && blockState.get(MechanicalPistonHeadBlock.FACING).getAxis() == d.getAxis())
                    frontier.add(offset);
                if (blockState.getBlock() instanceof MechanicalPistonBlock) {
                    Direction pistonFacing = blockState.get(MechanicalPistonBlock.FACING);
                    if (pistonFacing == d || pistonFacing == d.getOpposite() && blockState.get(MechanicalPistonBlock.STATE) == PistonState.EXTENDED)
                        frontier.add(offset);
                }
            }
        }
    }

    protected void moveGantryPinion(World world, BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited, BlockState state) {
        BlockPos offset = pos.offset(state.get(GantryCarriageBlock.FACING));
        if (!visited.contains(offset))
            frontier.add(offset);
        Axis rotationAxis = ((IRotate) state.getBlock()).getRotationAxis(state);
        for (Direction d : Iterate.directionsInAxis(rotationAxis)) {
            offset = pos.offset(d);
            BlockState offsetState = world.getBlockState(offset);
            if (offsetState.isOf(AllBlocks.GANTRY_SHAFT) && offsetState.get(GantryShaftBlock.FACING).getAxis() == d.getAxis())
                if (!visited.contains(offset))
                    frontier.add(offset);
        }
    }

    protected void moveGantryShaft(World world, BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited, BlockState state) {
        for (Direction d : Iterate.directions) {
            BlockPos offset = pos.offset(d);
            if (!visited.contains(offset)) {
                BlockState offsetState = world.getBlockState(offset);
                Direction facing = state.get(GantryShaftBlock.FACING);
                if (d.getAxis() == facing.getAxis() && offsetState.isOf(AllBlocks.GANTRY_SHAFT) && offsetState.get(GantryShaftBlock.FACING) == facing)
                    frontier.add(offset);
                else if (offsetState.isOf(AllBlocks.GANTRY_CARRIAGE) && offsetState.get(GantryCarriageBlock.FACING) == d)
                    frontier.add(offset);
            }
        }
    }

    private void moveWindmillBearing(BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited, BlockState state) {
        Direction facing = state.get(WindmillBearingBlock.FACING);
        BlockPos offset = pos.offset(facing);
        if (!visited.contains(offset))
            frontier.add(offset);
    }

    private void moveBearing(BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited, BlockState state) {
        Direction facing = state.get(MechanicalBearingBlock.FACING);
        if (!canBeStabilized(facing, pos.subtract(anchor))) {
            BlockPos offset = pos.offset(facing);
            if (!visited.contains(offset))
                frontier.add(offset);
            return;
        }
        pendingSubContraptions.add(new BlockFace(pos, facing));
    }

    private void moveBelt(BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited, BlockState state) {
        BlockPos nextPos = BeltBlock.nextSegmentPosition(state, pos, true);
        BlockPos prevPos = BeltBlock.nextSegmentPosition(state, pos, false);
        if (nextPos != null && !visited.contains(nextPos))
            frontier.add(nextPos);
        if (prevPos != null && !visited.contains(prevPos))
            frontier.add(prevPos);
    }

    private void moveSeat(World world, BlockPos pos) {
        BlockPos local = toLocalPos(pos);
        getSeats().add(local);
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        List<SeatEntity> seatsEntities = world.getNonSpectatingEntities(SeatEntity.class, new Box(x, y - 0.1f, z, x + 1, y + 1, z + 1));
        if (!seatsEntities.isEmpty()) {
            SeatEntity seat = seatsEntities.getFirst();
            List<Entity> passengers = seat.getPassengerList();
            if (!passengers.isEmpty())
                initialPassengers.put(local, passengers.getFirst());
        }
    }

    private void movePulley(World world, BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited) {
        int limit = AllConfigs.server().kinetics.maxRopeLength.get();
        BlockPos ropePos = pos;
        while (limit-- >= 0) {
            ropePos = ropePos.down();
            if (!world.isPosLoaded(ropePos))
                break;
            BlockState ropeState = world.getBlockState(ropePos);
            Block block = ropeState.getBlock();
            if (!(block instanceof RopeBlock) && !(block instanceof MagnetBlock)) {
                if (!visited.contains(ropePos))
                    frontier.add(ropePos);
                break;
            }
            addBlock(world, ropePos, capture(world, ropePos));
        }
    }

    private boolean moveMechanicalPiston(
        World world,
        BlockPos pos,
        Queue<BlockPos> frontier,
        Set<BlockPos> visited,
        BlockState state
    ) throws AssemblyException {
        Direction direction = state.get(MechanicalPistonBlock.FACING);
        PistonState pistonState = state.get(MechanicalPistonBlock.STATE);
        if (pistonState == PistonState.MOVING)
            return false;

        BlockPos offset = pos.offset(direction.getOpposite());
        if (!visited.contains(offset)) {
            BlockState poleState = world.getBlockState(offset);
            if (poleState.isOf(AllBlocks.PISTON_EXTENSION_POLE) && poleState.get(PistonExtensionPoleBlock.FACING).getAxis() == direction.getAxis())
                frontier.add(offset);
        }

        if (pistonState == PistonState.EXTENDED || MechanicalPistonBlock.isStickyPiston(state)) {
            offset = pos.offset(direction);
            if (!visited.contains(offset))
                frontier.add(offset);
        }

        return true;
    }

    private boolean moveChassis(World world, BlockPos pos, Direction movementDirection, Queue<BlockPos> frontier, Set<BlockPos> visited) {
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof ChassisBlockEntity chassis))
            return false;
        chassis.addAttachedChasses(frontier, visited);
        List<BlockPos> includedBlockPositions = chassis.getIncludedBlockPositions(movementDirection, false);
        if (includedBlockPositions == null)
            return false;
        for (BlockPos blockPos : includedBlockPositions)
            if (!visited.contains(blockPos))
                frontier.add(blockPos);
        return true;
    }

    protected Pair<StructureBlockInfo, BlockEntity> capture(World world, BlockPos pos) {
        BlockState blockstate = world.getBlockState(pos);
        if (blockstate.isOf(AllBlocks.REDSTONE_CONTACT))
            blockstate = blockstate.with(RedstoneContactBlock.POWERED, true);
        if (blockstate.isOf(AllBlocks.POWERED_SHAFT))
            blockstate = BlockHelper.copyProperties(blockstate, AllBlocks.SHAFT.getDefaultState());
        if (blockstate.getBlock() instanceof ControlsBlock && getType().is(AllContraptionTypeTags.OPENS_CONTROLS))
            blockstate = blockstate.with(ControlsBlock.OPEN, true);
        if (blockstate.contains(SlidingDoorBlock.VISIBLE))
            blockstate = blockstate.with(SlidingDoorBlock.VISIBLE, false);
        if (blockstate.getBlock() instanceof ButtonBlock) {
            blockstate = blockstate.with(ButtonBlock.POWERED, false);
            world.scheduleBlockTick(pos, blockstate.getBlock(), -1);
        }
        if (blockstate.getBlock() instanceof PressurePlateBlock) {
            blockstate = blockstate.with(PressurePlateBlock.POWERED, false);
            world.scheduleBlockTick(pos, blockstate.getBlock(), -1);
        }
        NbtCompound compoundnbt = getBlockEntityNBT(world, pos);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof PoweredShaftBlockEntity)
            blockEntity = new BracketedKineticBlockEntity(pos, blockstate);
        if (blockEntity instanceof FactoryPanelBlockEntity fpbe) {
            try (ErrorReporter.Logging logging = new ErrorReporter.Logging(blockEntity.getReporterContext(), LOGGER)) {
                fpbe.writeSafe(new NbtWriteView(logging, world.getRegistryManager().getOps(NbtOps.INSTANCE), compoundnbt));
            }
        }

        return Pair.of(new StructureBlockInfo(pos, blockstate, compoundnbt), blockEntity);
    }

    protected void addBlock(World level, BlockPos pos, Pair<StructureBlockInfo, BlockEntity> pair) {
        StructureBlockInfo captured = pair.getKey();
        BlockPos localPos = pos.subtract(anchor);
        BlockState state = captured.state();
        StructureBlockInfo structureBlockInfo = new StructureBlockInfo(localPos, state, captured.nbt());

        if (blocks.put(localPos, structureBlockInfo) != null)
            return;
        bounds = bounds.union(new Box(localPos));

        BlockEntity be = pair.getValue();

        if (be != null) {
            NbtCompound updateTag = be.toInitialChunkDataNbt(level.getRegistryManager());
            // empty tags are intentionally kept, see writeBlocksCompound
            // for testing, this line can be commented to emulate legacy behavior
            updateTags.put(localPos, updateTag);
        }

        storage.addBlock(level, state, pos, localPos, be);

        captureMultiblock(localPos, structureBlockInfo, be);

        if (MovementBehaviour.REGISTRY.get(state) != null)
            actors.add(MutablePair.of(structureBlockInfo, null));

        MovingInteractionBehaviour interactionBehaviour = MovingInteractionBehaviour.REGISTRY.get(state);
        if (interactionBehaviour != null)
            interactors.put(localPos, interactionBehaviour);

        if (be instanceof CreativeCrateBlockEntity crateBlockEntity && crateBlockEntity.getBehaviour(ServerFilteringBehaviour.TYPE).getFilter()
            .isEmpty())
            hasUniversalCreativeCrate = true;
    }

    protected void captureMultiblock(BlockPos localPos, StructureBlockInfo structureBlockInfo, BlockEntity be) {
        if (!(be instanceof IMultiBlockEntityContainer multiBlockBE))
            return;

        NbtCompound nbt = structureBlockInfo.nbt();
        BlockPos controllerPos = nbt.get("Controller", BlockPos.CODEC).map(this::toLocalPos).orElse(localPos);
        nbt.put("Controller", BlockPos.CODEC, controllerPos);

        if (updateTags.containsKey(localPos))
            updateTags.get(localPos).put("Controller", BlockPos.CODEC, controllerPos);

        if (multiBlockBE.isController() && multiBlockBE.getHeight() <= 1 && multiBlockBE.getWidth() <= 1) {
            nbt.put("LastKnownPos", BlockPos.CODEC, BlockPos.ORIGIN.down(Integer.MAX_VALUE - 1));
            return;
        }

        nbt.remove("LastKnownPos");
        capturedMultiblocks.put(controllerPos, structureBlockInfo);
    }

    @Nullable
    protected NbtCompound getBlockEntityNBT(World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity == null)
            return null;
        NbtCompound nbt = blockEntity.createNbtWithIdentifyingData(world.getRegistryManager());
        nbt.remove("x");
        nbt.remove("y");
        nbt.remove("z");

        return nbt;
    }

    protected BlockPos toLocalPos(BlockPos globalPos) {
        return globalPos.subtract(anchor);
    }

    protected boolean movementAllowed(BlockState state, World world, BlockPos pos) {
        return BlockMovementChecks.isMovementAllowed(state, world, pos);
    }

    protected boolean isAnchoringBlockAt(BlockPos pos) {
        return pos.equals(anchor);
    }

    public void read(World world, ReadView view, boolean spawnData) {
        blocks.clear();
        presentBlockEntities.clear();
        renderedBlockEntities.clear();

        readBlocksCompound(view.getReadView("Blocks"), world);

        capturedMultiblocks.clear();
        view.getListReadView("CapturedMultiblocks").forEach(c -> {
            BlockPos controllerPos = c.read("Controller", BlockPos.CODEC).orElseThrow();
            c.read("Parts", CreateCodecs.BLOCK_POS_LIST_CODEC).orElseThrow().forEach(pos -> capturedMultiblocks.put(controllerPos, blocks.get(pos)));
        });

        storage.read(view, spawnData, this);

        actors.clear();
        view.getListReadView("Actors").forEach(c -> {
            c.read("Pos", BlockPos.CODEC).ifPresent(pos -> {
                StructureBlockInfo info = blocks.get(pos);
                if (info == null)
                    return;
                MovementContext context = MovementContext.read(world, info, c, this);
                actors.add(MutablePair.of(info, context));
            });
        });

        disabledActors.clear();
        superglue.clear();
        seats.clear();
        seatMapping.clear();
        stabilizedSubContraptions.clear();
        view.read("DisabledActors", CreateCodecs.ITEM_LIST_CODEC).ifPresent(list -> {
            disabledActors.addAll(list);
            for (ItemStack stack : disabledActors) {
                setActorsActive(stack, false);
            }
        });
        view.read("Superglue", CreateCodecs.BOX_CODEC.listOf()).ifPresent(list -> superglue.addAll(list));
        view.read("Seats", CreateCodecs.BLOCK_POS_LIST_CODEC).ifPresent(list -> seats.addAll(list));
        view.read("Passengers", SEAT_MAP_CODEC).ifPresent(map -> seatMapping.putAll(map));
        view.read("SubContraptions", SUB_CONTRAPTIONS_CODEC).ifPresent(map -> stabilizedSubContraptions.putAll(map));

        view.read("Interactors", CreateCodecs.BLOCK_POS_LIST_CODEC).ifPresentOrElse(
            list -> list.forEach(pos -> {
                StructureBlockInfo structureBlockInfo = blocks.get(pos);
                if (structureBlockInfo == null)
                    return;
                MovingInteractionBehaviour behaviour = MovingInteractionBehaviour.REGISTRY.get(structureBlockInfo.state());
                if (behaviour != null)
                    interactors.put(pos, behaviour);
            }), interactors::clear
        );

        view.read("BoundsFront", CreateCodecs.BOX_CODEC).ifPresent(box -> bounds = box);
        stalled = view.getBoolean("Stalled", false);
        hasUniversalCreativeCrate = view.getBoolean("BottomlessSupply", false);
        anchor = view.read("Anchor", BlockPos.CODEC).orElseThrow();
    }

    public void write(WriteView view, boolean spawnPacket) {
        view.put("Type", CreateRegistries.CONTRAPTION_TYPE.getCodec(), getType());

        writeBlocksCompound(view.get("Blocks"), spawnPacket);

        WriteView.ListView multiblocks = view.getList("CapturedMultiblocks");
        capturedMultiblocks.keySet().forEach(controllerPos -> {
            WriteView block = multiblocks.add();
            block.put("Controller", BlockPos.CODEC, controllerPos);

            Collection<StructureBlockInfo> multiblockParts = capturedMultiblocks.get(controllerPos);
            List<BlockPos> list = multiblockParts.stream().map(StructureBlockInfo::pos).toList();
            block.put("Parts", CreateCodecs.BLOCK_POS_LIST_CODEC, list);
        });

        WriteView.ListView actors = view.getList("Actors");
        for (MutablePair<StructureBlockInfo, MovementContext> actor : getActors()) {
            MovementBehaviour behaviour = MovementBehaviour.REGISTRY.get(actor.left.state());
            if (behaviour == null)
                continue;
            WriteView item = actors.add();
            item.put("Pos", BlockPos.CODEC, actor.left.pos());
            behaviour.writeExtraData(actor.right);
            actor.right.write(item);
        }

        view.put("DisabledActors", CreateCodecs.ITEM_LIST_CODEC, disabledActors);
        if (!spawnPacket) {
            view.put("Superglue", CreateCodecs.BOX_CODEC.listOf(), superglue);
        }

        writeStorage(view, spawnPacket);

        view.put("Interactors", CreateCodecs.BLOCK_POS_LIST_CODEC, interactors.keySet().stream().toList());
        view.put("Seats", CreateCodecs.BLOCK_POS_LIST_CODEC, seats);
        view.put("Passengers", SEAT_MAP_CODEC, seatMapping);
        view.put("SubContraptions", SUB_CONTRAPTIONS_CODEC, stabilizedSubContraptions);
        view.put("Anchor", BlockPos.CODEC, anchor);
        view.putBoolean("Stalled", stalled);
        view.putBoolean("BottomlessSupply", hasUniversalCreativeCrate);

        if (bounds != null) {
            view.put("BoundsFront", CreateCodecs.BOX_CODEC, bounds);
        }
    }

    public void writeStorage(WriteView view, boolean spawnPacket) {
        storage.write(view, spawnPacket);
    }

    private void writeBlocksCompound(WriteView view, boolean spawnPacket) {
        BiMapPalette<BlockState> palette = new BiMapPalette<>(
            Block.STATE_IDS, 16, (i, s) -> {
            throw new IllegalStateException("Palette Map index exceeded maximum");
        }
        );
        WriteView.ListView blockList = view.getList("BlockList");

        for (StructureBlockInfo block : this.blocks.values()) {
            int id = palette.index(block.state());
            BlockPos pos = block.pos();
            WriteView c = blockList.add();
            c.putLong("Pos", pos.asLong());
            c.putInt("State", id);

            NbtCompound updateTag = updateTags.get(pos);
            if (spawnPacket) {
                // for client sync, treat the updateTag as the data
                if (updateTag != null) {
                    c.put("Data", NbtCompound.CODEC, updateTag);
                } else if (block.nbt() != null) {
                    // an updateTag is saved for all BlockEntities, even when empty.
                    // this case means that the contraption was assembled pre-updateTags.
                    // in this case, we need to use the full BlockEntity data.
                    c.put("Data", NbtCompound.CODEC, block.nbt());
                    c.putBoolean("Legacy", true);
                }
            } else {
                // otherwise, write actual data as the data, save updateTag on its own
                if (block.nbt() != null) {
                    c.put("Data", NbtCompound.CODEC, block.nbt());
                }
                if (updateTag != null) {
                    c.put("UpdateTag", NbtCompound.CODEC, updateTag);
                }
            }
        }

        int size = palette.getSize();
        List<BlockState> paletteData = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            paletteData.add(palette.get(i));
        }
        view.put("Palette", CreateCodecs.BLOCK_STATE_LIST_CODEC, paletteData);
    }

    private void readBlocksCompound(ReadView view, World world) {
        BiMapPalette<BlockState> palette = new BiMapPalette<>(
            Block.STATE_IDS, 16, (i, s) -> {
            throw new IllegalStateException("Palette Map index exceeded maximum");
        }, view.read("Palette", CreateCodecs.BLOCK_STATE_LIST_CODEC).orElseGet(ArrayList::new)
        );

        view.getListReadView("BlockList").forEach(c -> {
            StructureBlockInfo info = readStructureBlockInfo(c, palette);

            blocks.put(info.pos(), info);

            // it's very important that empty tags are read here. see writeBlocksCompound
            c.read("UpdateTag", NbtCompound.CODEC).ifPresent(updateTag -> updateTags.put(info.pos(), updateTag));

            if (!world.isClient())
                return;

            // create the BlockEntity client-side for rendering
            BlockEntity be = readBlockEntity(world, info, c);
            if (be == null)
                return;

            presentBlockEntities.put(info.pos(), be);

            MovementBehaviour movementBehaviour = MovementBehaviour.REGISTRY.get(info.state());
            if (movementBehaviour == null || !movementBehaviour.disableBlockEntityRendering()) {
                renderedBlockEntities.add(be);
            }
        });
    }

    @Nullable
    protected BlockEntity readBlockEntity(World level, StructureBlockInfo info, ReadView view) {
        BlockState state = info.state();
        BlockPos pos = info.pos();
        NbtCompound nbt = info.nbt();

        if (view.getBoolean("Legacy", false)) {
            // for contraptions that were assembled pre-updateTags, we need to use the old strategy.
            if (nbt == null)
                return null;

            nbt.putInt("x", pos.getX());
            nbt.putInt("y", pos.getY());
            nbt.putInt("z", pos.getZ());

            BlockEntity be = BlockEntity.createFromNbt(pos, state, nbt, level.getRegistryManager());
            postprocessReadBlockEntity(level, be);
            return be;
        }

        if (!state.hasBlockEntity() || !(state.getBlock() instanceof BlockEntityProvider entityBlock))
            return null;

        BlockEntity be = entityBlock.createBlockEntity(pos, state);
        postprocessReadBlockEntity(level, be);
        if (be != null && nbt != null) {
            try (ErrorReporter.Logging logging = new ErrorReporter.Logging(be.getReporterContext(), LOGGER)) {
                be.read(NbtReadView.create(logging, level.getRegistryManager(), nbt));
            }
        }

        return be;
    }

    private static void postprocessReadBlockEntity(World level, @Nullable BlockEntity be) {
        if (be != null) {
            be.setWorld(level);
            if (be instanceof KineticBlockEntity kbe) {
                kbe.setSpeed(0);
            }
        }
    }

    private static StructureBlockInfo readStructureBlockInfo(ReadView view, BiMapPalette<BlockState> palette) {
        return new StructureBlockInfo(
            BlockPos.fromLong(view.getLong("Pos", 0)),
            Objects.requireNonNull(palette.get(view.getInt("State", 0))),
            view.read("Data", NbtCompound.CODEC).orElse(null)
        );
    }

    private static StructureBlockInfo legacyReadStructureBlockInfo(NbtCompound blockListEntry, RegistryEntryLookup<Block> holderGetter) {
        return new StructureBlockInfo(
            NBTHelper.readBlockPos(blockListEntry, "Pos"),
            NbtHelper.toBlockState(holderGetter, blockListEntry.getCompoundOrEmpty("Block")),
            blockListEntry.contains("Data") ? blockListEntry.getCompoundOrEmpty("Data") : null
        );
    }

    public void removeBlocksFromWorld(World world, BlockPos offset) {
        glueToRemove.forEach(glue -> {
            superglue.add(glue.getBoundingBox().offset(Vec3d.of(offset.add(anchor)).multiply(-1)));
            glue.discard();
        });

        List<BlockBox> minimisedGlue = new ArrayList<>();
        for (int i = 0; i < superglue.size(); i++)
            minimisedGlue.add(null);

        for (boolean brittles : Iterate.trueAndFalse) {
            for (Iterator<StructureBlockInfo> iterator = blocks.values().iterator(); iterator.hasNext(); ) {
                StructureBlockInfo block = iterator.next();
                if (brittles != BlockMovementChecks.isBrittle(block.state()))
                    continue;

                for (int i = 0; i < superglue.size(); i++) {
                    Box aabb = superglue.get(i);
                    if (aabb == null || !aabb.contains(block.pos().getX() + .5, block.pos().getY() + .5, block.pos().getZ() + .5))
                        continue;
                    if (minimisedGlue.get(i) == null)
                        minimisedGlue.set(i, new BlockBox(block.pos()));
                    else
                        minimisedGlue.set(i, BBHelper.encapsulate(minimisedGlue.get(i), block.pos()));
                }

                BlockPos add = block.pos().add(anchor).add(offset);
                if (customBlockRemoval(world, add, block.state()))
                    continue;
                BlockState oldState = world.getBlockState(add);
                Block blockIn = oldState.getBlock();
                boolean blockMismatch = block.state().getBlock() != blockIn;
                blockMismatch &= AllBlocks.POWERED_SHAFT != blockIn || !block.state().isOf(AllBlocks.SHAFT);
                if (blockMismatch)
                    iterator.remove();
                world.removeBlockEntity(add);
                int flags = Block.MOVED | Block.SKIP_DROPS | Block.FORCE_STATE | Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD;
                if (blockIn instanceof Waterloggable && oldState.contains(Properties.WATERLOGGED) && oldState.get(Properties.WATERLOGGED)) {
                    world.setBlockState(add, Blocks.WATER.getDefaultState(), flags);
                    continue;
                }
                world.setBlockState(add, Blocks.AIR.getDefaultState(), flags);
            }
        }

        superglue.clear();
        for (BlockBox box : minimisedGlue) {
            if (box == null)
                continue;
            Box bb = new Box(box.getMinX(), box.getMinY(), box.getMinZ(), box.getMaxX() + 1, box.getMaxY() + 1, box.getMaxZ() + 1);
            if (bb.getAverageSideLength() > 1.01)
                superglue.add(bb);
        }

        for (StructureBlockInfo block : blocks.values()) {
            BlockPos add = block.pos().add(anchor).add(offset);
            //			if (!shouldUpdateAfterMovement(block))
            //				continue;

            int flags = Block.MOVED | Block.NOTIFY_ALL;
            world.updateListeners(add, block.state(), Blocks.AIR.getDefaultState(), flags);

            // when the blockstate is set to air, the block's POI data is removed, but
            // markAndNotifyBlock tries to
            // remove it again, so to prevent an error from being logged by double-removal
            // we add the POI data back now
            // (code copied from ServerWorld.onBlockStateChange)
            ServerWorld serverWorld = (ServerWorld) world;
            PointOfInterestTypes.getTypeForState(block.state()).ifPresent(poiType -> {
                world.getServer().execute(() -> {
                    serverWorld.getPointOfInterestStorage().add(add, poiType);
                });
            });

            BlockHelper.markAndNotifyBlock(world, add, world.getWorldChunk(add), block.state(), Blocks.AIR.getDefaultState(), flags);
            block.state().prepare(world, add, flags & -2);
        }
    }

    public void addBlocksToWorld(World world, StructureTransform transform) {
        if (disassembled)
            return;
        disassembled = true;

        boolean shouldDropBlocks = !AllConfigs.server().kinetics.noDropWhenContraptionReplaceBlocks.get();

        translateMultiblockControllers(transform);

        for (boolean nonBrittles : Iterate.trueAndFalse) {
            for (StructureBlockInfo block : blocks.values()) {
                if (nonBrittles == BlockMovementChecks.isBrittle(block.state()))
                    continue;

                BlockPos targetPos = transform.apply(block.pos());
                BlockState state = transform.apply(block.state());

                if (customBlockPlacement(world, targetPos, state))
                    continue;

                if (nonBrittles)
                    for (Direction face : Iterate.directions)
                        state = state.getStateForNeighborUpdate(
                            world,
                            world,
                            targetPos,
                            face,
                            targetPos.offset(face),
                            world.getBlockState(targetPos.offset(face)),
                            world.random
                        );

                BlockState blockState = world.getBlockState(targetPos);
                if (blockState.getHardness(world, targetPos) == -1 || (state.getCollisionShape(world, targetPos)
                    .isEmpty() && !blockState.getCollisionShape(world, targetPos).isEmpty())) {
                    if (targetPos.getY() == world.getBottomY())
                        targetPos = targetPos.up();
                    world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, targetPos, Block.getRawIdFromState(state));
                    if (shouldDropBlocks) {
                        Block.dropStacks(state, world, targetPos, null);
                    }
                    continue;
                }
                if (state.getBlock() instanceof Waterloggable && state.contains(Properties.WATERLOGGED)) {
                    FluidState FluidState = world.getFluidState(targetPos);
                    state = state.with(Properties.WATERLOGGED, FluidState.getFluid() == Fluids.WATER);
                }

                world.breakBlock(targetPos, shouldDropBlocks);

                if (state.isOf(AllBlocks.SHAFT))
                    state = ShaftBlock.pickCorrectShaftType(state, world, targetPos);
                if (state.contains(SlidingDoorBlock.VISIBLE))
                    state = state.with(SlidingDoorBlock.VISIBLE, !state.get(SlidingDoorBlock.OPEN)).with(SlidingDoorBlock.POWERED, false);
                // Stop Sculk shriekers from getting "stuck" if moved mid-shriek.
                if (state.isOf(Blocks.SCULK_SHRIEKER)) {
                    state = Blocks.SCULK_SHRIEKER.getDefaultState();
                }

                world.setBlockState(targetPos, state, Block.MOVED | Block.NOTIFY_ALL);

                boolean verticalRotation = transform.rotationAxis == null || transform.rotationAxis.isHorizontal();
                verticalRotation = verticalRotation && transform.rotation != BlockRotation.NONE;
                if (verticalRotation) {
                    if (state.getBlock() instanceof RopeBlock || state.getBlock() instanceof MagnetBlock || state.getBlock() instanceof DoorBlock)
                        world.breakBlock(targetPos, shouldDropBlocks);
                }

                BlockEntity blockEntity = world.getBlockEntity(targetPos);

                NbtCompound tag = block.nbt();

                // Temporary fix: Calling load(CompoundTag tag) on a Sculk sensor causes it to not react to vibrations.
                if (state.isOf(Blocks.SCULK_SENSOR) || state.isOf(Blocks.SCULK_SHRIEKER))
                    tag = null;

                if (blockEntity != null)
                    tag = NBTProcessors.process(state, blockEntity, tag, false);
                if (blockEntity != null && tag != null) {
                    tag.putInt("x", targetPos.getX());
                    tag.putInt("y", targetPos.getY());
                    tag.putInt("z", targetPos.getZ());

                    if (verticalRotation && blockEntity instanceof PulleyBlockEntity) {
                        tag.remove("Offset");
                        tag.remove("InitialOffset");
                    }

                    if (blockEntity instanceof IMultiBlockEntityContainer) {
                        if (tag.contains("LastKnownPos") || capturedMultiblocks.isEmpty()) {
                            tag.put("LastKnownPos", BlockPos.CODEC, BlockPos.ORIGIN.down(Integer.MAX_VALUE - 1));
                            tag.remove("Controller");
                        }
                    }

                    try (ErrorReporter.Logging logging = new ErrorReporter.Logging(blockEntity.getReporterContext(), LOGGER)) {
                        blockEntity.read(NbtReadView.create(logging, world.getRegistryManager(), tag));
                    }
                }

                storage.unmount(world, block, targetPos, blockEntity);

                if (blockEntity != null) {
                    transform.apply(blockEntity);
                }
            }
        }

        for (StructureBlockInfo block : blocks.values()) {
            if (!shouldUpdateAfterMovement(block))
                continue;
            BlockPos targetPos = transform.apply(block.pos());
            BlockHelper.markAndNotifyBlock(
                world,
                targetPos,
                world.getWorldChunk(targetPos),
                block.state(),
                block.state(),
                Block.MOVED | Block.NOTIFY_ALL
            );
        }

        for (Box box : superglue) {
            box = new Box(transform.apply(new Vec3d(box.minX, box.minY, box.minZ)), transform.apply(new Vec3d(box.maxX, box.maxY, box.maxZ)));
            if (!world.isClient())
                world.spawnEntity(new SuperGlueEntity(world, box));
        }
    }

    protected void translateMultiblockControllers(StructureTransform transform) {
        if (transform.rotationAxis != null && transform.rotationAxis != Axis.Y && transform.rotation != BlockRotation.NONE) {
            capturedMultiblocks.values().forEach(info -> {
                info.nbt().put("LastKnownPos", BlockPos.CODEC, BlockPos.ORIGIN.down(Integer.MAX_VALUE - 1));
            });
            return;
        }

        capturedMultiblocks.keySet().forEach(controllerPos -> {
            Collection<StructureBlockInfo> multiblockParts = capturedMultiblocks.get(controllerPos);
            Optional<BlockBox> optionalBoundingBox = BlockBox.encompassPositions(multiblockParts.stream().map(info -> transform.apply(info.pos()))
                .toList());
            if (optionalBoundingBox.isEmpty())
                return;

            BlockBox boundingBox = optionalBoundingBox.get();
            BlockPos newControllerPos = new BlockPos(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ());
            BlockPos otherPos = transform.unapply(newControllerPos);

            multiblockParts.forEach(info -> info.nbt().put("Controller", BlockPos.CODEC, newControllerPos));

            if (controllerPos.equals(otherPos))
                return;

            // swap nbt data to the new controller position
            StructureBlockInfo prevControllerInfo = blocks.get(controllerPos);
            StructureBlockInfo newControllerInfo = blocks.get(otherPos);
            if (prevControllerInfo == null || newControllerInfo == null)
                return;

            blocks.put(otherPos, new StructureBlockInfo(newControllerInfo.pos(), newControllerInfo.state(), prevControllerInfo.nbt()));
            blocks.put(controllerPos, new StructureBlockInfo(prevControllerInfo.pos(), prevControllerInfo.state(), newControllerInfo.nbt()));
        });
    }

    public void addPassengersToWorld(World world, StructureTransform transform, List<Entity> seatedEntities) {
        for (Entity seatedEntity : seatedEntities) {
            if (getSeatMapping().isEmpty())
                continue;
            Integer seatIndex = getSeatMapping().get(seatedEntity.getUuid());
            if (seatIndex == null)
                continue;
            BlockPos seatPos = getSeats().get(seatIndex);
            seatPos = transform.apply(seatPos);
            if (!(world.getBlockState(seatPos).getBlock() instanceof SeatBlock))
                continue;
            if (SeatBlock.isSeatOccupied(world, seatPos))
                continue;
            SeatBlock.sitDown(world, seatPos, seatedEntity);
        }
    }

    public void startMoving(World world) {
        disabledActors.clear();

        for (MutablePair<StructureBlockInfo, MovementContext> pair : actors) {
            MovementContext context = new MovementContext(world, pair.left, this);
            MovementBehaviour behaviour = MovementBehaviour.REGISTRY.get(pair.left.state());
            if (behaviour != null)
                behaviour.startMoving(context);
            pair.setRight(context);
            if (behaviour instanceof ContraptionControlsMovement)
                disableActorOnStart(context);
        }

        for (ItemStack stack : disabledActors)
            setActorsActive(stack, false);
    }

    protected void disableActorOnStart(MovementContext context) {
        if (!ContraptionControlsMovement.isDisabledInitially(context))
            return;
        ItemStack filter = ContraptionControlsMovement.getFilter(context);
        if (filter == null)
            return;
        if (isActorTypeDisabled(filter))
            return;
        disabledActors.add(filter);
    }

    public boolean isActorTypeDisabled(ItemStack filter) {
        return disabledActors.stream().anyMatch(i -> ContraptionControlsMovement.isSameFilter(i, filter));
    }

    public void setActorsActive(ItemStack referenceStack, boolean enable) {
        for (MutablePair<StructureBlockInfo, MovementContext> pair : actors) {
            MovementBehaviour behaviour = MovementBehaviour.REGISTRY.get(pair.left.state());
            if (behaviour == null)
                continue;
            ItemStack behaviourStack = behaviour.canBeDisabledVia(pair.right);
            if (behaviourStack == null)
                continue;
            if (!referenceStack.isEmpty() && !ContraptionControlsMovement.isSameFilter(referenceStack, behaviourStack))
                continue;
            pair.right.disabled = !enable;
            if (!enable)
                behaviour.onDisabledByControls(pair.right);
        }
    }

    public List<ItemStack> getDisabledActors() {
        return disabledActors;
    }

    public void stop(World world) {
        forEachActor(
            world, (behaviour, ctx) -> {
                behaviour.stopMoving(ctx);
                ctx.position = null;
                ctx.motion = Vec3d.ZERO;
                ctx.relativeMotion = Vec3d.ZERO;
                ctx.rotation = v -> v;
            }
        );
    }

    public void forEachActor(World world, BiConsumer<MovementBehaviour, MovementContext> callBack) {
        for (MutablePair<StructureBlockInfo, MovementContext> pair : actors) {
            MovementBehaviour behaviour = MovementBehaviour.REGISTRY.get(pair.getLeft().state());
            if (behaviour == null)
                continue;
            callBack.accept(behaviour, pair.getRight());
        }
    }

    protected boolean shouldUpdateAfterMovement(StructureBlockInfo info) {
        if (PointOfInterestTypes.getTypeForState(info.state()).isPresent())
            return false;
        if (info.state().getBlock() instanceof SlidingDoorBlock)
            return false;
        return true;
    }

    public void expandBoundsAroundAxis(Axis axis) {
        Set<BlockPos> blocks = getBlocks().keySet();

        int radius = (int) (Math.ceil(getRadius(blocks, axis)));

        int maxX = radius + 2;
        int maxY = radius + 2;
        int maxZ = radius + 2;
        int minX = -radius - 1;
        int minY = -radius - 1;
        int minZ = -radius - 1;

        if (axis == Axis.X) {
            maxX = (int) bounds.maxX;
            minX = (int) bounds.minX;
        } else if (axis == Axis.Y) {
            maxY = (int) bounds.maxY;
            minY = (int) bounds.minY;
        } else if (axis == Axis.Z) {
            maxZ = (int) bounds.maxZ;
            minZ = (int) bounds.minZ;
        }

        bounds = new Box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public Map<UUID, Integer> getSeatMapping() {
        return seatMapping;
    }

    public BlockPos getSeatOf(UUID entityId) {
        if (!getSeatMapping().containsKey(entityId))
            return null;
        int seatIndex = getSeatMapping().get(entityId);
        if (seatIndex >= getSeats().size())
            return null;
        return getSeats().get(seatIndex);
    }

    public BlockPos getBearingPosOf(UUID subContraptionEntityId) {
        if (stabilizedSubContraptions.containsKey(subContraptionEntityId))
            return stabilizedSubContraptions.get(subContraptionEntityId).getConnectedPos();
        return null;
    }

    public void setSeatMapping(Map<UUID, Integer> seatMapping) {
        this.seatMapping = seatMapping;
    }

    public List<BlockPos> getSeats() {
        return seats;
    }

    public Map<BlockPos, StructureBlockInfo> getBlocks() {
        return blocks;
    }

    public List<MutablePair<StructureBlockInfo, MovementContext>> getActors() {
        return actors;
    }

    @Nullable
    public MutablePair<StructureBlockInfo, MovementContext> getActorAt(BlockPos localPos) {
        for (MutablePair<StructureBlockInfo, MovementContext> pair : actors)
            if (localPos.equals(pair.left.pos()))
                return pair;
        return null;
    }

    public Map<BlockPos, MovingInteractionBehaviour> getInteractors() {
        return interactors;
    }

    public void invalidateColliders() {
        simplifiedEntityColliders = Optional.empty();
        gatherBBsOffThread();
    }

    private void gatherBBsOffThread() {
        getContraptionWorld();
        if (simplifiedEntityColliderProvider != null) {
            simplifiedEntityColliderProvider.cancel(false);
        }
        simplifiedEntityColliderProvider = CompletableFuture.supplyAsync(() -> {
            VoxelShape combinedShape = VoxelShapes.empty();
            for (Map.Entry<BlockPos, StructureBlockInfo> entry : blocks.entrySet()) {
                StructureBlockInfo info = entry.getValue();
                BlockPos localPos = entry.getKey();
                VoxelShape collisionShape = info.state().getCollisionShape(world, localPos, ShapeContext.absent());
                if (collisionShape.isEmpty())
                    continue;
                combinedShape = VoxelShapes.combine(
                    combinedShape,
                    collisionShape.offset(localPos.getX(), localPos.getY(), localPos.getZ()),
                    BooleanBiFunction.OR
                );
            }
            return combinedShape.simplify().getBoundingBoxes();
        }).thenAccept(r -> {
            simplifiedEntityColliders = Optional.of(r);
        });
    }

    public static double getRadius(Iterable<? extends Vec3i> blocks, Axis axis) {
        Axis axisA;
        Axis axisB;

        switch (axis) {
            case X -> {
                axisA = Axis.Y;
                axisB = Axis.Z;
            }
            case Y -> {
                axisA = Axis.X;
                axisB = Axis.Z;
            }
            case Z -> {
                axisA = Axis.X;
                axisB = Axis.Y;
            }
            default -> throw new IllegalStateException("Unexpected value: " + axis);
        }

        int maxDistSq = 0;
        for (Vec3i vec : blocks) {
            int a = vec.getComponentAlongAxis(axisA);
            int b = vec.getComponentAlongAxis(axisB);

            int distSq = a * a + b * b;

            if (distSq > maxDistSq)
                maxDistSq = distSq;
        }

        return Math.sqrt(maxDistSq);
    }

    public MountedStorageManager getStorage() {
        return this.storage;
    }

    public RenderedBlocks getRenderedBlocks() {
        return new RenderedBlocks(
            pos -> {
                StructureBlockInfo info = blocks.get(pos);
                if (info == null) {
                    return Blocks.AIR.getDefaultState();
                }
                return info.state();
            }, blocks.keySet()
        );
    }

    public Collection<BlockEntity> getRenderedBEs() {
        return renderedBlockEntities;
    }

    public boolean isHiddenInPortal(BlockPos localPos) {
        return false;
    }

    public Optional<List<Box>> getSimplifiedEntityColliders() {
        return simplifiedEntityColliders;
    }

    public void tickStorage(AbstractContraptionEntity entity) {
        getStorage().tick(entity);
    }

    public boolean containsBlockBreakers() {
        for (MutablePair<StructureBlockInfo, MovementContext> pair : actors) {
            MovementBehaviour behaviour = MovementBehaviour.REGISTRY.get(pair.getLeft().state());
            if (behaviour instanceof BlockBreakingMovementBehaviour || behaviour instanceof HarvesterMovementBehaviour)
                return true;
        }
        return false;
    }

    public record RenderedBlocks(Function<BlockPos, BlockState> lookup, Iterable<BlockPos> positions) {
    }

}
