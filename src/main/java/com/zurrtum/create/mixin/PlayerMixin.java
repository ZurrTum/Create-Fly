package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.content.contraptions.minecart.MinecartCouplingItem;
import com.zurrtum.create.content.contraptions.mounted.MinecartContraptionItem;
import com.zurrtum.create.content.equipment.extendoGrip.ExtendoGripItem;
import com.zurrtum.create.content.equipment.wrench.WrenchItem;
import com.zurrtum.create.foundation.item.CustomAttackSoundItem;
import com.zurrtum.create.foundation.item.DamageControlItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "interactOn(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;", ordinal = 0), cancellable = true)
    private void interact(Entity entity, InteractionHand hand, Vec3 location, CallbackInfoReturnable<InteractionResult> cir) {
        Player player = (Player) (Object) this;
        InteractionResult result = MinecartCouplingItem.handleInteractionWithMinecart(player, hand, entity);
        if (result != null) {
            cir.setReturnValue(result);
        }
        result = MinecartContraptionItem.wrenchCanBeUsedToPickUpMinecartContraptions(player, hand, entity);
        if (result != null) {
            cir.setReturnValue(result);
        }
    }

    @Inject(method = "itemAttackInteraction(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/damagesource/DamageSource;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtEnemy(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private void attack(Entity entity, ItemStack attackingItemStack, DamageSource damageSource, boolean applyToTarget, CallbackInfo ci) {
        ExtendoGripItem.postDamageEntity((Player) (Object) this);
    }

    @WrapOperation(method = "attack(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean damage(Entity entity, DamageSource source, float amount, Operation<Boolean> original, @Local ItemStack stack) {
        if (stack.getItem() instanceof DamageControlItem item && !item.damage(entity)) {
            return true;
        }
        return original.call(entity, source, amount);
    }

    @WrapOperation(method = "itemAttackInteraction(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/damagesource/DamageSource;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;postHurtEnemy(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void postDamageEntity(ItemStack stack, LivingEntity target, LivingEntity user, Operation<Void> original) {
        if (stack.getItem() instanceof DamageControlItem item && !item.damage(target)) {
            return;
        }
        original.call(stack, target, user);
    }

    @WrapOperation(method = "attack(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;playServerSideSound(Lnet/minecraft/sounds/SoundEvent;)V"))
    private void playSound(Player player, SoundEvent sound, Operation<Void> original, @Local ItemStack attackingItemStack) {
        if (attackingItemStack.getItem() instanceof CustomAttackSoundItem item) {
            item.playSound(player.level(), player, player.getX(), player.getY(), player.getZ(), sound, player.getSoundSource(), 1f, 1f);
        } else {
            original.call(player, sound);
        }
    }

    @WrapOperation(method = "attack(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;attackVisualEffects(Lnet/minecraft/world/entity/Entity;ZZZZF)V"))
    private void playSound(
        Player player,
        Entity entity,
        boolean criticalAttack,
        boolean sweepAttack,
        boolean fullStrengthAttack,
        boolean stabAttack,
        float magicBoost,
        Operation<Void> original,
        @Local ItemStack attackingItemStack
    ) {
        if (attackingItemStack.getItem() instanceof CustomAttackSoundItem item) {
            SoundEvent sound;
            if (criticalAttack) {
                sound = SoundEvents.PLAYER_ATTACK_CRIT;
                player.crit(entity);
            } else if (!sweepAttack && !stabAttack) {
                sound = fullStrengthAttack ? SoundEvents.PLAYER_ATTACK_STRONG : SoundEvents.PLAYER_ATTACK_WEAK;
            } else {
                sound = null;
            }
            if (sound != null) {
                item.playSound(player.level(), player, player.getX(), player.getY(), player.getZ(), sound, player.getSoundSource(), 1f, 1f);
                original.call(player, entity, false, true, fullStrengthAttack, stabAttack, magicBoost);
                return;
            }
        }
        original.call(player, entity, criticalAttack, sweepAttack, fullStrengthAttack, stabAttack, magicBoost);
    }

    @Inject(method = "attack(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void attack(Entity target, CallbackInfo ci) {
        if (((Object) this) instanceof ServerPlayer player && WrenchItem.wrenchInstaKillsMinecarts(player, target)) {
            ci.cancel();
        }
    }

    @Inject(method = "addAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueOutput;)V", at = @At("TAIL"))
    private void writeCustomData(ValueOutput view, CallbackInfo ci) {
        CompoundTag compound = AllSynchedDatas.TOOLBOX.get((Player) (Object) this);
        if (!compound.isEmpty()) {
            view.store("CreateToolboxData", CompoundTag.CODEC, compound);
        }
    }

    @Inject(method = "readAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueInput;)V", at = @At("TAIL"))
    private void readCustomData(ValueInput view, CallbackInfo ci) {
        view.read("CreateToolboxData", CompoundTag.CODEC).ifPresent(compound -> AllSynchedDatas.TOOLBOX.set((Player) (Object) this, compound));
    }
}
