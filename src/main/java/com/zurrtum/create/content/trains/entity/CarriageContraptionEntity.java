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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Function;

public class CarriageContraptionEntity extends OrientedContraptionEntity {

    private static final EntityDataAccessor<CarriageSyncData> CARRIAGE_DATA = SynchedEntityData.defineId(
        CarriageContraptionEntity.class,
        AllSynchedDatas.CARRIAGE_DATA_HANDLER
    );
    private static final EntityDataAccessor<Optional<UUID>> TRACK_GRAPH = SynchedEntityData.defineId(
        CarriageContraptionEntity.class,
        AllSynchedDatas.OPTIONAL_UUID_HANDLER
    );
    private static final EntityDataAccessor<Boolean> SCHEDULED = SynchedEntityData.defineId(
        CarriageContraptionEntity.class,
        EntityDataSerializers.BOOLEAN
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

    private Vec3 serverPrevPos;

    public CarriageContraptionEntity(EntityType<? extends CarriageContraptionEntity> type, Level world) {
        super(type, world);
        validForRender = false;
        firstPositionUpdate = true;
        arrivalSoundTicks = Integer.MIN_VALUE;
        derailParticleOffset = VecHelper.offsetRandomly(Vec3.ZERO, world.getRandom(), 1.5f).multiply(1, .25f, 1);
        for (Function<Entity, EntityBehaviour<?>> factory : EntityBehaviour.REGISTRY.get(type)) {
            EntityBehaviour<?> behaviour = factory.apply(this);
            behaviours.put(behaviour.getType(), behaviour);
        }
        if (world.isClientSide()) {
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
    public boolean isLocalInstanceAuthoritative() {
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CARRIAGE_DATA, new CarriageSyncData());
        builder.define(TRACK_GRAPH, Optional.empty());
        builder.define(SCHEDULED, false);
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
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);

        if (!level().isClientSide())
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
        return entityData.get(CARRIAGE_DATA);
    }

    public boolean hasSchedule() {
        return entityData.get(SCHEDULED);
    }

    public void setServerSidePrevPosition() {
        serverPrevPos = position();
    }

    @Override
    public Vec3 getPrevPositionVec() {
        if (!level().isClientSide() && serverPrevPos != null)
            return serverPrevPos;
        return super.getPrevPositionVec();
    }

    public boolean isLocalCoordWithin(BlockPos localPos, int min, int max) {
        if (!(getContraption() instanceof CarriageContraption cc))
            return false;
        Direction facing = cc.getAssemblyDirection();
        Axis axis = facing.getClockWise().getAxis();
        int coord = axis.choose(localPos.getZ(), localPos.getY(), localPos.getX()) * -facing.getAxisDirection().getStep();
        return coord >= min && coord <= max;
    }

    public static CarriageContraptionEntity create(Level world, CarriageContraption contraption) {
        CarriageContraptionEntity entity = new CarriageContraptionEntity(AllEntityTypes.CARRIAGE_CONTRAPTION, world);
        entity.setContraption(contraption);
        entity.setInitialOrientation(contraption.getAssemblyDirection().getClockWise());
        entity.startAtInitialYaw();
        return entity;
    }

    @Override
    public void tick() {
        super.tick();

        if (contraption instanceof CarriageContraption cc)
            for (Entity entity : getPassengers()) {
                if (entity instanceof Player)
                    continue;
                BlockPos seatOf = cc.getSeatOf(entity.getUUID());
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
            ((ServerLevel) cce.level()).getChunkSource()
                .sendToTrackingPlayers(cce, new ContraptionBlockChangedPacket(cce.getId(), localPos, newInfo.state()));
        });
    }

    @Override
    protected void tickContraption() {
        if (nonDamageTicks > 0)
            nonDamageTicks--;
        if (!(contraption instanceof CarriageContraption cc))
            return;

        if (carriage == null) {
            if (level().isClientSide())
                bindCarriage();
            else
                discard();
            return;
        }

        if (!Create.RAILWAYS.sided(level()).trains.containsKey(carriage.train.id)) {
            discard();
            return;
        }

        tickActors();
        boolean isStalled = isStalled();
        carriage.stalled = isStalled;

        CarriageSyncData carriageData = getCarriageData();

        if (!level().isClientSide()) {

            entityData.set(SCHEDULED, carriage.train.runtime.getSchedule() != null);

            boolean shouldCarriageSyncThisTick = carriage.train.shouldCarriageSyncThisTick(level().getGameTime(), getType().updateInterval());
            if (shouldCarriageSyncThisTick && carriageData.isDirty()) {
                entityData.set(CARRIAGE_DATA, carriageData, true);
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

            entityData.set(TRACK_GRAPH, Optional.ofNullable(carriage.train.graph).map(g -> g.id));

            level().gameEvent(this, GameEvent.RESONATE_8, position());

            return;
        }

        DimensionalCarriageEntity dce = carriage.getDimensional(level());
        if (tickCount % 10 == 0)
            updateTrackGraph();

        if (!dce.pointsInitialised)
            return;

        carriageData.approach(this, carriage, 1f / getType().updateInterval());

        if (!carriage.train.derailed)
            carriage.updateContraptionAnchors();

        xo = getX();
        yo = getY();
        zo = getZ();

        dce.alignEntity(this);

        behaviours.values().forEach(EntityBehaviour::tick);

        double distanceTo = 0;
        if (!firstPositionUpdate) {
            Vec3 diff = position().subtract(xo, yo, zo);
            Vec3 relativeDiff = VecHelper.rotate(diff, yaw, Axis.Y);
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
        Train train = Create.RAILWAYS.sided(level()).trains.get(trainId);
        if (train == null || train.carriages.size() <= carriageIndex)
            return;
        carriage = train.carriages.get(carriageIndex);
        if (carriage != null) {
            DimensionalCarriageEntity dimensional = carriage.getDimensional(level());
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
                CarriageContraptionEntity entity = carriage.getDimensional(level()).entity.get();
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

        if (tickCount % 2 == 0)
            return;

        boolean keepTicking = false;
        for (Carriage c : carriages) {
            CarriageContraptionEntity entity = c.getDimensional(level()).entity.get();
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
        return cc.notInPortal() || level().isClientSide();
    }

    @Override
    public void handleStallInformation(double x, double y, double z, float angle) {
    }

    Vec3 derailParticleOffset;

    private void spawnDerailParticles(Carriage carriage) {
        if (random.nextFloat() < 1 / 20f) {
            Vec3 v = position().add(derailParticleOffset);
            level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, v.x, v.y, v.z, 0, .04, 0);
        }
    }

    @Override
    protected void addPassenger(Entity pPassenger) {
        super.addPassenger(pPassenger);
        if (!(pPassenger instanceof Player player))
            return;
        AllSynchedDatas.CONTRAPTION_MOUNT_LOCATION.set(player, Optional.ofNullable(player.position()));
    }

    public Set<BlockPos> particleSlice = new HashSet<>();
    public float particleAvgY = 0;

    private void spawnPortalParticles(DimensionalCarriageEntity dce) {
        Vec3 pivot = dce.pivot.getLocation().add(0, 1.5f, 0);
        if (particleSlice.isEmpty())
            return;

        boolean alongX = Mth.equal(pivot.x, Math.round(pivot.x));
        int extraFlip = Direction.fromYRot(yaw).getAxisDirection().getStep();

        Vec3 emitter = pivot.add(0, particleAvgY, 0);
        double speed = position().distanceTo(getPrevPositionVec());
        int size = (int) (particleSlice.size() * Mth.clamp(4 - speed * 4, 0, 4));

        for (BlockPos pos : particleSlice) {
            if (size != 0 && random.nextInt(size) != 0)
                continue;
            if (alongX)
                pos = new BlockPos(0, pos.getY(), pos.getX());
            Vec3 v = pivot.add(pos.getX() * extraFlip, pos.getY(), pos.getZ() * extraFlip);
            CubeParticleData data = new CubeParticleData(.25f, 0, .5f, .65f + (random.nextFloat() - .5f) * .25f, 4, false);
            Vec3 m = v.subtract(emitter).normalize().scale(.325f);
            m = VecHelper.rotate(m, random.nextFloat() * 360, alongX ? Axis.X : Axis.Z);
            m = m.add(VecHelper.offsetRandomly(Vec3.ZERO, random, 0.25f));
            level().addParticle(data, v.x, v.y, v.z, m.x, m.y, m.z);
        }

    }

    @Override
    public void onClientRemoval() {
        super.onClientRemoval();
        entityData.set(CARRIAGE_DATA, new CarriageSyncData());
        if (carriage != null) {
            DimensionalCarriageEntity dce = carriage.getDimensional(level());
            dce.pointsInitialised = false;
            carriage.leadingBogey().couplingAnchors = Couple.create(null, null);
            carriage.trailingBogey().couplingAnchors = Couple.create(null, null);
        }
        firstPositionUpdate = true;
        behaviours.values().forEach(EntityBehaviour::destroy);
    }

    @Override
    protected void writeAdditional(ValueOutput view, boolean spawnPacket) {
        super.writeAdditional(view, spawnPacket);
        view.store("TrainId", UUIDUtil.CODEC, trainId);
        view.putInt("CarriageIndex", carriageIndex);
    }

    @Override
    protected void readAdditional(ValueInput view, boolean spawnPacket) {
        super.readAdditional(view, spawnPacket);
        trainId = view.read("TrainId", UUIDUtil.CODEC).orElseThrow();
        carriageIndex = view.getIntOr("CarriageIndex", 0);
        if (spawnPacket) {
            xOld = getX();
            yOld = getY();
            zOld = getZ();
        }
    }

    @Override
    public Component getContraptionName() {
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

        for (Entity entity : getPassengers()) {
            if (entity instanceof Player)
                continue;
            BlockPos seatOf = cc.getSeatOf(entity.getUUID());
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
    public boolean startControlling(BlockPos controlsLocalPos, Player player) {
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
    public Component getDisplayName() {
        if (carriage == null)
            return Component.nullToEmpty("create.train");
        return carriage.train.name;
    }

    double navDistanceTotal = 0;
    int hudPacketCooldown = 0;

    @Override
    public boolean control(BlockPos controlsLocalPos, Collection<Integer> heldControls, Player player) {
        if (carriage == null)
            return false;
        if (carriage.train.derailed)
            return false;
        if (level().isClientSide())
            return true;
        if (player.isSpectator())
            return false;
        if (!toGlobalVector(VecHelper.getCenterOf(controlsLocalPos), 1).closerThan(player.position(), 8))
            return false;
        if (heldControls.contains(5))
            return false;

        StructureBlockInfo info = contraption.getBlocks().get(controlsLocalPos);
        Direction initialOrientation = getInitialOrientation().getCounterClockWise();
        boolean inverted = false;
        if (info != null && info.state().hasProperty(ControlsBlock.FACING))
            inverted = !info.state().getValue(ControlsBlock.FACING).equals(initialOrientation);

        if (hudPacketCooldown-- <= 0 && player instanceof ServerPlayer sp) {
            sp.connection.send(new TrainHUDControlUpdatePacket(carriage.train));
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
            carriage.train.burnFuel(level());

        boolean slow = inverted ^ targetSpeed < 0;
        boolean spaceDown = heldControls.contains(4);
        GlobalStation currentStation = carriage.train.getCurrentStation();
        if (currentStation != null && spaceDown) {
            sendPrompt(player, Component.translatable("create.train.arrived_at", Component.literal(currentStation.name).withColor(0x704630)), false);
            return true;
        }

        if (carriage.train.speedBeforeStall != null && targetSpeed != 0 && Math.signum(carriage.train.speedBeforeStall) != Math.signum(targetSpeed)) {
            carriage.train.cancelStall();
        }

        if (currentStation != null && targetSpeed != 0) {
            stationMessage = false;
            sendPrompt(
                player,
                Component.translatable("create.train.departing_from", Component.literal(currentStation.name).withColor(0x704630)),
                false
            );
        }

        if (currentStation == null) {

            Navigation nav = carriage.train.navigation;
            if (nav.destination != null) {
                if (!spaceDown)
                    nav.cancelNavigation();
                if (spaceDown) {
                    double f = (nav.distanceToDestination / navDistanceTotal);
                    int progress = (int) (Mth.clamp(1 - ((1 - f) * (1 - f)), 0, 1) * 30);
                    boolean arrived = progress == 0;
                    MutableComponent whiteComponent = Component.literal(Strings.repeat("|", progress));
                    MutableComponent greenComponent = Component.literal(Strings.repeat("|", 30 - progress));

                    int fromColor = 0x00_FFC244;
                    int toColor = 0x00_529915;

                    int mixedColor = Color.mixColors(toColor, fromColor, progress / 30f);
                    int targetColor = arrived ? toColor : 0x00_544D45;

                    MutableComponent component = greenComponent.withColor(mixedColor).append(whiteComponent.withColor(targetColor));
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

    private void sendPrompt(Player player, MutableComponent component, boolean shadow) {
        if (player instanceof ServerPlayer sp)
            sp.connection.send(new TrainPromptPacket(component, shadow));
    }

    boolean stationMessage = false;

    private void displayApproachStationMessage(Player player, GlobalStation station) {
        sendPrompt(
            player,
            Component.translatable("create.contraption.controls.approach_station", Component.keybind("key.jump"), station.name),
            false
        );
        stationMessage = true;
    }

    private void cleanUpApproachStationMessage(Player player) {
        if (!stationMessage)
            return;
        player.displayClientMessage(CommonComponents.EMPTY, true);
        stationMessage = false;
    }

    private void updateTrackGraph() {
        if (carriage == null)
            return;
        Optional<UUID> optional = entityData.get(TRACK_GRAPH);
        if (optional.isEmpty()) {
            carriage.train.graph = null;
            carriage.train.derailed = true;
            return;
        }

        TrackGraph graph = Create.RAILWAYS.sided(level()).trackNetworks.get(optional.get());
        if (graph == null)
            return;
        carriage.train.graph = graph;
        carriage.train.derailed = false;
    }

    @Override
    public boolean shouldBeSaved() {
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
            entityData.set(TRACK_GRAPH, Optional.of(carriage.train.graph.id));

        DimensionalCarriageEntity dimensional = carriage.getDimensional(level());
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
            Direction forward = cc.getAssemblyDirection().getClockWise();
            Axis axis = forward.getAxis();
            boolean x = axis == Axis.X;
            boolean flip = true;

            for (BlockPos pos : contraption.getBlocks().keySet()) {
                if (!cc.atSeam(pos))
                    continue;
                int pX = x ? pos.getX() : pos.getZ();
                pX *= forward.getAxisDirection().getStep() * (flip ? 1 : -1);
                pos = new BlockPos(pX, pos.getY(), 0);
                particleSlice.add(pos);
                particleAvgY += pos.getY();
            }

        }
        if (particleSlice.size() > 0)
            particleAvgY /= particleSlice.size();
    }
}
