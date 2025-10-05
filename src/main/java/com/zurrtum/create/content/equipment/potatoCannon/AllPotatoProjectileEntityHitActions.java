package com.zurrtum.create.content.equipment.potatoCannon;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.api.equipment.potatoCannon.PotatoProjectileEntityHitAction;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.catnip.data.WorldAttached;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.infrastructure.player.FakePlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.ConsumableComponents;
import net.minecraft.component.type.SuspiciousStewEffectsComponent;
import net.minecraft.component.type.SuspiciousStewEffectsComponent.StewEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.ConsumeEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

import static com.zurrtum.create.Create.MOD_ID;

public class AllPotatoProjectileEntityHitActions {
    public static void register() {
        register("set_on_fire", SetOnFire.CODEC);
        register("potion_effect", PotionEffect.CODEC);
        register("food_effects", FoodEffects.CODEC);
        register("chorus_teleport", ChorusTeleport.CODEC);
        register("cure_zombie_villager", CureZombieVillager.CODEC);
        register("suspicious_stew", SuspiciousStew.CODEC);
    }

    private static void register(String name, MapCodec<? extends PotatoProjectileEntityHitAction> codec) {
        Registry.register(CreateRegistries.POTATO_PROJECTILE_ENTITY_HIT_ACTION, Identifier.of(MOD_ID, name), codec);
    }

    public record SetOnFire(int ticks) implements PotatoProjectileEntityHitAction {
        public static final MapCodec<SetOnFire> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Codecs.POSITIVE_INT.fieldOf("ticks")
            .forGetter(SetOnFire::ticks)).apply(instance, SetOnFire::new));

        public static SetOnFire seconds(int seconds) {
            return new SetOnFire(seconds * 20);
        }

        @Override
        public boolean execute(ItemStack projectile, EntityHitResult ray, Type type) {
            ray.getEntity().setFireTicks(ticks);
            return false;
        }

        @Override
        public MapCodec<? extends PotatoProjectileEntityHitAction> codec() {
            return CODEC;
        }
    }

    public record PotionEffect(
        RegistryEntry<StatusEffect> effect, int level, int ticks, boolean recoverable
    ) implements PotatoProjectileEntityHitAction {
        public static final MapCodec<PotionEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Registries.STATUS_EFFECT.getEntryCodec().fieldOf("effect").forGetter(PotionEffect::effect),
            Codecs.POSITIVE_INT.fieldOf("level").forGetter(PotionEffect::level),
            Codecs.POSITIVE_INT.fieldOf("ticks").forGetter(PotionEffect::ticks),
            Codec.BOOL.fieldOf("recoverable").forGetter(PotionEffect::recoverable)
        ).apply(instance, PotionEffect::new));

        @Override
        public boolean execute(ItemStack projectile, EntityHitResult ray, Type type) {
            Entity entity = ray.getEntity();
            if (entity.getEntityWorld().isClient())
                return true;
            if (entity instanceof LivingEntity livingEntity)
                applyEffect(livingEntity, new StatusEffectInstance(effect, ticks, level - 1));
            return !recoverable;
        }

        @Override
        public MapCodec<? extends PotatoProjectileEntityHitAction> codec() {
            return CODEC;
        }
    }

    public record FoodEffects(
        ConsumableComponent foodProperty, boolean recoverable
    ) implements PotatoProjectileEntityHitAction {
        public static final MapCodec<FoodEffects> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ConsumableComponent.CODEC.fieldOf(
                "food_property").forGetter(FoodEffects::foodProperty),
            Codec.BOOL.fieldOf("recoverable").forGetter(FoodEffects::recoverable)
        ).apply(instance, FoodEffects::new));

        @Override
        public boolean execute(ItemStack projectile, EntityHitResult ray, Type type) {
            Entity entity = ray.getEntity();
            World world = entity.getEntityWorld();
            if (world.isClient())
                return true;

            if (entity instanceof LivingEntity livingEntity) {
                for (ConsumeEffect effect : foodProperty.onConsumeEffects()) {
                    effect.onConsume(world, projectile, livingEntity);
                }
            }
            return !recoverable;
        }

        @Override
        public MapCodec<? extends PotatoProjectileEntityHitAction> codec() {
            return CODEC;
        }
    }

    public record ChorusTeleport(double teleportDiameter) implements PotatoProjectileEntityHitAction {
        public static final MapCodec<ChorusTeleport> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(CreateCodecs.POSITIVE_DOUBLE.fieldOf(
            "teleport_diameter").forGetter(ChorusTeleport::teleportDiameter)).apply(instance, ChorusTeleport::new));

        @Override
        public boolean execute(ItemStack projectile, EntityHitResult ray, Type type) {
            Entity entity = ray.getEntity();
            World level = entity.getEntityWorld();
            if (level.isClient())
                return true;
            if (!(entity instanceof LivingEntity livingEntity))
                return false;

            double entityX = livingEntity.getX();
            double entityY = livingEntity.getY();
            double entityZ = livingEntity.getZ();

            for (int teleportTry = 0; teleportTry < 16; ++teleportTry) {
                double teleportX = entityX + (livingEntity.getRandom().nextDouble() - 0.5D) * teleportDiameter;
                double teleportY = MathHelper.clamp(
                    entityY + (livingEntity.getRandom()
                        .nextInt((int) teleportDiameter) - (int) (teleportDiameter / 2)), 0.0D, level.getHeight() - 1
                );
                double teleportZ = entityZ + (livingEntity.getRandom().nextDouble() - 0.5D) * teleportDiameter;

                //TODO
                //                EntityTeleportEvent.ChorusFruit event = EventHooks.onChorusFruitTeleport(livingEntity, teleportX, teleportY, teleportZ);
                //                if (event.isCanceled())
                //                    return false;
                if (livingEntity.teleport(teleportX, teleportY, teleportZ, true)) {
                    if (livingEntity.hasVehicle())
                        livingEntity.stopRiding();

                    SoundEvent soundevent = livingEntity instanceof FoxEntity ? SoundEvents.ENTITY_FOX_TELEPORT : SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT;
                    level.playSound(null, entityX, entityY, entityZ, soundevent, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    livingEntity.playSound(soundevent, 1.0F, 1.0F);
                    livingEntity.setVelocity(Vec3d.ZERO);
                    return true;
                }
            }

            return false;
        }

        @Override
        public MapCodec<? extends PotatoProjectileEntityHitAction> codec() {
            return CODEC;
        }
    }

    public enum CureZombieVillager implements PotatoProjectileEntityHitAction {
        INSTANCE;

        private static final FoodEffects EFFECT = new FoodEffects(ConsumableComponents.GOLDEN_APPLE, false);
        private static final GameProfile ZOMBIE_CONVERTER_NAME = new GameProfile(
            UUID.fromString("be12d3dc-27d3-4992-8c97-66be53fd49c5"),
            "Converter"
        );
        private static final WorldAttached<FakePlayerEntity> ZOMBIE_CONVERTERS = new WorldAttached<>(w -> new FakePlayerEntity(
            (ServerWorld) w,
            ZOMBIE_CONVERTER_NAME
        ));

        public static final MapCodec<CureZombieVillager> CODEC = MapCodec.unit(INSTANCE);

        @Override
        public boolean execute(ItemStack projectile, EntityHitResult ray, Type type) {
            Entity entity = ray.getEntity();
            World world = entity.getEntityWorld();

            if (!(entity instanceof ZombieVillagerEntity zombieVillager) || !zombieVillager.hasStatusEffect(StatusEffects.WEAKNESS))
                return EFFECT.execute(projectile, ray, type);
            if (world.isClient())
                return false;

            FakePlayerEntity dummy = ZOMBIE_CONVERTERS.get(world);
            dummy.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.GOLDEN_APPLE, 1));
            zombieVillager.interactMob(dummy, Hand.MAIN_HAND);
            return true;
        }

        @Override
        public MapCodec<? extends PotatoProjectileEntityHitAction> codec() {
            return CODEC;
        }
    }

    public enum SuspiciousStew implements PotatoProjectileEntityHitAction {
        INSTANCE;

        public static final MapCodec<SuspiciousStew> CODEC = MapCodec.unit(INSTANCE);

        @Override
        public boolean execute(ItemStack projectile, EntityHitResult ray, Type type) {
            if (ray.getEntity() instanceof LivingEntity livingEntity) {
                SuspiciousStewEffectsComponent stew = projectile.getOrDefault(
                    DataComponentTypes.SUSPICIOUS_STEW_EFFECTS,
                    SuspiciousStewEffectsComponent.DEFAULT
                );
                for (StewEffect effect : stew.effects())
                    livingEntity.addStatusEffect(effect.createStatusEffectInstance());
            }

            return false;
        }

        @Override
        public MapCodec<? extends PotatoProjectileEntityHitAction> codec() {
            return CODEC;
        }
    }

    private static void applyEffect(LivingEntity entity, StatusEffectInstance effect) {
        if (effect.getEffectType().value().isInstant()) {
            if (entity.getEntityWorld() instanceof ServerWorld serverWorld) {
                effect.getEffectType().value().applyInstantEffect(serverWorld, null, null, entity, effect.getDuration(), 1.0);
            }
        } else {
            entity.addStatusEffect(effect);
        }
    }
}
