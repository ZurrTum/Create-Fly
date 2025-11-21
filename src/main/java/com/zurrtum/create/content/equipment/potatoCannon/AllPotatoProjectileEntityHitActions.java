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
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.component.SuspiciousStewEffects.Entry;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

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
        Registry.register(CreateRegistries.POTATO_PROJECTILE_ENTITY_HIT_ACTION, Identifier.fromNamespaceAndPath(MOD_ID, name), codec);
    }

    public record SetOnFire(int ticks) implements PotatoProjectileEntityHitAction {
        public static final MapCodec<SetOnFire> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(ExtraCodecs.POSITIVE_INT.fieldOf(
            "ticks").forGetter(SetOnFire::ticks)).apply(instance, SetOnFire::new));

        public static SetOnFire seconds(int seconds) {
            return new SetOnFire(seconds * 20);
        }

        @Override
        public boolean execute(ItemStack projectile, EntityHitResult ray, Type type) {
            ray.getEntity().setRemainingFireTicks(ticks);
            return false;
        }

        @Override
        public MapCodec<? extends PotatoProjectileEntityHitAction> codec() {
            return CODEC;
        }
    }

    public record PotionEffect(
        Holder<MobEffect> effect, int level, int ticks, boolean recoverable
    ) implements PotatoProjectileEntityHitAction {
        public static final MapCodec<PotionEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("effect").forGetter(PotionEffect::effect),
            ExtraCodecs.POSITIVE_INT.fieldOf("level").forGetter(PotionEffect::level),
            ExtraCodecs.POSITIVE_INT.fieldOf("ticks").forGetter(PotionEffect::ticks),
            Codec.BOOL.fieldOf("recoverable").forGetter(PotionEffect::recoverable)
        ).apply(instance, PotionEffect::new));

        @Override
        public boolean execute(ItemStack projectile, EntityHitResult ray, Type type) {
            Entity entity = ray.getEntity();
            if (entity.level().isClientSide())
                return true;
            if (entity instanceof LivingEntity livingEntity)
                applyEffect(livingEntity, new MobEffectInstance(effect, ticks, level - 1));
            return !recoverable;
        }

        @Override
        public MapCodec<? extends PotatoProjectileEntityHitAction> codec() {
            return CODEC;
        }
    }

    public record FoodEffects(
        Consumable foodProperty, boolean recoverable
    ) implements PotatoProjectileEntityHitAction {
        public static final MapCodec<FoodEffects> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Consumable.CODEC.fieldOf(
                "food_property").forGetter(FoodEffects::foodProperty),
            Codec.BOOL.fieldOf("recoverable").forGetter(FoodEffects::recoverable)
        ).apply(instance, FoodEffects::new));

        @Override
        public boolean execute(ItemStack projectile, EntityHitResult ray, Type type) {
            Entity entity = ray.getEntity();
            Level world = entity.level();
            if (world.isClientSide())
                return true;

            if (entity instanceof LivingEntity livingEntity) {
                for (ConsumeEffect effect : foodProperty.onConsumeEffects()) {
                    effect.apply(world, projectile, livingEntity);
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
            Level level = entity.level();
            if (level.isClientSide())
                return true;
            if (!(entity instanceof LivingEntity livingEntity))
                return false;

            double entityX = livingEntity.getX();
            double entityY = livingEntity.getY();
            double entityZ = livingEntity.getZ();

            for (int teleportTry = 0; teleportTry < 16; ++teleportTry) {
                double teleportX = entityX + (livingEntity.getRandom().nextDouble() - 0.5D) * teleportDiameter;
                double teleportY = Mth.clamp(
                    entityY + (livingEntity.getRandom().nextInt((int) teleportDiameter) - (int) (teleportDiameter / 2)),
                    0.0D,
                    level.getHeight() - 1
                );
                double teleportZ = entityZ + (livingEntity.getRandom().nextDouble() - 0.5D) * teleportDiameter;

                //TODO
                //                EntityTeleportEvent.ChorusFruit event = EventHooks.onChorusFruitTeleport(livingEntity, teleportX, teleportY, teleportZ);
                //                if (event.isCanceled())
                //                    return false;
                if (livingEntity.randomTeleport(teleportX, teleportY, teleportZ, true)) {
                    if (livingEntity.isPassenger())
                        livingEntity.stopRiding();

                    SoundEvent soundevent = livingEntity instanceof Fox ? SoundEvents.FOX_TELEPORT : SoundEvents.CHORUS_FRUIT_TELEPORT;
                    level.playSound(null, entityX, entityY, entityZ, soundevent, SoundSource.PLAYERS, 1.0F, 1.0F);
                    livingEntity.playSound(soundevent, 1.0F, 1.0F);
                    livingEntity.setDeltaMovement(Vec3.ZERO);
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

        private static final FoodEffects EFFECT = new FoodEffects(Consumables.GOLDEN_APPLE, false);
        private static final GameProfile ZOMBIE_CONVERTER_NAME = new GameProfile(
            UUID.fromString("be12d3dc-27d3-4992-8c97-66be53fd49c5"),
            "Converter"
        );
        private static final WorldAttached<FakePlayerEntity> ZOMBIE_CONVERTERS = new WorldAttached<>(w -> new FakePlayerEntity(
            (ServerLevel) w,
            ZOMBIE_CONVERTER_NAME
        ));

        public static final MapCodec<CureZombieVillager> CODEC = MapCodec.unit(INSTANCE);

        @Override
        public boolean execute(ItemStack projectile, EntityHitResult ray, Type type) {
            Entity entity = ray.getEntity();
            Level world = entity.level();

            if (!(entity instanceof ZombieVillager zombieVillager) || !zombieVillager.hasEffect(MobEffects.WEAKNESS))
                return EFFECT.execute(projectile, ray, type);
            if (world.isClientSide())
                return false;

            FakePlayerEntity dummy = ZOMBIE_CONVERTERS.get(world);
            dummy.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLDEN_APPLE, 1));
            zombieVillager.mobInteract(dummy, InteractionHand.MAIN_HAND);
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
                SuspiciousStewEffects stew = projectile.getOrDefault(DataComponents.SUSPICIOUS_STEW_EFFECTS, SuspiciousStewEffects.EMPTY);
                for (Entry effect : stew.effects())
                    livingEntity.addEffect(effect.createEffectInstance());
            }

            return false;
        }

        @Override
        public MapCodec<? extends PotatoProjectileEntityHitAction> codec() {
            return CODEC;
        }
    }

    private static void applyEffect(LivingEntity entity, MobEffectInstance effect) {
        if (effect.getEffect().value().isInstantenous()) {
            if (entity.level() instanceof ServerLevel serverWorld) {
                effect.getEffect().value().applyInstantenousEffect(serverWorld, null, null, entity, effect.getDuration(), 1.0);
            }
        } else {
            entity.addEffect(effect);
        }
    }
}
