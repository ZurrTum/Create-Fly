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
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Shadow
    public abstract @NotNull ItemStack getWeaponStack();

    @Inject(method = "interact(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;", ordinal = 0), cancellable = true)
    private void interact(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        ActionResult result = MinecartCouplingItem.handleInteractionWithMinecart(player, hand, entity);
        if (result != null) {
            cir.setReturnValue(result);
        }
        result = MinecartContraptionItem.wrenchCanBeUsedToPickUpMinecartContraptions(player, hand, entity);
        if (result != null) {
            cir.setReturnValue(result);
        }
    }

    @Inject(method = "attack(Lnet/minecraft/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;postHit(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)Z"))
    private void attack(Entity target, CallbackInfo ci) {
        ExtendoGripItem.postDamageEntity((PlayerEntity) (Object) this);
    }

    @WrapOperation(method = "attack(Lnet/minecraft/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;sidedDamage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
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

    @WrapOperation(method = "attack(Lnet/minecraft/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;postDamageEntity(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V"))
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

    @WrapOperation(method = "attack(Lnet/minecraft/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/Entity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"))
    private void playSound(
        World world,
        Entity source,
        double x,
        double y,
        double z,
        SoundEvent sound,
        SoundCategory category,
        float volume,
        float pitch,
        Operation<Void> original
    ) {
        ItemStack stack = getWeaponStack();
        if (stack.getItem() instanceof CustomAttackSoundItem item) {
            item.playSound(world, (PlayerEntity) (Object) this, x, y, z, sound, category, volume, pitch);
        } else {
            original.call(world, source, x, y, z, sound, category, volume, pitch);
        }
    }

    @Inject(method = "writeCustomData(Lnet/minecraft/storage/WriteView;)V", at = @At("TAIL"))
    private void writeCustomData(WriteView view, CallbackInfo ci) {
        NbtCompound compound = AllSynchedDatas.TOOLBOX.get((PlayerEntity) (Object) this);
        if (!compound.isEmpty()) {
            view.put("CreateToolboxData", NbtCompound.CODEC, compound);
        }
    }

    @Inject(method = "readCustomData(Lnet/minecraft/storage/ReadView;)V", at = @At("TAIL"))
    private void readCustomData(ReadView view, CallbackInfo ci) {
        view.read("CreateToolboxData", NbtCompound.CODEC).ifPresent(compound -> AllSynchedDatas.TOOLBOX.set((PlayerEntity) (Object) this, compound));
    }
}
