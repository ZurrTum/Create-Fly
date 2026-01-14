package com.zurrtum.create.content.contraptions;

import com.zurrtum.create.*;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.contraptions.actors.psi.PortableStorageInterfaceMovement;
import com.zurrtum.create.content.contraptions.actors.seat.SeatBlock;
import com.zurrtum.create.content.contraptions.actors.seat.SeatEntity;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.contraptions.elevator.ElevatorContraption;
import com.zurrtum.create.content.contraptions.glue.SuperGlueEntity;
import com.zurrtum.create.content.contraptions.mounted.MountedContraption;
import com.zurrtum.create.content.trains.entity.CarriageContraption;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.foundation.collision.Matrix3d;
import com.zurrtum.create.infrastructure.packet.s2c.*;
import io.netty.handler.codec.DecoderException;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.text.Text;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class AbstractContraptionEntity extends Entity {

    private static final TrackedData<Boolean> STALLED = DataTracker.registerData(AbstractContraptionEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Optional<UUID>> CONTROLLED_BY = DataTracker.registerData(
        AbstractContraptionEntity.class,
        AllSynchedDatas.OPTIONAL_UUID_HANDLER
    );

    public final Map<Entity, MutableInt> collidingEntities;

    protected Contraption contraption;
    protected boolean initialized;
    protected boolean prevPosInvalid;
    private boolean skipActorStop;

    /*
     * staleTicks are a band-aid to prevent a frame or two of missing blocks between
     * contraption discard and off-thread block placement on disassembly
     *
     * FIXME this timeout should be longer but then also cancelled early based on a
     * chunk rebuild listener
     */
    public int staleTicks = 3;

    public AbstractContraptionEntity(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
        prevPosInvalid = true;
        collidingEntities = new IdentityHashMap<>();
    }

    protected void setContraption(Contraption contraption) {
        this.contraption = contraption;
        if (contraption == null)
            return;
        if (getEntityWorld().isClient())
            return;
        contraption.onEntityCreated(this);
    }

    @Override
    public void move(MovementType pType, Vec3d pPos) {
        if (pType == MovementType.SHULKER)
            return;
        if (pType == MovementType.SHULKER_BOX)
            return;
        if (pType == MovementType.PISTON)
            return;
        super.move(pType, pPos);
    }

    public boolean supportsTerrainCollision() {
        return contraption instanceof TranslatingContraption && !(contraption instanceof ElevatorContraption);
    }

    protected void contraptionInitialize() {
        contraption.onEntityInitialize(getEntityWorld(), this);
        initialized = true;
    }

    public boolean collisionEnabled() {
        return true;
    }

    public void registerColliding(Entity collidingEntity) {
        collidingEntities.put(collidingEntity, new MutableInt());
    }

    public void addSittingPassenger(Entity passenger, int seatIndex) {
        for (Entity entity : getPassengerList()) {
            BlockPos seatOf = contraption.getSeatOf(entity.getUuid());
            if (seatOf != null && seatOf.equals(contraption.getSeats().get(seatIndex))) {
                if (entity instanceof PlayerEntity)
                    return;
                if (!(passenger instanceof PlayerEntity))
                    return;
                entity.stopRiding();
            }
        }
        passenger.startRiding(this, true, true);
        if (passenger instanceof TameableEntity ta)
            ta.setInSittingPose(true);
        if (getEntityWorld().isClient())
            return;
        contraption.getSeatMapping().put(passenger.getUuid(), seatIndex);

        ((ServerChunkManager) getEntityWorld().getChunkManager()).sendToOtherNearbyPlayers(
            this,
            new ContraptionSeatMappingPacket(getId(), contraption.getSeatMapping())
        );
    }

    @Override
    protected void removePassenger(Entity passenger) {
        Vec3d transformedVector = getPassengerPosition(passenger, 1);
        super.removePassenger(passenger);
        if (passenger instanceof TameableEntity ta)
            ta.setInSittingPose(false);
        if (getEntityWorld().isClient())
            return;
        if (transformedVector != null && passenger instanceof LivingEntity entity) {
            AllSynchedDatas.CONTRAPTION_DISMOUNT_LOCATION.set(entity, Optional.of(transformedVector));
        }
        contraption.getSeatMapping().remove(passenger.getUuid());

        ((ServerChunkManager) getEntityWorld().getChunkManager()).sendToOtherNearbyPlayers(
            this,
            new ContraptionSeatMappingPacket(getId(), contraption.getSeatMapping(), passenger.getId())
        );
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity entityLiving) {
        Vec3d position = super.updatePassengerForDismount(entityLiving);
        return AllSynchedDatas.CONTRAPTION_DISMOUNT_LOCATION.get(entityLiving).map(dismount -> {
            entityLiving.setOnGround(false);
            AllSynchedDatas.CONTRAPTION_DISMOUNT_LOCATION.set(entityLiving, Optional.empty());
            if (entityLiving instanceof PlayerEntity player) {
                AllSynchedDatas.CONTRAPTION_MOUNT_LOCATION.get(player).ifPresent(mount -> {
                    AllSynchedDatas.CONTRAPTION_MOUNT_LOCATION.set(player, Optional.empty());
                    if (entityLiving instanceof ServerPlayerEntity serverPlayer && !mount.isInRange(position, 5000))
                        AllAdvancements.LONG_TRAVEL.trigger(serverPlayer);
                });
            }
            return dismount;
        }).orElse(position);
    }

    @Override
    public void updatePassengerPosition(Entity passenger, PositionUpdater callback) {
        if (!hasPassenger(passenger))
            return;
        Vec3d transformedVector = getPassengerPosition(passenger, 1);
        if (transformedVector == null)
            return;

        float offset = -1 / 8f;
        if (passenger instanceof AbstractContraptionEntity)
            offset = 0.0f;
        callback.accept(
            passenger,
            transformedVector.x,
            transformedVector.y + SeatEntity.getCustomEntitySeatOffset(passenger) + offset,
            transformedVector.z
        );
    }

    public Vec3d getPassengerPosition(Entity passenger, float partialTicks) {
        if (contraption == null)
            return null;

        UUID id = passenger.getUuid();
        if (passenger instanceof OrientedContraptionEntity) {
            BlockPos localPos = contraption.getBearingPosOf(id);
            if (localPos != null)
                return toGlobalVector(VecHelper.getCenterOf(localPos), partialTicks).add(VecHelper.getCenterOf(BlockPos.ZERO)).subtract(.5f, 1, .5f);
        }

        Box bb = passenger.getBoundingBox();
        double ySize = bb.getLengthY();
        BlockPos seat = contraption.getSeatOf(id);
        if (seat == null)
            return null;

        Vec3d transformedVector = toGlobalVector(
            Vec3d.of(seat).add(.5, -passenger.getVehicleAttachmentPos(this).y + ySize + .125 - SeatEntity.getCustomEntitySeatOffset(passenger), .5),
            partialTicks
        ).add(VecHelper.getCenterOf(BlockPos.ZERO)).subtract(0.5, ySize, 0.5);
        return transformedVector;
    }

    @Override
    protected boolean canAddPassenger(@NotNull Entity pPassenger) {
        if (pPassenger instanceof OrientedContraptionEntity)
            return true;
        return contraption.getSeatMapping().size() < contraption.getSeats().size();
    }

    public Text getContraptionName() {
        return getName();
    }

    public Optional<UUID> getControllingPlayer() {
        return dataTracker.get(CONTROLLED_BY);
    }

    public void setControllingPlayer(@Nullable LivingEntity player) {
        dataTracker.set(CONTROLLED_BY, player == null ? Optional.empty() : Optional.of(player.getUuid()));
    }

    public boolean startControlling(BlockPos controlsLocalPos, PlayerEntity player) {
        return false;
    }

    public boolean control(BlockPos controlsLocalPos, Collection<Integer> heldControls, PlayerEntity player) {
        return true;
    }

    public void stopControlling(BlockPos controlsLocalPos) {
        getControllingPlayer().map(getEntityWorld()::getPlayerByUuid).map(p -> (p instanceof ServerPlayerEntity) ? ((ServerPlayerEntity) p) : null)
            .ifPresent(p -> p.networkHandler.sendPacket(AllPackets.CONTROLS_ABORT));
        setControllingPlayer(null);
    }

    public boolean handlePlayerInteraction(PlayerEntity player, BlockPos localPos, Direction side, Hand interactionHand) {
        int indexOfSeat = contraption.getSeats().indexOf(localPos);
        if (indexOfSeat == -1 || player.getStackInHand(interactionHand).isOf(AllItems.WRENCH)) {
            if (contraption.interactors.containsKey(localPos))
                return contraption.interactors.get(localPos).handlePlayerInteraction(player, interactionHand, localPos, this);
            return contraption.storage.handlePlayerStorageInteraction(contraption, player, localPos);
        }
        if (player.hasVehicle())
            return false;

        // Eject potential existing passenger
        Entity toDismount = null;
        for (Map.Entry<UUID, Integer> entry : contraption.getSeatMapping().entrySet()) {
            if (entry.getValue() != indexOfSeat)
                continue;
            for (Entity entity : getPassengerList()) {
                if (!entry.getKey().equals(entity.getUuid()))
                    continue;
                if (entity instanceof PlayerEntity)
                    return false;
                toDismount = entity;
            }
        }

        if (toDismount != null && !getEntityWorld().isClient()) {
            Vec3d transformedVector = getPassengerPosition(toDismount, 1);
            toDismount.stopRiding();
            if (transformedVector != null)
                toDismount.requestTeleport(transformedVector.x, transformedVector.y, transformedVector.z);
        }

        if (getEntityWorld().isClient())
            return true;
        addSittingPassenger(SeatBlock.getLeashed(getEntityWorld(), player).or(player), indexOfSeat);
        return true;
    }

    public Vec3d toGlobalVector(Vec3d localVec, float partialTicks) {
        return toGlobalVector(localVec, partialTicks, false);
    }

    public Vec3d toGlobalVector(Vec3d localVec, float partialTicks, boolean prevAnchor) {
        Vec3d anchor = prevAnchor ? getPrevAnchorVec() : getAnchorVec();
        Vec3d rotationOffset = VecHelper.getCenterOf(BlockPos.ZERO);
        localVec = localVec.subtract(rotationOffset);
        localVec = applyRotation(localVec, partialTicks);
        localVec = localVec.add(rotationOffset).add(anchor);
        return localVec;
    }

    public Vec3d toLocalVector(Vec3d localVec, float partialTicks) {
        return toLocalVector(localVec, partialTicks, false);
    }

    public Vec3d toLocalVector(Vec3d globalVec, float partialTicks, boolean prevAnchor) {
        Vec3d anchor = prevAnchor ? getPrevAnchorVec() : getAnchorVec();
        Vec3d rotationOffset = VecHelper.getCenterOf(BlockPos.ZERO);
        globalVec = globalVec.subtract(anchor).subtract(rotationOffset);
        globalVec = reverseRotation(globalVec, partialTicks);
        globalVec = globalVec.add(rotationOffset);
        return globalVec;
    }

    @Override
    public void tick() {
        if (contraption == null) {
            discard();
            return;
        }

        collidingEntities.entrySet().removeIf(e -> e.getValue().incrementAndGet() > 3);

        lastX = getX();
        lastY = getY();
        lastZ = getZ();
        prevPosInvalid = false;

        if (!initialized)
            contraptionInitialize();

        contraption.tickStorage(this);
        tickContraption();
        super.tick();

        if (!(getEntityWorld() instanceof ServerWorld sl))
            return;

        for (Entity entity : getPassengerList()) {
            if (entity instanceof PlayerEntity)
                continue;
            if (entity.isPlayer())
                continue;
            if (sl.entityList.has(entity))
                continue;
            updatePassengerPosition(entity);
        }
    }

    public void alignPassenger(Entity passenger) {
        Vec3d motion = getContactPointMotion(passenger.getEyePos());
        if (MathHelper.approximatelyEquals(motion.length(), 0))
            return;
        if (passenger instanceof ArmorStandEntity)
            return;
        if (!(passenger instanceof LivingEntity living))
            return;
        float prevAngle = living.getYaw();
        float angle = AngleHelper.deg(-MathHelper.atan2(motion.x, motion.z));
        angle = AngleHelper.angleLerp(0.4f, prevAngle, angle);
        if (getEntityWorld().isClient()) {
            PositionInterpolator interpolator = living.getInterpolator();
            if (interpolator != null) {
                interpolator.data.step = 0;
            }
            living.updateTrackedHeadRotation(0, 0);
            living.setYaw(angle);
            living.setPitch(0);
            living.updateLastAngles();
            living.bodyYaw = angle;
            living.headYaw = angle;
        } else {
            living.setYaw(angle);
        }
    }

    public void setBlock(BlockPos localPos, StructureBlockInfo newInfo) {
        contraption.blocks.put(localPos, newInfo);
        ((ServerChunkManager) getEntityWorld().getChunkManager()).sendToOtherNearbyPlayers(
            this,
            new ContraptionBlockChangedPacket(getId(), localPos, newInfo.state())
        );
    }

    protected abstract void tickContraption();

    public abstract Vec3d applyRotation(Vec3d localPos, float partialTicks);

    public abstract Vec3d reverseRotation(Vec3d localPos, float partialTicks);

    public void tickActors() {
        boolean stalledPreviously = contraption.stalled;

        if (!getEntityWorld().isClient())
            contraption.stalled = false;

        skipActorStop = true;
        for (MutablePair<StructureBlockInfo, MovementContext> pair : contraption.getActors()) {
            MovementContext context = pair.right;
            StructureBlockInfo blockInfo = pair.left;
            MovementBehaviour actor = MovementBehaviour.REGISTRY.get(blockInfo.state());

            if (actor == null)
                continue;

            Vec3d oldMotion = context.motion;
            Vec3d actorPosition = toGlobalVector(VecHelper.getCenterOf(blockInfo.pos()).add(actor.getActiveAreaOffset(context)), 1);
            BlockPos gridPosition = BlockPos.ofFloored(actorPosition);
            boolean newPosVisited = !context.stall && shouldActorTrigger(context, blockInfo, actor, actorPosition, gridPosition);

            context.rotation = v -> applyRotation(v, 1);
            context.position = actorPosition;
            if (!isActorActive(context, actor) && !actor.mustTickWhileDisabled())
                continue;
            if (newPosVisited && !context.stall) {
                actor.visitNewPosition(context, gridPosition);
                if (!isAlive())
                    break;
                context.firstMovement = false;
            }
            if (!oldMotion.equals(context.motion)) {
                actor.onSpeedChanged(context, oldMotion, context.motion);
                if (!isAlive())
                    break;
            }
            actor.tick(context);
            if (!isAlive())
                break;
            contraption.stalled |= context.stall;
        }
        if (!isAlive()) {
            contraption.stop(getEntityWorld());
            return;
        }
        skipActorStop = false;

        for (Entity entity : getPassengerList()) {
            if (!(entity instanceof OrientedContraptionEntity orientedCE))
                continue;
            if (!contraption.stabilizedSubContraptions.containsKey(entity.getUuid()))
                continue;
            if (orientedCE.contraption != null && orientedCE.contraption.stalled) {
                contraption.stalled = true;
                break;
            }
        }

        if (!getEntityWorld().isClient()) {
            if (!stalledPreviously && contraption.stalled)
                onContraptionStalled();
            dataTracker.set(STALLED, contraption.stalled);
            return;
        }

        contraption.stalled = isStalled();
    }

    public void refreshPSIs() {
        for (MutablePair<StructureBlockInfo, MovementContext> pair : contraption.getActors()) {
            MovementContext context = pair.right;
            StructureBlockInfo blockInfo = pair.left;
            MovementBehaviour actor = MovementBehaviour.REGISTRY.get(blockInfo.state());
            if (actor instanceof PortableStorageInterfaceMovement && isActorActive(context, actor))
                if (context.position != null)
                    actor.visitNewPosition(context, BlockPos.ofFloored(context.position));
        }
    }

    protected boolean isActorActive(MovementContext context, MovementBehaviour actor) {
        return actor.isActive(context);
    }

    protected void onContraptionStalled() {
        ((ServerChunkManager) getEntityWorld().getChunkManager()).sendToOtherNearbyPlayers(
            this,
            new ContraptionStallPacket(getId(), getX(), getY(), getZ(), getStalledAngle())
        );
    }

    protected boolean shouldActorTrigger(
        MovementContext context,
        StructureBlockInfo blockInfo,
        MovementBehaviour actor,
        Vec3d actorPosition,
        BlockPos gridPosition
    ) {
        Vec3d previousPosition = context.position;
        if (previousPosition == null)
            return false;

        context.motion = actorPosition.subtract(previousPosition);

        if (!getEntityWorld().isClient() && context.contraption.entity instanceof CarriageContraptionEntity cce && cce.getCarriage() != null) {
            Train train = cce.getCarriage().train;
            double actualSpeed = train.speedBeforeStall != null ? train.speedBeforeStall : train.speed;
            context.motion = context.motion.normalize().multiply(Math.abs(actualSpeed));
        }

        Vec3d relativeMotion = context.motion;
        relativeMotion = reverseRotation(relativeMotion, 1);
        context.relativeMotion = relativeMotion;

        boolean ignoreMotionForFirstMovement = context.contraption instanceof CarriageContraption || actor instanceof PortableStorageInterfaceMovement;

        return !BlockPos.ofFloored(previousPosition)
            .equals(gridPosition) || (context.relativeMotion.length() > 0 || ignoreMotionForFirstMovement) && context.firstMovement;
    }

    public void move(double x, double y, double z) {
        setPosition(getX() + x, getY() + y, getZ() + z);
    }

    public Vec3d getAnchorVec() {
        return getEntityPos();
    }

    public Vec3d getPrevAnchorVec() {
        return getPrevPositionVec();
    }

    public float getYawOffset() {
        return 0;
    }

    @Override
    public void setPosition(double x, double y, double z) {
        super.setPosition(x, y, z);
        if (contraption == null)
            return;
        Box cbox = contraption.bounds;
        if (cbox == null)
            return;
        Vec3d actualVec = getAnchorVec();
        setBoundingBox(cbox.offset(actualVec));
    }

    public static float yawFromVector(Vec3d vec) {
        return (float) ((3 * Math.PI / 2 + Math.atan2(vec.z, vec.x)) / Math.PI * 180);
    }

    public static float pitchFromVector(Vec3d vec) {
        return (float) ((Math.acos(vec.y)) / Math.PI * 180);
    }

    public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
        @SuppressWarnings("unchecked") EntityType.Builder<AbstractContraptionEntity> entityBuilder = (EntityType.Builder<AbstractContraptionEntity>) builder;
        return entityBuilder.dimensions(1, 1);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(STALLED, false);
        builder.add(CONTROLLED_BY, Optional.empty());
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(getErrorReporterContext(), Create.LOGGER)) {
            NbtWriteView view = NbtWriteView.create(logging, getRegistryManager());
            writeAdditional(view, true);
            return new NbtSpawnPacket(this, entityTrackerEntry, view.getNbt());
        }
    }

    @Override
    protected final void writeCustomData(WriteView view) {
        writeAdditional(view, false);
    }

    protected void writeAdditional(WriteView view, boolean spawnPacket) {
        if (contraption != null)
            contraption.write(view.get("Contraption"), spawnPacket);
        view.putBoolean("Stalled", isStalled());
        view.putBoolean("Initialized", initialized);
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        NbtCompound nbt = ((NbtSpawnPacket) packet).getNbt();
        if (nbt == null) {
            return;
        }
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(getErrorReporterContext(), Create.LOGGER)) {
            readAdditional(NbtReadView.create(logging, getRegistryManager(), nbt), true);
        }
    }

    @Override
    protected final void readCustomData(ReadView view) {
        readAdditional(view, false);
    }

    @Nullable
    private static NbtCompound readAnySizeNbt(RegistryByteBuf buf) {
        NbtElement tag = buf.readNbt(NbtSizeTracker.ofUnlimitedBytes());
        if (tag != null && !(tag instanceof NbtCompound)) {
            throw new DecoderException("Not a compound tag: " + tag);
        } else {
            return (NbtCompound) tag;
        }
    }

    protected void readAdditional(ReadView view, boolean spawnData) {
        initialized = view.getBoolean("Initialized", false);
        view.getOptionalReadView("Contraption").ifPresent(child -> {
            contraption = Contraption.fromData(getEntityWorld(), child, spawnData);
            contraption.entity = this;
        });
        dataTracker.set(STALLED, view.getBoolean("Stalled", false));
    }

    public void disassemble() {
        if (!isAlive())
            return;
        if (contraption == null)
            return;

        StructureTransform transform = makeStructureTransform();

        contraption.stop(getEntityWorld());
        if (getEntityWorld().getChunkManager() instanceof ServerChunkManager manager) {
            manager.sendToOtherNearbyPlayers(this, new ContraptionDisassemblyPacket(this.getId(), transform));
        }

        contraption.addBlocksToWorld(getEntityWorld(), transform);
        contraption.addPassengersToWorld(getEntityWorld(), transform, getPassengerList());

        for (Entity entity : getPassengerList()) {
            if (!(entity instanceof OrientedContraptionEntity))
                continue;
            UUID id = entity.getUuid();
            if (!contraption.stabilizedSubContraptions.containsKey(id))
                continue;
            BlockPos transformed = transform.apply(contraption.stabilizedSubContraptions.get(id).getConnectedPos());
            entity.setPosition(transformed.getX(), transformed.getY(), transformed.getZ());
            ((AbstractContraptionEntity) entity).disassemble();
        }

        skipActorStop = true;
        discard();

        removeAllPassengers();
        moveCollidedEntitiesOnDisassembly(transform);
        AllSoundEvents.CONTRAPTION_DISASSEMBLE.playOnServer(getEntityWorld(), getBlockPos());
    }

    public void moveCollidedEntitiesOnDisassembly(StructureTransform transform) {
        for (Entity entity : collidingEntities.keySet()) {
            Vec3d localVec = toLocalVector(entity.getEntityPos(), 0);
            Vec3d transformed = transform.apply(localVec);
            if (getEntityWorld().isClient())
                entity.setPosition(transformed.x, transformed.y + 1 / 16f, transformed.z);
            else
                entity.requestTeleport(transformed.x, transformed.y + 1 / 16f, transformed.z);
        }
    }

    @Override
    public void remove(RemovalReason p_146834_) {
        if (!getEntityWorld().isClient() && !isRemoved() && contraption != null && !skipActorStop)
            contraption.stop(getEntityWorld());
        super.remove(p_146834_);
    }

    protected abstract StructureTransform makeStructureTransform();

    @Override
    public void kill(ServerWorld world) {
        removeAllPassengers();
        super.kill(world);
    }

    @Override
    protected void tickInVoid() {
        removeAllPassengers();
        super.tickInVoid();
    }

    @Override
    protected void onSwimmingStart() {
    }

    public Contraption getContraption() {
        return contraption;
    }

    public boolean isStalled() {
        return dataTracker.get(STALLED);
    }

    protected abstract float getStalledAngle();

    public abstract void handleStallInformation(double x, double y, double z, float angle);

    @Override
    public void writeData(WriteView view) {
        Vec3d vec = getEntityPos();
        List<Entity> passengers = getPassengerList();

        for (Entity entity : passengers) {
            // setPos has world accessing side-effects when removed == null
            entity.removalReason = RemovalReason.UNLOADED_TO_CHUNK;

            // Gather passengers into same chunk when saving
            Vec3d prevVec = entity.getEntityPos();
            entity.setPos(vec.x, prevVec.y, vec.z);

            // Super requires all passengers to not be removed in order to write them to the
            // tag
            entity.removalReason = null;
        }

        super.writeData(view);
    }

    @Override
    // Make sure nothing can move contraptions out of the way
    public void setVelocity(Vec3d motionIn) {
    }

    @Override
    public PistonBehavior getPistonBehavior() {
        return PistonBehavior.IGNORE;
    }

    public void setContraptionMotion(Vec3d vec) {
        super.setVelocity(vec);
    }

    @Override
    public boolean canHit() {
        return false;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }

    public Vec3d getPrevPositionVec() {
        return prevPosInvalid ? getEntityPos() : new Vec3d(lastX, lastY, lastZ);
    }

    public abstract ContraptionRotationState getRotationState();

    public Vec3d getContactPointMotion(Vec3d globalContactPoint) {
        if (prevPosInvalid)
            return Vec3d.ZERO;

        Vec3d contactPoint = toGlobalVector(toLocalVector(globalContactPoint, 0, true), 1, true);
        Vec3d contraptionLocalMovement = contactPoint.subtract(globalContactPoint);
        Vec3d contraptionAnchorMovement = getEntityPos().subtract(getPrevPositionVec());
        return contraptionLocalMovement.add(contraptionAnchorMovement);
    }

    @Override
    public boolean collidesWith(Entity e) {
        if (e instanceof PlayerEntity && e.isSpectator())
            return false;
        if (e.noClip)
            return false;
        if (e instanceof AbstractDecorationEntity)
            return false;
        if (e instanceof AbstractMinecartEntity)
            return !(contraption instanceof MountedContraption);
        if (e instanceof SuperGlueEntity)
            return false;
        if (e instanceof SeatEntity)
            return false;
        if (e instanceof ProjectileEntity)
            return false;
        if (e.getVehicle() != null)
            return false;

        Entity riding = this.getVehicle();
        while (riding != null) {
            if (riding == e)
                return false;
            riding = riding.getVehicle();
        }

        return e.getPistonBehavior() == PistonBehavior.NORMAL;
    }

    @Override
    public boolean hasPlayerRider() {
        return false;
    }

    public static class ContraptionRotationState {
        public static final ContraptionRotationState NONE = new ContraptionRotationState();

        public float xRotation = 0;
        public float yRotation = 0;
        public float zRotation = 0;
        public float secondYRotation = 0;

        Matrix3d matrix;

        public Matrix3d asMatrix() {
            if (matrix != null)
                return matrix;

            matrix = new Matrix3d().asIdentity();
            if (xRotation != 0)
                matrix.multiply(new Matrix3d().asXRotation(AngleHelper.rad(-xRotation)));
            if (yRotation != 0)
                matrix.multiply(new Matrix3d().asYRotation(AngleHelper.rad(-yRotation)));
            if (zRotation != 0)
                matrix.multiply(new Matrix3d().asZRotation(AngleHelper.rad(-zRotation)));
            return matrix;
        }

        public boolean hasVerticalRotation() {
            return xRotation != 0 || zRotation != 0;
        }

        public float getYawOffset() {
            return secondYRotation;
        }

    }

    @Override
    protected boolean updateWaterState() {
        /*
         * Override this with an empty method to reduce enormous calculation time when
         * contraptions are in water WARNING: THIS HAS A BUNCH OF SIDE EFFECTS! - Fluids
         * will not try to change contraption movement direction - this.inWater and
         * this.isInWater() will return unreliable data - entities riding a contraption
         * will not cause water splashes (seats are their own entity so this should be
         * fine) - fall distance is not reset when the contraption is in water -
         * this.eyesInWater and this.canSwim() will always be false - swimming state
         * will never be updated
         */
        return false;
    }

    @Override
    public void setOnFireForTicks(int ticks) {
        // Contraptions no longer catch fire
    }

    @Override
    public boolean isFireImmune() {
        return true;
    }

    // Contraptions shouldn't activate pressure plates and tripwires
    @Override
    public boolean canAvoidTraps() {
        return true;
    }

    public boolean isReadyForRender() {
        return initialized;
    }

    public boolean isAliveOrStale() {
        return isAlive() || getEntityWorld().isClient() ? staleTicks > 0 : false;
    }

    public boolean isPrevPosInvalid() {
        return prevPosInvalid;
    }
}
