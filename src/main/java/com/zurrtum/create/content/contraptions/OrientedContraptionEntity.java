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
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Ex: Minecarts, Couplings <br>
 * Oriented Contraption Entities can rotate freely around two axes
 * simultaneously.
 */
public class OrientedContraptionEntity extends AbstractContraptionEntity {

    private static final Ingredient FUEL_ITEMS = Ingredient.ofItems(Items.COAL, Items.CHARCOAL);

    private static final TrackedData<Optional<UUID>> COUPLING = DataTracker.registerData(
        OrientedContraptionEntity.class,
        AllSynchedDatas.OPTIONAL_UUID_HANDLER
    );
    private static final TrackedData<Direction> INITIAL_ORIENTATION = DataTracker.registerData(
        OrientedContraptionEntity.class,
        TrackedDataHandlerRegistry.FACING
    );

    protected Vec3d motionBeforeStall;
    protected boolean forceAngle;
    private boolean attachedExtraInventories;
    private boolean manuallyPlaced;

    public float prevYaw;
    public float yaw;
    public float targetYaw;

    public float prevPitch;
    public float pitch;

    public int nonDamageTicks;

    public OrientedContraptionEntity(EntityType<? extends OrientedContraptionEntity> type, World world) {
        super(type, world);
        motionBeforeStall = Vec3d.ZERO;
        attachedExtraInventories = false;
        nonDamageTicks = 10;
    }

    public static OrientedContraptionEntity create(World world, Contraption contraption, Direction initialOrientation) {
        OrientedContraptionEntity entity = new OrientedContraptionEntity(AllEntityTypes.ORIENTED_CONTRAPTION, world);
        entity.setContraption(contraption);
        entity.setInitialOrientation(initialOrientation);
        entity.startAtInitialYaw();
        return entity;
    }

    public static OrientedContraptionEntity createAtYaw(World world, Contraption contraption, Direction initialOrientation, float initialYaw) {
        OrientedContraptionEntity entity = create(world, contraption, initialOrientation);
        entity.startAtYaw(initialYaw);
        entity.manuallyPlaced = true;
        return entity;
    }

    public void setInitialOrientation(Direction direction) {
        dataTracker.set(INITIAL_ORIENTATION, direction);
    }

    public Direction getInitialOrientation() {
        return dataTracker.get(INITIAL_ORIENTATION);
    }

    @Override
    public float getYawOffset() {
        return getInitialYaw();
    }

    public float getInitialYaw() {
        return (isInitialOrientationPresent() ? dataTracker.get(INITIAL_ORIENTATION) : Direction.SOUTH).getPositiveHorizontalDegrees();
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(COUPLING, Optional.empty());
        builder.add(INITIAL_ORIENTATION, Direction.UP);
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
        if (!getWorld().isClient() && isAlive())
            disassemble();
        super.stopRiding();
    }

    @Override
    protected void readAdditional(ReadView view, boolean spawnPacket) {
        super.readAdditional(view, spawnPacket);

        view.read("InitialOrientation", Direction.CODEC).ifPresent(this::setInitialOrientation);

        yaw = view.getFloat("Yaw", 0);
        pitch = view.getFloat("Pitch", 0);
        manuallyPlaced = view.getBoolean("Placed", false);

        float forceYaw = view.getFloat("ForceYaw", -1);
        if (forceYaw != -1) {
            startAtYaw(forceYaw);
        }

        view.read("CachedMotion", Vec3d.CODEC).ifPresent(motion -> {
            motionBeforeStall = motion;
            if (!motionBeforeStall.equals(Vec3d.ZERO))
                targetYaw = prevYaw = yaw += yawFromVector(motionBeforeStall);
            setVelocity(Vec3d.ZERO);
        });

        setCouplingId(view.read("OnCoupling", Uuids.INT_STREAM_CODEC).orElse(null));
    }

    @Override
    protected void writeAdditional(WriteView view, boolean spawnPacket) {
        super.writeAdditional(view, spawnPacket);

        if (motionBeforeStall != null)
            view.put("CachedMotion", Vec3d.CODEC, motionBeforeStall);

        Direction optional = dataTracker.get(INITIAL_ORIENTATION);
        if (optional.getAxis().isHorizontal())
            view.put("InitialOrientation", Direction.CODEC, optional);
        if (forceAngle) {
            view.putFloat("ForceYaw", yaw);
            forceAngle = false;
        }

        view.putBoolean("Placed", manuallyPlaced);
        view.putFloat("Yaw", yaw);
        view.putFloat("Pitch", pitch);

        if (getCouplingId() != null)
            view.put("OnCoupling", Uuids.INT_STREAM_CODEC, getCouplingId());
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> key) {
        super.onTrackedDataSet(key);
        if (INITIAL_ORIENTATION.equals(key) && isInitialOrientationPresent() && !manuallyPlaced)
            startAtInitialYaw();
    }

    public boolean isInitialOrientationPresent() {
        return dataTracker.get(INITIAL_ORIENTATION).getAxis().isHorizontal();
    }

    public void startAtInitialYaw() {
        startAtYaw(getInitialYaw());
    }

    public void startAtYaw(float yaw) {
        targetYaw = this.yaw = prevYaw = yaw;
        forceAngle = true;
    }

    @Override
    public Vec3d applyRotation(Vec3d localPos, float partialTicks) {
        localPos = VecHelper.rotate(localPos, getInitialYaw(), Axis.Y);
        localPos = VecHelper.rotate(localPos, getViewXRot(partialTicks), Axis.Z);
        localPos = VecHelper.rotate(localPos, getViewYRot(partialTicks), Axis.Y);
        return localPos;
    }

    @Override
    public Vec3d reverseRotation(Vec3d localPos, float partialTicks) {
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
        isOnCoupling = couplingId != null && riding instanceof AbstractMinecartEntity;

        if (!attachedExtraInventories) {
            attachInventoriesFromRidingCarts(riding, isOnCoupling, couplingId);
            attachedExtraInventories = true;
        }

        boolean rotating = updateOrientation(rotationLock, wasStalled, riding, isOnCoupling);
        if (!rotating || !pauseWhileRotating)
            tickActors();
        boolean isStalled = isStalled();
        boolean isClient = getWorld().isClient();

        boolean isUpdate = true;
        if (riding instanceof AbstractMinecartEntity) {
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
                    motionBeforeStall = riding.getVelocity();
                riding.setVelocity(0, 0, 0);
            }
            if (wasStalled && !isStalled) {
                riding.setVelocity(motionBeforeStall);
                motionBeforeStall = Vec3d.ZERO;
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

    private BlockPos getCurrentRailPosition(AbstractMinecartEntity entity) {
        int x = MathHelper.floor(entity.getX());
        int y = MathHelper.floor(entity.getY());
        int z = MathHelper.floor(entity.getZ());
        BlockPos pos = new BlockPos(x, y, z);
        if (entity.getWorld().getBlockState(pos.down()).isIn(BlockTags.RAILS))
            pos = pos.down();
        return pos;
    }

    protected boolean updateOrientation(boolean rotationLock, boolean wasStalled, Entity riding, boolean isOnCoupling) {
        if (isOnCoupling) {
            Couple<MinecartController> coupledCarts = getCoupledCartsIfPresent();
            if (coupledCarts == null)
                return false;

            Vec3d positionVec = coupledCarts.getFirst().cart().getPos();
            Vec3d coupledVec = coupledCarts.getSecond().cart().getPos();

            double diffX = positionVec.x - coupledVec.x;
            double diffY = positionVec.y - coupledVec.y;
            double diffZ = positionVec.z - coupledVec.z;

            prevYaw = yaw;
            prevPitch = pitch;
            yaw = (float) (MathHelper.atan2(diffZ, diffX) * 180 / Math.PI);
            pitch = (float) (Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ)) * 180 / Math.PI);

            if (getCouplingId().equals(riding.getUuid())) {
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
        Vec3d movementVector = riding.getVelocity();
        Vec3d locationDiff = riding.getPos().subtract(riding.lastX, riding.lastY, riding.lastZ);
        if (!(riding instanceof AbstractMinecartEntity))
            movementVector = locationDiff;
        Vec3d motion = movementVector.normalize();

        if (!rotationLock) {
            if (riding instanceof AbstractMinecartEntity minecartEntity) {
                BlockPos railPosition = getCurrentRailPosition(minecartEntity);
                BlockState blockState = getWorld().getBlockState(railPosition);
                if (blockState.getBlock() instanceof AbstractRailBlock abstractRailBlock) {
                    RailShape railDirection = blockState.get(abstractRailBlock.getShapeProperty());
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
            float maxApproachSpeed = (float) (motion.length() * 12f / (Math.max(1, getBoundingBox().getLengthX() / 6f)));
            float yawHint = AngleHelper.getShortestAngleDiff(yaw, yawFromVector(locationDiff));
            float approach = AngleHelper.getShortestAngleDiff(yaw, targetYaw, yawHint);
            approach = MathHelper.clamp(approach, -maxApproachSpeed, maxApproachSpeed);
            yaw += approach;
            if (Math.abs(AngleHelper.getShortestAngleDiff(yaw, targetYaw)) < 1f)
                yaw = targetYaw;
            else
                rotating = true;
        }
        return rotating;
    }

    protected void powerFurnaceCartWithFuelFromStorage(Entity riding) {
        if (!(riding instanceof FurnaceMinecartEntity furnaceCart))
            return;

        int fuel = furnaceCart.fuel;
        int fuelBefore = fuel;
        double pushX = furnaceCart.pushVec.x;
        double pushZ = furnaceCart.pushVec.z;

        int i = MathHelper.floor(furnaceCart.getX());
        int j = MathHelper.floor(furnaceCart.getY());
        int k = MathHelper.floor(furnaceCart.getZ());
        if (furnaceCart.getWorld().getBlockState(new BlockPos(i, j - 1, k)).isIn(BlockTags.RAILS))
            --j;

        BlockPos blockpos = new BlockPos(i, j, k);
        if (getWorld().getBlockState(blockpos).isIn(BlockTags.RAILS))
            if (fuel > 1)
                riding.setVelocity(riding.getVelocity().normalize().multiply(1));
        if (fuel < 5 && contraption != null) {
            MountedItemStorageWrapper fuelItems = contraption.getStorage().getFuelItems();
            if (fuelItems != null) {
                ItemStack coal = fuelItems.extract(FUEL_ITEMS, 1);
                if (!coal.isEmpty())
                    fuel += 3600;
            }
        }

        if (fuel != fuelBefore || pushX != 0 || pushZ != 0) {
            furnaceCart.pushVec = new Vec3d(pushX, 0.0, pushZ);
            furnaceCart.fuel = fuel;
        }
    }

    @Nullable
    public Couple<MinecartController> getCoupledCartsIfPresent() {
        UUID couplingId = getCouplingId();
        if (couplingId == null)
            return null;
        MinecartController controller = CapabilityMinecartController.getIfPresent(getWorld(), couplingId);
        if (controller == null || !controller.isPresent())
            return null;
        UUID coupledCart = controller.getCoupledCart(true);
        MinecartController coupledController = CapabilityMinecartController.getIfPresent(getWorld(), coupledCart);
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
        return dataTracker.get(COUPLING).orElse(null);
    }

    public void setCouplingId(UUID id) {
        dataTracker.set(COUPLING, Optional.ofNullable(id));
    }

    @Override
    public Vec3d getVehicleAttachmentPos(Entity entity) {
        return entity instanceof AbstractContraptionEntity ? Vec3d.ZERO : new Vec3d(0, 0.19, 0);
    }

    @Override
    public Vec3d getAnchorVec() {
        Vec3d anchorVec = super.getAnchorVec();
        return anchorVec.subtract(.5, 0, .5);
    }

    @Override
    public Vec3d getPrevAnchorVec() {
        Vec3d prevAnchorVec = super.getPrevAnchorVec();
        return prevAnchorVec.subtract(.5, 0, .5);
    }

    @Override
    protected StructureTransform makeStructureTransform() {
        BlockPos offset = BlockPos.ofFloored(getAnchorVec().add(.5, .5, .5));
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
