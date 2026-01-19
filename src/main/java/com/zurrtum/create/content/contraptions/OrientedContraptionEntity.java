package com.zurrtum.create.content.contraptions;

import com.zurrtum.create.AllEntityTypes;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageWrapper;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.contraptions.bearing.StabilizedContraption;
import com.zurrtum.create.content.contraptions.minecart.MinecartSim2020;
import com.zurrtum.create.content.contraptions.minecart.capability.CapabilityMinecartController;
import com.zurrtum.create.content.contraptions.minecart.capability.MinecartController;
import com.zurrtum.create.content.contraptions.mounted.CartAssemblerBlockEntity.CartMovementMode;
import com.zurrtum.create.content.contraptions.mounted.MountedContraption;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Ex: Minecarts, Couplings <br>
 * Oriented Contraption Entities can rotate freely around two axes
 * simultaneously.
 */
public class OrientedContraptionEntity extends AbstractContraptionEntity {

    private static final Ingredient FUEL_ITEMS = Ingredient.of(Items.COAL, Items.CHARCOAL);

    private static final EntityDataAccessor<Optional<UUID>> COUPLING = SynchedEntityData.defineId(
        OrientedContraptionEntity.class,
        AllSynchedDatas.OPTIONAL_UUID_HANDLER
    );
    private static final EntityDataAccessor<Direction> INITIAL_ORIENTATION = SynchedEntityData.defineId(
        OrientedContraptionEntity.class,
        EntityDataSerializers.DIRECTION
    );

    protected Vec3 motionBeforeStall;
    protected boolean forceAngle;
    private boolean attachedExtraInventories;
    private boolean manuallyPlaced;

    public float prevYaw;
    public float yaw;
    public float targetYaw;

    public float prevPitch;
    public float pitch;

    public int nonDamageTicks;

    public OrientedContraptionEntity(EntityType<? extends OrientedContraptionEntity> type, Level world) {
        super(type, world);
        motionBeforeStall = Vec3.ZERO;
        attachedExtraInventories = false;
        nonDamageTicks = 10;
    }

    public static OrientedContraptionEntity create(Level world, Contraption contraption, Direction initialOrientation) {
        OrientedContraptionEntity entity = new OrientedContraptionEntity(AllEntityTypes.ORIENTED_CONTRAPTION, world);
        entity.setContraption(contraption);
        entity.setInitialOrientation(initialOrientation);
        entity.startAtInitialYaw();
        return entity;
    }

    public static OrientedContraptionEntity createAtYaw(Level world, Contraption contraption, Direction initialOrientation, float initialYaw) {
        OrientedContraptionEntity entity = create(world, contraption, initialOrientation);
        entity.startAtYaw(initialYaw);
        entity.manuallyPlaced = true;
        return entity;
    }

    public void setInitialOrientation(Direction direction) {
        entityData.set(INITIAL_ORIENTATION, direction);
    }

    public Direction getInitialOrientation() {
        return entityData.get(INITIAL_ORIENTATION);
    }

    @Override
    public float getYawOffset() {
        return getInitialYaw();
    }

    public float getInitialYaw() {
        return (isInitialOrientationPresent() ? entityData.get(INITIAL_ORIENTATION) : Direction.SOUTH).toYRot();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(COUPLING, Optional.empty());
        builder.define(INITIAL_ORIENTATION, Direction.UP);
    }

    @Override
    public ContraptionRotationState getRotationState() {
        ContraptionRotationState crs = new ContraptionRotationState();

        float yawOffset = getYawOffset();
        crs.zRotation = pitch;
        crs.yRotation = -yaw + yawOffset;

        if (pitch != 0 && yaw != 0) {
            crs.secondYRotation = -yaw;
            crs.yRotation = yawOffset;
        }

        return crs;
    }

    @Override
    public void stopRiding() {
        if (!level().isClientSide() && isAlive())
            disassemble();
        super.stopRiding();
    }

    @Override
    protected void readAdditional(ValueInput view, boolean spawnPacket) {
        super.readAdditional(view, spawnPacket);

        view.read("InitialOrientation", Direction.CODEC).ifPresent(this::setInitialOrientation);

        yaw = view.getFloatOr("Yaw", 0);
        pitch = view.getFloatOr("Pitch", 0);
        manuallyPlaced = view.getBooleanOr("Placed", false);

        float forceYaw = view.getFloatOr("ForceYaw", -1);
        if (forceYaw != -1) {
            startAtYaw(forceYaw);
        }

        view.read("CachedMotion", Vec3.CODEC).ifPresent(motion -> {
            motionBeforeStall = motion;
            if (!motionBeforeStall.equals(Vec3.ZERO))
                targetYaw = prevYaw = yaw += yawFromVector(motionBeforeStall);
            setDeltaMovement(Vec3.ZERO);
        });

        setCouplingId(view.read("OnCoupling", UUIDUtil.CODEC).orElse(null));
    }

    @Override
    protected void writeAdditional(ValueOutput view, boolean spawnPacket) {
        super.writeAdditional(view, spawnPacket);

        if (motionBeforeStall != null)
            view.store("CachedMotion", Vec3.CODEC, motionBeforeStall);

        Direction optional = entityData.get(INITIAL_ORIENTATION);
        if (optional.getAxis().isHorizontal())
            view.store("InitialOrientation", Direction.CODEC, optional);
        if (forceAngle) {
            view.putFloat("ForceYaw", yaw);
            forceAngle = false;
        }

        view.putBoolean("Placed", manuallyPlaced);
        view.putFloat("Yaw", yaw);
        view.putFloat("Pitch", pitch);

        if (getCouplingId() != null)
            view.store("OnCoupling", UUIDUtil.CODEC, getCouplingId());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (INITIAL_ORIENTATION.equals(key) && isInitialOrientationPresent() && !manuallyPlaced)
            startAtInitialYaw();
    }

    public boolean isInitialOrientationPresent() {
        return entityData.get(INITIAL_ORIENTATION).getAxis().isHorizontal();
    }

    public void startAtInitialYaw() {
        startAtYaw(getInitialYaw());
    }

    public void startAtYaw(float yaw) {
        targetYaw = this.yaw = prevYaw = yaw;
        forceAngle = true;
    }

    @Override
    public Vec3 applyRotation(Vec3 localPos, float partialTicks) {
        localPos = VecHelper.rotate(localPos, getInitialYaw(), Axis.Y);
        localPos = VecHelper.rotate(localPos, getViewXRot(partialTicks), Axis.Z);
        localPos = VecHelper.rotate(localPos, getViewYRot(partialTicks), Axis.Y);
        return localPos;
    }

    @Override
    public Vec3 reverseRotation(Vec3 localPos, float partialTicks) {
        localPos = VecHelper.rotate(localPos, -getViewYRot(partialTicks), Axis.Y);
        localPos = VecHelper.rotate(localPos, -getViewXRot(partialTicks), Axis.Z);
        localPos = VecHelper.rotate(localPos, -getInitialYaw(), Axis.Y);
        return localPos;
    }

    public float getViewYRot(float partialTicks) {
        return -(partialTicks == 1.0F ? yaw : AngleHelper.angleLerp(partialTicks, prevYaw, yaw));
    }

    public float getViewXRot(float partialTicks) {
        return partialTicks == 1.0F ? pitch : AngleHelper.angleLerp(partialTicks, prevPitch, pitch);
    }

    @Override
    protected void tickContraption() {
        if (nonDamageTicks > 0)
            nonDamageTicks--;
        Entity e = getVehicle();
        if (e == null)
            return;

        boolean rotationLock = false;
        boolean pauseWhileRotating = false;
        boolean wasStalled = isStalled();
        if (contraption instanceof MountedContraption mountedContraption) {
            rotationLock = mountedContraption.rotationMode == CartMovementMode.ROTATION_LOCKED;
            pauseWhileRotating = mountedContraption.rotationMode == CartMovementMode.ROTATE_PAUSED;
        }

        Entity riding = e;
        while (riding.getVehicle() != null && !(contraption instanceof StabilizedContraption))
            riding = riding.getVehicle();

        boolean isOnCoupling = false;
        UUID couplingId = getCouplingId();
        isOnCoupling = couplingId != null && riding instanceof AbstractMinecart;

        if (!attachedExtraInventories) {
            attachInventoriesFromRidingCarts(riding, isOnCoupling, couplingId);
            attachedExtraInventories = true;
        }

        boolean rotating = updateOrientation(rotationLock, wasStalled, riding, isOnCoupling);
        if (!rotating || !pauseWhileRotating)
            tickActors();
        boolean isStalled = isStalled();
        boolean isClient = level().isClientSide();

        boolean isUpdate = true;
        if (riding instanceof AbstractMinecart) {
            Optional<MinecartController> data = AllSynchedDatas.MINECART_CONTROLLER.get(riding);
            if (data.isPresent()) {
                if (!isClient) {
                    data.get().setStalledExternally(isStalled);
                }
                isUpdate = false;
            }
        }
        if (isUpdate) {
            if (isStalled) {
                if (!wasStalled)
                    motionBeforeStall = riding.getDeltaMovement();
                riding.setDeltaMovement(0, 0, 0);
            }
            if (wasStalled && !isStalled) {
                riding.setDeltaMovement(motionBeforeStall);
                motionBeforeStall = Vec3.ZERO;
            }
        }

        if (isClient)
            return;

        if (!isStalled()) {
            if (isOnCoupling) {
                Couple<MinecartController> coupledCarts = getCoupledCartsIfPresent();
                if (coupledCarts == null)
                    return;
                coupledCarts.map(MinecartController::cart).forEach(this::powerFurnaceCartWithFuelFromStorage);
                return;
            }
            powerFurnaceCartWithFuelFromStorage(riding);
        }
    }

    private BlockPos getCurrentRailPosition(AbstractMinecart entity) {
        int x = Mth.floor(entity.getX());
        int y = Mth.floor(entity.getY());
        int z = Mth.floor(entity.getZ());
        BlockPos pos = new BlockPos(x, y, z);
        if (entity.level().getBlockState(pos.below()).is(BlockTags.RAILS))
            pos = pos.below();
        return pos;
    }

    protected boolean updateOrientation(boolean rotationLock, boolean wasStalled, Entity riding, boolean isOnCoupling) {
        if (isOnCoupling) {
            Couple<MinecartController> coupledCarts = getCoupledCartsIfPresent();
            if (coupledCarts == null)
                return false;

            Vec3 positionVec = coupledCarts.getFirst().cart().position();
            Vec3 coupledVec = coupledCarts.getSecond().cart().position();

            double diffX = positionVec.x - coupledVec.x;
            double diffY = positionVec.y - coupledVec.y;
            double diffZ = positionVec.z - coupledVec.z;

            prevYaw = yaw;
            prevPitch = pitch;
            yaw = (float) (Mth.atan2(diffZ, diffX) * 180 / Math.PI);
            pitch = (float) (Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ)) * 180 / Math.PI);

            if (getCouplingId().equals(riding.getUUID())) {
                pitch *= -1;
                yaw += 180;
            }
            return false;
        }

        if (contraption instanceof StabilizedContraption stabilized) {
            if (!(riding instanceof OrientedContraptionEntity parent))
                return false;
            Direction facing = stabilized.getFacing();
            if (facing.getAxis().isVertical())
                return false;
            prevYaw = yaw;
            yaw = AngleHelper.wrapAngle180(getInitialYaw() - parent.getInitialYaw()) - parent.getViewYRot(1);
            return false;
        }

        prevYaw = yaw;
        if (wasStalled)
            return false;

        boolean rotating = false;
        Vec3 movementVector = riding.getDeltaMovement();
        Vec3 locationDiff = riding.position().subtract(riding.xo, riding.yo, riding.zo);
        if (!(riding instanceof AbstractMinecart))
            movementVector = locationDiff;
        Vec3 motion = movementVector.normalize();

        if (!rotationLock) {
            if (riding instanceof AbstractMinecart minecartEntity) {
                BlockPos railPosition = getCurrentRailPosition(minecartEntity);
                BlockState blockState = level().getBlockState(railPosition);
                if (blockState.getBlock() instanceof BaseRailBlock abstractRailBlock) {
                    RailShape railDirection = blockState.getValue(abstractRailBlock.getShapeProperty());
                    motion = VecHelper.project(motion, MinecartSim2020.getRailVec(railDirection));
                }
            }

            if (motion.length() > 0) {
                targetYaw = yawFromVector(motion);
                if (targetYaw < 0)
                    targetYaw += 360;
                if (yaw < 0)
                    yaw += 360;
            }

            prevYaw = yaw;
            float maxApproachSpeed = (float) (motion.length() * 12f / (Math.max(1, getBoundingBox().getXsize() / 6f)));
            float yawHint = AngleHelper.getShortestAngleDiff(yaw, yawFromVector(locationDiff));
            float approach = AngleHelper.getShortestAngleDiff(yaw, targetYaw, yawHint);
            approach = Mth.clamp(approach, -maxApproachSpeed, maxApproachSpeed);
            yaw += approach;
            if (Math.abs(AngleHelper.getShortestAngleDiff(yaw, targetYaw)) < 1f)
                yaw = targetYaw;
            else
                rotating = true;
        }
        return rotating;
    }

    protected void powerFurnaceCartWithFuelFromStorage(Entity riding) {
        if (!(riding instanceof MinecartFurnace furnaceCart))
            return;

        int fuel = furnaceCart.fuel;
        int fuelBefore = fuel;
        double pushX = furnaceCart.push.x;
        double pushZ = furnaceCart.push.z;

        int i = Mth.floor(furnaceCart.getX());
        int j = Mth.floor(furnaceCart.getY());
        int k = Mth.floor(furnaceCart.getZ());
        if (furnaceCart.level().getBlockState(new BlockPos(i, j - 1, k)).is(BlockTags.RAILS))
            --j;

        BlockPos blockpos = new BlockPos(i, j, k);
        if (level().getBlockState(blockpos).is(BlockTags.RAILS))
            if (fuel > 1)
                riding.setDeltaMovement(riding.getDeltaMovement().normalize().scale(1));
        if (fuel < 5 && contraption != null) {
            MountedItemStorageWrapper fuelItems = contraption.getStorage().getFuelItems();
            if (fuelItems != null) {
                ItemStack coal = fuelItems.extract(FUEL_ITEMS, 1);
                if (!coal.isEmpty())
                    fuel += 3600;
            }
        }

        if (fuel != fuelBefore || pushX != 0 || pushZ != 0) {
            furnaceCart.push = new Vec3(pushX, 0.0, pushZ);
            furnaceCart.fuel = fuel;
        }
    }

    @Nullable
    public Couple<MinecartController> getCoupledCartsIfPresent() {
        UUID couplingId = getCouplingId();
        if (couplingId == null)
            return null;
        MinecartController controller = CapabilityMinecartController.getIfPresent(level(), couplingId);
        if (controller == null || !controller.isPresent())
            return null;
        UUID coupledCart = controller.getCoupledCart(true);
        MinecartController coupledController = CapabilityMinecartController.getIfPresent(level(), coupledCart);
        if (coupledController == null || !coupledController.isPresent())
            return null;
        return Couple.create(controller, coupledController);
    }

    protected void attachInventoriesFromRidingCarts(Entity riding, boolean isOnCoupling, UUID couplingId) {
        if (!(contraption instanceof MountedContraption mc))
            return;
        if (!isOnCoupling) {
            mc.addExtraInventories(riding);
            return;
        }
        Couple<MinecartController> coupledCarts = getCoupledCartsIfPresent();
        if (coupledCarts == null)
            return;
        coupledCarts.map(MinecartController::cart).forEach(mc::addExtraInventories);
    }

    @Nullable
    public UUID getCouplingId() {
        return entityData.get(COUPLING).orElse(null);
    }

    public void setCouplingId(UUID id) {
        entityData.set(COUPLING, Optional.ofNullable(id));
    }

    @Override
    public Vec3 getVehicleAttachmentPoint(Entity entity) {
        return entity instanceof AbstractContraptionEntity ? Vec3.ZERO : new Vec3(0, 0.19, 0);
    }

    @Override
    public Vec3 getAnchorVec() {
        Vec3 anchorVec = super.getAnchorVec();
        return anchorVec.subtract(.5, 0, .5);
    }

    @Override
    public Vec3 getPrevAnchorVec() {
        Vec3 prevAnchorVec = super.getPrevAnchorVec();
        return prevAnchorVec.subtract(.5, 0, .5);
    }

    @Override
    protected StructureTransform makeStructureTransform() {
        BlockPos offset = BlockPos.containing(getAnchorVec().add(.5, .5, .5));
        return new StructureTransform(offset, 0, -yaw + getInitialYaw(), 0);
    }

    @Override
    protected float getStalledAngle() {
        return yaw;
    }

    @Override
    public void handleStallInformation(double x, double y, double z, float angle) {
        yaw = angle;
    }
}
