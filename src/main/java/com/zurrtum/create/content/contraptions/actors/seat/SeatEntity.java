package com.zurrtum.create.content.contraptions.actors.seat;

import com.zurrtum.create.AllEntityTypes;
import com.zurrtum.create.api.entity.FakePlayerHandler;
import com.zurrtum.create.content.logistics.box.PackageEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SeatEntity extends Entity {

    public SeatEntity(EntityType<? extends SeatEntity> p_i48580_1_, World p_i48580_2_) {
        super(p_i48580_1_, p_i48580_2_);
    }

    public SeatEntity(World world, BlockPos pos) {
        this(AllEntityTypes.SEAT, world);
        noClip = true;
    }

    @Override
    public void setPosition(double x, double y, double z) {
        super.setPos(x, y, z);
        Box bb = getBoundingBox();
        Vec3d diff = new Vec3d(x, y, z).subtract(bb.getCenter());
        setBoundingBox(bb.offset(diff));
    }

    @Override
    protected void updatePassengerPosition(Entity pEntity, Entity.PositionUpdater pCallback) {
        if (!this.hasPassenger(pEntity))
            return;
        double heightOffset = getPassengerRidingPos(pEntity).y - pEntity.getVehicleAttachmentPos(this).y;

        pCallback.accept(pEntity, this.getX(), 1.0 / 16.0 + heightOffset + getCustomEntitySeatOffset(pEntity), this.getZ());
    }

    public static double getCustomEntitySeatOffset(Entity entity) {
        if (entity instanceof SlimeEntity)
            return 0.0f;
        if (entity instanceof ParrotEntity)
            return 1 / 12f;
        if (entity instanceof SkeletonEntity)
            return 1 / 8f;
        if (entity instanceof CatEntity)
            return 1 / 12f;
        if (entity instanceof WolfEntity)
            return 1 / 16f;
        if (entity instanceof FrogEntity)
            return 1.5 / 16f;
        if (entity instanceof SpiderEntity)
            return 1 / 8.0;
        if (entity instanceof PackageEntity)
            return 3 / 32f;
        return 0;
    }

    @Override
    public void setVelocity(Vec3d p_213317_1_) {
    }

    @Override
    public void tick() {
        if (getWorld().isClient)
            return;
        boolean blockPresent = getWorld().getBlockState(getBlockPos()).getBlock() instanceof SeatBlock;
        if (hasPassengers() && blockPresent)
            return;
        this.discard();
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected boolean canStartRiding(Entity entity) {
        // Fake Players (tested with deployers) have a BUNCH of weird issues, don't let
        // them ride seats
        return !(FakePlayerHandler.has(entity));
    }

    @Override
    protected void removePassenger(Entity entity) {
        super.removePassenger(entity);
        if (entity instanceof TameableEntity ta)
            ta.setInSittingPose(false);
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity pLivingEntity) {
        return super.updatePassengerForDismount(pLivingEntity).add(0, 0.5f, 0);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    @Override
    protected void readCustomData(ReadView view) {
    }

    @Override
    protected void writeCustomData(WriteView view) {
    }
}
