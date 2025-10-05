package com.zurrtum.create.content.logistics.box;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllEntityTypes;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.logistics.chute.ChuteBlock;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import com.zurrtum.create.infrastructure.packet.s2c.PackageDestroyPacket;
import com.zurrtum.create.infrastructure.packet.s2c.PackageSpawnPacket;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;

public class PackageEntity extends LivingEntity {

    private Entity originalEntity;
    public ItemStack box;

    public int insertionDelay;

    public Vec3d vec2 = Vec3d.ZERO, vec3 = Vec3d.ZERO;

    public WeakReference<PlayerEntity> tossedBy = new WeakReference<>(null);

    public PackageEntity(EntityType<? extends PackageEntity> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
        box = ItemStack.EMPTY;
        setYaw(this.random.nextFloat() * 360.0F);
        setHeadYaw(getYaw());
        lastYaw = getYaw();
        insertionDelay = 30;
    }

    public PackageEntity(World worldIn, double x, double y, double z) {
        this(AllEntityTypes.PACKAGE, worldIn);
        this.setPosition(x, y, z);
        this.calculateDimensions();
    }

    @Override
    public void move(MovementType type, Vec3d movement) {
        super.move(type, movement);
        if (movement.lengthSquared() >= 0.01f) {
            velocityDirty = true;
        }
    }

    public static PackageEntity fromDroppedItem(World world, Entity originalEntity, ItemStack itemstack) {
        PackageEntity packageEntity = new PackageEntity(AllEntityTypes.PACKAGE, world);

        Vec3d position = originalEntity.getEntityPos();
        packageEntity.setPosition(position);
        packageEntity.setBox(itemstack);
        packageEntity.setVelocity(originalEntity.getVelocity().multiply(1.5f));
        packageEntity.originalEntity = originalEntity;

        if (world != null && !world.isClient())
            if (ChuteBlock.isChute(world.getBlockState(BlockPos.ofFloored(position.x, position.y + .5f, position.z))))
                packageEntity.setYaw(((int) packageEntity.getYaw()) / 90 * 90);

        return packageEntity;
    }

    public static PackageEntity fromItemStack(World world, Vec3d position, ItemStack itemstack) {
        PackageEntity packageEntity = new PackageEntity(AllEntityTypes.PACKAGE, world);
        packageEntity.setPosition(position);
        packageEntity.setBox(itemstack);
        return packageEntity;
    }

    @Override
    public ItemStack getPickBlockStack() {
        return box.copy();
    }

    public static DefaultAttributeContainer.Builder createPackageAttributes() {
        return LivingEntity.createLivingAttributes().add(EntityAttributes.MAX_HEALTH, 5f).add(EntityAttributes.MOVEMENT_SPEED, 1f);
    }

    @Override
    public boolean canMoveVoluntarily() {
        return true;
    }

    @Override
    public boolean canActVoluntarily() {
        return true;
    }

    @Override
    public void travel(Vec3d p_213352_1_) {
        super.travel(p_213352_1_);

        if (!getEntityWorld().isClient())
            return;
        if (getVelocity().length() < 1 / 128f)
            return;
        if (age >= 20)
            return;

        Vec3d motion = getVelocity().multiply(.75f);
        Box bb = getBoundingBox();
        List<VoxelShape> entityStream = getEntityWorld().getEntityCollisions(this, bb.stretch(motion));
        motion = adjustMovementForCollisions(this, motion, bb, getEntityWorld(), entityStream);

        Vec3d clientPos = getEntityPos().add(motion);
        if (isInterpolating())
            clientPos = VecHelper.lerp(Math.min(1, age / 20f), clientPos, getInterpolator().getLerpedPos());
        if (age < 5)
            setPosition(clientPos.x, clientPos.y, clientPos.z);
        if (age < 20)
            getInterpolator().refreshPositionAndAngles(clientPos, getYaw(), getPitch());
    }

    @Override
    public void setVelocityClient(double x, double y, double z) {
        setVelocity(getVelocity().add(x, y, z).multiply(.5f));
    }

    public String getAddress() {
        return box.get(AllDataComponents.PACKAGE_ADDRESS);
    }

    @Override
    public void tick() {
        if (firstUpdate) {
            verifyInitialEntity();
            originalEntity = null;
        }

        //        if (getWorld() instanceof PonderLevel) {
        //            setVelocity(getVelocity().add(0, -0.06, 0));
        //            if (getPos().y < 0.125)
        //                discard();
        //        }

        insertionDelay = Math.min(insertionDelay + 1, 30);
        super.tick();

        if (!PackageItem.isPackage(box))
            discard();
    }

    /*
     * Forge created package entities even when an ItemEntity is spawned as 'fake'.
     * See: GiveCommand#giveItem. This method discards the package if it originated
     * from such a fake item
     */
    protected void verifyInitialEntity() {
        if (!(originalEntity instanceof ItemEntity itemEntity))
            return;
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(getErrorReporterContext(), Create.LOGGER)) {
            NbtWriteView view = NbtWriteView.create(logging, getRegistryManager());
            itemEntity.writeCustomData(view);
            if (view.getNbt().getInt("PickupDelay", 0) != 32767) // See: ItemEntity#setDespawnImmediately
                return;
            discard();
        }
    }

    @Override
    protected EntityDimensions getBaseDimensions(EntityPose pose) {
        if (box == null)
            return super.getBaseDimensions(pose);
        return EntityDimensions.fixed(PackageItem.getWidth(box), PackageItem.getHeight(box));
    }

    public ItemStack getBox() {
        return box;
    }

    public static boolean centerPackage(Entity entity, Vec3d target) {
        if (!(entity instanceof PackageEntity packageEntity))
            return true;
        return packageEntity.decreaseInsertionTimer(target);
    }

    public boolean decreaseInsertionTimer(@Nullable Vec3d targetSpot) {
        if (targetSpot != null) {
            setVelocity(getVelocity().multiply(.75f).multiply(1, .25f, 1));
            Vec3d pos = getEntityPos().add(targetSpot.subtract(getEntityPos()).multiply(.2f));
            setPosition(pos.x, pos.y, pos.z);
            float yawTarget = ((int) getYaw()) / 90 * 90;
            setYaw(AngleHelper.angleLerp(.5f, getYaw(), yawTarget));
        }
        insertionDelay = Math.max(insertionDelay - 3, 0);
        return insertionDelay == 0;
    }

    public void setBox(ItemStack box) {
        this.box = box.copy();
        calculateDimensions();
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public boolean collidesWith(Entity pEntity) {
        return pEntity instanceof PackageEntity && pEntity.getBoundingBox().maxY < getBoundingBox().minY + .125f;
    }

    @Override
    public ActionResult interact(PlayerEntity pPlayer, Hand pHand) {
        if (!pPlayer.getStackInHand(pHand).isEmpty())
            return super.interact(pPlayer, pHand);
        if (pPlayer.getEntityWorld().isClient())
            return ActionResult.SUCCESS;
        pPlayer.setStackInHand(pHand, box);
        getEntityWorld().playSound(
            null,
            getBlockPos(),
            SoundEvents.ENTITY_ITEM_PICKUP,
            SoundCategory.PLAYERS,
            .2f,
            .75f + getEntityWorld().random.nextFloat()
        );
        remove(RemovalReason.DISCARDED);
        return ActionResult.SUCCESS;
    }

    @Override
    public void pushAwayFrom(Entity entityIn) {
        boolean isOtherPackage = entityIn instanceof PackageEntity;

        if (!isOtherPackage && tossedBy.get() != null)
            tossedBy = new WeakReference<>(null); // no nudging

        if (isOtherPackage) {
            if (entityIn.getBoundingBox().minY < this.getBoundingBox().maxY)
                super.pushAwayFrom(entityIn);
        } else if (entityIn.getBoundingBox().minY <= this.getBoundingBox().minY) {
            super.pushAwayFrom(entityIn);
        }
    }

    @Override
    public Vec3d getPassengerRidingPos(Entity entity) {
        return getEntityPos().add(0, entity.getDimensions(getPose()).height(), 0);
    }

    @Override
    protected Vec3d getPassengerAttachmentPos(Entity entity, EntityDimensions dimensions, float partialTick) {
        return super.getPassengerAttachmentPos(entity, dimensions, partialTick).add(0, 2 / 16f, 0);
    }

    @Override
    protected void onBlockCollision(BlockState state) {
        super.onBlockCollision(state);
        if (!isAlive())
            return;
        if (state.getBlock() == Blocks.WATER || (state.contains(Properties.WATERLOGGED) && state.get(Properties.WATERLOGGED))) {
            destroy(getDamageSources().drown());
            remove(RemovalReason.KILLED);
        }
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (getEntityWorld().isClient() || !this.isAlive())
            return false;

        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            this.remove(RemovalReason.KILLED);
            return false;
        }

        if (source.equals(getDamageSources().inWall()) && (hasVehicle() || insertionDelay < 20))
            return false;

        if (source.isIn(DamageTypeTags.IS_FALL))
            return false;

        if (this.isInvulnerableTo((ServerWorld) getEntityWorld(), source))
            return false;

        if (source.isIn(DamageTypeTags.IS_EXPLOSION)) {
            this.destroy(source);
            this.remove(RemovalReason.KILLED);
            return false;
        }

        if (source.isIn(DamageTypeTags.IS_FIRE)) {
            if (this.isOnFire()) {
                this.takeDamage(source, 0.15F);
            } else {
                this.setFireTicks(100); // 5 seconds
            }
            return false;
        }

        boolean shotCanPierce;
        if (source.getSource() instanceof PersistentProjectileEntity persistentProjectileEntity) {
            shotCanPierce = persistentProjectileEntity.getPierceLevel() > 0;
        } else {
            shotCanPierce = false;
        }

        if (source.getAttacker() instanceof PlayerEntity player && !player.getAbilities().allowModifyWorld)
            return false;

        this.destroy(source);
        this.remove(RemovalReason.KILLED);
        return shotCanPierce;
    }

    private void takeDamage(DamageSource source, float amount) {
        float hp = this.getHealth();
        hp = hp - amount;
        if (hp <= 0.5F) {
            this.destroy(source);
            this.remove(RemovalReason.KILLED);
        } else {
            this.setHealth(hp);
        }
    }

    private void destroy(DamageSource source) {
        AllSoundEvents.PACKAGE_POP.playOnServer(getEntityWorld(), getBlockPos());
        if (getEntityWorld() instanceof ServerWorld serverLevel) {
            this.drop(serverLevel, source);
            serverLevel.getChunkManager().sendToOtherNearbyPlayers(this, new PackageDestroyPacket(getBoundingBox().getCenter(), box));
        }
    }

    @Override
    protected void drop(ServerWorld level, DamageSource pDamageSource) {
        super.drop(level, pDamageSource);
        ItemStackHandler contents = PackageItem.getContents(box);
        for (int i = 0, size = contents.size(); i < size; i++) {
            ItemStack itemstack = contents.getStack(i);

            if (itemstack.getItem() instanceof SpawnEggItem sei) {
                EntityType<?> entitytype = sei.getEntityType(getRegistryManager(), itemstack);
                Entity entity = entitytype.spawnFromItemStack(level, itemstack, null, getBlockPos(), SpawnReason.SPAWN_ITEM_USE, false, false);
                if (entity != null)
                    itemstack.decrement(1);
            }

            if (itemstack.isEmpty())
                continue;
            ItemEntity entityIn = new ItemEntity(level, getX(), getY(), getZ(), itemstack);
            level.spawnEntity(entityIn);
        }
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        box = view.read("Box", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        calculateDimensions();
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        if (!box.isEmpty()) {
            view.put("Box", ItemStack.CODEC, box);
        }
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot pSlot) {
        if (pSlot == EquipmentSlot.MAINHAND)
            return getBox();
        return ItemStack.EMPTY;
    }

    @Override
    public void equipStack(EquipmentSlot pSlot, ItemStack pStack) {
        if (pSlot == EquipmentSlot.MAINHAND)
            setBox(pStack);
    }

    @Override
    public Arm getMainArm() {
        return Arm.RIGHT;
    }

    @Override
    public Hand getActiveHand() {
        return Hand.MAIN_HAND;
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
        return new PackageSpawnPacket(this, entityTrackerEntry);
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        PackageSpawnPacket spawnPacket = (PackageSpawnPacket) packet;
        setBox(spawnPacket.getBox());
    }

    @Override
    public float getSoundPitch() {
        return 1.5f;
    }

    @Override
    public FallSounds getFallSounds() {
        return new FallSounds(SoundEvents.BLOCK_CHISELED_BOOKSHELF_FALL, SoundEvents.BLOCK_CHISELED_BOOKSHELF_FALL);
    }

    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return null;
    }

    @Nullable
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    public boolean isAffectedBySplashPotions() {
        return false;
    }

    @Override
    public boolean isFireImmune() {
        return box.contains(DataComponentTypes.DAMAGE_RESISTANT) || super.isFireImmune();
    }
}