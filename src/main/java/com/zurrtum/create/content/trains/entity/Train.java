package com.zurrtum.create.content.trains.entity;

import com.mojang.serialization.*;
import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.Create;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.catnip.codecs.stream.CatnipLargerStreamCodecs;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.logistics.filter.FilterItemStack;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlockEntity;
import com.zurrtum.create.content.trains.entity.Carriage.DimensionalCarriageEntity;
import com.zurrtum.create.content.trains.entity.TravellingPoint.IEdgePointListener;
import com.zurrtum.create.content.trains.entity.TravellingPoint.SteerDirection;
import com.zurrtum.create.content.trains.graph.*;
import com.zurrtum.create.content.trains.observer.TrackObserver;
import com.zurrtum.create.content.trains.schedule.ScheduleRuntime;
import com.zurrtum.create.content.trains.schedule.ScheduleRuntime.State;
import com.zurrtum.create.content.trains.signal.SignalBlock.SignalType;
import com.zurrtum.create.content.trains.signal.SignalBoundary;
import com.zurrtum.create.content.trains.signal.SignalEdgeGroup;
import com.zurrtum.create.content.trains.station.GlobalStation;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import com.zurrtum.create.infrastructure.packet.s2c.RemoveTrainPacket;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.FuelRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.World.ExplosionSourceType;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Train {
    public static final PacketCodec<RegistryByteBuf, Train> STREAM_CODEC = CatnipLargerStreamCodecs.composite(
        Uuids.PACKET_CODEC,
        train -> train.id,
        CatnipStreamCodecBuilders.nullable(Uuids.PACKET_CODEC),
        train -> train.owner,
        CatnipStreamCodecBuilders.list(Carriage.STREAM_CODEC),
        train -> train.carriages,
        CatnipStreamCodecBuilders.list(PacketCodecs.VAR_INT),
        train -> train.carriageSpacing,
        PacketCodecs.BOOLEAN,
        train -> train.doubleEnded,
        TextCodecs.PACKET_CODEC,
        train -> train.name,
        TrainIconType.STREAM_CODEC,
        train -> train.icon,
        PacketCodecs.INTEGER,
        train -> train.mapColorIndex,
        Train::new
    );
    public static final Random RANDOM = new Random();

    public double speed = 0;
    public double targetSpeed = 0;
    public Double speedBeforeStall = null;
    public int carriageWaitingForChunks = -1;

    public double throttle = 1;
    public boolean honk = false;

    public UUID id;
    @Nullable
    public UUID owner;
    public TrackGraph graph;
    public Navigation navigation;
    public ScheduleRuntime runtime;
    public TrainIconType icon;
    public int mapColorIndex;
    public Text name;
    public TrainStatus status;

    public boolean invalid;

    public SteerDirection manualSteer;
    public boolean manualTick;

    public UUID currentStation;
    public boolean currentlyBackwards;

    public boolean doubleEnded;
    public List<Carriage> carriages;
    public List<Integer> carriageSpacing;

    public boolean updateSignalBlocks;
    public Map<UUID, UUID> occupiedSignalBlocks;
    public Set<UUID> reservedSignalBlocks;

    public Set<UUID> occupiedObservers;
    public Map<UUID, Pair<Integer, Boolean>> cachedObserverFiltering;

    List<TrainMigration> migratingPoints;
    public int migrationCooldown;
    public boolean derailed;

    public int fuelTicks;
    public int honkTicks;

    public Boolean lowHonk;
    public int honkPitch;

    public float accumulatedSteamRelease;

    int tickOffset;
    int ticksSinceLastMailTransfer;
    double[] stress;

    // advancements
    public PlayerEntity backwardsDriver;

    private Train(
        UUID id,
        UUID owner,
        List<Carriage> carriages,
        List<Integer> carriageSpacing,
        boolean doubleEnded,
        Text name,
        TrainIconType icon,
        int mapColorIndex
    ) {
        this(id, owner, null, carriages, carriageSpacing, doubleEnded, name, icon, mapColorIndex);
    }

    public Train(
        UUID id,
        UUID owner,
        TrackGraph graph,
        List<Carriage> carriages,
        List<Integer> carriageSpacing,
        boolean doubleEnded,
        int mapColorIndex
    ) {
        this(
            id,
            owner,
            graph,
            carriages,
            carriageSpacing,
            doubleEnded,
            Text.translatable("create.train.unnamed"),
            TrainIconType.TRADITIONAL,
            mapColorIndex
        );
    }

    public Train(
        UUID id,
        UUID owner,
        TrackGraph graph,
        List<Carriage> carriages,
        List<Integer> carriageSpacing,
        boolean doubleEnded,
        Text name,
        TrainIconType icon,
        int mapColorIndex
    ) {

        this.id = id;
        this.owner = owner;
        this.graph = graph;
        this.carriages = carriages;
        this.carriageSpacing = carriageSpacing;
        this.icon = icon;
        this.mapColorIndex = mapColorIndex;
        this.stress = new double[carriageSpacing.size()];
        this.name = name;
        this.status = new TrainStatus(this);
        this.doubleEnded = doubleEnded;

        carriages.forEach(c -> c.setTrain(this));

        navigation = new Navigation(this);
        runtime = new ScheduleRuntime(this);
        migratingPoints = new ArrayList<>();
        currentStation = null;
        manualSteer = SteerDirection.NONE;
        occupiedSignalBlocks = new HashMap<>();
        reservedSignalBlocks = new HashSet<>();
        occupiedObservers = new HashSet<>();
        cachedObserverFiltering = new HashMap<>();
        tickOffset = RANDOM.nextInt(100);
    }

    public void earlyTick(World level) {
        status.tick(level);
        if (graph == null && !migratingPoints.isEmpty())
            reattachToTracks(level);
        if (graph == null) {
            addToSignalGroups(occupiedSignalBlocks.keySet());
            return;
        }

        if (updateSignalBlocks) {
            updateSignalBlocks = false;
            collectInitiallyOccupiedSignalBlocks();
        }

        addToSignalGroups(occupiedSignalBlocks.keySet());
        addToSignalGroups(reservedSignalBlocks);

        if (occupiedObservers.isEmpty())
            return;

        tickOccupiedObservers(level);
    }

    private void tickOccupiedObservers(World level) {
        int storageVersion = 0;
        for (Carriage carriage : carriages)
            storageVersion += carriage.storage.getVersion();

        for (UUID uuid : occupiedObservers) {
            TrackObserver observer = graph.getPoint(EdgePointType.OBSERVER, uuid);
            if (observer == null)
                continue;

            FilterItemStack filter = observer.getFilter();
            if (filter.isEmpty()) {
                observer.keepAlive(this);
                continue;
            }

            Pair<Integer, Boolean> cachedMatch = cachedObserverFiltering.computeIfAbsent(uuid, $ -> Pair.of(-1, false));
            boolean shouldActivate = cachedMatch.getSecond();

            if (cachedMatch.getFirst() == storageVersion) {
                if (shouldActivate)
                    observer.keepAlive(this);
                continue;
            }

            shouldActivate = false;
            for (Carriage carriage : carriages) {
                Inventory inv = carriage.storage.getAllItems();
                if (inv != null) {
                    ItemStack find = inv.count(stack -> filter.test(level, stack), 1);
                    if (!find.isEmpty()) {
                        shouldActivate = true;
                        break;
                    }
                }

                FluidInventory tank = carriage.storage.getFluids();
                if (tank != null) {
                    FluidStack find = tank.count(stack -> filter.test(level, stack), 1);
                    if (!find.isEmpty()) {
                        shouldActivate = true;
                        break;
                    }
                }
            }

            cachedObserverFiltering.put(uuid, Pair.of(storageVersion, shouldActivate));

            if (shouldActivate)
                observer.keepAlive(this);
        }
    }

    private void addToSignalGroups(Collection<UUID> groups) {
        Map<UUID, SignalEdgeGroup> groupMap = Create.RAILWAYS.signalEdgeGroups;
        for (Iterator<UUID> iterator = groups.iterator(); iterator.hasNext(); ) {
            SignalEdgeGroup signalEdgeGroup = groupMap.get(iterator.next());
            if (signalEdgeGroup == null)
                iterator.remove();
            else
                signalEdgeGroup.trains.add(this);
        }
    }

    public void tick(World level) {
        Create.RAILWAYS.markTracksDirty();

        if (graph == null) {
            carriages.forEach(c -> c.manageEntities(level));
            updateConductors();
            return;
        }

        GlobalStation currentStation = getCurrentStation();
        if (currentStation != null) {
            ticksSinceLastMailTransfer++;
            if (ticksSinceLastMailTransfer > 20) {
                currentStation.runMailTransfer();
                ticksSinceLastMailTransfer = 0;
            }
        }

        updateConductors();
        runtime.tick(level);
        navigation.tick(level);

        tickPassiveSlowdown();
        if (derailed)
            tickDerailedSlowdown();

        double distance = speed;
        Carriage previousCarriage = null;
        int carriageCount = carriages.size();
        boolean stalled = false;
        double maxStress = 0;

        if (carriageWaitingForChunks != -1)
            distance = 0;

        for (int i = 0; i < carriageCount; i++) {
            Carriage carriage = carriages.get(i);
            if (previousCarriage != null) {
                int target = carriageSpacing.get(i - 1);
                double actual = target;

                TravellingPoint leadingPoint = carriage.getLeadingPoint();
                TravellingPoint trailingPoint = previousCarriage.getTrailingPoint();

                int entries = 0;
                double total = 0;

                if (leadingPoint.node1 != null && trailingPoint.node1 != null) {
                    RegistryKey<World> d1 = leadingPoint.node1.getLocation().dimension;
                    RegistryKey<World> d2 = trailingPoint.node1.getLocation().dimension;
                    for (boolean b : Iterate.trueAndFalse) {
                        RegistryKey<World> d = b ? d1 : d2;
                        if (!b && d1.equals(d2))
                            continue;
                        if (!d1.equals(d2))
                            continue;

                        DimensionalCarriageEntity dimensional = carriage.getDimensionalIfPresent(d);
                        DimensionalCarriageEntity dimensional2 = previousCarriage.getDimensionalIfPresent(d);
                        if (dimensional == null || dimensional2 == null)
                            continue;

                        Vec3d leadingAnchor = dimensional.leadingAnchor();
                        Vec3d trailingAnchor = dimensional2.trailingAnchor();
                        if (leadingAnchor == null || trailingAnchor == null)
                            continue;

                        double distanceTo = leadingAnchor.squaredDistanceTo(trailingAnchor);
                        if (carriage.leadingBogey().isUpsideDown() != previousCarriage.trailingBogey().isUpsideDown()) {
                            distanceTo = Math.sqrt(distanceTo - 4);
                        } else {
                            distanceTo = Math.sqrt(distanceTo);
                        }
                        total += distanceTo;
                        entries++;
                    }
                }


                if (entries > 0)
                    actual = total / entries;

                stress[i - 1] = target - actual;
                maxStress = Math.max(maxStress, Math.abs(target - actual));
            }

            previousCarriage = carriage;

            if (carriage.stalled) {
                if (speedBeforeStall == null)
                    speedBeforeStall = speed;
                distance = 0;
                speed = 0;
                stalled = true;
            }
        }

        if (!stalled && speedBeforeStall != null) {
            speed = MathHelper.clamp(speedBeforeStall, -1, 1);
            speedBeforeStall = null;
        }

        // positive stress: carriages should move apart
        // negative stress: carriages should move closer

        boolean approachingStation = navigation.distanceToDestination < 5;
        double leadingModifier = approachingStation ? 0.75d : 0.5d;
        double trailingModifier = approachingStation ? 0d : 0.125d;

        boolean blocked = false;
        boolean iterateFromBack = speed < 0;

        for (int index = 0; index < carriageCount; index++) {
            int i = iterateFromBack ? carriageCount - 1 - index : index;
            double leadingStress = i == 0 ? 0 : stress[i - 1] * -(iterateFromBack ? trailingModifier : leadingModifier);
            double trailingStress = i == stress.length ? 0 : stress[i] * (iterateFromBack ? leadingModifier : trailingModifier);

            Carriage carriage = carriages.get(i);

            TravellingPoint toFollowForward = i == 0 ? null : carriages.get(i - 1).getTrailingPoint();

            TravellingPoint toFollowBackward = i == carriageCount - 1 ? null : carriages.get(i + 1).getLeadingPoint();

            double totalStress = derailed ? 0 : leadingStress + trailingStress;

            boolean first = i == 0;
            boolean last = i == carriageCount - 1;
            int carriageType = first ? last ? Carriage.BOTH : Carriage.FIRST : last ? Carriage.LAST : Carriage.MIDDLE;
            double actualDistance = carriage.travel(level, graph, distance + totalStress, toFollowForward, toFollowBackward, carriageType);
            blocked |= carriage.blocked || carriage.isOnIncompatibleTrack();

            boolean onTwoBogeys = carriage.isOnTwoBogeys();
            maxStress = Math.max(maxStress, onTwoBogeys ? carriage.bogeySpacing - carriage.getAnchorDiff() : 0);
            maxStress = Math.max(maxStress, carriage.leadingBogey().getStress());
            if (onTwoBogeys)
                maxStress = Math.max(maxStress, carriage.trailingBogey().getStress());

            if (index == 0) {
                distance = actualDistance;
                collideWithOtherTrains(level, carriage);
                backwardsDriver = null;
                if (graph == null)
                    return;
            }
        }

        if (blocked) {
            speed = 0;
            navigation.cancelNavigation();
            runtime.tick(level);
            status.endOfTrack();

        } else if (maxStress > 4) {
            speed = 0;
            navigation.cancelNavigation();
            runtime.tick(level);
            derailed = true;
            status.highStress();

        } else if (speed != 0)
            status.trackOK();

        updateNavigationTarget(level, distance);
    }

    public IEdgePointListener frontSignalListener() {
        return (distance, couple) -> {

            if (couple.getFirst() instanceof GlobalStation station) {
                if (!station.canApproachFrom(couple.getSecond().getSecond()) || navigation.destination != station)
                    return false;
                speed = 0;
                navigation.distanceToDestination = 0;
                navigation.currentPath.clear();
                arriveAt(navigation.destination);
                navigation.destination = null;
                return true;
            }

            if (couple.getFirst() instanceof TrackObserver observer) {
                occupiedObservers.add(observer.getId());
                return false;
            }

            if (!(couple.getFirst() instanceof SignalBoundary signal))
                return false;
            if (navigation.waitingForSignal != null && navigation.waitingForSignal.getFirst().equals(signal.getId())) {
                speed = 0;
                navigation.distanceToSignal = 0;
                return true;
            }

            UUID groupId = signal.getGroup(couple.getSecond().getSecond());
            SignalEdgeGroup signalEdgeGroup = Create.RAILWAYS.signalEdgeGroups.get(groupId);
            if (signalEdgeGroup == null)
                return false;

            if ((runtime.getSchedule() == null || runtime.paused) && signalEdgeGroup.isOccupiedUnless(this))
                carriages.forEach(c -> c.forEachPresentEntity(cce -> cce.getControllingPlayer()
                    .map(uuid -> cce.getWorld().getPlayerByUuid(uuid) instanceof ServerPlayerEntity player ? player : null)
                    .ifPresent(AllAdvancements.RED_SIGNAL::trigger)));

            signalEdgeGroup.reserved = signal;
            occupy(groupId, signal.id);
            return false;

        };
    }

    public void cancelStall() {
        speedBeforeStall = null;
        carriages.forEach(c -> {
            c.stalled = false;
            c.forEachPresentEntity(cce -> cce.getContraption().getActors().forEach(pair -> {
                MovementBehaviour behaviour = MovementBehaviour.REGISTRY.get(pair.getKey().state());
                if (behaviour != null)
                    behaviour.cancelStall(pair.getValue());
            }));
        });
    }

    private boolean occupy(UUID groupId, @Nullable UUID boundaryId) {
        reservedSignalBlocks.remove(groupId);
        if (boundaryId != null && occupiedSignalBlocks.containsKey(groupId))
            if (boundaryId.equals(occupiedSignalBlocks.get(groupId)))
                return false;
        return occupiedSignalBlocks.put(groupId, boundaryId) == null;
    }

    public IEdgePointListener backSignalListener() {
        return (distance, couple) -> {
            if (couple.getFirst() instanceof TrackObserver observer) {
                occupiedObservers.remove(observer.getId());
                cachedObserverFiltering.remove(observer.getId());
                return false;
            }
            if (!(couple.getFirst() instanceof SignalBoundary signal))
                return false;
            UUID groupId = signal.getGroup(couple.getSecond().getFirst());
            occupiedSignalBlocks.remove(groupId);
            return false;
        };
    }

    private void updateNavigationTarget(World level, double distance) {
        if (navigation.destination == null)
            return;

        Pair<UUID, Boolean> blockingSignal = navigation.waitingForSignal;
        boolean fullRefresh = navigation.distanceToDestination > 100 && navigation.distanceToDestination % 100 > 20;
        boolean signalRefresh = blockingSignal != null && navigation.distanceToSignal % 50 > 5;
        boolean partialRefresh = navigation.distanceToDestination < 100 && navigation.distanceToDestination % 50 > 5;

        double toSubstract = navigation.destinationBehindTrain ? -distance : distance;
        boolean navigatingManually = runtime.paused;

        navigation.distanceToDestination -= toSubstract;
        if (blockingSignal != null) {
            navigation.distanceToSignal -= toSubstract;
            signalRefresh &= navigation.distanceToSignal % 50 < 5;
        }

        fullRefresh &= navigation.distanceToDestination % 100 <= 20;
        partialRefresh &= navigation.distanceToDestination % 50 <= 5;

        if (blockingSignal != null && navigation.ticksWaitingForSignal % 100 == 50) {
            SignalBoundary signal = graph.getPoint(EdgePointType.SIGNAL, blockingSignal.getFirst());
            fullRefresh |= signal != null && signal.types.get(blockingSignal.getSecond()) == SignalType.CROSS_SIGNAL;
        }

        if (signalRefresh)
            navigation.waitingForSignal = null;
        if (!fullRefresh && !partialRefresh)
            return;
        if (!reservedSignalBlocks.isEmpty())
            return;

        if (!navigatingManually && fullRefresh) {
            DiscoveredPath preferredPath = runtime.startCurrentInstruction(level);
            if (preferredPath != null) {
                navigation.startNavigation(preferredPath);
            }
        }
    }

    private void tickDerailedSlowdown() {
        speed /= 3f;
        if (MathHelper.approximatelyEquals(speed, 0))
            speed = 0;
    }

    private void tickPassiveSlowdown() {
        if (!manualTick && navigation.destination == null && speed != 0) {
            double acceleration = acceleration();
            if (speed > 0) {
                speed = Math.max(speed - acceleration, 0);
            } else
                speed = Math.min(speed + acceleration, 0);
        }
        manualTick = false;
    }

    private void updateConductors() {
        for (Carriage carriage : carriages)
            carriage.updateConductors();
    }

    public boolean hasForwardConductor() {
        for (Carriage carriage : carriages)
            if (carriage.presentConductors.getFirst())
                return true;
        return false;
    }

    public boolean hasBackwardConductor() {
        for (Carriage carriage : carriages)
            if (carriage.presentConductors.getSecond())
                return true;
        return false;
    }

    private void collideWithOtherTrains(World level, Carriage carriage) {
        if (derailed)
            return;

        TravellingPoint trailingPoint = carriage.getTrailingPoint();
        TravellingPoint leadingPoint = carriage.getLeadingPoint();

        if (leadingPoint.node1 == null || trailingPoint.node1 == null)
            return;
        RegistryKey<World> dimension = leadingPoint.node1.getLocation().dimension;
        if (!dimension.equals(trailingPoint.node1.getLocation().dimension))
            return;

        Vec3d start = (speed < 0 ? trailingPoint : leadingPoint).getPosition(graph);
        Vec3d end = (speed < 0 ? leadingPoint : trailingPoint).getPosition(graph);

        Pair<Train, Vec3d> collision = findCollidingTrain(level, start, end, dimension);
        if (collision == null)
            return;

        Train train = collision.getFirst();

        double combinedSpeed = Math.abs(speed) + Math.abs(train.speed);
        if (combinedSpeed > .2f) {
            Vec3d v = collision.getSecond();
            level.createExplosion(null, v.x, v.y, v.z, (float) Math.min(3 * combinedSpeed, 5), ExplosionSourceType.NONE);
        }

        crash();
        train.crash();
    }

    public Pair<Train, Vec3d> findCollidingTrain(World level, Vec3d start, Vec3d end, RegistryKey<World> dimension) {
        Vec3d diff = end.subtract(start);
        double maxDistanceSqr = Math.pow(AllConfigs.server().trains.maxAssemblyLength.get(), 2.0);

        Trains:
        for (Train train : Create.RAILWAYS.sided(level).trains.values()) {
            if (train == this)
                continue;
            if (train.graph != null && train.graph != graph)
                continue;

            Vec3d lastPoint = null;

            for (Carriage otherCarriage : train.carriages) {
                for (boolean betweenBits : Iterate.trueAndFalse) {
                    if (betweenBits && lastPoint == null)
                        continue;

                    TravellingPoint otherLeading = otherCarriage.getLeadingPoint();
                    TravellingPoint otherTrailing = otherCarriage.getTrailingPoint();
                    if (otherLeading.edge == null || otherTrailing.edge == null)
                        continue;
                    RegistryKey<World> otherDimension = otherLeading.node1.getLocation().dimension;
                    if (!otherDimension.equals(otherTrailing.node1.getLocation().dimension))
                        continue;
                    if (!otherDimension.equals(dimension))
                        continue;

                    Vec3d start2 = otherLeading.getPosition(train.graph);
                    Vec3d end2 = otherTrailing.getPosition(train.graph);

                    if (Math.min(start2.squaredDistanceTo(start), end2.squaredDistanceTo(start)) > maxDistanceSqr)
                        continue Trains;

                    if (betweenBits) {
                        end2 = start2;
                        start2 = lastPoint;
                    }

                    lastPoint = end2;

                    if ((end.y < end2.y - 3 || end2.y < end.y - 3) && (start.y < start2.y - 3 || start2.y < start.y - 3))
                        continue;

                    Vec3d diff2 = end2.subtract(start2);
                    Vec3d normedDiff = diff.normalize();
                    Vec3d normedDiff2 = diff2.normalize();
                    double[] intersect = VecHelper.intersect(start, start2, normedDiff, normedDiff2, Axis.Y);

                    if (intersect == null) {
                        Vec3d intersectSphere = VecHelper.intersectSphere(start2, normedDiff2, start, .125f);
                        if (intersectSphere == null)
                            continue;
                        if (!MathHelper.approximatelyEquals(normedDiff2.dotProduct(intersectSphere.subtract(start2).normalize()), 1))
                            continue;
                        intersect = new double[2];
                        intersect[0] = intersectSphere.distanceTo(start) - .125;
                        intersect[1] = intersectSphere.distanceTo(start2) - .125;
                    }

                    if (intersect[0] > diff.length())
                        continue;
                    if (intersect[1] > diff2.length())
                        continue;
                    if (intersect[0] < 0)
                        continue;
                    if (intersect[1] < 0)
                        continue;

                    return Pair.of(train, start.add(normedDiff.multiply(intersect[0])));
                }
            }
        }
        return null;
    }

    public void crash() {
        navigation.cancelNavigation();
        if (derailed)
            return;
        speed = -MathHelper.clamp(speed, -.5, .5);
        derailed = true;
        graph = null;
        status.crash();

        for (Carriage carriage : carriages)
            carriage.forEachPresentEntity(e -> e.getPassengersDeep().forEach(entity -> {
                if (!(entity instanceof ServerPlayerEntity p))
                    return;
                Optional<UUID> controllingPlayer = e.getControllingPlayer();
                if (controllingPlayer.isPresent() && controllingPlayer.get().equals(p.getUuid()))
                    return;
                AllAdvancements.TRAIN_CRASH.trigger(p);
            }));

        if (backwardsDriver != null)
            AllAdvancements.TRAIN_CRASH_BACKWARDS.trigger((ServerPlayerEntity) backwardsDriver);
    }

    public boolean disassemble(ServerPlayerEntity sender, Direction assemblyDirection, BlockPos pos) {
        if (!canDisassemble())
            return false;

        int offset = 1;
        boolean backwards = currentlyBackwards;
        World level = null;

        for (int i = 0; i < carriages.size(); i++) {

            Carriage carriage = carriages.get(backwards ? carriages.size() - i - 1 : i);
            CarriageContraptionEntity entity = carriage.anyAvailableEntity();
            if (entity == null)
                return false;
            level = entity.getWorld();

            if (entity.getContraption() instanceof CarriageContraption cc)
                cc.returnStorageForDisassembly(carriage.storage);
            entity.setPosition(Vec3d.of(pos.offset(assemblyDirection, backwards ? offset + carriage.bogeySpacing : offset)
                .down(carriage.leadingBogey().isUpsideDown() ? 2 : 0)));
            entity.disassemble();

            for (CarriageBogey bogey : carriage.bogeys) {
                if (bogey == null)
                    continue;
                Vec3d bogeyPosition = bogey.getAnchorPosition();
                if (bogeyPosition == null)
                    continue;
                BlockEntity be = level.getBlockEntity(BlockPos.ofFloored(bogeyPosition));
                if (!(be instanceof AbstractBogeyBlockEntity sbbe))
                    continue;
                sbbe.setBogeyData(bogey.bogeyData);
            }

            offset += carriage.bogeySpacing;

            if (i < carriageSpacing.size())
                offset += carriageSpacing.get(backwards ? carriageSpacing.size() - i - 1 : i);
        }

        GlobalStation currentStation = getCurrentStation();
        if (currentStation != null) {
            currentStation.cancelReservation(this);
            BlockPos blockEntityPos = currentStation.getBlockEntityPos();
            if (level.getBlockEntity(blockEntityPos) instanceof StationBlockEntity sbe) {
                sbe.lastDisassembledTrainName = name.copy();
                sbe.lastDisassembledMapColorIndex = mapColorIndex;
            }
        }

        Create.RAILWAYS.removeTrain(id);
        sender.getServer().getPlayerManager().sendToAll(new RemoveTrainPacket(this));
        return true;
    }

    public boolean canDisassemble() {
        for (Carriage carriage : carriages) {
            if (carriage.presentInMultipleDimensions())
                return false;
            CarriageContraptionEntity entity = carriage.anyAvailableEntity();
            if (entity == null)
                return false;
            if (!MathHelper.approximatelyEquals(entity.pitch, 0))
                return false;
            if (!MathHelper.approximatelyEquals(((entity.yaw % 90) + 360) % 90, 0))
                return false;
        }
        return true;
    }

    public boolean isTravellingOn(TrackNode node) {
        MutableBoolean affected = new MutableBoolean(false);
        forEachTravellingPoint(tp -> {
            if (tp.node1 == node || tp.node2 == node)
                affected.setTrue();
        });
        return affected.booleanValue();
    }

    public void detachFromTracks() {
        migratingPoints.clear();
        navigation.cancelNavigation();
        forEachTravellingPoint(tp -> migratingPoints.add(new TrainMigration(tp)));
        graph = null;
    }

    public void forEachTravellingPoint(Consumer<TravellingPoint> callback) {
        for (Carriage c : carriages) {
            c.leadingBogey().points.forEach(callback::accept);
            if (c.isOnTwoBogeys())
                c.trailingBogey().points.forEach(callback::accept);
        }
    }

    public void forEachTravellingPointBackwards(BiConsumer<TravellingPoint, Double> callback) {
        double lastWheelOffset = 0;
        for (int i = 0; i < carriages.size(); i++) {
            int index = carriages.size() - i - 1;
            Carriage carriage = carriages.get(index);
            CarriageBogey trailingBogey = carriage.trailingBogey();
            double trailSpacing = trailingBogey.type.getWheelPointSpacing();

            // trailing point
            callback.accept(trailingBogey.trailing(), i == 0 ? 0 : carriageSpacing.get(index) - lastWheelOffset - trailSpacing / 2);

            // inside 1st bogey
            callback.accept(trailingBogey.leading(), trailSpacing);

            lastWheelOffset = trailSpacing / 2;

            if (!carriage.isOnTwoBogeys())
                continue;

            CarriageBogey leadingBogey = carriage.leadingBogey();
            double leadSpacing = carriage.leadingBogey().type.getWheelPointSpacing();

            // between bogeys
            callback.accept(leadingBogey.trailing(), carriage.bogeySpacing - lastWheelOffset - leadSpacing / 2);

            // inside 2nd bogey
            callback.accept(trailingBogey.leading(), leadSpacing);

            lastWheelOffset = leadSpacing / 2;
        }
    }

    public void reattachToTracks(World level) {
        if (migrationCooldown > 0) {
            migrationCooldown--;
            return;
        }

        Set<Map.Entry<UUID, TrackGraph>> entrySet = new HashSet<>(Create.RAILWAYS.trackNetworks.entrySet());
        Map<UUID, List<TrackGraphLocation>> successfulMigrations = new HashMap<>();
        for (TrainMigration md : migratingPoints) {
            for (Iterator<Map.Entry<UUID, TrackGraph>> iterator = entrySet.iterator(); iterator.hasNext(); ) {
                Map.Entry<UUID, TrackGraph> entry = iterator.next();
                TrackGraphLocation gl = md.tryMigratingTo(entry.getValue());
                if (gl == null) {
                    iterator.remove();
                    continue;
                }
                successfulMigrations.computeIfAbsent(entry.getKey(), uuid -> new ArrayList<>()).add(gl);
            }
        }

        if (entrySet.isEmpty()) {
            migrationCooldown = 40;
            status.failedMigration();
            derailed = true;
            return;
        }

        for (Map.Entry<UUID, TrackGraph> entry : entrySet) {
            graph = entry.getValue();
            List<TrackGraphLocation> locations = successfulMigrations.get(entry.getKey());
            forEachTravellingPoint(tp -> tp.migrateTo(locations));
            migratingPoints.clear();
            if (derailed)
                status.successfulMigration();
            derailed = false;
            if (runtime.getSchedule() != null && runtime.state == State.IN_TRANSIT)
                runtime.state = State.PRE_TRANSIT;
            GlobalStation currentStation = getCurrentStation();
            if (currentStation != null)
                currentStation.reserveFor(this);
            updateSignalBlocks = true;
            migrationCooldown = 0;
            return;
        }
    }

    public int getTotalLength() {
        int length = 0;
        for (int i = 0; i < carriages.size(); i++) {
            Carriage carriage = carriages.get(i);
            if (i == 0)
                length += carriage.leadingBogey().type.getWheelPointSpacing() / 2;
            if (i == carriages.size() - 1)
                length += carriage.trailingBogey().type.getWheelPointSpacing() / 2;

            length += carriage.bogeySpacing;
            if (i < carriageSpacing.size())
                length += carriageSpacing.get(i);
        }
        return length;
    }

    public void leaveStation() {
        GlobalStation currentStation = getCurrentStation();
        if (currentStation != null)
            currentStation.trainDeparted(this);
        this.currentStation = null;
    }

    public void arriveAt(GlobalStation station) {
        setCurrentStation(station);
        reservedSignalBlocks.clear();
        runtime.destinationReached();
        station.runMailTransfer();
        ticksSinceLastMailTransfer = 0;
    }

    public void setCurrentStation(GlobalStation station) {
        currentStation = station.id;
    }

    public GlobalStation getCurrentStation() {
        if (currentStation == null)
            return null;
        if (graph == null)
            return null;
        return graph.getPoint(EdgePointType.STATION, currentStation);
    }

    @Nullable
    public LivingEntity getOwner(World level) {
        if (level.getServer() == null)
            return null;
        try {
            UUID uuid = owner;
            return uuid == null ? null : level.getServer().getPlayerManager().getPlayer(uuid);
        } catch (IllegalArgumentException illegalargumentexception) {
            return null;
        }
    }

    public void approachTargetSpeed(float accelerationMod) {
        double actualTarget = targetSpeed;
        if (MathHelper.approximatelyEquals(actualTarget, speed))
            return;
        if (manualTick)
            leaveStation();
        double acceleration = acceleration();
        if (speed < actualTarget)
            speed = Math.min(speed + acceleration * accelerationMod, actualTarget);
        else if (speed > actualTarget)
            speed = Math.max(speed - acceleration * accelerationMod, actualTarget);
    }

    public void collectInitiallyOccupiedSignalBlocks() {
        TravellingPoint trailingPoint = carriages.get(carriages.size() - 1).getTrailingPoint();
        TrackNode node1 = trailingPoint.node1;
        TrackNode node2 = trailingPoint.node2;
        TrackEdge edge = trailingPoint.edge;

        if (edge == null)
            return;

        double position = trailingPoint.position;
        EdgeData signalData = edge.getEdgeData();

        occupiedSignalBlocks.clear();
        reservedSignalBlocks.clear();
        occupiedObservers.clear();
        cachedObserverFiltering.clear();

        TravellingPoint signalScout = new TravellingPoint(node1, node2, edge, position, false);
        Map<UUID, SignalEdgeGroup> allGroups = Create.RAILWAYS.signalEdgeGroups;
        MutableObject<UUID> prevGroup = new MutableObject<>(null);

        if (signalData.hasSignalBoundaries()) {
            SignalBoundary nextBoundary = signalData.next(EdgePointType.SIGNAL, position);
            if (nextBoundary == null) {
                double d = 0;
                SignalBoundary prev = null;
                SignalBoundary current = signalData.next(EdgePointType.SIGNAL, 0);
                while (current != null) {
                    prev = current;
                    d = current.getLocationOn(edge);
                    current = signalData.next(EdgePointType.SIGNAL, d);
                }
                if (prev != null) {
                    UUID group = prev.getGroup(node2);
                    if (Create.RAILWAYS.signalEdgeGroups.containsKey(group)) {
                        occupy(group, null);
                        prevGroup.setValue(group);
                    }
                }

            } else {
                UUID group = nextBoundary.getGroup(node1);
                if (Create.RAILWAYS.signalEdgeGroups.containsKey(group)) {
                    occupy(group, null);
                    prevGroup.setValue(group);
                }
            }

        } else {
            UUID groupId = signalData.getEffectiveEdgeGroupId(graph);
            if (allGroups.containsKey(groupId)) {
                occupy(groupId, null);
                prevGroup.setValue(groupId);
            }
        }

        forEachTravellingPointBackwards((tp, d) -> {
            signalScout.travel(
                graph, d, signalScout.follow(tp), (distance, couple) -> {
                    if (couple.getFirst() instanceof TrackObserver observer) {
                        occupiedObservers.add(observer.getId());
                        return false;
                    }
                    if (!(couple.getFirst() instanceof SignalBoundary signal))
                        return false;
                    couple.getSecond().map(signal::getGroup).forEach(id -> {
                        if (!Create.RAILWAYS.signalEdgeGroups.containsKey(id))
                            return;
                        if (id.equals(prevGroup.getValue()))
                            return;
                        occupy(id, null);
                        prevGroup.setValue(id);
                    });
                    return false;
                }, signalScout.ignoreTurns()
            );
        });

    }

    public boolean shouldCarriageSyncThisTick(long gameTicks, int updateInterval) {
        return (gameTicks + tickOffset) % updateInterval == 0;
    }

    public Couple<Couple<TrackNode>> getEndpointEdges() {
        return Couple.create(carriages.get(0).getLeadingPoint(), carriages.get(carriages.size() - 1).getTrailingPoint())
            .map(tp -> Couple.create(tp.node1, tp.node2));
    }

    public static class Penalties {
        static final int STATION = 50, STATION_WITH_TRAIN = 300;
        static final int MANUAL_TRAIN = 200, IDLE_TRAIN = 700, ARRIVING_TRAIN = 50, WAITING_TRAIN = 50, ANY_TRAIN = 25, RED_SIGNAL = 25, REDSTONE_RED_SIGNAL = 400;
    }

    public int getNavigationPenalty() {
        if (manualTick)
            return Penalties.MANUAL_TRAIN;
        if (runtime.getSchedule() == null || runtime.paused)
            return Penalties.IDLE_TRAIN;
        if (navigation.waitingForSignal != null && navigation.ticksWaitingForSignal > 0)
            return Penalties.WAITING_TRAIN + Math.min(navigation.ticksWaitingForSignal / 20, 1000);
        if (navigation.destination != null && navigation.distanceToDestination < 50 || navigation.distanceToSignal < 20)
            return Penalties.ARRIVING_TRAIN;
        return Penalties.ANY_TRAIN;
    }

    public void burnFuel(World world) {
        if (fuelTicks > 0) {
            fuelTicks--;
            return;
        }

        boolean iterateFromBack = speed < 0;
        int carriageCount = carriages.size();

        FuelRegistry fuelRegistry = world.getFuelRegistry();
        for (int index = 0; index < carriageCount; index++) {
            int i = iterateFromBack ? carriageCount - 1 - index : index;
            Carriage carriage = carriages.get(i);
            Inventory fuelItems = carriage.storage.getFuelItems();
            if (fuelItems == null)
                continue;

            MutableInt burnTime = new MutableInt();
            ItemStack extract = fuelItems.extract(
                stack -> {
                    int ticks = fuelRegistry.getFuelTicks(stack);
                    if (ticks > 0) {
                        burnTime.setValue(ticks);
                        return true;
                    }
                    return false;
                }, 1
            );
            if (extract.isEmpty()) {
                continue;
            }
            fuelTicks += burnTime.getValue();
            ItemStack remainder = extract.getItem().getRecipeRemainder();
            if (!remainder.isEmpty()) {
                fuelItems.insertExist(remainder);
            }
            return;
        }
    }

    public float maxSpeed() {
        return (fuelTicks > 0 ? AllConfigs.server().trains.poweredTrainTopSpeed.getF() : AllConfigs.server().trains.trainTopSpeed.getF()) / 20;
    }

    public float maxTurnSpeed() {
        return (fuelTicks > 0 ? AllConfigs.server().trains.poweredTrainTurningTopSpeed.getF() : AllConfigs.server().trains.trainTurningTopSpeed.getF()) / 20;
    }

    public float acceleration() {
        return (fuelTicks > 0 ? AllConfigs.server().trains.poweredTrainAcceleration.getF() : AllConfigs.server().trains.trainAcceleration.getF()) / 400;
    }

    public void write(WriteView view, DimensionPalette dimensions) {
        view.put("Id", Uuids.INT_STREAM_CODEC, id);
        if (owner != null)
            view.put("Owner", Uuids.INT_STREAM_CODEC, owner);
        if (graph != null)
            view.put("Graph", Uuids.INT_STREAM_CODEC, graph.id);
        WriteView.ListView carriageList = view.getList("Carriages");
        carriages.forEach(carriage -> carriage.write(carriageList.add(), dimensions));
        view.putIntArray("CarriageSpacing", carriageSpacing.stream().mapToInt(Integer::intValue).toArray());
        view.putBoolean("DoubleEnded", doubleEnded);
        view.putDouble("Speed", speed);
        view.putDouble("Throttle", throttle);
        if (speedBeforeStall != null)
            view.put("SpeedBeforeStall", Codec.DOUBLE, speedBeforeStall);
        view.putInt("Fuel", fuelTicks);
        view.putDouble("TargetSpeed", targetSpeed);
        view.put("IconType", TrainIconType.CODEC, icon);
        view.putInt("MapColorIndex", mapColorIndex);
        view.put("Name", TextCodecs.CODEC, name);
        if (currentStation != null)
            view.put("Station", Uuids.INT_STREAM_CODEC, currentStation);
        view.putBoolean("Backwards", currentlyBackwards);
        view.putBoolean("Derailed", derailed);
        view.putBoolean("UpdateSignals", updateSignalBlocks);
        WriteView.ListView occupiedSignalBlockList = view.getList("SignalBlocks");
        occupiedSignalBlocks.forEach((id, boundary) -> {
            WriteView item = occupiedSignalBlockList.add();
            item.put("Id", Uuids.INT_STREAM_CODEC, id);
            if (boundary != null) {
                item.put("Boundary", Uuids.INT_STREAM_CODEC, boundary);
            }
        });
        view.put("ReservedSignalBlocks", CreateCodecs.UUID_SET_CODEC, reservedSignalBlocks);
        view.put("OccupiedObservers", CreateCodecs.UUID_SET_CODEC, occupiedObservers);
        WriteView.ListView migratingPointList = view.getList("MigratingPoints");
        migratingPoints.forEach(tm -> tm.write(migratingPointList.add(), dimensions));

        runtime.write(view.get("Runtime"));
        navigation.write(view.get("Navigation"), dimensions);
    }

    public static <T> DataResult<T> encode(final Train input, final DynamicOps<T> ops, final T empty, DimensionPalette dimensions) {
        RecordBuilder<T> map = ops.mapBuilder();
        map.add("Id", input.id, Uuids.INT_STREAM_CODEC);
        if (input.owner != null)
            map.add("Owner", input.owner, Uuids.INT_STREAM_CODEC);
        if (input.graph != null)
            map.add("Graph", input.graph.id, Uuids.INT_STREAM_CODEC);
        ListBuilder<T> carriageList = ops.listBuilder();
        input.carriages.forEach(carriage -> carriageList.add(Carriage.encode(carriage, ops, empty, dimensions)));
        map.add("Carriages", carriageList.build(empty));
        map.add("CarriageSpacing", ops.createIntList(input.carriageSpacing.stream().mapToInt(Integer::intValue)));
        map.add("DoubleEnded", ops.createBoolean(input.doubleEnded));
        map.add("Speed", ops.createDouble(input.speed));
        map.add("Throttle", ops.createDouble(input.throttle));
        if (input.speedBeforeStall != null)
            map.add("SpeedBeforeStall", ops.createDouble(input.speedBeforeStall));
        map.add("Fuel", ops.createInt(input.fuelTicks));
        map.add("TargetSpeed", ops.createDouble(input.targetSpeed));
        map.add("IconType", input.icon, TrainIconType.CODEC);
        map.add("MapColorIndex", ops.createInt(input.mapColorIndex));
        map.add("Name", input.name, TextCodecs.CODEC);
        if (input.currentStation != null)
            map.add("Station", input.currentStation, Uuids.INT_STREAM_CODEC);
        map.add("Backwards", ops.createBoolean(input.currentlyBackwards));
        map.add("Derailed", ops.createBoolean(input.derailed));
        map.add("UpdateSignals", ops.createBoolean(input.updateSignalBlocks));
        ListBuilder<T> occupiedSignalBlockList = ops.listBuilder();
        input.occupiedSignalBlocks.forEach((id, boundary) -> {
            RecordBuilder<T> item = ops.mapBuilder();
            item.add("Id", id, Uuids.INT_STREAM_CODEC);
            if (boundary != null) {
                item.add("Boundary", boundary, Uuids.INT_STREAM_CODEC);
            }
            occupiedSignalBlockList.add(item.build(empty));
        });
        map.add("SignalBlocks", occupiedSignalBlockList.build(empty));
        map.add("ReservedSignalBlocks", input.reservedSignalBlocks, CreateCodecs.UUID_SET_CODEC);
        map.add("OccupiedObservers", input.occupiedObservers, CreateCodecs.UUID_SET_CODEC);
        ListBuilder<T> migratingPointList = ops.listBuilder();
        input.migratingPoints.forEach(tm -> migratingPointList.add(TrainMigration.encode(tm, ops, empty, dimensions)));
        map.add("MigratingPoints", migratingPointList.build(empty));

        map.add("Runtime", ScheduleRuntime.encode(input.runtime, ops, empty));
        map.add("Navigation", Navigation.encode(input.navigation, ops, empty, dimensions));
        return map.build(empty);
    }

    public static Train read(ReadView view, Map<UUID, TrackGraph> trackNetworks, DimensionPalette dimensions) {
        UUID id = view.read("Id", Uuids.INT_STREAM_CODEC).orElse(null);
        UUID owner = view.read("Owner", Uuids.INT_STREAM_CODEC).orElse(null);
        UUID graphId = view.read("Graph", Uuids.INT_STREAM_CODEC).orElse(null);
        TrackGraph graph = graphId == null ? null : trackNetworks.get(graphId);
        List<Carriage> carriages = view.getListReadView("Carriages").stream().map(item -> Carriage.read(item, graph, dimensions))
            .collect(Collectors.toList());
        List<Integer> carriageSpacing = new ArrayList<>();
        view.getOptionalIntArray("CarriageSpacing").ifPresent(array -> {
            for (int i : array) {
                carriageSpacing.add(i);
            }
        });
        boolean doubleEnded = view.getBoolean("DoubleEnded", false);
        int mapColorIndex = view.getInt("MapColorIndex", 0);

        Train train = new Train(id, owner, graph, carriages, carriageSpacing, doubleEnded, mapColorIndex);

        train.speed = view.getDouble("Speed", 0);
        train.throttle = view.getDouble("Throttle", 0);
        view.read("SpeedBeforeStall", Codec.DOUBLE).ifPresent(value -> {
            train.speedBeforeStall = value;
        });
        train.targetSpeed = view.getDouble("TargetSpeed", 0);
        train.icon = view.read("IconType", TrainIconType.CODEC).orElseThrow();
        train.name = view.read("Name", TextCodecs.CODEC).orElse(ScreenTexts.EMPTY);
        train.currentStation = view.read("Station", Uuids.INT_STREAM_CODEC).orElse(null);
        train.currentlyBackwards = view.getBoolean("Backwards", false);
        train.derailed = view.getBoolean("Derailed", false);
        train.updateSignalBlocks = view.getBoolean("UpdateSignals", false);
        train.fuelTicks = view.getInt("Fuel", 0);

        view.getListReadView("SignalBlocks").forEach(item -> train.occupiedSignalBlocks.put(
            item.read("Id", Uuids.INT_STREAM_CODEC).orElseThrow(),
            item.read("Boundary", Uuids.INT_STREAM_CODEC).orElse(null)
        ));
        view.read("ReservedSignalBlocks", CreateCodecs.UUID_SET_CODEC).ifPresent(set -> train.reservedSignalBlocks.addAll(set));
        view.read("OccupiedObservers", CreateCodecs.UUID_SET_CODEC).ifPresent(set -> train.occupiedObservers.addAll(set));
        view.getListReadView("MigratingPoints").forEach(item -> {
            train.migratingPoints.add(TrainMigration.read(item, dimensions));
        });

        train.runtime.read(view.getReadView("Runtime"));
        train.navigation.read(view.getReadView("Navigation"), graph, dimensions);

        if (train.getCurrentStation() != null)
            train.getCurrentStation().reserveFor(train);

        return train;
    }

    public static <T> Train decode(DynamicOps<T> ops, T input, Map<UUID, TrackGraph> trackNetworks, DimensionPalette dimensions) {
        MapLike<T> map = ops.getMap(input).getOrThrow();
        UUID id = Uuids.INT_STREAM_CODEC.parse(ops, map.get("Id")).result().orElse(null);
        UUID owner = Uuids.INT_STREAM_CODEC.parse(ops, map.get("Owner")).result().orElse(null);
        UUID graphId = Uuids.INT_STREAM_CODEC.parse(ops, map.get("Graph")).result().orElse(null);
        TrackGraph graph = graphId == null ? null : trackNetworks.get(graphId);
        List<Carriage> carriages = ops.getStream(map.get("Carriages"))
            .mapOrElse(
                stream -> stream.map(item -> Carriage.decode(ops, item, graph, dimensions)).collect(Collectors.toList()),
                e -> new ArrayList<>()
            );
        List<Integer> carriageSpacing = ops.getIntStream(map.get("CarriageSpacing"))
            .mapOrElse(stream -> stream.boxed().collect(Collectors.toList()), e -> new ArrayList<>());
        boolean doubleEnded = ops.getBooleanValue(map.get("DoubleEnded")).result().orElse(false);
        int mapColorIndex = ops.getNumberValue(map.get("MapColorIndex"), 0).intValue();

        Train train = new Train(id, owner, graph, carriages, carriageSpacing, doubleEnded, mapColorIndex);

        train.speed = ops.getNumberValue(map.get("Speed"), 0).doubleValue();
        train.throttle = ops.getNumberValue(map.get("Throttle"), 0).doubleValue();
        Optional.ofNullable(map.get("SpeedBeforeStall")).ifPresent(value -> train.speedBeforeStall = ops.getNumberValue(value, 0).doubleValue());
        train.targetSpeed = ops.getNumberValue(map.get("TargetSpeed"), 0).doubleValue();
        train.icon = TrainIconType.CODEC.parse(ops, map.get("IconType")).getOrThrow();
        train.name = TextCodecs.CODEC.parse(ops, map.get("Name")).result().orElse(ScreenTexts.EMPTY);
        train.currentStation = Uuids.INT_STREAM_CODEC.parse(ops, map.get("Station")).result().orElse(null);
        train.currentlyBackwards = ops.getBooleanValue(map.get("Backwards")).result().orElse(false);
        train.derailed = ops.getBooleanValue(map.get("Derailed")).result().orElse(false);
        train.updateSignalBlocks = ops.getBooleanValue(map.get("UpdateSignals")).result().orElse(false);
        train.fuelTicks = ops.getNumberValue(map.get("Fuel"), 0).intValue();

        ops.getList(map.get("SignalBlocks")).getOrThrow().accept(item -> {
            MapLike<T> data = ops.getMap(item).getOrThrow();
            train.occupiedSignalBlocks.put(
                Uuids.INT_STREAM_CODEC.parse(ops, data.get("Id")).getOrThrow(),
                Uuids.INT_STREAM_CODEC.parse(ops, data.get("Boundary")).result().orElse(null)
            );
        });
        CreateCodecs.UUID_SET_CODEC.parse(ops, map.get("ReservedSignalBlocks")).ifSuccess(set -> train.reservedSignalBlocks.addAll(set));
        CreateCodecs.UUID_SET_CODEC.parse(ops, map.get("OccupiedObservers")).ifSuccess(set -> train.occupiedObservers.addAll(set));
        ops.getList(map.get("MigratingPoints")).getOrThrow().accept(item -> {
            train.migratingPoints.add(TrainMigration.decode(ops, item, dimensions));
        });

        train.runtime.decode(ops, map.get("Runtime"));
        train.navigation.decode(ops, map.get("Navigation"), graph, dimensions);

        if (train.getCurrentStation() != null)
            train.getCurrentStation().reserveFor(train);

        return train;
    }

    public int countPlayerPassengers() {
        AtomicInteger count = new AtomicInteger();
        for (Carriage carriage : carriages)
            carriage.forEachPresentEntity(e -> e.getPassengersDeep().forEach(p -> {
                if (p instanceof PlayerEntity)
                    count.incrementAndGet();
            }));
        return count.intValue();
    }

    public void determineHonk(World level) {
        if (lowHonk != null)
            return;
        for (Carriage carriage : carriages) {
            DimensionalCarriageEntity dimensional = carriage.getDimensionalIfPresent(level.getRegistryKey());
            if (dimensional == null)
                return;
            CarriageContraptionEntity entity = dimensional.entity.get();
            if (entity == null || !(entity.getContraption() instanceof CarriageContraption otherCC))
                break;
            Pair<Boolean, Integer> first = otherCC.soundQueue.getFirstWhistle(entity);
            if (first != null) {
                lowHonk = first.getFirst();
                honkPitch = first.getSecond();
            }
        }
    }

    public float distanceToLocationSqr(World level, Vec3d location) {
        float distance = Float.MAX_VALUE;
        for (Carriage carriage : carriages) {
            DimensionalCarriageEntity dce = carriage.getDimensionalIfPresent(level.getRegistryKey());
            if (dce == null || dce.positionAnchor == null)
                continue;
            distance = Math.min(distance, (float) dce.positionAnchor.squaredDistanceTo(location));
        }
        return distance;
    }

    public List<RegistryKey<World>> getPresentDimensions() {
        return carriages.stream().flatMap((Carriage carriage) -> carriage.getPresentDimensions().stream()).distinct().toList();
    }

    public Optional<BlockPos> getPositionInDimension(RegistryKey<World> dimension) {
        return carriages.stream().map(carriage -> carriage.getPositionInDimension(dimension)).filter(Optional::isPresent).map(Optional::get)
            .findFirst();
    }

}
