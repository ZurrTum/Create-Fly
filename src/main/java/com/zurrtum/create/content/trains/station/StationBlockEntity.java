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
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;

public class StationBlockEntity extends SmartBlockEntity implements TransformableBlockEntity {

    public TrackTargetingBehaviour<GlobalStation> edgePoint;
    public DoorControlBehaviour doorControls;
    public LerpedFloat flag;

    public int failedCarriageIndex;
    public @Nullable AssemblyException lastException;
    public DepotBehaviour depotBehaviour;

    // for display
    public @Nullable UUID imminentTrain;
    public boolean trainPresent;
    public boolean trainBackwards;
    public boolean trainCanDisassemble;
    public boolean trainHasSchedule;
    public boolean trainHasAutoSchedule;

    public int flagYRot = -1;
    public boolean flagFlipped;

    public @Nullable Component lastDisassembledTrainName;
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
        behaviours.add(depotBehaviour = new DepotBehaviour(this).onlyAccepts(stack -> stack.is(AllItems.SCHEDULE))
            .withCallback(s -> applyAutoSchedule()));
        depotBehaviour.addSubBehaviours(behaviours);
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.CONTRAPTION_ACTORS, AllAdvancements.TRAIN, AllAdvancements.LONG_TRAIN, AllAdvancements.CONDUCTOR);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        lastException = AssemblyException.read(view);
        failedCarriageIndex = view.getIntOr("FailedCarriageIndex", 0);
        super.read(view, clientPacket);
        invalidateRenderBoundingBox();

        trainPresent = view.getBooleanOr("ForceFlag", false);
        lastDisassembledTrainName = view.read("PrevTrainName", ComponentSerialization.CODEC).orElse(null);
        lastDisassembledMapColorIndex = view.getIntOr("PrevTrainColor", 0);

        if (!clientPacket)
            return;
        view.read("ImminentTrain", UUIDUtil.CODEC).ifPresentOrElse(
            uuid -> {
                imminentTrain = uuid;
                trainPresent = view.getBooleanOr("TrainPresent", false);
                trainCanDisassemble = view.getBooleanOr("TrainCanDisassemble", false);
                trainBackwards = view.getBooleanOr("TrainBackwards", false);
                trainHasSchedule = view.getBooleanOr("TrainHasSchedule", false);
                trainHasAutoSchedule = view.getBooleanOr("TrainHasAutoSchedule", false);
            }, () -> {
                imminentTrain = null;
                trainPresent = false;
                trainCanDisassemble = false;
                trainBackwards = false;
            }
        );
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        AssemblyException.write(view, lastException);
        view.putInt("FailedCarriageIndex", failedCarriageIndex);

        if (lastDisassembledTrainName != null)
            view.store("PrevTrainName", ComponentSerialization.CODEC, lastDisassembledTrainName);
        view.putInt("PrevTrainColor", lastDisassembledMapColorIndex);

        super.write(view, clientPacket);

        if (!clientPacket)
            return;
        if (imminentTrain == null)
            return;

        view.store("ImminentTrain", UUIDUtil.CODEC, imminentTrain);

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

    public static WorldAttached<Map<BlockPos, BoundingBox>> assemblyAreas = new WorldAttached<>(w -> new HashMap<>());

    public @Nullable Direction assemblyDirection;
    public int assemblyLength;
    public int @Nullable [] bogeyLocations;
    AbstractBogeyBlock<?> @Nullable [] bogeyTypes;
    boolean @Nullable [] upsideDownBogeys;
    public int bogeyCount;

    @Override
    public void lazyTick() {
        if (isAssembling() && !level.isClientSide())
            refreshAssemblyInfo();
        super.lazyTick();
    }

    @Override
    public void tick() {
        if (isAssembling() && level.isClientSide())
            refreshAssemblyInfo();
        super.tick();

        if (level.isClientSide()) {
            float currentTarget = flag.getChaseTarget();
            if (currentTarget == 0 || flag.settled()) {
                int target = trainPresent || isAssembling() ? 1 : 0;
                if (target != currentTarget) {
                    flag.chase(target, 0.1f, Chaser.LINEAR);
                    if (target == 1)
                        AllSoundEvents.CONTRAPTION_DISASSEMBLE.playAt(level, worldPosition, 1, 2, true);
                }
            }
            boolean settled = flag.getValue() > .15f;
            flag.tickChaser();
            if (currentTarget == 0 && settled != flag.getValue() > .15f)
                AllSoundEvents.CONTRAPTION_ASSEMBLE.playAt(level, worldPosition, 0.75f, 1.5f, true);
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
            DisplayLinkBlock.notifyGatherers(level, worldPosition);
            imminentTrain.runtime.displayLinkUpdateRequested = false;
        }

        if (!level.isClientSide()) {
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

    public boolean trackClicked(Player player, InteractionHand hand, ITrackBlock track, BlockState state, BlockPos pos) {
        refreshAssemblyInfo();
        BoundingBox bb = assemblyAreas.get(level).get(this.worldPosition);
        if (bb == null || !bb.isInside(pos))
            return false;

        BlockPos up = BlockPos.containing(track.getUpNormal(level, pos, state));
        BlockPos down = BlockPos.containing(track.getUpNormal(level, pos, state).scale(-1));
        int bogeyOffset = pos.distChessboard(edgePoint.getGlobalPosition()) - 1;

        if (!isValidBogeyOffset(bogeyOffset)) {
            for (boolean upsideDown : Iterate.falseAndTrue) {
                for (int i = -1; i <= 1; i++) {
                    BlockPos bogeyPos = pos.relative(assemblyDirection, i).offset(upsideDown ? down : up);
                    BlockState blockState = level.getBlockState(bogeyPos);
                    if (!(blockState.getBlock() instanceof AbstractBogeyBlock<?> bogey))
                        continue;
                    BlockEntity be = level.getBlockEntity(bogeyPos);
                    if (!(be instanceof AbstractBogeyBlockEntity oldBE))
                        continue;
                    CompoundTag oldData = oldBE.getBogeyData();
                    BlockState newBlock = bogey.getNextSize(oldBE);
                    if (newBlock.getBlock() == bogey)
                        player.displayClientMessage(Component.translatable("create.bogey.style.no_other_sizes").withStyle(ChatFormatting.RED), true);
                    level.setBlock(bogeyPos, newBlock, Block.UPDATE_ALL);
                    BlockEntity newEntity = level.getBlockEntity(bogeyPos);
                    if (!(newEntity instanceof AbstractBogeyBlockEntity newBE))
                        continue;
                    newBE.setBogeyData(oldData);
                    IWrenchable.playRotateSound(level, bogeyPos);
                    return true;
                }
            }

            return false;
        }

        ItemStack handItem = player.getItemInHand(hand);
        if (!player.isCreative() && !handItem.is(AllItems.RAILWAY_CASING)) {
            player.displayClientMessage(Component.translatable("create.train_assembly.requires_casing"), true);
            return false;
        }

        boolean upsideDown = (player.getXRot(1.0F) < 0 && (track.getBogeyAnchor(
            level,
            pos,
            state
        )).getBlock() instanceof AbstractBogeyBlock<?> bogey && bogey.canBeUpsideDown());

        BlockPos targetPos = upsideDown ? pos.offset(down) : pos.offset(up);
        if (level.getBlockState(targetPos).getDestroySpeed(level, targetPos) == -1) {
            return false;
        }

        level.destroyBlock(targetPos, true);

        BlockState bogeyAnchor = track.getBogeyAnchor(level, pos, state);
        if (bogeyAnchor.getBlock() instanceof AbstractBogeyBlock<?> bogey) {
            bogeyAnchor = bogey.getVersion(bogeyAnchor, upsideDown);
        }
        bogeyAnchor = ProperWaterloggedBlock.withWater(level, bogeyAnchor, pos);
        level.setBlock(targetPos, bogeyAnchor, Block.UPDATE_ALL);
        player.displayClientMessage(Component.translatable("create.train_assembly.bogey_created"), true);
        SoundType soundtype = bogeyAnchor.getSoundType();
        level.playSound(null, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

        if (!player.isCreative()) {
            ItemStack itemInHand = player.getItemInHand(hand);
            itemInHand.shrink(1);
            if (itemInHand.isEmpty())
                player.setItemInHand(hand, ItemStack.EMPTY);
        }

        return true;
    }

    public boolean enterAssemblyMode(@Nullable ServerPlayer sender) {
        if (isAssembling())
            return false;

        tryDisassembleTrain(sender);
        if (!tryEnterAssemblyMode())
            return false;

        // Check the station wasn't destroyed
        if (!(level.getBlockState(worldPosition).getBlock() instanceof StationBlock))
            return true;

        BlockState newState = getBlockState().setValue(StationBlock.ASSEMBLING, true);
        level.setBlock(getBlockPos(), newState, Block.UPDATE_ALL);
        refreshBlockState();
        refreshAssemblyInfo();

        updateStationState(station -> station.assembling = true);
        GlobalStation station = getStation();
        if (station != null) {
            for (Train train : Create.RAILWAYS.sided(level).trains.values()) {
                if (train.navigation.destination != station)
                    continue;

                DiscoveredPath preferredPath = train.runtime.startCurrentInstruction(level);
                train.navigation.startNavigation(preferredPath != null ? preferredPath : train.navigation.findPathTo(station, Double.MAX_VALUE));
            }
        }

        return true;
    }

    public boolean exitAssemblyMode() {
        if (!isAssembling())
            return false;

        cancelAssembly();
        BlockState newState = getBlockState().setValue(StationBlock.ASSEMBLING, false);
        level.setBlock(getBlockPos(), newState, Block.UPDATE_ALL);
        refreshBlockState();

        return updateStationState(station -> station.assembling = false);
    }

    public boolean tryDisassembleTrain(@Nullable ServerPlayer sender) {
        GlobalStation station = getStation();
        if (station == null)
            return false;

        Train train = station.getPresentTrain();
        if (train == null)
            return false;

        BlockPos trackPosition = edgePoint.getGlobalPosition();
        if (!train.disassemble(sender, getAssemblyDirection(), trackPosition.above()))
            return false;

        dropSchedule(sender, train);
        return true;
    }

    public boolean isAssembling() {
        BlockState state = getBlockState();
        return state.hasProperty(StationBlock.ASSEMBLING) && state.getValue(StationBlock.ASSEMBLING);
    }

    public boolean tryEnterAssemblyMode() {
        if (!edgePoint.hasValidTrack())
            return false;

        BlockPos targetPosition = edgePoint.getGlobalPosition();
        BlockState trackState = edgePoint.getTrackBlockState();
        ITrackBlock track = edgePoint.getTrack();
        Vec3 trackAxis = track.getTrackAxes(level, targetPosition, trackState).getFirst();

        boolean axisFound = false;
        for (Axis axis : Iterate.axes) {
            if (trackAxis.get(axis) == 0)
                continue;
            if (axisFound)
                return false;
            axisFound = true;
        }

        return true;
    }

    public void dropSchedule(@Nullable ServerPlayer sender, @Nullable Train train) {
        GlobalStation station = getStation();
        if (station == null)
            return;
        if (train == null)
            return;

        ItemStack schedule = train.runtime.returnSchedule(level.registryAccess());
        if (schedule.isEmpty())
            return;
        if (sender != null && sender.getMainHandItem().isEmpty()) {
            sender.getInventory().placeItemBackInInventory(schedule);
            return;
        }

        Vec3 v = VecHelper.getCenterOf(getBlockPos());
        ItemEntity itemEntity = new ItemEntity(getLevel(), v.x, v.y, v.z, schedule);
        itemEntity.setDeltaMovement(Vec3.ZERO);
        getLevel().addFreshEntity(itemEntity);
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

        BlockPos.MutableBlockPos currentPos = targetPosition.mutable();
        currentPos.move(assemblyDirection);

        BlockPos bogeyOffset = BlockPos.containing(track.getUpNormal(level, targetPosition, trackState));

        int maxLength = AllConfigs.server().trains.maxAssemblyLength.get();
        int maxBogeyCount = AllConfigs.server().trains.maxBogeyCount.get();

        int bogeyIndex = 0;
        if (bogeyLocations == null)
            bogeyLocations = new int[maxBogeyCount];
        if (bogeyTypes == null)
            bogeyTypes = new AbstractBogeyBlock[maxBogeyCount];
        if (upsideDownBogeys == null)
            upsideDownBogeys = new boolean[maxBogeyCount];
        Arrays.fill(bogeyLocations, -1);
        Arrays.fill(bogeyTypes, null);
        Arrays.fill(upsideDownBogeys, false);

        for (int i = 0; i < maxLength; i++) {
            if (i == maxLength - 1) {
                assemblyLength = i;
                break;
            }
            if (!track.trackEquals(trackState, level.getBlockState(currentPos))) {
                assemblyLength = Math.max(0, i - 1);
                break;
            }

            BlockState potentialBogeyState = level.getBlockState(bogeyOffset.offset(currentPos));
            BlockPos upsideDownBogeyOffset = new BlockPos(bogeyOffset.getX(), bogeyOffset.getY() * -1, bogeyOffset.getZ());
            if (bogeyIndex < bogeyLocations.length) {
                if (potentialBogeyState.getBlock() instanceof AbstractBogeyBlock<?> bogey && !bogey.isUpsideDown(potentialBogeyState)) {
                    bogeyTypes[bogeyIndex] = bogey;
                    bogeyLocations[bogeyIndex] = i;
                    upsideDownBogeys[bogeyIndex] = false;
                    bogeyIndex++;
                } else if ((potentialBogeyState = level.getBlockState(upsideDownBogeyOffset.offset(currentPos))).getBlock() instanceof AbstractBogeyBlock<?> bogey && bogey.isUpsideDown(
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

        if (level.isClientSide())
            return;
        if (prevLength == assemblyLength)
            return;
        if (isVirtual())
            return;

        Map<BlockPos, BoundingBox> map = assemblyAreas.get(level);
        BlockPos startPosition = targetPosition.relative(assemblyDirection);
        BlockPos trackEnd = startPosition.relative(assemblyDirection, assemblyLength - 1);
        map.put(worldPosition, BoundingBox.fromCorners(startPosition, trackEnd));
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

    @Nullable
    public Direction getAssemblyDirection() {
        if (assemblyDirection != null)
            return assemblyDirection;
        if (!edgePoint.hasValidTrack())
            return null;
        BlockPos targetPosition = edgePoint.getGlobalPosition();
        BlockState trackState = edgePoint.getTrackBlockState();
        ITrackBlock track = edgePoint.getTrack();
        AxisDirection axisDirection = edgePoint.getTargetDirection();
        Vec3 axis = track.getTrackAxes(level, targetPosition, trackState).get(0).normalize().scale(axisDirection.getStep());
        return assemblyDirection = Direction.getApproximateNearest(axis.x, axis.y, axis.z);
    }

    @Override
    public void remove() {
        assemblyAreas.get(level).remove(worldPosition);
        super.remove();
    }

    public void assemble(UUID playerUUID) {
        refreshAssemblyInfo();

        if (bogeyLocations == null)
            return;

        if (bogeyLocations[0] != 0) {
            exception(new AssemblyException(Component.translatable("create.train_assembly.frontmost_bogey_at_station")), -1);
            return;
        }

        if (!edgePoint.hasValidTrack())
            return;

        BlockPos trackPosition = edgePoint.getGlobalPosition();
        BlockState trackState = edgePoint.getTrackBlockState();
        ITrackBlock track = edgePoint.getTrack();
        BlockPos bogeyOffset = BlockPos.containing(track.getUpNormal(level, trackPosition, trackState));

        TrackNodeLocation location = null;
        Vec3 center = Vec3.atBottomCenterOf(trackPosition).add(0, track.getElevationAtCenter(level, trackPosition, trackState), 0);
        Collection<DiscoveredLocation> ends = track.getConnected(level, trackPosition, trackState, true, null);
        Vec3 targetOffset = Vec3.atLowerCornerOf(assemblyDirection.getUnitVec3i());
        for (DiscoveredLocation end : ends)
            if (Mth.equal(0, targetOffset.distanceToSqr(end.getLocation().subtract(center).normalize())))
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
                exception(new AssemblyException(Component.translatable("create.train_assembly.bogeys_too_close", i, i + 1)), -1);
                return;
            }

            double bogeySize = bogeyTypes[i].getWheelPointSpacing();
            pointOffsets.add(loc + .5 - bogeySize / 2);
            pointOffsets.add(loc + .5 + bogeySize / 2);
            iPrevious = loc;
        }

        List<TravellingPoint> points = new ArrayList<>();
        Vec3 directionVec = Vec3.atLowerCornerOf(assemblyDirection.getUnitVec3i());
        TrackGraph graph = null;
        TrackNode secondNode = null;

        for (int j = 0; j < assemblyLength * 2 + 40; j++) {
            double i = j / 2d;
            if (points.size() == pointOffsets.size())
                break;

            TrackNodeLocation currentLocation = location;
            location = new TrackNodeLocation(location.getLocation().add(directionVec.scale(.5))).in(location.dimension);

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
                        Vec3 edgeDirection = edge.getDirection(true);
                        if (Mth.equal(edgeDirection.normalize().dot(directionVec), -1d))
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

        if (points.isEmpty()) {
            exception(new AssemblyException(Component.translatable("create.train_assembly.no_bogeys")), -1);
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
            BlockPos bogeyPosOffset = trackPosition.offset(bogeyOffset);
            BlockPos upsideDownBogeyPosOffset = trackPosition.offset(new BlockPos(bogeyOffset.getX(), bogeyOffset.getY() * -1, bogeyOffset.getZ()));

            try {
                int offset = bogeyLocations[bogeyIndex] + 1;
                boolean success = contraption.assemble(
                    level,
                    upsideDownBogeys[bogeyIndex] ? upsideDownBogeyPosOffset.relative(assemblyDirection, offset) : bogeyPosOffset.relative(
                        assemblyDirection,
                        offset
                    )
                );
                atLeastOneForwardControls |= contraption.hasForwardControls();
                contraption.setSoundQueueOffset(offset);
                if (!success) {
                    exception(new AssemblyException(Component.translatable("create.train_assembly.nothing_attached", bogeyIndex + 1)), -1);
                    return;
                }
            } catch (AssemblyException e) {
                exception(e, contraptions.size() + 1);
                return;
            }

            AbstractBogeyBlock<?> typeOfFirstBogey = bogeyTypes[bogeyIndex];
            boolean firstBogeyIsUpsideDown = upsideDownBogeys[bogeyIndex];
            BlockPos firstBogeyPos = contraption.anchor;
            AbstractBogeyBlockEntity firstBogeyBlockEntity = (AbstractBogeyBlockEntity) level.getBlockEntity(firstBogeyPos);
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
                if (bogeyIndex == bogeyCount - 1 || !secondBogeyPos.equals((upsideDownBogeys[bogeyIndex + 1] ? upsideDownBogeyPosOffset : bogeyPosOffset).relative(assemblyDirection,
                    bogeyLocations[bogeyIndex + 1] + 1
                ))) {
                    exception(new AssemblyException(Component.translatable("create.train_assembly.not_connected_in_order")), contraptions.size() + 1);
                    return;
                }
                AbstractBogeyBlockEntity secondBogeyBlockEntity = (AbstractBogeyBlockEntity) level.getBlockEntity(secondBogeyPos);
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
                exception(new AssemblyException(Component.translatable("create.train_assembly.single_bogey_carriage")), contraptions.size() + 1);
                return;
            }

            contraptions.add(contraption);
            carriages.add(new Carriage(firstBogey, secondBogey, bogeySpacing));
        }

        if (!atLeastOneForwardControls) {
            exception(new AssemblyException(Component.translatable("create.train_assembly.no_controls")), -1);
            return;
        }

        for (CarriageContraption contraption : contraptions) {
            contraption.removeBlocksFromWorld(level, BlockPos.ZERO);
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
            carriage.setContraption(level, contraption);
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
        level.getServer().getPlayerList().broadcastAll(new AddTrainPacket(train));
        clearException();

        award(AllAdvancements.TRAIN);
        if (contraptions.size() >= 6)
            award(AllAdvancements.LONG_TRAIN);
    }

    public void cancelAssembly() {
        assemblyLength = 0;
        assemblyAreas.get(level).remove(worldPosition);
        clearException();
    }

    private void clearException() {
        exception(null, -1);
    }

    private void exception(@Nullable AssemblyException exception, int carriage) {
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
    protected AABB createRenderBoundingBox() {
        return new AABB(Vec3.atLowerCornerOf(worldPosition), Vec3.atLowerCornerOf(edgePoint.getGlobalPosition())).inflate(2);
    }

    public ItemStack getAutoSchedule() {
        return depotBehaviour.getHeldItemStack();
    }

    private void applyAutoSchedule() {
        ItemStack stack = getAutoSchedule();
        if (!stack.is(AllItems.SCHEDULE))
            return;
        Schedule schedule = ScheduleItem.getSchedule(level.registryAccess(), stack);
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
        AllSoundEvents.CONFIRM.playOnServer(level, worldPosition, 1, 1);

        if (!(level instanceof ServerLevel server))
            return;

        Vec3 v = Vec3.atBottomCenterOf(worldPosition.above());
        server.sendParticles(ParticleTypes.HAPPY_VILLAGER, v.x, v.y, v.z, 8, 0.35, 0.05, 0.35, 1);
        server.sendParticles(ParticleTypes.END_ROD, v.x, v.y + .25f, v.z, 10, 0.05, 1, 0.05, 0.005f);
    }

    public boolean resolveFlagAngle() {
        if (flagYRot != -1)
            return true;

        BlockState target = edgePoint.getTrackBlockState();
        if (!(target.getBlock() instanceof ITrackBlock def))
            return false;

        Vec3 axis = null;
        BlockPos trackPos = edgePoint.getGlobalPosition();
        for (Vec3 vec3 : def.getTrackAxes(level, trackPos, target))
            axis = vec3.scale(edgePoint.getTargetDirection().getStep());
        if (axis == null)
            return false;

        Direction nearest = Direction.getApproximateNearest(axis.x, 0, axis.z);
        flagYRot = (int) (-nearest.toYRot() - 90);

        Vec3 diff = Vec3.atLowerCornerOf(trackPos.subtract(worldPosition)).multiply(1, 0, 1);
        if (diff.lengthSqr() == 0)
            return true;

        flagFlipped = diff.dot(Vec3.atLowerCornerOf(nearest.getClockWise().getUnitVec3i())) > 0;

        return true;
    }

    @Override
    public void transform(BlockEntity be, StructureTransform transform) {
        edgePoint.transform(be, transform);
    }

    // Package port integration

    public void attachPackagePort(PackagePortBlockEntity ppbe) {
        GlobalStation station = getStation();
        if (station == null || level.isClientSide())
            return;

        if (ppbe instanceof PostboxBlockEntity pbe)
            pbe.trackedGlobalStation = new WeakReference<>(station);

        GlobalPackagePort globalPackagePort = station.connectedPorts.get(ppbe.getBlockPos());

        if (globalPackagePort == null) {
            globalPackagePort = new GlobalPackagePort();
            globalPackagePort.address = ppbe.addressFilter;
            station.connectedPorts.put(ppbe.getBlockPos(), globalPackagePort);
        } else {
            globalPackagePort.restoreOfflineBuffer(ppbe.inventory);
        }
    }

    public void removePackagePort(PackagePortBlockEntity ppbe) {
        GlobalStation station = getStation();
        if (station == null)
            return;

        station.connectedPorts.remove(ppbe.getBlockPos());
    }

}
