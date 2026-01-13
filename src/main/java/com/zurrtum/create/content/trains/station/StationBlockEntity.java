package com.zurrtum.create.content.trains.station;

import com.zurrtum.create.*;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.api.contraption.transformable.TransformableBlockEntity;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.WorldAttached;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.compat.computercraft.AbstractComputerBehaviour;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.decoration.slidingDoor.DoorControlBehaviour;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.logistics.depot.DepotBehaviour;
import com.zurrtum.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.zurrtum.create.content.logistics.packagePort.postbox.PostboxBlockEntity;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkBlock;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlock;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlockEntity;
import com.zurrtum.create.content.trains.entity.*;
import com.zurrtum.create.content.trains.graph.*;
import com.zurrtum.create.content.trains.graph.TrackNodeLocation.DiscoveredLocation;
import com.zurrtum.create.content.trains.schedule.Schedule;
import com.zurrtum.create.content.trains.schedule.ScheduleItem;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.packet.s2c.AddTrainPacket;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;

public class StationBlockEntity extends SmartBlockEntity implements TransformableBlockEntity {

    public TrackTargetingBehaviour<GlobalStation> edgePoint;
    public DoorControlBehaviour doorControls;
    public LerpedFloat flag;

    public int failedCarriageIndex;
    public AssemblyException lastException;
    public DepotBehaviour depotBehaviour;

    // for display
    public UUID imminentTrain;
    public boolean trainPresent;
    public boolean trainBackwards;
    public boolean trainCanDisassemble;
    public boolean trainHasSchedule;
    public boolean trainHasAutoSchedule;

    public int flagYRot = -1;
    public boolean flagFlipped;

    public Text lastDisassembledTrainName;
    public int lastDisassembledMapColorIndex;

    public StationBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.TRACK_STATION, pos, state);
        setLazyTickRate(20);
        lastException = null;
        failedCarriageIndex = -1;
        flag = LerpedFloat.linear().startWithValue(0);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(edgePoint = new TrackTargetingBehaviour<>(this, EdgePointType.STATION));
        behaviours.add(doorControls = new DoorControlBehaviour(this));
        behaviours.add(depotBehaviour = new DepotBehaviour(this).onlyAccepts(stack -> stack.isOf(AllItems.SCHEDULE))
            .withCallback(s -> applyAutoSchedule()));
        depotBehaviour.addSubBehaviours(behaviours);
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.CONTRAPTION_ACTORS, AllAdvancements.TRAIN, AllAdvancements.LONG_TRAIN, AllAdvancements.CONDUCTOR);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        lastException = AssemblyException.read(view);
        failedCarriageIndex = view.getInt("FailedCarriageIndex", 0);
        super.read(view, clientPacket);
        invalidateRenderBoundingBox();

        trainPresent = view.getBoolean("ForceFlag", false);
        lastDisassembledTrainName = view.read("PrevTrainName", TextCodecs.CODEC).orElse(null);
        lastDisassembledMapColorIndex = view.getInt("PrevTrainColor", 0);

        if (!clientPacket)
            return;
        view.read("ImminentTrain", Uuids.INT_STREAM_CODEC).ifPresentOrElse(
            uuid -> {
                imminentTrain = uuid;
                trainPresent = view.getBoolean("TrainPresent", false);
                trainCanDisassemble = view.getBoolean("TrainCanDisassemble", false);
                trainBackwards = view.getBoolean("TrainBackwards", false);
                trainHasSchedule = view.getBoolean("TrainHasSchedule", false);
                trainHasAutoSchedule = view.getBoolean("TrainHasAutoSchedule", false);
            }, () -> {
                imminentTrain = null;
                trainPresent = false;
                trainCanDisassemble = false;
                trainBackwards = false;
            }
        );
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        AssemblyException.write(view, lastException);
        view.putInt("FailedCarriageIndex", failedCarriageIndex);

        if (lastDisassembledTrainName != null)
            view.put("PrevTrainName", TextCodecs.CODEC, lastDisassembledTrainName);
        view.putInt("PrevTrainColor", lastDisassembledMapColorIndex);

        super.write(view, clientPacket);

        if (!clientPacket)
            return;
        if (imminentTrain == null)
            return;

        view.put("ImminentTrain", Uuids.INT_STREAM_CODEC, imminentTrain);

        if (trainPresent)
            view.putBoolean("TrainPresent", true);
        if (trainCanDisassemble)
            view.putBoolean("TrainCanDisassemble", true);
        if (trainBackwards)
            view.putBoolean("TrainBackwards", true);
        if (trainHasSchedule)
            view.putBoolean("TrainHasSchedule", true);
        if (trainHasAutoSchedule)
            view.putBoolean("TrainHasAutoSchedule", true);
    }

    @Nullable
    public GlobalStation getStation() {
        return edgePoint.getEdgePoint();
    }

    // Train Assembly

    public static WorldAttached<Map<BlockPos, BlockBox>> assemblyAreas = new WorldAttached<>(w -> new HashMap<>());

    public Direction assemblyDirection;
    public int assemblyLength;
    public int[] bogeyLocations;
    AbstractBogeyBlock<?>[] bogeyTypes;
    boolean[] upsideDownBogeys;
    public int bogeyCount;

    @Override
    public void lazyTick() {
        if (isAssembling() && !world.isClient())
            refreshAssemblyInfo();
        super.lazyTick();
    }

    @Override
    public void tick() {
        if (isAssembling() && world.isClient())
            refreshAssemblyInfo();
        super.tick();

        if (world.isClient()) {
            float currentTarget = flag.getChaseTarget();
            if (currentTarget == 0 || flag.settled()) {
                int target = trainPresent || isAssembling() ? 1 : 0;
                if (target != currentTarget) {
                    flag.chase(target, 0.1f, Chaser.LINEAR);
                    if (target == 1)
                        AllSoundEvents.CONTRAPTION_DISASSEMBLE.playAt(world, pos, 1, 2, true);
                }
            }
            boolean settled = flag.getValue() > .15f;
            flag.tickChaser();
            if (currentTarget == 0 && settled != flag.getValue() > .15f)
                AllSoundEvents.CONTRAPTION_ASSEMBLE.playAt(world, pos, 0.75f, 1.5f, true);
            return;
        }

        GlobalStation station = getStation();
        if (station == null)
            return;

        Train imminentTrain = station.getImminentTrain();
        boolean trainPresent = imminentTrain != null && imminentTrain.getCurrentStation() == station;
        boolean canDisassemble = trainPresent && imminentTrain.canDisassemble();
        UUID imminentID = imminentTrain != null ? imminentTrain.id : null;
        boolean trainHasSchedule = trainPresent && imminentTrain.runtime.getSchedule() != null;
        boolean trainHasAutoSchedule = trainHasSchedule && imminentTrain.runtime.isAutoSchedule;
        boolean newlyArrived = this.trainPresent != trainPresent;

        if (trainPresent && imminentTrain.runtime.displayLinkUpdateRequested) {
            DisplayLinkBlock.notifyGatherers(world, pos);
            imminentTrain.runtime.displayLinkUpdateRequested = false;
        }

        if (!world.isClient()) {
            AbstractComputerBehaviour computer = AbstractComputerBehaviour.get(this);
            if (computer != null) {
                computer.queueStationTrain(imminentTrain, newlyArrived, trainPresent);
            }
        }

        if (newlyArrived)
            applyAutoSchedule();

        if (newlyArrived || this.trainCanDisassemble != canDisassemble || !Objects.equals(
            imminentID,
            this.imminentTrain
        ) || this.trainHasSchedule != trainHasSchedule || this.trainHasAutoSchedule != trainHasAutoSchedule) {

            this.imminentTrain = imminentID;
            this.trainPresent = trainPresent;
            this.trainCanDisassemble = canDisassemble;
            this.trainBackwards = imminentTrain != null && imminentTrain.currentlyBackwards;
            this.trainHasSchedule = trainHasSchedule;
            this.trainHasAutoSchedule = trainHasAutoSchedule;

            notifyUpdate();
        }
    }

    public boolean trackClicked(PlayerEntity player, Hand hand, ITrackBlock track, BlockState state, BlockPos pos) {
        refreshAssemblyInfo();
        BlockBox bb = assemblyAreas.get(world).get(this.pos);
        if (bb == null || !bb.contains(pos))
            return false;

        BlockPos up = BlockPos.ofFloored(track.getUpNormal(world, pos, state));
        BlockPos down = BlockPos.ofFloored(track.getUpNormal(world, pos, state).multiply(-1));
        int bogeyOffset = pos.getChebyshevDistance(edgePoint.getGlobalPosition()) - 1;

        if (!isValidBogeyOffset(bogeyOffset)) {
            for (boolean upsideDown : Iterate.falseAndTrue) {
                for (int i = -1; i <= 1; i++) {
                    BlockPos bogeyPos = pos.offset(assemblyDirection, i).add(upsideDown ? down : up);
                    BlockState blockState = world.getBlockState(bogeyPos);
                    if (!(blockState.getBlock() instanceof AbstractBogeyBlock<?> bogey))
                        continue;
                    BlockEntity be = world.getBlockEntity(bogeyPos);
                    if (!(be instanceof AbstractBogeyBlockEntity oldBE))
                        continue;
                    NbtCompound oldData = oldBE.getBogeyData();
                    BlockState newBlock = bogey.getNextSize(oldBE);
                    if (newBlock.getBlock() == bogey)
                        player.sendMessage(Text.translatable("create.bogey.style.no_other_sizes").formatted(Formatting.RED), true);
                    world.setBlockState(bogeyPos, newBlock, Block.NOTIFY_ALL);
                    BlockEntity newEntity = world.getBlockEntity(bogeyPos);
                    if (!(newEntity instanceof AbstractBogeyBlockEntity newBE))
                        continue;
                    newBE.setBogeyData(oldData);
                    IWrenchable.playRotateSound(world, bogeyPos);
                    return true;
                }
            }

            return false;
        }

        ItemStack handItem = player.getStackInHand(hand);
        if (!player.isCreative() && !handItem.isOf(AllItems.RAILWAY_CASING)) {
            player.sendMessage(Text.translatable("create.train_assembly.requires_casing"), true);
            return false;
        }

        boolean upsideDown = (player.getLerpedPitch(1.0F) < 0 && (track.getBogeyAnchor(
            world,
            pos,
            state
        )).getBlock() instanceof AbstractBogeyBlock<?> bogey && bogey.canBeUpsideDown());

        BlockPos targetPos = upsideDown ? pos.add(down) : pos.add(up);
        if (world.getBlockState(targetPos).getHardness(world, targetPos) == -1) {
            return false;
        }

        world.breakBlock(targetPos, true);

        BlockState bogeyAnchor = track.getBogeyAnchor(world, pos, state);
        if (bogeyAnchor.getBlock() instanceof AbstractBogeyBlock<?> bogey) {
            bogeyAnchor = bogey.getVersion(bogeyAnchor, upsideDown);
        }
        bogeyAnchor = ProperWaterloggedBlock.withWater(world, bogeyAnchor, pos);
        world.setBlockState(targetPos, bogeyAnchor, Block.NOTIFY_ALL);
        player.sendMessage(Text.translatable("create.train_assembly.bogey_created"), true);
        BlockSoundGroup soundtype = bogeyAnchor.getSoundGroup();
        world.playSound(
            null,
            pos,
            soundtype.getPlaceSound(),
            SoundCategory.BLOCKS,
            (soundtype.getVolume() + 1.0F) / 2.0F,
            soundtype.getPitch() * 0.8F
        );

        if (!player.isCreative()) {
            ItemStack itemInHand = player.getStackInHand(hand);
            itemInHand.decrement(1);
            if (itemInHand.isEmpty())
                player.setStackInHand(hand, ItemStack.EMPTY);
        }

        return true;
    }

    public boolean enterAssemblyMode(@Nullable ServerPlayerEntity sender) {
        if (isAssembling())
            return false;

        tryDisassembleTrain(sender);
        if (!tryEnterAssemblyMode())
            return false;

        // Check the station wasn't destroyed
        if (!(world.getBlockState(pos).getBlock() instanceof StationBlock))
            return true;

        BlockState newState = getCachedState().with(StationBlock.ASSEMBLING, true);
        world.setBlockState(getPos(), newState, Block.NOTIFY_ALL);
        refreshBlockState();
        refreshAssemblyInfo();

        updateStationState(station -> station.assembling = true);
        GlobalStation station = getStation();
        if (station != null) {
            for (Train train : Create.RAILWAYS.sided(world).trains.values()) {
                if (train.navigation.destination != station)
                    continue;

                DiscoveredPath preferredPath = train.runtime.startCurrentInstruction(world);
                train.navigation.startNavigation(preferredPath != null ? preferredPath : train.navigation.findPathTo(station, Double.MAX_VALUE));
            }
        }

        return true;
    }

    public boolean exitAssemblyMode() {
        if (!isAssembling())
            return false;

        cancelAssembly();
        BlockState newState = getCachedState().with(StationBlock.ASSEMBLING, false);
        world.setBlockState(getPos(), newState, Block.NOTIFY_ALL);
        refreshBlockState();

        return updateStationState(station -> station.assembling = false);
    }

    public boolean tryDisassembleTrain(@Nullable ServerPlayerEntity sender) {
        GlobalStation station = getStation();
        if (station == null)
            return false;

        Train train = station.getPresentTrain();
        if (train == null)
            return false;

        BlockPos trackPosition = edgePoint.getGlobalPosition();
        if (!train.disassemble(sender, getAssemblyDirection(), trackPosition.up()))
            return false;

        dropSchedule(sender, train);
        return true;
    }

    public boolean isAssembling() {
        BlockState state = getCachedState();
        return state.contains(StationBlock.ASSEMBLING) && state.get(StationBlock.ASSEMBLING);
    }

    public boolean tryEnterAssemblyMode() {
        if (!edgePoint.hasValidTrack())
            return false;

        BlockPos targetPosition = edgePoint.getGlobalPosition();
        BlockState trackState = edgePoint.getTrackBlockState();
        ITrackBlock track = edgePoint.getTrack();
        Vec3d trackAxis = track.getTrackAxes(world, targetPosition, trackState).get(0);

        boolean axisFound = false;
        for (Axis axis : Iterate.axes) {
            if (trackAxis.getComponentAlongAxis(axis) == 0)
                continue;
            if (axisFound)
                return false;
            axisFound = true;
        }

        return true;
    }

    public void dropSchedule(@Nullable ServerPlayerEntity sender, @Nullable Train train) {
        GlobalStation station = getStation();
        if (station == null)
            return;
        if (train == null)
            return;

        ItemStack schedule = train.runtime.returnSchedule(world.getRegistryManager());
        if (schedule.isEmpty())
            return;
        if (sender != null && sender.getMainHandStack().isEmpty()) {
            sender.getInventory().offerOrDrop(schedule);
            return;
        }

        Vec3d v = VecHelper.getCenterOf(getPos());
        ItemEntity itemEntity = new ItemEntity(getWorld(), v.x, v.y, v.z, schedule);
        itemEntity.setVelocity(Vec3d.ZERO);
        getWorld().spawnEntity(itemEntity);
    }

    public void updateMapColor(int color) {
        GlobalStation station = getStation();
        if (station == null)
            return;

        Train train = station.getPresentTrain();
        if (train == null)
            return;

        train.mapColorIndex = color;
    }

    private boolean updateStationState(Consumer<GlobalStation> updateState) {
        GlobalStation station = getStation();
        TrackGraphLocation graphLocation = edgePoint.determineGraphLocation();
        if (station == null || graphLocation == null)
            return false;

        updateState.accept(station);
        Create.RAILWAYS.sync.pointAdded(graphLocation.graph, station);
        Create.RAILWAYS.markTracksDirty();
        return true;
    }

    public void refreshAssemblyInfo() {
        if (!edgePoint.hasValidTrack())
            return;

        if (!isVirtual()) {
            GlobalStation station = getStation();
            if (station == null || station.getPresentTrain() != null)
                return;
        }

        int prevLength = assemblyLength;
        BlockPos targetPosition = edgePoint.getGlobalPosition();
        BlockState trackState = edgePoint.getTrackBlockState();
        ITrackBlock track = edgePoint.getTrack();
        getAssemblyDirection();

        BlockPos.Mutable currentPos = targetPosition.mutableCopy();
        currentPos.move(assemblyDirection);

        BlockPos bogeyOffset = BlockPos.ofFloored(track.getUpNormal(world, targetPosition, trackState));

        int MAX_LENGTH = AllConfigs.server().trains.maxAssemblyLength.get();
        int MAX_BOGEY_COUNT = AllConfigs.server().trains.maxBogeyCount.get();

        int bogeyIndex = 0;
        int maxBogeyCount = MAX_BOGEY_COUNT;
        if (bogeyLocations == null)
            bogeyLocations = new int[maxBogeyCount];
        if (bogeyTypes == null)
            bogeyTypes = new AbstractBogeyBlock[maxBogeyCount];
        if (upsideDownBogeys == null)
            upsideDownBogeys = new boolean[maxBogeyCount];
        Arrays.fill(bogeyLocations, -1);
        Arrays.fill(bogeyTypes, null);
        Arrays.fill(upsideDownBogeys, false);

        for (int i = 0; i < MAX_LENGTH; i++) {
            if (i == MAX_LENGTH - 1) {
                assemblyLength = i;
                break;
            }
            if (!track.trackEquals(trackState, world.getBlockState(currentPos))) {
                assemblyLength = Math.max(0, i - 1);
                break;
            }

            BlockState potentialBogeyState = world.getBlockState(bogeyOffset.add(currentPos));
            BlockPos upsideDownBogeyOffset = new BlockPos(bogeyOffset.getX(), bogeyOffset.getY() * -1, bogeyOffset.getZ());
            if (bogeyIndex < bogeyLocations.length) {
                if (potentialBogeyState.getBlock() instanceof AbstractBogeyBlock<?> bogey && !bogey.isUpsideDown(potentialBogeyState)) {
                    bogeyTypes[bogeyIndex] = bogey;
                    bogeyLocations[bogeyIndex] = i;
                    upsideDownBogeys[bogeyIndex] = false;
                    bogeyIndex++;
                } else if ((potentialBogeyState = world.getBlockState(upsideDownBogeyOffset.add(currentPos))).getBlock() instanceof AbstractBogeyBlock<?> bogey && bogey.isUpsideDown(
                    potentialBogeyState)) {
                    bogeyTypes[bogeyIndex] = bogey;
                    bogeyLocations[bogeyIndex] = i;
                    upsideDownBogeys[bogeyIndex] = true;
                    bogeyIndex++;
                }
            }

            currentPos.move(assemblyDirection);
        }

        bogeyCount = bogeyIndex;

        if (world.isClient())
            return;
        if (prevLength == assemblyLength)
            return;
        if (isVirtual())
            return;

        Map<BlockPos, BlockBox> map = assemblyAreas.get(world);
        BlockPos startPosition = targetPosition.offset(assemblyDirection);
        BlockPos trackEnd = startPosition.offset(assemblyDirection, assemblyLength - 1);
        map.put(pos, BlockBox.create(startPosition, trackEnd));
    }

    public boolean updateName(String name) {
        if (!updateStationState(station -> station.name = name))
            return false;
        notifyUpdate();

        return true;
    }

    public boolean isValidBogeyOffset(int i) {
        if ((i < 3 || bogeyCount == 0) && i != 0)
            return false;
        for (int j : bogeyLocations) {
            if (j == -1)
                break;
            if (i >= j - 2 && i <= j + 2)
                return false;
        }
        return true;
    }

    public Direction getAssemblyDirection() {
        if (assemblyDirection != null)
            return assemblyDirection;
        if (!edgePoint.hasValidTrack())
            return null;
        BlockPos targetPosition = edgePoint.getGlobalPosition();
        BlockState trackState = edgePoint.getTrackBlockState();
        ITrackBlock track = edgePoint.getTrack();
        AxisDirection axisDirection = edgePoint.getTargetDirection();
        Vec3d axis = track.getTrackAxes(world, targetPosition, trackState).get(0).normalize().multiply(axisDirection.offset());
        return assemblyDirection = Direction.getFacing(axis.x, axis.y, axis.z);
    }

    @Override
    public void remove() {
        assemblyAreas.get(world).remove(pos);
        super.remove();
    }

    public void assemble(UUID playerUUID) {
        refreshAssemblyInfo();

        if (bogeyLocations == null)
            return;

        if (bogeyLocations[0] != 0) {
            exception(new AssemblyException(Text.translatable("create.train_assembly.frontmost_bogey_at_station")), -1);
            return;
        }

        if (!edgePoint.hasValidTrack())
            return;

        BlockPos trackPosition = edgePoint.getGlobalPosition();
        BlockState trackState = edgePoint.getTrackBlockState();
        ITrackBlock track = edgePoint.getTrack();
        BlockPos bogeyOffset = BlockPos.ofFloored(track.getUpNormal(world, trackPosition, trackState));

        TrackNodeLocation location = null;
        Vec3d center = Vec3d.ofBottomCenter(trackPosition).add(0, track.getElevationAtCenter(world, trackPosition, trackState), 0);
        Collection<DiscoveredLocation> ends = track.getConnected(world, trackPosition, trackState, true, null);
        Vec3d targetOffset = Vec3d.of(assemblyDirection.getVector());
        for (DiscoveredLocation end : ends)
            if (MathHelper.approximatelyEquals(0, targetOffset.squaredDistanceTo(end.getLocation().subtract(center).normalize())))
                location = end;
        if (location == null)
            return;

        List<Double> pointOffsets = new ArrayList<>();
        int iPrevious = -100;
        for (int i = 0; i < bogeyLocations.length; i++) {
            int loc = bogeyLocations[i];
            if (loc == -1)
                break;

            if (loc - iPrevious < 3) {
                exception(new AssemblyException(Text.translatable("create.train_assembly.bogeys_too_close", i, i + 1)), -1);
                return;
            }

            double bogeySize = bogeyTypes[i].getWheelPointSpacing();
            pointOffsets.add(loc + .5 - bogeySize / 2);
            pointOffsets.add(loc + .5 + bogeySize / 2);
            iPrevious = loc;
        }

        List<TravellingPoint> points = new ArrayList<>();
        Vec3d directionVec = Vec3d.of(assemblyDirection.getVector());
        TrackGraph graph = null;
        TrackNode secondNode = null;

        for (int j = 0; j < assemblyLength * 2 + 40; j++) {
            double i = j / 2d;
            if (points.size() == pointOffsets.size())
                break;

            TrackNodeLocation currentLocation = location;
            location = new TrackNodeLocation(location.getLocation().add(directionVec.multiply(.5))).in(location.dimension);

            if (graph == null)
                graph = Create.RAILWAYS.getGraph(currentLocation);
            if (graph == null)
                continue;
            TrackNode node = graph.locateNode(currentLocation);
            if (node == null)
                continue;

            for (int pointIndex = points.size(); pointIndex < pointOffsets.size(); pointIndex++) {
                double offset = pointOffsets.get(pointIndex);
                if (offset > i)
                    break;
                double positionOnEdge = i - offset;

                Map<TrackNode, TrackEdge> connectionsFromNode = graph.getConnectionsFrom(node);

                if (secondNode == null)
                    for (Map.Entry<TrackNode, TrackEdge> entry : connectionsFromNode.entrySet()) {
                        TrackEdge edge = entry.getValue();
                        TrackNode otherNode = entry.getKey();
                        if (edge.isTurn())
                            continue;
                        Vec3d edgeDirection = edge.getDirection(true);
                        if (MathHelper.approximatelyEquals(edgeDirection.normalize().dotProduct(directionVec), -1d))
                            secondNode = otherNode;
                    }

                if (secondNode == null) {
                    Create.LOGGER.warn("Cannot assemble: No valid starting node found");
                    return;
                }

                TrackEdge edge = connectionsFromNode.get(secondNode);

                if (edge == null) {
                    Create.LOGGER.warn("Cannot assemble: Missing graph edge");
                    return;
                }

                points.add(new TravellingPoint(node, secondNode, edge, positionOnEdge, false));
            }

            secondNode = node;
        }

        if (points.size() != pointOffsets.size()) {
            Create.LOGGER.warn("Cannot assemble: Not all Points created");
            return;
        }

        if (points.size() == 0) {
            exception(new AssemblyException(Text.translatable("create.train_assembly.no_bogeys")), -1);
            return;
        }

        List<CarriageContraption> contraptions = new ArrayList<>();
        List<Carriage> carriages = new ArrayList<>();
        List<Integer> spacing = new ArrayList<>();
        boolean atLeastOneForwardControls = false;

        for (int bogeyIndex = 0; bogeyIndex < bogeyCount; bogeyIndex++) {
            int pointIndex = bogeyIndex * 2;
            if (bogeyIndex > 0)
                spacing.add(bogeyLocations[bogeyIndex] - bogeyLocations[bogeyIndex - 1]);
            CarriageContraption contraption = new CarriageContraption(assemblyDirection);
            BlockPos bogeyPosOffset = trackPosition.add(bogeyOffset);
            BlockPos upsideDownBogeyPosOffset = trackPosition.add(new BlockPos(bogeyOffset.getX(), bogeyOffset.getY() * -1, bogeyOffset.getZ()));

            try {
                int offset = bogeyLocations[bogeyIndex] + 1;
                boolean success = contraption.assemble(
                    world,
                    upsideDownBogeys[bogeyIndex] ? upsideDownBogeyPosOffset.offset(assemblyDirection, offset) : bogeyPosOffset.offset(
                        assemblyDirection,
                        offset
                    )
                );
                atLeastOneForwardControls |= contraption.hasForwardControls();
                contraption.setSoundQueueOffset(offset);
                if (!success) {
                    exception(new AssemblyException(Text.translatable("create.train_assembly.nothing_attached", bogeyIndex + 1)), -1);
                    return;
                }
            } catch (AssemblyException e) {
                exception(e, contraptions.size() + 1);
                return;
            }

            AbstractBogeyBlock<?> typeOfFirstBogey = bogeyTypes[bogeyIndex];
            boolean firstBogeyIsUpsideDown = upsideDownBogeys[bogeyIndex];
            BlockPos firstBogeyPos = contraption.anchor;
            AbstractBogeyBlockEntity firstBogeyBlockEntity = (AbstractBogeyBlockEntity) world.getBlockEntity(firstBogeyPos);
            CarriageBogey firstBogey = new CarriageBogey(
                typeOfFirstBogey,
                firstBogeyIsUpsideDown,
                firstBogeyBlockEntity.getBogeyData(),
                points.get(pointIndex),
                points.get(pointIndex + 1)
            );
            CarriageBogey secondBogey = null;
            BlockPos secondBogeyPos = contraption.getSecondBogeyPos();
            int bogeySpacing = 0;

            if (secondBogeyPos != null) {
                if (bogeyIndex == bogeyCount - 1 || !secondBogeyPos.equals((upsideDownBogeys[bogeyIndex + 1] ? upsideDownBogeyPosOffset : bogeyPosOffset).offset(assemblyDirection,
                    bogeyLocations[bogeyIndex + 1] + 1
                ))) {
                    exception(new AssemblyException(Text.translatable("create.train_assembly.not_connected_in_order")), contraptions.size() + 1);
                    return;
                }
                AbstractBogeyBlockEntity secondBogeyBlockEntity = (AbstractBogeyBlockEntity) world.getBlockEntity(secondBogeyPos);
                bogeySpacing = bogeyLocations[bogeyIndex + 1] - bogeyLocations[bogeyIndex];
                secondBogey = new CarriageBogey(
                    bogeyTypes[bogeyIndex + 1],
                    upsideDownBogeys[bogeyIndex + 1],
                    secondBogeyBlockEntity.getBogeyData(),
                    points.get(pointIndex + 2),
                    points.get(pointIndex + 3)
                );
                bogeyIndex++;

            } else if (!typeOfFirstBogey.allowsSingleBogeyCarriage()) {
                exception(new AssemblyException(Text.translatable("create.train_assembly.single_bogey_carriage")), contraptions.size() + 1);
                return;
            }

            contraptions.add(contraption);
            carriages.add(new Carriage(firstBogey, secondBogey, bogeySpacing));
        }

        if (!atLeastOneForwardControls) {
            exception(new AssemblyException(Text.translatable("create.train_assembly.no_controls")), -1);
            return;
        }

        for (CarriageContraption contraption : contraptions) {
            contraption.removeBlocksFromWorld(world, BlockPos.ORIGIN);
            contraption.expandBoundsAroundAxis(Axis.Y);
        }

        Train train = new Train(
            UUID.randomUUID(),
            playerUUID,
            graph,
            carriages,
            spacing,
            contraptions.stream().anyMatch(CarriageContraption::hasBackwardControls),
            0
        );

        if (lastDisassembledTrainName != null) {
            train.name = lastDisassembledTrainName;
            train.mapColorIndex = lastDisassembledMapColorIndex;
            lastDisassembledTrainName = null;
            lastDisassembledMapColorIndex = 0;
        }

        for (int i = 0; i < contraptions.size(); i++) {
            CarriageContraption contraption = contraptions.get(i);
            Carriage carriage = carriages.get(i);
            carriage.setContraption(world, contraption);
            if (contraption.containsBlockBreakers())
                award(AllAdvancements.CONTRAPTION_ACTORS);
        }

        GlobalStation station = getStation();
        if (station != null) {
            train.setCurrentStation(station);
            station.reserveFor(train);
        }

        train.collectInitiallyOccupiedSignalBlocks();
        Create.RAILWAYS.addTrain(train);
        world.getServer().getPlayerManager().sendToAll(new AddTrainPacket(train));
        clearException();

        award(AllAdvancements.TRAIN);
        if (contraptions.size() >= 6)
            award(AllAdvancements.LONG_TRAIN);
    }

    public void cancelAssembly() {
        assemblyLength = 0;
        assemblyAreas.get(world).remove(pos);
        clearException();
    }

    private void clearException() {
        exception(null, -1);
    }

    private void exception(AssemblyException exception, int carriage) {
        failedCarriageIndex = carriage;
        lastException = exception;
        sendData();
    }

    //TODO
    //    @Override
    //    @OnlyIn(Dist.CLIENT)
    //    public AABB getRenderBoundingBox() {
    //        if (isAssembling())
    //            return AABB.INFINITE;
    //        return super.getRenderBoundingBox();
    //    }

    @Override
    protected Box createRenderBoundingBox() {
        return new Box(Vec3d.of(pos), Vec3d.of(edgePoint.getGlobalPosition())).expand(2);
    }

    public ItemStack getAutoSchedule() {
        return depotBehaviour.getHeldItemStack();
    }

    private void applyAutoSchedule() {
        ItemStack stack = getAutoSchedule();
        if (!stack.isOf(AllItems.SCHEDULE))
            return;
        Schedule schedule = ScheduleItem.getSchedule(world.getRegistryManager(), stack);
        if (schedule == null || schedule.entries.isEmpty())
            return;
        GlobalStation station = getStation();
        if (station == null)
            return;
        Train imminentTrain = station.getImminentTrain();
        if (imminentTrain == null || imminentTrain.getCurrentStation() != station)
            return;

        award(AllAdvancements.CONDUCTOR);
        imminentTrain.runtime.setSchedule(schedule, true);
        AllSoundEvents.CONFIRM.playOnServer(world, pos, 1, 1);

        if (!(world instanceof ServerWorld server))
            return;

        Vec3d v = Vec3d.ofBottomCenter(pos.up());
        server.spawnParticles(ParticleTypes.HAPPY_VILLAGER, v.x, v.y, v.z, 8, 0.35, 0.05, 0.35, 1);
        server.spawnParticles(ParticleTypes.END_ROD, v.x, v.y + .25f, v.z, 10, 0.05, 1, 0.05, 0.005f);
    }

    public boolean resolveFlagAngle() {
        if (flagYRot != -1)
            return true;

        BlockState target = edgePoint.getTrackBlockState();
        if (!(target.getBlock() instanceof ITrackBlock def))
            return false;

        Vec3d axis = null;
        BlockPos trackPos = edgePoint.getGlobalPosition();
        for (Vec3d vec3 : def.getTrackAxes(world, trackPos, target))
            axis = vec3.multiply(edgePoint.getTargetDirection().offset());
        if (axis == null)
            return false;

        Direction nearest = Direction.getFacing(axis.x, 0, axis.z);
        flagYRot = (int) (-nearest.getPositiveHorizontalDegrees() - 90);

        Vec3d diff = Vec3d.of(trackPos.subtract(pos)).multiply(1, 0, 1);
        if (diff.lengthSquared() == 0)
            return true;

        flagFlipped = diff.dotProduct(Vec3d.of(nearest.rotateYClockwise().getVector())) > 0;

        return true;
    }

    @Override
    public void transform(BlockEntity be, StructureTransform transform) {
        edgePoint.transform(be, transform);
    }

    // Package port integration

    public void attachPackagePort(PackagePortBlockEntity ppbe) {
        GlobalStation station = getStation();
        if (station == null || world.isClient())
            return;

        if (ppbe instanceof PostboxBlockEntity pbe)
            pbe.trackedGlobalStation = new WeakReference<>(station);

        GlobalPackagePort globalPackagePort = station.connectedPorts.get(ppbe.getPos());

        if (globalPackagePort == null) {
            globalPackagePort = new GlobalPackagePort();
            globalPackagePort.address = ppbe.addressFilter;
            station.connectedPorts.put(ppbe.getPos(), globalPackagePort);
        } else {
            globalPackagePort.restoreOfflineBuffer(ppbe.inventory);
        }
    }

    public void removePackagePort(PackagePortBlockEntity ppbe) {
        GlobalStation station = getStation();
        if (station == null)
            return;

        station.connectedPorts.remove(ppbe.getPos());
    }

}
