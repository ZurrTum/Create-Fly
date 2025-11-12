package com.zurrtum.create.content.contraptions.actors.seat;

import com.zurrtum.create.AllEntityTypes;
import com.zurrtum.create.api.entity.FakePlayerHandler;
import com.zurrtum.create.content.logistics.box.PackageEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SeatEntity extends Entity {

    public SeatEntity(EntityType<? extends SeatEntity> p_i48580_1_, Level p_i48580_2_) {
        super(p_i48580_1_, p_i48580_2_);
    }

    public SeatEntity(Level world, BlockPos pos) {
        this(AllEntityTypes.SEAT, world);
        noPhysics = true;
    }

    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
        AABB bb = getBoundingBox();
        Vec3 diff = new Vec3(x, y, z).subtract(bb.getCenter());
        setBoundingBox(bb.move(diff));
    }

    @Override
    protected void positionRider(Entity pEntity, Entity.MoveFunction pCallback) {
        if (!this.hasPassenger(pEntity))
            return;
        double heightOffset = getPassengerRidingPosition(pEntity).y - pEntity.getVehicleAttachmentPoint(this).y;

        pCallback.accept(pEntity, this.getX(), 1.0 / 16.0 + heightOffset + getCustomEntitySeatOffset(pEntity), this.getZ());
        if (pEntity instanceof Player player) {
            float diff = player.getDimensions(player.getPose()).height() - player.getDimensions(Pose.CROUCHING).height();
            if (diff != 0) {
                AABB boundingBox = pEntity.getBoundingBox();
                pEntity.setBoundingBox(boundingBox.setMinY(boundingBox.minY + diff));
            }
        }
    }

    public static double getCustomEntitySeatOffset(Entity entity) {
        if (entity instanceof Slime)
            return 0.0f;
        if (entity instanceof Parrot)
            return 1 / 12f;
        if (entity instanceof Skeleton)
            return 1 / 8f;
        if (entity instanceof Cat)
            return 1 / 12f;
        if (entity instanceof Wolf)
            return 1 / 16f;
        if (entity instanceof Frog)
            return 1.5 / 16f;
        if (entity instanceof Spider)
            return 1 / 8.0;
        if (entity instanceof PackageEntity)
            return 3 / 32f;
        return 0;
    }

    @Override
    public void setDeltaMovement(Vec3 p_213317_1_) {
    }

    @Override
    public void tick() {
        if (level().isClientSide())
            return;
        boolean blockPresent = level().getBlockState(blockPosition()).getBlock() instanceof SeatBlock;
        if (isVehicle() && blockPresent)
            return;
        this.discard();
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected boolean canRide(Entity entity) {
        // Fake Players (tested with deployers) have a BUNCH of weird issues, don't let
        // them ride seats
        return !(FakePlayerHandler.has(entity));
    }

    @Override
    protected void removePassenger(Entity entity) {
        super.removePassenger(entity);
        if (entity instanceof TamableAnimal ta)
            ta.setInSittingPose(false);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity pLivingEntity) {
        return super.getDismountLocationForPassenger(pLivingEntity).add(0, 0.5f, 0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(ValueInput view) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput view) {
    }
}
