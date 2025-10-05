package com.zurrtum.create.content.equipment.potatoCannon;

import com.zurrtum.create.*;
import com.zurrtum.create.api.equipment.potatoCannon.PotatoCannonProjectileType;
import com.zurrtum.create.api.equipment.potatoCannon.PotatoProjectileRenderMode;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.equipment.potatoCannon.AllPotatoProjectileRenderModes.StuckToEntity;
import com.zurrtum.create.infrastructure.packet.s2c.NbtSpawnPacket;
import com.zurrtum.create.infrastructure.particle.AirParticleData;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PotatoProjectileEntity extends ExplosiveProjectileEntity {

    protected PotatoCannonProjectileType type;
    protected ItemStack stack = ItemStack.EMPTY;

    protected Entity stuckEntity;
    protected Vec3d stuckOffset;
    protected PotatoProjectileRenderMode stuckRenderer;
    protected double stuckFallSpeed;

    protected float additionalDamageMult = 1;
    protected float additionalKnockback = 0;
    protected float recoveryChance = 0;

    public PotatoProjectileEntity(EntityType<? extends ExplosiveProjectileEntity> type, World level) {
        super(type, level);
    }

    public void setItem(ItemStack stack) {
        this.stack = stack;
        DynamicRegistryManager registryManager = getRegistryManager();
        type = PotatoCannonProjectileType.getTypeForItem(registryManager, stack.getItem())
            .orElseGet(() -> registryManager.getOrThrow(CreateRegistryKeys.POTATO_PROJECTILE_TYPE).getOrThrow(AllPotatoProjectileTypes.FALLBACK))
            .value();
    }

    public void setEnchantmentEffectsFromCannon(ItemStack cannon) {
        Registry<Enchantment> enchantmentRegistry = getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);

        int recovery = cannon.getEnchantments().getLevel(enchantmentRegistry.getOrThrow(AllEnchantments.POTATO_RECOVERY));

        if (recovery > 0)
            recoveryChance = .125f + recovery * .125f;
    }

    public ItemStack getItem() {
        return stack;
    }

    @Nullable
    public PotatoCannonProjectileType getProjectileType() {
        return type;
    }

    @Override
    public void readCustomData(ReadView view) {
        setItem(view.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY));
        additionalDamageMult = view.getFloat("AdditionalDamage", 0);
        additionalKnockback = view.getFloat("AdditionalKnockback", 0);
        recoveryChance = view.getFloat("Recovery", 0);
        super.readCustomData(view);
    }

    @Override
    public void writeCustomData(WriteView view) {
        if (!stack.isEmpty()) {
            view.put("Item", ItemStack.CODEC, stack);
        }
        view.putFloat("AdditionalDamage", additionalDamageMult);
        view.putFloat("AdditionalKnockback", additionalKnockback);
        view.putFloat("Recovery", recoveryChance);
        super.writeCustomData(view);
    }

    @Nullable
    public Entity getStuckEntity() {
        if (stuckEntity == null)
            return null;
        if (!stuckEntity.isAlive())
            return null;
        return stuckEntity;
    }

    public void setStuckEntity(Entity stuckEntity) {
        this.stuckEntity = stuckEntity;
        this.stuckOffset = getPos().subtract(stuckEntity.getPos());
        this.stuckRenderer = new StuckToEntity(stuckOffset);
        this.stuckFallSpeed = 0.0;
        setVelocity(Vec3d.ZERO);
    }

    public PotatoProjectileRenderMode getRenderMode() {
        if (getStuckEntity() != null)
            return stuckRenderer;

        return type.renderMode();
    }

    @Override
    public void tick() {
        Entity stuckEntity = getStuckEntity();
        if (stuckEntity != null) {
            if (getY() < stuckEntity.getY() - 0.1) {
                pop(getPos());
                if (getWorld() instanceof ServerWorld serverWorld) {
                    kill(serverWorld);
                }
            } else {
                stuckFallSpeed += 0.007 * type.gravityMultiplier();
                stuckOffset = stuckOffset.add(0, -stuckFallSpeed, 0);
                Vec3d pos = stuckEntity.getPos().add(stuckOffset);
                setPosition(pos.x, pos.y, pos.z);
            }
        } else {
            setVelocity(getVelocity().add(0, -0.05 * type.gravityMultiplier(), 0).multiply(type.drag()));
        }

        super.tick();
    }

    @Override
    protected float getDrag() {
        return 1;
    }

    @Override
    protected ParticleEffect getParticleType() {
        return new AirParticleData(1, 10);
    }

    @Override
    protected boolean isBurning() {
        return false;
    }

    @Override
    public boolean canHit() {
        return true;
    }

    @Override
    protected void onEntityHit(EntityHitResult ray) {
        super.onEntityHit(ray);

        if (getStuckEntity() != null)
            return;

        Vec3d hit = ray.getPos();
        Entity target = ray.getEntity();
        float damage = type.damage() * additionalDamageMult;
        float knockback = type.knockback() + additionalKnockback;
        Entity owner = this.getOwner();

        if (!target.isAlive())
            return;
        if (owner instanceof LivingEntity entity)
            entity.onAttacking(target);

        if (target instanceof PotatoProjectileEntity ppe) {
            if (age < 10 && target.age < 10)
                return;
            if (ppe.getProjectileType() != getProjectileType()) {
                if (owner instanceof ServerPlayerEntity p)
                    AllAdvancements.POTATO_CANNON_COLLIDE.trigger(p);
                if (ppe.getOwner() instanceof ServerPlayerEntity p)
                    AllAdvancements.POTATO_CANNON_COLLIDE.trigger(p);
            }
        }

        pop(hit);

        if (target instanceof WitherEntity wither && wither.shouldRenderOverlay())
            return;
        if (type.preEntityHit(stack, ray))
            return;

        boolean targetIsEnderman = target.getType() == EntityType.ENDERMAN;
        int k = target.getFireTicks();
        if (this.isOnFire() && !targetIsEnderman)
            target.setOnFireFor(5);

        World world = getWorld();
        boolean onServer = !world.isClient();
        DamageSource damageSource = causePotatoDamage();
        if (onServer && !target.damage((ServerWorld) world, damageSource, damage)) {
            target.setFireTicks(k);
            kill((ServerWorld) world);
            return;
        }

        if (targetIsEnderman)
            return;

        if (!type.onEntityHit(stack, ray) && onServer) {
            if (random.nextDouble() <= recoveryChance) {
                recoverItem();
            } else {
                dropStack((ServerWorld) world, type.dropStack());
            }
        }

        if (!(target instanceof LivingEntity livingentity)) {
            playHitSound(world, getPos());
            if (onServer) {
                kill((ServerWorld) world);
            }
            return;
        }

        if (type.reloadTicks() < 10)
            livingentity.timeUntilRegen = type.reloadTicks() + 10;

        if (onServer && knockback > 0) {
            Vec3d appliedMotion = getVelocity().multiply(1.0D, 0.0D, 1.0D).normalize();
            if (appliedMotion.lengthSquared() > 0.0D)
                livingentity.takeKnockback(knockback * 0.6, -appliedMotion.x, -appliedMotion.z);
        }

        if (onServer && owner instanceof LivingEntity) {
            EnchantmentHelper.onTargetDamaged((ServerWorld) world, livingentity, damageSource);
        }

        if (livingentity != owner && livingentity instanceof PlayerEntity && owner instanceof ServerPlayerEntity serverPlayer && !this.isSilent()) {
            serverPlayer.networkHandler.sendPacket(new GameStateChangeS2CPacket(
                GameStateChangeS2CPacket.PROJECTILE_HIT_PLAYER,
                GameStateChangeS2CPacket.DEMO_OPEN_SCREEN
            ));
        }

        if (onServer && owner instanceof ServerPlayerEntity serverplayerentity) {
            if (!target.isAlive() && target.getType().getSpawnGroup() == SpawnGroup.MONSTER || (target instanceof PlayerEntity && target != owner))
                AllAdvancements.POTATO_CANNON.trigger(serverplayerentity);
        }

        if (type.sticky() && target.isAlive()) {
            setStuckEntity(target);
        } else if (onServer) {
            kill((ServerWorld) world);
        }

    }

    private void recoverItem() {
        if (!stack.isEmpty() && getWorld() instanceof ServerWorld serverWorld)
            dropStack(serverWorld, stack.copyWithCount(1));
    }

    public static void playHitSound(World world, Vec3d location) {
        AllSoundEvents.POTATO_HIT.playOnServer(world, BlockPos.ofFloored(location));
    }

    public static void playLaunchSound(World world, Vec3d location, float pitch) {
        AllSoundEvents.FWOOMP.playAt(world, location, 1, pitch, true);
    }

    @Override
    protected void onBlockHit(BlockHitResult ray) {
        Vec3d hit = ray.getPos();
        pop(hit);
        World world = getWorld();
        if (!type.onBlockHit(world, stack, ray) && !world.isClient()) {
            if (random.nextDouble() <= recoveryChance) {
                recoverItem();
            } else {
                dropStack((ServerWorld) world, getProjectileType().dropStack());
            }
        }

        super.onBlockHit(ray);
        if (world instanceof ServerWorld serverWorld) {
            kill(serverWorld);
        }
    }

    @Override
    public boolean damage(ServerWorld world, @NotNull DamageSource source, float amt) {
        if (source.isIn(DamageTypeTags.IS_FIRE))
            return false;
        if (isAlwaysInvulnerableTo(source))
            return false;
        pop(getPos());
        kill(world);
        return true;
    }

    private void pop(Vec3d hit) {
        if (!stack.isEmpty()) {
            for (int i = 0; i < 7; i++) {
                Vec3d m = VecHelper.offsetRandomly(Vec3d.ZERO, this.random, .25f);
                getWorld().addParticleClient(new ItemStackParticleEffect(ParticleTypes.ITEM, stack), hit.x, hit.y, hit.z, m.x, m.y, m.z);
            }
        }
        if (!getWorld().isClient())
            playHitSound(getWorld(), getPos());
    }

    private DamageSource causePotatoDamage() {
        return AllDamageSources.get(getWorld()).potatoCannon(this, getOwner());
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(getErrorReporterContext(), Create.LOGGER)) {
            NbtWriteView view = NbtWriteView.create(logging, getRegistryManager());
            writeCustomData(view);
            return new NbtSpawnPacket(this, entityTrackerEntry, view.getNbt());
        }
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        NbtCompound nbt = ((NbtSpawnPacket) packet).getNbt();
        if (nbt == null) {
            return;
        }
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(getErrorReporterContext(), Create.LOGGER)) {
            readCustomData(NbtReadView.create(logging, getRegistryManager(), nbt));
        }
    }
}
