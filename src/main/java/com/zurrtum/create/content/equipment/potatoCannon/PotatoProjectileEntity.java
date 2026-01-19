package com.zurrtum.create.content.equipment.potatoCannon;

import com.zurrtum.create.*;
import com.zurrtum.create.api.equipment.potatoCannon.PotatoCannonProjectileType;
import com.zurrtum.create.api.equipment.potatoCannon.PotatoProjectileRenderMode;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.equipment.potatoCannon.AllPotatoProjectileRenderModes.StuckToEntity;
import com.zurrtum.create.infrastructure.packet.s2c.NbtSpawnPacket;
import com.zurrtum.create.infrastructure.particle.AirParticleData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class PotatoProjectileEntity extends AbstractHurtingProjectile {

    protected PotatoCannonProjectileType type;
    protected ItemStack stack = ItemStack.EMPTY;

    protected Entity stuckEntity;
    protected Vec3 stuckOffset;
    protected PotatoProjectileRenderMode stuckRenderer;
    protected double stuckFallSpeed;

    protected float additionalDamageMult = 1;
    protected float additionalKnockback = 0;
    protected float recoveryChance = 0;

    public PotatoProjectileEntity(EntityType<? extends AbstractHurtingProjectile> type, Level level) {
        super(type, level);
    }

    public void setItem(ItemStack stack) {
        this.stack = stack;
        RegistryAccess registryManager = registryAccess();
        type = PotatoCannonProjectileType.getTypeForItem(registryManager, stack.getItem())
            .orElseGet(() -> registryManager.lookupOrThrow(CreateRegistryKeys.POTATO_PROJECTILE_TYPE).getOrThrow(AllPotatoProjectileTypes.FALLBACK))
            .value();
    }

    public void setEnchantmentEffectsFromCannon(ItemStack cannon) {
        Registry<Enchantment> enchantmentRegistry = registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

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
    public void readAdditionalSaveData(ValueInput view) {
        setItem(view.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY));
        additionalDamageMult = view.getFloatOr("AdditionalDamage", 0);
        additionalKnockback = view.getFloatOr("AdditionalKnockback", 0);
        recoveryChance = view.getFloatOr("Recovery", 0);
        super.readAdditionalSaveData(view);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput view) {
        if (!stack.isEmpty()) {
            view.store("Item", ItemStack.CODEC, stack);
        }
        view.putFloat("AdditionalDamage", additionalDamageMult);
        view.putFloat("AdditionalKnockback", additionalKnockback);
        view.putFloat("Recovery", recoveryChance);
        super.addAdditionalSaveData(view);
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
        this.stuckOffset = position().subtract(stuckEntity.position());
        this.stuckRenderer = new StuckToEntity(stuckOffset);
        this.stuckFallSpeed = 0.0;
        setDeltaMovement(Vec3.ZERO);
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
                pop(position());
                if (level() instanceof ServerLevel serverWorld) {
                    kill(serverWorld);
                }
            } else {
                stuckFallSpeed += 0.007 * type.gravityMultiplier();
                stuckOffset = stuckOffset.add(0, -stuckFallSpeed, 0);
                Vec3 pos = stuckEntity.position().add(stuckOffset);
                setPos(pos.x, pos.y, pos.z);
            }
        } else {
            setDeltaMovement(getDeltaMovement().add(0, -0.05 * type.gravityMultiplier(), 0).scale(type.drag()));
        }

        super.tick();
    }

    @Override
    protected float getInertia() {
        return 1;
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return new AirParticleData(1, 10);
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    protected void onHitEntity(EntityHitResult ray) {
        super.onHitEntity(ray);

        if (getStuckEntity() != null)
            return;

        Vec3 hit = ray.getLocation();
        Entity target = ray.getEntity();
        float damage = type.damage() * additionalDamageMult;
        float knockback = type.knockback() + additionalKnockback;
        Entity owner = this.getOwner();

        if (!target.isAlive())
            return;
        if (owner instanceof LivingEntity entity)
            entity.setLastHurtMob(target);

        if (target instanceof PotatoProjectileEntity ppe) {
            if (tickCount < 10 && target.tickCount < 10)
                return;
            if (ppe.getProjectileType() != getProjectileType()) {
                if (owner instanceof ServerPlayer p)
                    AllAdvancements.POTATO_CANNON_COLLIDE.trigger(p);
                if (ppe.getOwner() instanceof ServerPlayer p)
                    AllAdvancements.POTATO_CANNON_COLLIDE.trigger(p);
            }
        }

        pop(hit);

        if (target instanceof WitherBoss wither && wither.isPowered())
            return;
        if (type.preEntityHit(stack, ray))
            return;

        boolean targetIsEnderman = target.getType() == EntityType.ENDERMAN;
        int k = target.getRemainingFireTicks();
        if (this.isOnFire() && !targetIsEnderman)
            target.igniteForSeconds(5);

        Level world = level();
        boolean onServer = !world.isClientSide();
        DamageSource damageSource = causePotatoDamage();
        if (onServer && !target.hurtServer((ServerLevel) world, damageSource, damage)) {
            target.setRemainingFireTicks(k);
            kill((ServerLevel) world);
            return;
        }

        if (targetIsEnderman)
            return;

        if (!type.onEntityHit(stack, ray) && onServer) {
            if (random.nextDouble() <= recoveryChance) {
                recoverItem();
            } else {
                spawnAtLocation((ServerLevel) world, type.dropStack());
            }
        }

        if (!(target instanceof LivingEntity livingentity)) {
            playHitSound(world, position());
            if (onServer) {
                kill((ServerLevel) world);
            }
            return;
        }

        if (type.reloadTicks() < 10)
            livingentity.invulnerableTime = type.reloadTicks() + 10;

        if (onServer && knockback > 0) {
            Vec3 appliedMotion = getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize();
            if (appliedMotion.lengthSqr() > 0.0D)
                livingentity.knockback(knockback * 0.6, -appliedMotion.x, -appliedMotion.z);
        }

        if (onServer && owner instanceof LivingEntity) {
            EnchantmentHelper.doPostAttackEffects((ServerLevel) world, livingentity, damageSource);
        }

        if (livingentity != owner && livingentity instanceof Player && owner instanceof ServerPlayer serverPlayer && !this.isSilent()) {
            serverPlayer.connection.send(new ClientboundGameEventPacket(
                ClientboundGameEventPacket.PLAY_ARROW_HIT_SOUND,
                ClientboundGameEventPacket.DEMO_PARAM_INTRO
            ));
        }

        if (onServer && owner instanceof ServerPlayer serverplayerentity) {
            if (!target.isAlive() && target.getType().getCategory() == MobCategory.MONSTER || (target instanceof Player && target != owner))
                AllAdvancements.POTATO_CANNON.trigger(serverplayerentity);
        }

        if (type.sticky() && target.isAlive()) {
            setStuckEntity(target);
        } else if (onServer) {
            kill((ServerLevel) world);
        }

    }

    private void recoverItem() {
        if (!stack.isEmpty() && level() instanceof ServerLevel serverWorld)
            spawnAtLocation(serverWorld, stack.copyWithCount(1));
    }

    public static void playHitSound(Level world, Vec3 location) {
        AllSoundEvents.POTATO_HIT.playOnServer(world, BlockPos.containing(location));
    }

    public static void playLaunchSound(Level world, Vec3 location, float pitch) {
        AllSoundEvents.FWOOMP.playAt(world, location, 1, pitch, true);
    }

    @Override
    protected void onHitBlock(BlockHitResult ray) {
        Vec3 hit = ray.getLocation();
        pop(hit);
        Level world = level();
        if (!type.onBlockHit(world, stack, ray) && !world.isClientSide()) {
            if (random.nextDouble() <= recoveryChance) {
                recoverItem();
            } else {
                spawnAtLocation((ServerLevel) world, getProjectileType().dropStack());
            }
        }

        super.onHitBlock(ray);
        if (world instanceof ServerLevel serverWorld) {
            kill(serverWorld);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amt) {
        if (source.is(DamageTypeTags.IS_FIRE))
            return false;
        if (isInvulnerableToBase(source))
            return false;
        pop(position());
        kill(world);
        return true;
    }

    private void pop(Vec3 hit) {
        if (!stack.isEmpty()) {
            for (int i = 0; i < 7; i++) {
                Vec3 m = VecHelper.offsetRandomly(Vec3.ZERO, this.random, .25f);
                level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), hit.x, hit.y, hit.z, m.x, m.y, m.z);
            }
        }
        if (!level().isClientSide())
            playHitSound(level(), position());
    }

    private DamageSource causePotatoDamage() {
        return AllDamageSources.get(level()).potatoCannon(this, getOwner());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entityTrackerEntry) {
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(problemPath(), Create.LOGGER)) {
            TagValueOutput view = TagValueOutput.createWithContext(logging, registryAccess());
            addAdditionalSaveData(view);
            return new NbtSpawnPacket(this, entityTrackerEntry, view.buildResult());
        }
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        CompoundTag nbt = ((NbtSpawnPacket) packet).getNbt();
        if (nbt == null) {
            return;
        }
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(problemPath(), Create.LOGGER)) {
            readAdditionalSaveData(TagValueInput.create(logging, registryAccess(), nbt));
        }
    }
}
