package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.content.contraptions.minecart.MinecartCouplingItem;
import com.zurrtum.create.content.contraptions.mounted.MinecartContraptionItem;
import com.zurrtum.create.content.equipment.extendoGrip.ExtendoGripItem;
import com.zurrtum.create.foundation.item.CustomAttackSoundItem;
import com.zurrtum.create.foundation.item.DamageControlItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerEntityMixin {
    @Shadow
    public abstract @NotNull ItemStack getWeaponItem();

    @Inject(method = "interactOn(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;", ordinal = 0), cancellable = true)
    private void interact(Entity entity, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
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

    @Inject(method = "attack(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtEnemy(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private void attack(Entity target, CallbackInfo ci) {
        ExtendoGripItem.postDamageEntity((Player) (Object) this);
    }

    @WrapOperation(method = "attack(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean damage(
        Entity entity,
        DamageSource source,
        float amount,
        Operation<Boolean> original,
        @Local ItemStack stack,
        @Share("nodamage") LocalBooleanRef nodamage
    ) {
        if (stack.getItem() instanceof DamageControlItem item) {
            if (!item.damage(entity)) {
                nodamage.set(true);
                return true;
            }
        }
        return original.call(entity, source, amount);
    }

    @WrapOperation(method = "attack(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;postHurtEnemy(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void postDamageEntity(
        ItemStack instance,
        LivingEntity target,
        LivingEntity user,
        Operation<Void> original,
        @Share("nodamage") LocalBooleanRef nodamage
    ) {
        if (nodamage.get()) {
            return;
        }
        original.call(instance, target, user);
    }

    @WrapOperation(method = "attack(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    private void playSound(
        Level world,
        Entity source,
        double x,
        double y,
        double z,
        SoundEvent sound,
        SoundSource category,
        float volume,
        float pitch,
        Operation<Void> original
    ) {
        ItemStack stack = getWeaponItem();
        if (stack.getItem() instanceof CustomAttackSoundItem item) {
            item.playSound(world, (Player) (Object) this, x, y, z, sound, category, volume, pitch);
        } else {
            original.call(world, source, x, y, z, sound, category, volume, pitch);
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
