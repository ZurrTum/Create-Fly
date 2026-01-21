package com.zurrtum.create.content.contraptions;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllClientHandle;
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
import com.zurrtum.create.foundation.collision.CollisionList;
import com.zurrtum.create.foundation.collision.CollisionList.Populate;
import com.zurrtum.create.foundation.utility.BlockHelper;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.core.*;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.chunk.HashMapPalette;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static com.zurrtum.create.Create.LOGGER;
import static com.zurrtum.create.content.contraptions.piston.MechanicalPistonBlock.isExtensionPole;
import static com.zurrtum.create.content.contraptions.piston.MechanicalPistonBlock.isPistonHead;

public abstract class Contraption {
    public static final Codec<Map<UUID, Integer>> SEAT_MAP_CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.INT);
    public static final Codec<Map<UUID, BlockFace>> SUB_CONTRAPTIONS_CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, BlockFace.CODEC);

    public final CollisionList simplifiedEntityColliders = new CollisionList();
    public AbstractContraptionEntity entity;

    public @Nullable AABB bounds;
    public BlockPos anchor;
    public boolean stalled;
    public boolean hasUniversalCreativeCrate;
    public boolean disassembled;

    // TODO: SoA to reduce map lookups.
    protected Map<BlockPos, StructureBlockInfo> blocks;
    protected Map<BlockPos, CompoundTag> updateTags;
    public Object2BooleanMap<BlockPos> isLegacy;
    protected List<MutablePair<StructureBlockInfo, MovementContext>> actors;
    protected Map<BlockPos, MovingInteractionBehaviour> interactors;
    protected List<ItemStack> disabledActors;

    protected List<AABB> superglue;
    protected List<BlockPos> seats;
    protected Map<UUID, Integer> seatMapping;
    protected Map<UUID, BlockFace> stabilizedSubContraptions;
    protected MountedStorageManager storage;
    protected Multimap<BlockPos, StructureBlockInfo> capturedMultiblocks;

    private Set<SuperGlueEntity> glueToRemove;
    private Map<BlockPos, Entity> initialPassengers;
    private List<BlockFace> pendingSubContraptions;

    /**
     * All client-only data should be encapsulated here.
     *
     * <p>This field must be atomic as it is lazily accessed from both
     * the render thread and flywheel executors.
     *
     * <h2>Client/Server Safety</h2>
     * <p>Wrapping in an AtomicReference also makes this field server-safe,
     * as type erasure means ClientContraption will not be class loaded when
     * Contraption is class loaded.
     * Even still, care must be taken to not call getOrCreateClientContraptionLazy()
     * from the server. The only references to that method should be in rendering code.
     * Additional utilities are provided to safely access and send signals to the ClientContraption,
     * without initializing it.
     */
    public final AtomicReference<?> clientContraption = new AtomicReference<>();
    // Thin server and client side level used for generating optimized collision shapes.
    protected @Nullable ContraptionWorld collisionLevel;

    public Contraption() {
        blocks = new HashMap<>();
        updateTags = new HashMap<>();
        isLegacy = new Object2BooleanArrayMap<>();
        seats = new ArrayList<>();
        actors = new ArrayList<>();
        disabledActors = new ArrayList<>();
        interactors = new HashMap<>();
        superglue = new ArrayList<>();
        seatMapping = new HashMap<>();
        glueToRemove = new HashSet<>();
        initialPassengers = new HashMap<>();
        pendingSubContraptions = new ArrayList<>();
        stabilizedSubContraptions = new HashMap<>();
        storage = new MountedStorageManager();
        capturedMultiblocks = ArrayListMultimap.create();
    }

    public ContraptionWorld getContraptionWorld() {
        if (collisionLevel == null)
            collisionLevel = new ContraptionWorld(entity.level(), this);
        return collisionLevel;
    }

    public abstract boolean assemble(Level world, BlockPos pos) throws AssemblyException;

    public abstract boolean canBeStabilized(Direction facing, BlockPos localPos);

    public abstract ContraptionType getType();

    protected boolean customBlockPlacement(LevelAccessor world, BlockPos pos, BlockState state) {
        return false;
    }

    protected boolean customBlockRemoval(LevelAccessor world, BlockPos pos, BlockState state) {
        return false;
    }

    protected boolean addToInitialFrontier(
        Level world,
        BlockPos pos,
        @Nullable Direction forcedDirection,
        Queue<BlockPos> frontier
    ) throws AssemblyException {
        return true;
    }

    public static Contraption fromData(Level world, ValueInput view, boolean spawnData) {
        ContraptionType type = view.read("Type", ContraptionType.CODEC).orElseThrow();
        Contraption contraption = type.factory.get();
        contraption.read(world, view, spawnData);
        contraption.collisionLevel = new ContraptionWorld(world, contraption);
        contraption.invalidateColliders();
        return contraption;
    }

    public boolean searchMovedStructure(Level world, BlockPos pos, @Nullable Direction forcedDirection) throws AssemblyException {
        initialPassengers.clear();
        Queue<BlockPos> frontier = new UniqueLinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        anchor = pos;

        if (bounds == null)
            bounds = new AABB(BlockPos.ZERO);

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
            Level world = entity.level();
            BlockPos pos = blockFace.getPos();
            try {
                if (!subContraption.assemble(world, pos))
                    continue;
            } catch (AssemblyException e) {
                continue;
            }
            subContraption.removeBlocksFromWorld(world, BlockPos.ZERO);
            OrientedContraptionEntity movedContraption = OrientedContraptionEntity.create(world, subContraption, face);
            BlockPos anchor = blockFace.getConnectedPos();
            movedContraption.setPos(anchor.getX() + .5f, anchor.getY(), anchor.getZ() + .5f);
            world.addFreshEntity(movedContraption);
            stabilizedSubContraptions.put(movedContraption.getUUID(), new BlockFace(toLocalPos(pos), face));
        }

        storage.initialize();
        invalidateColliders();
    }

    public void onEntityInitialize(Level world, AbstractContraptionEntity contraptionEntity) {
        if (world.isClientSide())
            return;

        for (OrientedContraptionEntity orientedCE : world.getEntitiesOfClass(
            OrientedContraptionEntity.class,
            contraptionEntity.getBoundingBox().inflate(1)
        ))
            if (stabilizedSubContraptions.containsKey(orientedCE.getUUID()))
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
        Level world,
        @Nullable Direction forcedDirection,
        Queue<BlockPos> frontier,
        Set<BlockPos> visited
    ) throws AssemblyException {
        BlockPos pos = frontier.poll();
        if (pos == null)
            return false;
        visited.add(pos);

        if (world.isOutsideBuildHeight(pos))
            return true;
        if (!world.isLoaded(pos))
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

        if (state.is(AllBlocks.BELT))
            moveBelt(pos, frontier, visited, state);

        if (state.is(AllBlocks.WINDMILL_BEARING) && world.getBlockEntity(pos) instanceof WindmillBearingBlockEntity wbbe)
            wbbe.disassembleForMovement();

        if (state.is(AllBlocks.GANTRY_CARRIAGE))
            moveGantryPinion(world, pos, frontier, visited, state);

        if (state.is(AllBlocks.GANTRY_SHAFT))
            moveGantryShaft(world, pos, frontier, visited, state);

        if (state.is(AllBlocks.STICKER) && state.getValue(StickerBlock.EXTENDED)) {
            Direction offset = state.getValue(StickerBlock.FACING);
            BlockPos attached = pos.relative(offset);
            if (!visited.contains(attached) && !BlockMovementChecks.isNotSupportive(world.getBlockState(attached), offset.getOpposite()))
                frontier.add(attached);
        }

        if (world.getBlockEntity(pos) instanceof ChainConveyorBlockEntity ccbe)
            ccbe.notifyConnectedToValidate();

        // Double Chest halves stick together
        if (state.hasProperty(ChestBlock.TYPE) && state.hasProperty(ChestBlock.FACING) && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            Direction offset = ChestBlock.getConnectedDirection(state);
            BlockPos attached = pos.relative(offset);
            if (!visited.contains(attached))
                frontier.add(attached);
        }

        // Bogeys tend to have sticky sides
        if (state.getBlock() instanceof AbstractBogeyBlock<?> bogey)
            for (Direction d : bogey.getStickySurfaces(world, pos, state))
                if (!visited.contains(pos.relative(d)))
                    frontier.add(pos.relative(d));

        // Bearings potentially create stabilized sub-contraptions
        if (state.is(AllBlocks.MECHANICAL_BEARING))
            moveBearing(pos, frontier, visited, state);

        // WM Bearings attach their structure when moved
        if (state.is(AllBlocks.WINDMILL_BEARING))
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
        BlockPos posDown = pos.below();
        BlockState stateBelow = world.getBlockState(posDown);
        if (!visited.contains(posDown) && stateBelow.is(AllBlocks.CART_ASSEMBLER))
            frontier.add(posDown);

        // Slime blocks and super glue drag adjacent blocks if possible
        for (Direction offset : Iterate.directions) {
            BlockPos offsetPos = pos.relative(offset);
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
                if (state.getPistonPushReaction() == PushReaction.PUSH_ONLY || blockState.getPistonPushReaction() == PushReaction.PUSH_ONLY) {
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

    protected void movePistonHead(Level world, BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited, BlockState state) {
        Direction direction = state.getValue(MechanicalPistonHeadBlock.FACING);
        BlockPos offset = pos.relative(direction.getOpposite());
        if (!visited.contains(offset)) {
            BlockState blockState = world.getBlockState(offset);
            if (isExtensionPole(blockState) && blockState.getValue(PistonExtensionPoleBlock.FACING).getAxis() == direction.getAxis())
                frontier.add(offset);
            if (blockState.getBlock() instanceof MechanicalPistonBlock) {
                Direction pistonFacing = blockState.getValue(MechanicalPistonBlock.FACING);
                if (pistonFacing == direction && blockState.getValue(MechanicalPistonBlock.STATE) == PistonState.EXTENDED)
                    frontier.add(offset);
            }
        }
        if (state.getValue(MechanicalPistonHeadBlock.TYPE) == PistonType.STICKY) {
            BlockPos attached = pos.relative(direction);
            if (!visited.contains(attached))
                frontier.add(attached);
        }
    }

    protected void movePistonPole(Level world, BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited, BlockState state) {
        for (Direction d : Iterate.directionsInAxis(state.getValue(PistonExtensionPoleBlock.FACING).getAxis())) {
            BlockPos offset = pos.relative(d);
            if (!visited.contains(offset)) {
                BlockState blockState = world.getBlockState(offset);
                if (isExtensionPole(blockState) && blockState.getValue(PistonExtensionPoleBlock.FACING).getAxis() == d.getAxis())
                    frontier.add(offset);
                if (isPistonHead(blockState) && blockState.getValue(MechanicalPistonHeadBlock.FACING).getAxis() == d.getAxis())
                    frontier.add(offset);
                if (blockState.getBlock() instanceof MechanicalPistonBlock) {
                    Direction pistonFacing = blockState.getValue(MechanicalPistonBlock.FACING);
                    if (pistonFacing == d || pistonFacing == d.getOpposite() && blockState.getValue(MechanicalPistonBlock.STATE) == PistonState.EXTENDED)
                        frontier.add(offset);
                }
            }
        }
    }

    protected void moveGantryPinion(Level world, BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited, BlockState state) {
        BlockPos offset = pos.relative(state.getValue(GantryCarriageBlock.FACING));
        if (!visited.contains(offset))
            frontier.add(offset);
        Axis rotationAxis = ((IRotate) state.getBlock()).getRotationAxis(state);
        for (Direction d : Iterate.directionsInAxis(rotationAxis)) {
            offset = pos.relative(d);
            BlockState offsetState = world.getBlockState(offset);
            if (offsetState.is(AllBlocks.GANTRY_SHAFT) && offsetState.getValue(GantryShaftBlock.FACING).getAxis() == d.getAxis())
                if (!visited.contains(offset))
                    frontier.add(offset);
        }
    }

    protected void moveGantryShaft(Level world, BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited, BlockState state) {
        for (Direction d : Iterate.directions) {
            BlockPos offset = pos.relative(d);
            if (!visited.contains(offset)) {
                BlockState offsetState = world.getBlockState(offset);
                Direction facing = state.getValue(GantryShaftBlock.FACING);
                if (d.getAxis() == facing.getAxis() && offsetState.is(AllBlocks.GANTRY_SHAFT) && offsetState.getValue(GantryShaftBlock.FACING) == facing)
                    frontier.add(offset);
                else if (offsetState.is(AllBlocks.GANTRY_CARRIAGE) && offsetState.getValue(GantryCarriageBlock.FACING) == d)
                    frontier.add(offset);
            }
        }
    }

    private void moveWindmillBearing(BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited, BlockState state) {
        Direction facing = state.getValue(WindmillBearingBlock.FACING);
        BlockPos offset = pos.relative(facing);
        if (!visited.contains(offset))
            frontier.add(offset);
    }

    private void moveBearing(BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited, BlockState state) {
        Direction facing = state.getValue(MechanicalBearingBlock.FACING);
        if (!canBeStabilized(facing, pos.subtract(anchor))) {
            BlockPos offset = pos.relative(facing);
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

    private void moveSeat(Level world, BlockPos pos) {
        BlockPos local = toLocalPos(pos);
        getSeats().add(local);
        List<SeatEntity> seatsEntities = world.getEntitiesOfClass(SeatEntity.class, new AABB(pos));
        if (!seatsEntities.isEmpty()) {
            SeatEntity seat = seatsEntities.getFirst();
            List<Entity> passengers = seat.getPassengers();
            if (!passengers.isEmpty())
                initialPassengers.put(local, passengers.getFirst());
        }
    }

    private void movePulley(Level world, BlockPos pos, Queue<BlockPos> frontier, Set<BlockPos> visited) {
        int limit = AllConfigs.server().kinetics.maxRopeLength.get();
        BlockPos ropePos = pos;
        while (limit-- >= 0) {
            ropePos = ropePos.below();
            if (!world.isLoaded(ropePos))
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
        Level world,
        BlockPos pos,
        Queue<BlockPos> frontier,
        Set<BlockPos> visited,
        BlockState state
    ) throws AssemblyException {
        Direction direction = state.getValue(MechanicalPistonBlock.FACING);
        PistonState pistonState = state.getValue(MechanicalPistonBlock.STATE);
        if (pistonState == PistonState.MOVING)
            return false;

        BlockPos offset = pos.relative(direction.getOpposite());
        if (!visited.contains(offset)) {
            BlockState poleState = world.getBlockState(offset);
            if (poleState.is(AllBlocks.PISTON_EXTENSION_POLE) && poleState.getValue(PistonExtensionPoleBlock.FACING).getAxis() == direction.getAxis())
                frontier.add(offset);
        }

        if (pistonState == PistonState.EXTENDED || MechanicalPistonBlock.isStickyPiston(state)) {
            offset = pos.relative(direction);
            if (!visited.contains(offset))
                frontier.add(offset);
        }

        return true;
    }

    private boolean moveChassis(Level world, BlockPos pos, @Nullable Direction movementDirection, Queue<BlockPos> frontier, Set<BlockPos> visited) {
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

    protected Pair<StructureBlockInfo, BlockEntity> capture(Level world, BlockPos pos) {
        BlockState blockstate = world.getBlockState(pos);
        if (blockstate.is(AllBlocks.REDSTONE_CONTACT))
            blockstate = blockstate.setValue(RedstoneContactBlock.POWERED, true);
        if (blockstate.is(AllBlocks.POWERED_SHAFT))
            blockstate = BlockHelper.copyProperties(blockstate, AllBlocks.SHAFT.defaultBlockState());
        if (blockstate.getBlock() instanceof ControlsBlock && getType().is(AllContraptionTypeTags.OPENS_CONTROLS))
            blockstate = blockstate.setValue(ControlsBlock.OPEN, true);
        if (blockstate.hasProperty(SlidingDoorBlock.VISIBLE))
            blockstate = blockstate.setValue(SlidingDoorBlock.VISIBLE, false);
        if (blockstate.getBlock() instanceof ButtonBlock) {
            blockstate = blockstate.setValue(ButtonBlock.POWERED, false);
            world.scheduleTick(pos, blockstate.getBlock(), -1);
        }
        if (blockstate.getBlock() instanceof PressurePlateBlock) {
            blockstate = blockstate.setValue(PressurePlateBlock.POWERED, false);
            world.scheduleTick(pos, blockstate.getBlock(), -1);
        }
        CompoundTag compoundnbt = getBlockEntityNBT(world, pos);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof PoweredShaftBlockEntity)
            blockEntity = new BracketedKineticBlockEntity(pos, blockstate);
        if (blockEntity instanceof FactoryPanelBlockEntity fpbe) {
            try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(blockEntity.problemPath(), LOGGER)) {
                fpbe.writeSafe(new TagValueOutput(logging, world.registryAccess().createSerializationContext(NbtOps.INSTANCE), compoundnbt));
            }
        }

        return Pair.of(new StructureBlockInfo(pos, blockstate, compoundnbt), blockEntity);
    }

    protected void addBlock(Level level, BlockPos pos, Pair<StructureBlockInfo, @Nullable BlockEntity> pair) {
        StructureBlockInfo captured = pair.getKey();
        BlockPos localPos = pos.subtract(anchor);
        BlockState state = captured.state();
        StructureBlockInfo structureBlockInfo = new StructureBlockInfo(localPos, state, captured.nbt());

        if (blocks.put(localPos, structureBlockInfo) != null)
            return;
        bounds = bounds.minmax(new AABB(localPos));

        BlockEntity be = pair.getValue();

        if (be != null) {
            CompoundTag updateTag = be.getUpdateTag(level.registryAccess());
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

        CompoundTag nbt = structureBlockInfo.nbt();
        BlockPos controllerPos = nbt.read("Controller", BlockPos.CODEC).map(this::toLocalPos).orElse(localPos);
        nbt.store("Controller", BlockPos.CODEC, controllerPos);

        if (updateTags.containsKey(localPos))
            updateTags.get(localPos).store("Controller", BlockPos.CODEC, controllerPos);

        if (multiBlockBE.isController() && multiBlockBE.getHeight() <= 1 && multiBlockBE.getWidth() <= 1) {
            nbt.store("LastKnownPos", BlockPos.CODEC, BlockPos.ZERO.below(Integer.MAX_VALUE - 1));
            return;
        }

        nbt.remove("LastKnownPos");
        capturedMultiblocks.put(controllerPos, structureBlockInfo);
    }

    @Nullable
    protected CompoundTag getBlockEntityNBT(Level world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity == null)
            return null;
        CompoundTag nbt = blockEntity.saveWithFullMetadata(world.registryAccess());
        nbt.remove("x");
        nbt.remove("y");
        nbt.remove("z");

        return nbt;
    }

    protected BlockPos toLocalPos(BlockPos globalPos) {
        return globalPos.subtract(anchor);
    }

    protected boolean movementAllowed(BlockState state, Level world, BlockPos pos) {
        return BlockMovementChecks.isMovementAllowed(state, world, pos);
    }

    protected boolean isAnchoringBlockAt(BlockPos pos) {
        return pos.equals(anchor);
    }

    public void read(Level world, ValueInput view, boolean spawnData) {
        readBlocksCompound(view.childOrEmpty("Blocks"), world);

        capturedMultiblocks.clear();
        view.childrenListOrEmpty("CapturedMultiblocks").forEach(c -> {
            BlockPos controllerPos = c.read("Controller", BlockPos.CODEC).orElseThrow();
            c.read("Parts", CreateCodecs.BLOCK_POS_LIST_CODEC).orElseThrow().forEach(pos -> capturedMultiblocks.put(controllerPos, blocks.get(pos)));
        });

        storage.read(view, spawnData, this);

        actors.clear();
        view.childrenListOrEmpty("Actors").forEach(c -> {
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
        stalled = view.getBooleanOr("Stalled", false);
        hasUniversalCreativeCrate = view.getBooleanOr("BottomlessSupply", false);
        anchor = view.read("Anchor", BlockPos.CODEC).orElseThrow();
    }

    public void write(ValueOutput view, boolean spawnPacket) {
        view.store("Type", CreateRegistries.CONTRAPTION_TYPE.byNameCodec(), getType());

        writeBlocksCompound(view.child("Blocks"), spawnPacket);

        ValueOutput.ValueOutputList multiblocks = view.childrenList("CapturedMultiblocks");
        capturedMultiblocks.keySet().forEach(controllerPos -> {
            ValueOutput block = multiblocks.addChild();
            block.store("Controller", BlockPos.CODEC, controllerPos);

            Collection<StructureBlockInfo> multiblockParts = capturedMultiblocks.get(controllerPos);
            List<BlockPos> list = multiblockParts.stream().map(StructureBlockInfo::pos).toList();
            block.store("Parts", CreateCodecs.BLOCK_POS_LIST_CODEC, list);
        });

        ValueOutput.ValueOutputList actors = view.childrenList("Actors");
        for (MutablePair<StructureBlockInfo, MovementContext> actor : getActors()) {
            MovementBehaviour behaviour = MovementBehaviour.REGISTRY.get(actor.left.state());
            if (behaviour == null)
                continue;
            ValueOutput item = actors.addChild();
            item.store("Pos", BlockPos.CODEC, actor.left.pos());
            behaviour.writeExtraData(actor.right);
            actor.right.write(item);
        }

        view.store("DisabledActors", CreateCodecs.ITEM_LIST_CODEC, disabledActors);
        if (!spawnPacket) {
            view.store("Superglue", CreateCodecs.BOX_CODEC.listOf(), superglue);
        }

        writeStorage(view, spawnPacket);

        view.store("Interactors", CreateCodecs.BLOCK_POS_LIST_CODEC, interactors.keySet().stream().toList());
        view.store("Seats", CreateCodecs.BLOCK_POS_LIST_CODEC, seats);
        view.store("Passengers", SEAT_MAP_CODEC, seatMapping);
        view.store("SubContraptions", SUB_CONTRAPTIONS_CODEC, stabilizedSubContraptions);
        view.store("Anchor", BlockPos.CODEC, anchor);
        view.putBoolean("Stalled", stalled);
        view.putBoolean("BottomlessSupply", hasUniversalCreativeCrate);

        if (bounds != null) {
            view.store("BoundsFront", CreateCodecs.BOX_CODEC, bounds);
        }
    }

    public void writeStorage(ValueOutput view, boolean spawnPacket) {
        storage.write(view, spawnPacket);
    }

    private void writeBlocksCompound(ValueOutput view, boolean spawnPacket) {
        HashMapPalette<BlockState> palette = new HashMapPalette<>(16);
        ValueOutput.ValueOutputList blockList = view.childrenList("BlockList");

        boolean isClient = spawnPacket && entity.level().isClientSide();
        for (StructureBlockInfo block : this.blocks.values()) {
            int id = palette.idFor(
                block.state(), (i, s) -> {
                    throw new IllegalStateException("Palette Map index exceeded maximum");
                }
            );
            BlockPos pos = block.pos();
            ValueOutput c = blockList.addChild();
            c.putLong("Pos", pos.asLong());
            c.putInt("State", id);

            CompoundTag updateTag = updateTags.get(pos);
            if (spawnPacket) {
                // for client sync, treat the updateTag as the data
                if (updateTag != null) {
                    c.store("Data", CompoundTag.CODEC, updateTag);
                } else if (block.nbt() != null) {
                    if (isClient) {
                        c.store("UpdateTag", CompoundTag.CODEC, block.nbt());
                    } else {
                        // an updateTag is saved for all BlockEntities, even when empty.
                        // this case means that the contraption was assembled pre-updateTags.
                        // in this case, we need to use the full BlockEntity data.
                        c.store("Data", CompoundTag.CODEC, block.nbt());
                        c.putBoolean("Legacy", true);
                    }
                }
            } else {
                // otherwise, write actual data as the data, save updateTag on its own
                if (block.nbt() != null) {
                    c.store("Data", CompoundTag.CODEC, block.nbt());
                }
                if (updateTag != null) {
                    c.store("UpdateTag", CompoundTag.CODEC, updateTag);
                }
            }
        }

        int size = palette.getSize();
        List<BlockState> paletteData = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            paletteData.add(palette.valueFor(i));
        }
        view.store("Palette", CreateCodecs.BLOCK_STATE_LIST_CODEC, paletteData);
    }

    private void readBlocksCompound(ValueInput view, Level world) {
        blocks.clear();
        updateTags.clear();
        isLegacy.clear();

        HashMapPalette<BlockState> palette = new HashMapPalette<>(
            16,
            view.read("Palette", CreateCodecs.BLOCK_STATE_LIST_CODEC).orElseGet(ArrayList::new)
        );

        boolean isServer = !world.isClientSide();
        view.childrenListOrEmpty("BlockList").forEach(c -> {
            StructureBlockInfo info = readStructureBlockInfo(c, palette);

            blocks.put(info.pos(), info);
            boolean legacy = c.getBooleanOr("Legacy", false);

            // it's very important that empty tags are read here. see writeBlocksCompound
            c.read("UpdateTag", CompoundTag.CODEC).ifPresentOrElse(
                updateTag -> updateTags.put(info.pos(), updateTag), () -> {
                    if (isServer && !legacy) {
                        CompoundTag updateTag = info.nbt();
                        if (updateTag != null) {
                            updateTags.put(info.pos(), info.nbt());
                        }
                    }
                }
            );

            // Mark the pos if it has the legacy marker.
            // This will be used when creating BlockEntities for the ClientContraption.
            this.isLegacy.put(info.pos(), legacy);
        });
        AllClientHandle.INSTANCE.resetClientContraption(this);
    }

    private static StructureBlockInfo readStructureBlockInfo(ValueInput view, HashMapPalette<BlockState> palette) {
        return new StructureBlockInfo(
            BlockPos.of(view.getLongOr("Pos", 0)),
            Objects.requireNonNull(palette.valueFor(view.getIntOr("State", 0))),
            view.read("Data", CompoundTag.CODEC).orElse(null)
        );
    }

    private static StructureBlockInfo legacyReadStructureBlockInfo(CompoundTag blockListEntry, HolderGetter<Block> holderGetter) {
        return new StructureBlockInfo(
            NBTHelper.readBlockPos(blockListEntry, "Pos"),
            NbtUtils.readBlockState(holderGetter, blockListEntry.getCompoundOrEmpty("Block")),
            blockListEntry.contains("Data") ? blockListEntry.getCompoundOrEmpty("Data") : null
        );
    }

    public void removeBlocksFromWorld(Level world, BlockPos offset) {
        glueToRemove.forEach(glue -> {
            superglue.add(glue.getBoundingBox().move(Vec3.atLowerCornerOf(offset.offset(anchor)).scale(-1)));
            glue.discard();
        });

        List<@Nullable BoundingBox> minimisedGlue = new ArrayList<>();
        for (int i = 0; i < superglue.size(); i++)
            minimisedGlue.add(null);

        for (boolean brittles : Iterate.trueAndFalse) {
            for (Iterator<StructureBlockInfo> iterator = blocks.values().iterator(); iterator.hasNext(); ) {
                StructureBlockInfo block = iterator.next();
                if (brittles != BlockMovementChecks.isBrittle(block.state()))
                    continue;

                for (int i = 0; i < superglue.size(); i++) {
                    AABB aabb = superglue.get(i);
                    if (aabb == null || !aabb.contains(block.pos().getX() + .5, block.pos().getY() + .5, block.pos().getZ() + .5))
                        continue;
                    if (minimisedGlue.get(i) == null)
                        minimisedGlue.set(i, new BoundingBox(block.pos()));
                    else
                        minimisedGlue.set(i, BBHelper.encapsulate(minimisedGlue.get(i), block.pos()));
                }

                BlockPos add = block.pos().offset(anchor).offset(offset);
                if (customBlockRemoval(world, add, block.state()))
                    continue;
                BlockState oldState = world.getBlockState(add);
                Block blockIn = oldState.getBlock();
                boolean blockMismatch = block.state().getBlock() != blockIn;
                blockMismatch &= AllBlocks.POWERED_SHAFT != blockIn || !block.state().is(AllBlocks.SHAFT);
                if (blockMismatch)
                    iterator.remove();
                world.removeBlockEntity(add);
                int flags = Block.UPDATE_MOVE_BY_PISTON | Block.UPDATE_SUPPRESS_DROPS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE;
                if (blockIn instanceof SimpleWaterloggedBlock && oldState.hasProperty(BlockStateProperties.WATERLOGGED) && oldState.getValue(
                    BlockStateProperties.WATERLOGGED)) {
                    world.setBlock(add, Blocks.WATER.defaultBlockState(), flags);
                    continue;
                }
                world.setBlock(add, Blocks.AIR.defaultBlockState(), flags);
            }
        }

        superglue.clear();
        for (BoundingBox box : minimisedGlue) {
            if (box == null)
                continue;
            AABB bb = new AABB(box.minX(), box.minY(), box.minZ(), box.maxX() + 1, box.maxY() + 1, box.maxZ() + 1);
            if (bb.getSize() > 1.01)
                superglue.add(bb);
        }

        for (StructureBlockInfo block : blocks.values()) {
            BlockPos add = block.pos().offset(anchor).offset(offset);
            //			if (!shouldUpdateAfterMovement(block))
            //				continue;

            int flags = Block.UPDATE_MOVE_BY_PISTON | Block.UPDATE_ALL;
            world.sendBlockUpdated(add, block.state(), Blocks.AIR.defaultBlockState(), flags);

            // when the blockstate is set to air, the block's POI data is removed, but
            // markAndNotifyBlock tries to
            // remove it again, so to prevent an error from being logged by double-removal
            // we add the POI data back now
            // (code copied from ServerWorld.onBlockStateChange)
            ServerLevel serverWorld = (ServerLevel) world;
            PoiTypes.forState(block.state()).ifPresent(poiType -> {
                world.getServer().execute(() -> {
                    serverWorld.getPoiManager().add(add, poiType);
                });
            });

            BlockHelper.markAndNotifyBlock(world, add, world.getChunkAt(add), block.state(), Blocks.AIR.defaultBlockState(), flags);
            block.state().updateIndirectNeighbourShapes(world, add, flags & -2);
        }
    }

    public void addBlocksToWorld(Level world, StructureTransform transform) {
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
                        state = state.updateShape(
                            world,
                            world,
                            targetPos,
                            face,
                            targetPos.relative(face),
                            world.getBlockState(targetPos.relative(face)),
                            world.getRandom()
                        );

                BlockState blockState = world.getBlockState(targetPos);
                if (blockState.getDestroySpeed(world, targetPos) == -1 || (state.getCollisionShape(world, targetPos)
                    .isEmpty() && !blockState.getCollisionShape(world, targetPos).isEmpty())) {
                    if (targetPos.getY() == world.getMinY())
                        targetPos = targetPos.above();
                    world.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, targetPos, Block.getId(state));
                    if (shouldDropBlocks) {
                        Block.dropResources(state, world, targetPos, null);
                    }
                    continue;
                }
                if (state.getBlock() instanceof SimpleWaterloggedBlock && state.hasProperty(BlockStateProperties.WATERLOGGED)) {
                    FluidState FluidState = world.getFluidState(targetPos);
                    state = state.setValue(BlockStateProperties.WATERLOGGED, FluidState.getType() == Fluids.WATER);
                }

                world.destroyBlock(targetPos, shouldDropBlocks);

                if (state.is(AllBlocks.SHAFT))
                    state = ShaftBlock.pickCorrectShaftType(state, world, targetPos);
                if (state.hasProperty(SlidingDoorBlock.VISIBLE))
                    state = state.setValue(SlidingDoorBlock.VISIBLE, !state.getValue(SlidingDoorBlock.OPEN))
                        .setValue(SlidingDoorBlock.POWERED, false);
                // Stop Sculk shriekers from getting "stuck" if moved mid-shriek.
                if (state.is(Blocks.SCULK_SHRIEKER)) {
                    state = Blocks.SCULK_SHRIEKER.defaultBlockState();
                }

                world.setBlock(targetPos, state, Block.UPDATE_MOVE_BY_PISTON | Block.UPDATE_ALL);

                boolean verticalRotation = transform.rotationAxis == null || transform.rotationAxis.isHorizontal();
                verticalRotation = verticalRotation && transform.rotation != Rotation.NONE;
                if (verticalRotation) {
                    if (state.getBlock() instanceof RopeBlock || state.getBlock() instanceof MagnetBlock || state.getBlock() instanceof DoorBlock)
                        world.destroyBlock(targetPos, shouldDropBlocks);
                }

                BlockEntity blockEntity = world.getBlockEntity(targetPos);

                CompoundTag tag = block.nbt();

                // Temporary fix: Calling load(CompoundTag tag) on a Sculk sensor causes it to not react to vibrations.
                if (state.is(Blocks.SCULK_SENSOR) || state.is(Blocks.SCULK_SHRIEKER))
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
                            tag.store("LastKnownPos", BlockPos.CODEC, BlockPos.ZERO.below(Integer.MAX_VALUE - 1));
                            tag.remove("Controller");
                        }
                    }

                    try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(blockEntity.problemPath(), LOGGER)) {
                        blockEntity.loadWithComponents(TagValueInput.create(logging, world.registryAccess(), tag));
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
                world.getChunkAt(targetPos),
                block.state(),
                block.state(),
                Block.UPDATE_MOVE_BY_PISTON | Block.UPDATE_ALL
            );
        }

        for (AABB box : superglue) {
            box = new AABB(transform.apply(new Vec3(box.minX, box.minY, box.minZ)), transform.apply(new Vec3(box.maxX, box.maxY, box.maxZ)));
            if (!world.isClientSide())
                world.addFreshEntity(new SuperGlueEntity(world, box));
        }
    }

    protected void translateMultiblockControllers(StructureTransform transform) {
        if (transform.rotationAxis != null && transform.rotationAxis != Axis.Y && transform.rotation != Rotation.NONE) {
            capturedMultiblocks.values().forEach(info -> {
                info.nbt().store("LastKnownPos", BlockPos.CODEC, BlockPos.ZERO.below(Integer.MAX_VALUE - 1));
            });
            return;
        }

        capturedMultiblocks.keySet().forEach(controllerPos -> {
            Collection<StructureBlockInfo> multiblockParts = capturedMultiblocks.get(controllerPos);
            Optional<BoundingBox> optionalBoundingBox = BoundingBox.encapsulatingPositions(multiblockParts.stream()
                .map(info -> transform.apply(info.pos())).toList());
            if (optionalBoundingBox.isEmpty())
                return;

            BoundingBox boundingBox = optionalBoundingBox.get();
            BlockPos newControllerPos = new BlockPos(boundingBox.minX(), boundingBox.minY(), boundingBox.minZ());
            BlockPos otherPos = transform.unapply(newControllerPos);

            multiblockParts.forEach(info -> info.nbt().store("Controller", BlockPos.CODEC, newControllerPos));

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

    public void addPassengersToWorld(Level world, StructureTransform transform, List<Entity> seatedEntities) {
        for (Entity seatedEntity : seatedEntities) {
            if (getSeatMapping().isEmpty())
                continue;
            Integer seatIndex = getSeatMapping().get(seatedEntity.getUUID());
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

    public void startMoving(Level world) {
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

    public void stop(Level world) {
        forEachActor(
            world, (behaviour, ctx) -> {
                behaviour.stopMoving(ctx);
                ctx.position = null;
                ctx.motion = Vec3.ZERO;
                ctx.relativeMotion = Vec3.ZERO;
                ctx.rotation = v -> v;
            }
        );
    }

    public void forEachActor(Level world, BiConsumer<MovementBehaviour, MovementContext> callBack) {
        for (MutablePair<StructureBlockInfo, MovementContext> pair : actors) {
            MovementBehaviour behaviour = MovementBehaviour.REGISTRY.get(pair.getLeft().state());
            if (behaviour == null)
                continue;
            callBack.accept(behaviour, pair.getRight());
        }
    }

    protected boolean shouldUpdateAfterMovement(StructureBlockInfo info) {
        if (PoiTypes.forState(info.state()).isPresent())
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

        bounds = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public Map<UUID, Integer> getSeatMapping() {
        return seatMapping;
    }

    @Nullable
    public BlockPos getSeatOf(UUID entityId) {
        if (!getSeatMapping().containsKey(entityId))
            return null;
        int seatIndex = getSeatMapping().get(entityId);
        if (seatIndex >= getSeats().size())
            return null;
        return getSeats().get(seatIndex);
    }

    @Nullable
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

    public Object2BooleanMap<BlockPos> getIsLegacy() {
        return isLegacy;
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
        getContraptionWorld();
        simplifiedEntityColliders.size = 0;

        var populate = new Populate(simplifiedEntityColliders);

        for (Map.Entry<BlockPos, StructureBlockInfo> entry : blocks.entrySet()) {
            StructureBlockInfo info = entry.getValue();
            BlockPos localPos = entry.getKey();
            VoxelShape collisionShape = info.state().getCollisionShape(collisionLevel, localPos, CollisionContext.empty());
            if (collisionShape.isEmpty())
                continue;

            populate.offsetX = localPos.getX();
            populate.offsetY = localPos.getY();
            populate.offsetZ = localPos.getZ();
            collisionShape.forAllBoxes(populate);
        }
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
            int a = vec.get(axisA);
            int b = vec.get(axisB);

            int distSq = a * a + b * b;

            if (distSq > maxDistSq)
                maxDistSq = distSq;
        }

        return Math.sqrt(maxDistSq);
    }

    public MountedStorageManager getStorage() {
        return this.storage;
    }

    public boolean isHiddenInPortal(BlockPos localPos) {
        return false;
    }

    @Nullable
    public CollisionList getSimplifiedEntityColliders() {
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
}
