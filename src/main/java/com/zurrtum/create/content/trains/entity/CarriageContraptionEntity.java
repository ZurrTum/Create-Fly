package com.zurrtum.create.content.trains.entity;

import com.google.common.base.Strings;
import com.zurrtum.create.AllEntityTypes;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.Create;
import com.zurrtum.create.api.behaviour.EntityBehaviour;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.content.contraptions.OrientedContraptionEntity;
import com.zurrtum.create.content.contraptions.actors.trainControls.ControlsBlock;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.trains.entity.Carriage.DimensionalCarriageEntity;
import com.zurrtum.create.content.trains.entity.TravellingPoint.SteerDirection;
import com.zurrtum.create.content.trains.graph.TrackGraph;
import com.zurrtum.create.content.trains.station.GlobalStation;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.packet.s2c.ContraptionBlockChangedPacket;
import com.zurrtum.create.infrastructure.packet.s2c.TrainHUDControlUpdatePacket;
import com.zurrtum.create.infrastructure.packet.s2c.TrainPromptPacket;
import com.zurrtum.create.infrastructure.particle.CubeParticleData;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Function;

public class CarriageContraptionEntity extends OrientedContraptionEntity {

    private static final TrackedData<CarriageSyncData> CARRIAGE_DATA = DataTracker.registerData(
        CarriageContraptionEntity.class,
        AllSynchedDatas.CARRIAGE_DATA_HANDLER
    );
    private static final TrackedData<Optional<UUID>> TRACK_GRAPH = DataTracker.registerData(
        CarriageContraptionEntity.class,
        AllSynchedDatas.OPTIONAL_UUID_HANDLER
    );
    private static final TrackedData<Boolean> SCHEDULED = DataTracker.registerData(
        CarriageContraptionEntity.class,
        TrackedDataHandlerRegistry.BOOLEAN
    );
    private final Map<BehaviourType<?>, EntityBehaviour<?>> behaviours = new Reference2ObjectArrayMap<>();

    public UUID trainId;
    public int carriageIndex;

    private Carriage carriage;
    public boolean validForRender;
    public boolean movingBackwards;

    public boolean leftTickingChunks;
    public boolean firstPositionUpdate;

    private boolean arrivalSoundPlaying;
    private boolean arrivalSoundReversed;
    private int arrivalSoundTicks;

    private Vec3d serverPrevPos;

    public CarriageContraptionEntity(EntityType<? extends CarriageContraptionEntity> type, World world) {
        super(type, world);
        validForRender = false;
        firstPositionUpdate = true;
        arrivalSoundTicks = Integer.MIN_VALUE;
        derailParticleOffset = VecHelper.offsetRandomly(Vec3d.ZERO, world.random, 1.5f).multiply(1, .25f, 1);
        for (Function<Entity, EntityBehaviour<?>> factory : EntityBehaviour.REGISTRY.get(type)) {
            EntityBehaviour<?> behaviour = factory.apply(this);
            behaviours.put(behaviour.getType(), behaviour);
        }
        if (world.isClient()) {
            for (Function<Entity, EntityBehaviour<?>> factory : EntityBehaviour.CLIENT_REGISTRY.get(getType())) {
                EntityBehaviour<?> behaviour = factory.apply(this);
                behaviours.put(behaviour.getType(), behaviour);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends EntityBehaviour<?>> T getBehaviour(BehaviourType<T> type) {
        return (T) behaviours.get(type);
    }

    @Override
    public boolean isLogicalSideForUpdatingMovement() {
        return true;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(CARRIAGE_DATA, new CarriageSyncData());
        builder.add(TRACK_GRAPH, Optional.empty());
        builder.add(SCHEDULED, false);
    }

    public void syncCarriage() {
        CarriageSyncData carriageData = getCarriageData();
        if (carriageData == null)
            return;
        if (carriage == null)
            return;
        carriageData.update(this, carriage);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> key) {
        super.onTrackedDataSet(key);

        if (!getWorld().isClient)
            return;

        bindCarriage();

        if (TRACK_GRAPH.equals(key))
            updateTrackGraph();

        if (CARRIAGE_DATA.equals(key)) {
            CarriageSyncData carriageData = getCarriageData();
            if (carriageData == null)
                return;
            if (carriage == null)
                return;
            carriageData.apply(this, carriage);
        }
    }

    public CarriageSyncData getCarriageData() {
        return dataTracker.get(CARRIAGE_DATA);
    }

    public boolean hasSchedule() {
        return dataTracker.get(SCHEDULED);
    }

    public void setServerSidePrevPosition() {
        serverPrevPos = getPos();
    }

    @Override
    public Vec3d getPrevPositionVec() {
        if (!getWorld().isClient() && serverPrevPos != null)
            return serverPrevPos;
        return super.getPrevPositionVec();
    }

    public boolean isLocalCoordWithin(BlockPos localPos, int min, int max) {
        if (!(getContraption() instanceof CarriageContraption cc))
            return false;
        Direction facing = cc.getAssemblyDirection();
        Axis axis = facing.rotateYClockwise().getAxis();
        int coord = axis.choose(localPos.getZ(), localPos.getY(), localPos.getX()) * -facing.getDirection().offset();
        return coord >= min && coord <= max;
    }

    public static CarriageContraptionEntity create(World world, CarriageContraption contraption) {
        CarriageContraptionEntity entity = new CarriageContraptionEntity(AllEntityTypes.CARRIAGE_CONTRAPTION, world);
        entity.setContraption(contraption);
        entity.setInitialOrientation(contraption.getAssemblyDirection().rotateYClockwise());
        entity.startAtInitialYaw();
        return entity;
    }

    @Override
    public void tick() {
        super.tick();

        if (contraption instanceof CarriageContraption cc)
            for (Entity entity : getPassengerList()) {
                if (entity instanceof PlayerEntity)
                    continue;
                BlockPos seatOf = cc.getSeatOf(entity.getUuid());
                if (seatOf == null)
                    continue;
                if (cc.conductorSeats.get(seatOf) == null)
                    continue;
                alignPassenger(entity);
            }
    }

    @Override
    public void setBlock(BlockPos localPos, StructureBlockInfo newInfo) {
        if (carriage == null)
            return;
        carriage.forEachPresentEntity(cce -> {
            cce.contraption.getBlocks().put(localPos, newInfo);
            ((ServerWorld) cce.getWorld()).getChunkManager()
                .sendToOtherNearbyPlayers(cce, new ContraptionBlockChangedPacket(cce.getId(), localPos, newInfo.state()));
        });
    }

    @Override
    protected void tickContraption() {
        if (nonDamageTicks > 0)
            nonDamageTicks--;
        if (!(contraption instanceof CarriageContraption cc))
            return;

        if (carriage == null) {
            if (getWorld().isClient)
                bindCarriage();
            else
                discard();
            return;
        }

        if (!Create.RAILWAYS.sided(getWorld()).trains.containsKey(carriage.train.id)) {
            discard();
            return;
        }

        tickActors();
        boolean isStalled = isStalled();
        carriage.stalled = isStalled;

        CarriageSyncData carriageData = getCarriageData();

        if (!getWorld().isClient) {

            dataTracker.set(SCHEDULED, carriage.train.runtime.getSchedule() != null);

            boolean shouldCarriageSyncThisTick = carriage.train.shouldCarriageSyncThisTick(getWorld().getTime(), getType().getTrackTickInterval());
            if (shouldCarriageSyncThisTick && carriageData.isDirty()) {
                dataTracker.set(CARRIAGE_DATA, carriageData, true);
                carriageData.setDirty(false);
            }

            Navigation navigation = carriage.train.navigation;
            if (navigation.announceArrival && Math.abs(navigation.distanceToDestination) < 60 && carriageIndex == (carriage.train.speed < 0 ? carriage.train.carriages.size() - 1 : 0)) {
                navigation.announceArrival = false;
                arrivalSoundPlaying = true;
                arrivalSoundReversed = carriage.train.speed < 0;
                arrivalSoundTicks = Integer.MIN_VALUE;
            }

            if (arrivalSoundPlaying)
                tickArrivalSound(cc);

            dataTracker.set(TRACK_GRAPH, Optional.ofNullable(carriage.train.graph).map(g -> g.id));

            getWorld().emitGameEvent(this, GameEvent.RESONATE_8, getPos());

            return;
        }

        DimensionalCarriageEntity dce = carriage.getDimensional(getWorld());
        if (age % 10 == 0)
            updateTrackGraph();

        if (!dce.pointsInitialised)
            return;

        carriageData.approach(this, carriage, 1f / getType().getTrackTickInterval());

        if (!carriage.train.derailed)
            carriage.updateContraptionAnchors();

        lastX = getX();
        lastY = getY();
        lastZ = getZ();

        dce.alignEntity(this);

        behaviours.values().forEach(EntityBehaviour::tick);

        double distanceTo = 0;
        if (!firstPositionUpdate) {
            Vec3d diff = getPos().subtract(lastX, lastY, lastZ);
            Vec3d relativeDiff = VecHelper.rotate(diff, yaw, Axis.Y);
            double signum = Math.signum(-relativeDiff.x);
            distanceTo = diff.length() * signum;
            movingBackwards = signum < 0;
        }

        carriage.bogeys.getFirst().updateAngles(this, distanceTo);
        if (carriage.isOnTwoBogeys())
            carriage.bogeys.getSecond().updateAngles(this, distanceTo);

        if (carriage.train.derailed)
            spawnDerailParticles(carriage);
        if (dce.pivot != null)
            spawnPortalParticles(dce);

        firstPositionUpdate = false;
        validForRender = true;
    }

    private void bindCarriage() {
        if (carriage != null)
            return;
        Train train = Create.RAILWAYS.sided(getWorld()).trains.get(trainId);
        if (train == null || train.carriages.size() <= carriageIndex)
            return;
        carriage = train.carriages.get(carriageIndex);
        if (carriage != null) {
            DimensionalCarriageEntity dimensional = carriage.getDimensional(getWorld());
            dimensional.entity = new WeakReference<>(this);
            dimensional.pivot = null;
            carriage.updateContraptionAnchors();
            dimensional.updateRenderedCutoff();
        }
        updateTrackGraph();
    }

    private void tickArrivalSound(CarriageContraption cc) {
        List<Carriage> carriages = carriage.train.carriages;

        if (arrivalSoundTicks == Integer.MIN_VALUE) {
            int carriageCount = carriages.size();
            Integer tick = null;

            for (int index = 0; index < carriageCount; index++) {
                int i = arrivalSoundReversed ? carriageCount - 1 - index : index;
                Carriage carriage = carriages.get(i);
                CarriageContraptionEntity entity = carriage.getDimensional(getWorld()).entity.get();
                if (entity == null || !(entity.contraption instanceof CarriageContraption otherCC))
                    break;
                tick = arrivalSoundReversed ? otherCC.soundQueue.lastTick() : otherCC.soundQueue.firstTick();
                if (tick != null)
                    break;
            }

            if (tick == null) {
                arrivalSoundPlaying = false;
                return;
            }

            arrivalSoundTicks = tick;
        }

        if (age % 2 == 0)
            return;

        boolean keepTicking = false;
        for (Carriage c : carriages) {
            CarriageContraptionEntity entity = c.getDimensional(getWorld()).entity.get();
            if (entity == null || !(entity.contraption instanceof CarriageContraption otherCC))
                continue;
            keepTicking |= otherCC.soundQueue.tick(entity, arrivalSoundTicks, arrivalSoundReversed);
        }

        if (!keepTicking) {
            arrivalSoundPlaying = false;
            return;
        }

        arrivalSoundTicks += arrivalSoundReversed ? -1 : 1;
    }

    @Override
    public void tickActors() {
        super.tickActors();
    }

    @Override
    protected boolean isActorActive(MovementContext context, MovementBehaviour actor) {
        if (!(contraption instanceof CarriageContraption cc))
            return false;
        if (!super.isActorActive(context, actor))
            return false;
        return cc.notInPortal() || getWorld().isClient();
    }

    @Override
    public void handleStallInformation(double x, double y, double z, float angle) {
    }

    Vec3d derailParticleOffset;

    private void spawnDerailParticles(Carriage carriage) {
        if (random.nextFloat() < 1 / 20f) {
            Vec3d v = getPos().add(derailParticleOffset);
            getWorld().addParticleClient(ParticleTypes.CAMPFIRE_COSY_SMOKE, v.x, v.y, v.z, 0, .04, 0);
        }
    }

    @Override
    protected void addPassenger(Entity pPassenger) {
        super.addPassenger(pPassenger);
        if (!(pPassenger instanceof PlayerEntity player))
            return;
        AllSynchedDatas.CONTRAPTION_MOUNT_LOCATION.set(player, Optional.ofNullable(player.getPos()));
    }

    public Set<BlockPos> particleSlice = new HashSet<>();
    public float particleAvgY = 0;

    private void spawnPortalParticles(DimensionalCarriageEntity dce) {
        Vec3d pivot = dce.pivot.getLocation().add(0, 1.5f, 0);
        if (particleSlice.isEmpty())
            return;

        boolean alongX = MathHelper.approximatelyEquals(pivot.x, Math.round(pivot.x));
        int extraFlip = Direction.fromHorizontalDegrees(yaw).getDirection().offset();

        Vec3d emitter = pivot.add(0, particleAvgY, 0);
        double speed = getPos().distanceTo(getPrevPositionVec());
        int size = (int) (particleSlice.size() * MathHelper.clamp(4 - speed * 4, 0, 4));

        for (BlockPos pos : particleSlice) {
            if (size != 0 && random.nextInt(size) != 0)
                continue;
            if (alongX)
                pos = new BlockPos(0, pos.getY(), pos.getX());
            Vec3d v = pivot.add(pos.getX() * extraFlip, pos.getY(), pos.getZ() * extraFlip);
            CubeParticleData data = new CubeParticleData(.25f, 0, .5f, .65f + (random.nextFloat() - .5f) * .25f, 4, false);
            Vec3d m = v.subtract(emitter).normalize().multiply(.325f);
            m = VecHelper.rotate(m, random.nextFloat() * 360, alongX ? Axis.X : Axis.Z);
            m = m.add(VecHelper.offsetRandomly(Vec3d.ZERO, random, 0.25f));
            getWorld().addParticleClient(data, v.x, v.y, v.z, m.x, m.y, m.z);
        }

    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        dataTracker.set(CARRIAGE_DATA, new CarriageSyncData());
        if (carriage != null) {
            DimensionalCarriageEntity dce = carriage.getDimensional(getWorld());
            dce.pointsInitialised = false;
            carriage.leadingBogey().couplingAnchors = Couple.create(null, null);
            carriage.trailingBogey().couplingAnchors = Couple.create(null, null);
        }
        firstPositionUpdate = true;
        behaviours.values().forEach(EntityBehaviour::destroy);
    }

    @Override
    protected void writeAdditional(WriteView view, boolean spawnPacket) {
        super.writeAdditional(view, spawnPacket);
        view.put("TrainId", Uuids.INT_STREAM_CODEC, trainId);
        view.putInt("CarriageIndex", carriageIndex);
    }

    @Override
    protected void readAdditional(ReadView view, boolean spawnPacket) {
        super.readAdditional(view, spawnPacket);
        trainId = view.read("TrainId", Uuids.INT_STREAM_CODEC).orElseThrow();
        carriageIndex = view.getInt("CarriageIndex", 0);
        if (spawnPacket) {
            lastRenderX = getX();
            lastRenderY = getY();
            lastRenderZ = getZ();
        }
    }

    @Override
    public Text getContraptionName() {
        if (carriage != null)
            return carriage.train.name;
        return super.getContraptionName();
    }

    public Couple<Boolean> checkConductors() {
        Couple<Boolean> sides = Couple.create(false, false);
        if (!(contraption instanceof CarriageContraption cc))
            return sides;

        sides.setFirst(cc.blockConductors.getFirst());
        sides.setSecond(cc.blockConductors.getSecond());

        for (Entity entity : getPassengerList()) {
            if (entity instanceof PlayerEntity)
                continue;
            BlockPos seatOf = cc.getSeatOf(entity.getUuid());
            if (seatOf == null)
                continue;
            Couple<Boolean> validSides = cc.conductorSeats.get(seatOf);
            if (validSides == null)
                continue;
            sides.setFirst(sides.getFirst() || validSides.getFirst());
            sides.setSecond(sides.getSecond() || validSides.getSecond());
        }

        return sides;
    }

    @Override
    public boolean startControlling(BlockPos controlsLocalPos, PlayerEntity player) {
        if (player == null || player.isSpectator())
            return false;
        if (carriage == null)
            return false;
        if (carriage.train.derailed)
            return false;

        Train train = carriage.train;
        if (train.runtime.getSchedule() != null && !train.runtime.paused)
            train.status.manualControls();
        train.navigation.cancelNavigation();
        train.runtime.paused = true;
        train.navigation.waitingForSignal = null;
        return true;
    }

    @Override
    public Text getDisplayName() {
        if (carriage == null)
            return Text.of("create.train");
        return carriage.train.name;
    }

    double navDistanceTotal = 0;
    int hudPacketCooldown = 0;

    @Override
    public boolean control(BlockPos controlsLocalPos, Collection<Integer> heldControls, PlayerEntity player) {
        if (carriage == null)
            return false;
        if (carriage.train.derailed)
            return false;
        if (getWorld().isClient)
            return true;
        if (player.isSpectator())
            return false;
        if (!toGlobalVector(VecHelper.getCenterOf(controlsLocalPos), 1).isInRange(player.getPos(), 8))
            return false;
        if (heldControls.contains(5))
            return false;

        StructureBlockInfo info = contraption.getBlocks().get(controlsLocalPos);
        Direction initialOrientation = getInitialOrientation().rotateYCounterclockwise();
        boolean inverted = false;
        if (info != null && info.state().contains(ControlsBlock.FACING))
            inverted = !info.state().get(ControlsBlock.FACING).equals(initialOrientation);

        if (hudPacketCooldown-- <= 0 && player instanceof ServerPlayerEntity sp) {
            sp.networkHandler.sendPacket(new TrainHUDControlUpdatePacket(carriage.train));
            hudPacketCooldown = 5;
        }

        int targetSpeed = 0;
        if (heldControls.contains(0))
            targetSpeed++;
        if (heldControls.contains(1))
            targetSpeed--;

        int targetSteer = 0;
        if (heldControls.contains(2))
            targetSteer++;
        if (heldControls.contains(3))
            targetSteer--;

        if (inverted) {
            targetSpeed *= -1;
            targetSteer *= -1;
        }

        if (targetSpeed != 0)
            carriage.train.burnFuel(getWorld());

        boolean slow = inverted ^ targetSpeed < 0;
        boolean spaceDown = heldControls.contains(4);
        GlobalStation currentStation = carriage.train.getCurrentStation();
        if (currentStation != null && spaceDown) {
            sendPrompt(player, Text.translatable("create.train.arrived_at", Text.literal(currentStation.name).withColor(0x704630)), false);
            return true;
        }

        if (carriage.train.speedBeforeStall != null && targetSpeed != 0 && Math.signum(carriage.train.speedBeforeStall) != Math.signum(targetSpeed)) {
            carriage.train.cancelStall();
        }

        if (currentStation != null && targetSpeed != 0) {
            stationMessage = false;
            sendPrompt(player, Text.translatable("create.train.departing_from", Text.literal(currentStation.name).withColor(0x704630)), false);
        }

        if (currentStation == null) {

            Navigation nav = carriage.train.navigation;
            if (nav.destination != null) {
                if (!spaceDown)
                    nav.cancelNavigation();
                if (spaceDown) {
                    double f = (nav.distanceToDestination / navDistanceTotal);
                    int progress = (int) (MathHelper.clamp(1 - ((1 - f) * (1 - f)), 0, 1) * 30);
                    boolean arrived = progress == 0;
                    MutableText whiteComponent = Text.literal(Strings.repeat("|", progress));
                    MutableText greenComponent = Text.literal(Strings.repeat("|", 30 - progress));

                    int fromColor = 0x00_FFC244;
                    int toColor = 0x00_529915;

                    int mixedColor = Color.mixColors(toColor, fromColor, progress / 30f);
                    int targetColor = arrived ? toColor : 0x00_544D45;

                    MutableText component = greenComponent.withColor(mixedColor).append(whiteComponent.withColor(targetColor));
                    sendPrompt(player, component, true);
                    carriage.train.manualTick = true;
                    return true;
                }
            }

            double directedSpeed = targetSpeed != 0 ? targetSpeed : carriage.train.speed;
            GlobalStation lookAhead = nav.findNearestApproachable(!carriage.train.doubleEnded || (directedSpeed != 0 ? directedSpeed > 0 : !inverted));

            if (lookAhead != null) {
                if (spaceDown) {
                    carriage.train.manualTick = true;
                    nav.startNavigation(nav.findPathTo(lookAhead, -1));
                    carriage.train.manualTick = false;
                    navDistanceTotal = nav.distanceToDestination;
                    return true;
                }
                displayApproachStationMessage(player, lookAhead);
            } else
                cleanUpApproachStationMessage(player);
        }

        carriage.train.manualSteer = targetSteer < 0 ? SteerDirection.RIGHT : targetSteer > 0 ? SteerDirection.LEFT : SteerDirection.NONE;

        double topSpeed = carriage.train.maxSpeed() * AllConfigs.server().trains.manualTrainSpeedModifier.getF();
        double cappedTopSpeed = topSpeed * carriage.train.throttle;

        if (carriage.getLeadingPoint().edge != null && carriage.getLeadingPoint().edge.isTurn() || carriage.getTrailingPoint().edge != null && carriage.getTrailingPoint().edge.isTurn())
            topSpeed = carriage.train.maxTurnSpeed();

        if (slow)
            topSpeed /= 4;
        carriage.train.targetSpeed = Math.min(topSpeed, cappedTopSpeed) * targetSpeed;

        boolean counteringAcceleration = Math.abs(Math.signum(targetSpeed) - Math.signum(carriage.train.speed)) > 1.5f;

        if (slow && !counteringAcceleration)
            carriage.train.backwardsDriver = player;

        carriage.train.manualTick = true;
        carriage.train.approachTargetSpeed(counteringAcceleration ? 2 : 1);
        return true;
    }

    private void sendPrompt(PlayerEntity player, MutableText component, boolean shadow) {
        if (player instanceof ServerPlayerEntity sp)
            sp.networkHandler.sendPacket(new TrainPromptPacket(component, shadow));
    }

    boolean stationMessage = false;

    private void displayApproachStationMessage(PlayerEntity player, GlobalStation station) {
        sendPrompt(player, Text.translatable("create.contraption.controls.approach_station", Text.keybind("key.jump"), station.name), false);
        stationMessage = true;
    }

    private void cleanUpApproachStationMessage(PlayerEntity player) {
        if (!stationMessage)
            return;
        player.sendMessage(ScreenTexts.EMPTY, true);
        stationMessage = false;
    }

    private void updateTrackGraph() {
        if (carriage == null)
            return;
        Optional<UUID> optional = dataTracker.get(TRACK_GRAPH);
        if (optional.isEmpty()) {
            carriage.train.graph = null;
            carriage.train.derailed = true;
            return;
        }

        TrackGraph graph = Create.RAILWAYS.sided(getWorld()).trackNetworks.get(optional.get());
        if (graph == null)
            return;
        carriage.train.graph = graph;
        carriage.train.derailed = false;
    }

    @Override
    public boolean shouldSave() {
        return false;
    }

    public Carriage getCarriage() {
        return carriage;
    }

    public void setCarriage(Carriage carriage) {
        this.carriage = carriage;
        this.trainId = carriage.train.id;
        this.carriageIndex = carriage.train.carriages.indexOf(carriage);
        if (contraption instanceof CarriageContraption cc)
            cc.swapStorageAfterAssembly(this);
        if (carriage.train.graph != null)
            dataTracker.set(TRACK_GRAPH, Optional.of(carriage.train.graph.id));

        DimensionalCarriageEntity dimensional = carriage.getDimensional(getWorld());
        dimensional.pivot = null;
        carriage.updateContraptionAnchors();
        dimensional.updateRenderedCutoff();
    }

    public void updateRenderedPortalCutoff() {
        if (carriage == null)
            return;

        // update portal slice
        particleSlice.clear();
        particleAvgY = 0;

        if (contraption instanceof CarriageContraption cc) {
            Direction forward = cc.getAssemblyDirection().rotateYClockwise();
            Axis axis = forward.getAxis();
            boolean x = axis == Axis.X;
            boolean flip = true;

            for (BlockPos pos : contraption.getBlocks().keySet()) {
                if (!cc.atSeam(pos))
                    continue;
                int pX = x ? pos.getX() : pos.getZ();
                pX *= forward.getDirection().offset() * (flip ? 1 : -1);
                pos = new BlockPos(pX, pos.getY(), 0);
                particleSlice.add(pos);
                particleAvgY += pos.getY();
            }

        }
        if (particleSlice.size() > 0)
            particleAvgY /= particleSlice.size();
    }
}
