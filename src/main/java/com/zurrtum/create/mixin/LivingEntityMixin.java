package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.zurrtum.create.AllDamageTypes;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.content.equipment.armor.CardboardArmorHandler;
import com.zurrtum.create.content.equipment.armor.DivingBootsItem;
import com.zurrtum.create.content.equipment.armor.DivingHelmetItem;
import com.zurrtum.create.content.equipment.armor.NetheriteDivingHandler;
import com.zurrtum.create.content.kinetics.deployer.DeployerPlayer;
import com.zurrtum.create.foundation.block.LandingEffectControlBlock;
import com.zurrtum.create.foundation.block.ScaffoldingControlBlock;
import com.zurrtum.create.foundation.block.SlipperinessControlBlock;
import com.zurrtum.create.foundation.block.SoundControlBlock;
import com.zurrtum.create.foundation.item.SwingControlItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    public abstract @Nullable PlayerEntity getAttackingPlayer();

    @Shadow
    public abstract ItemStack getStackInHand(Hand hand);

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @WrapOperation(method = "travelMidAir(Lnet/minecraft/util/math/Vec3d;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getSlipperiness()F"))
    private float getSlipperiness(Block block, Operation<Float> original, @Local BlockPos pos) {
        if (block instanceof SlipperinessControlBlock controlBlock) {
            return controlBlock.getSlipperiness(getWorld(), pos);
        }
        return original.call(block);
    }

    @WrapOperation(method = "baseTick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSubmergedIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean breatheInLava(LivingEntity entity, TagKey<Fluid> tagKey, Operation<Boolean> original, @Local ServerWorld serverWorld) {
        if (original.call(entity, tagKey)) {
            return true;
        }
        if (entity instanceof ServerPlayerEntity serverPlayer && !serverPlayer.getAbilities().invulnerable && entity.isInLava()) {
            DivingHelmetItem.breatheInLava(serverPlayer, serverWorld);
        }
        return false;
    }

    @WrapOperation(method = "baseTick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffectUtil;hasWaterBreathing(Lnet/minecraft/entity/LivingEntity;)Z"))
    private boolean canBreatheInWater(LivingEntity entity, Operation<Boolean> original, @Local ServerWorld serverWorld) {
        if (original.call(entity)) {
            return true;
        }
        if (entity instanceof ServerPlayerEntity serverPlayer && !serverPlayer.getAbilities().invulnerable) {
            return DivingHelmetItem.breatheUnderwater(serverPlayer, serverWorld);
        }
        return false;
    }

    @Inject(method = "getEquipmentChanges()Ljava/util/Map;", at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;"))
    private void onLivingEquipmentChange(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir) {
        if (((Object) this) instanceof PlayerEntity player) {
            NetheriteDivingHandler.onEquipmentChange(player);
        }
    }

    @Inject(method = "travelInFluid(Lnet/minecraft/util/math/Vec3d;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V", ordinal = 1))
    private void setOnGround(Vec3d movementInput, CallbackInfo ci, @Share("onGround") LocalBooleanRef onGround) {
        if (((Object) this) instanceof PlayerEntity player) {
            onGround.set(player.isOnGround());
        }
    }

    @Inject(method = "travelInFluid(Lnet/minecraft/util/math/Vec3d;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V", shift = At.Shift.AFTER, ordinal = 1))
    private void onTravelInFluid(Vec3d movementInput, CallbackInfo ci, @Share("onGround") LocalBooleanRef onGround) {
        if (((Object) this) instanceof PlayerEntity player) {
            DivingBootsItem.onLavaTravel(player, onGround.get());
        }
    }

    @WrapOperation(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    private boolean captureDrops(World world, Entity entity, Operation<Boolean> original) {
        if (AllSynchedDatas.CRUSH_DROP.get(this)) {
            entity.setVelocity(Vec3d.ZERO);
        } else if (world instanceof ServerWorld) {
            Optional<List<ItemStack>> value = AllSynchedDatas.CAPTURE_DROPS.get(this);
            if (value.isPresent()) {
                value.get().add(((ItemEntity) entity).getStack());
                return true;
            }
        }
        return original.call(world, entity);
    }

    @Inject(method = "dropExperience(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ExperienceOrbEntity;spawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/Vec3d;I)V"), cancellable = true)
    private void onDropExperience(ServerWorld world, Entity attacker, CallbackInfo ci) {
        if (getAttackingPlayer() instanceof DeployerPlayer) {
            ci.cancel();
        }
    }

    @Inject(method = "drop(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;)V", at = @At(value = "HEAD"))
    private void onDropPre(ServerWorld world, DamageSource damageSource, CallbackInfo ci, @Share("handler") LocalIntRef handler) {
        if (damageSource.isOf(AllDamageTypes.CRUSH)) {
            AllSynchedDatas.CRUSH_DROP.set(this, true);
            handler.set(1);
        } else if (damageSource.getAttacker() instanceof DeployerPlayer) {
            AllSynchedDatas.CAPTURE_DROPS.set(this, Optional.of(new ArrayList<>()));
            handler.set(2);
        }
    }

    @Inject(method = "drop(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;)V", at = @At(value = "TAIL"))
    private void onDropPost(ServerWorld world, DamageSource damageSource, CallbackInfo ci, @Share("handler") LocalIntRef handler) {
        switch (handler.get()) {
            case 1 -> AllSynchedDatas.CRUSH_DROP.set(this, false);
            case 2 -> AllSynchedDatas.CAPTURE_DROPS.get(this).ifPresent(drops -> {
                PlayerInventory inventory = ((DeployerPlayer) damageSource.getAttacker()).getInventory();
                drops.forEach(inventory::offerOrDrop);
                AllSynchedDatas.CAPTURE_DROPS.set(this, Optional.empty());
            });
        }
    }

    @WrapOperation(method = "fall(DZLnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isAir()Z"))
    private boolean onLandingEffect(
        BlockState state,
        Operation<Boolean> original,
        @Local(argsOnly = true) BlockPos pos,
        @Local ServerWorld world,
        @Local(ordinal = 1) double distance
    ) {
        if (original.call(state)) {
            return true;
        }
        if (state.getBlock() instanceof LandingEffectControlBlock block) {
            return block.addLandingEffects(state, world, pos, (LivingEntity) (Object) this, distance);
        }
        return false;
    }

    @Inject(method = "swingHand(Lnet/minecraft/util/Hand;Z)V", at = @At("HEAD"), cancellable = true)
    private void swingHand(Hand hand, boolean fromServerPlayer, CallbackInfo ci) {
        ItemStack stack = getStackInHand(hand);
        if (stack.getItem() instanceof SwingControlItem item) {
            if (item.onEntitySwing(stack, (LivingEntity) (Object) this, hand)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "getAttackDistanceScalingFactor(Lnet/minecraft/entity/Entity;)D", at = @At("HEAD"), cancellable = true)
    private void getAttackDistanceScalingFactor(Entity entity, CallbackInfoReturnable<Double> cir) {
        if (CardboardArmorHandler.testForStealth(entity)) {
            cir.setReturnValue(0d);
        }
    }

    @WrapOperation(method = "applyClimbingSpeed(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
    private boolean isScaffolding(BlockState state, Block block, Operation<Boolean> original) {
        return original.call(state, block) || state.getBlock() instanceof ScaffoldingControlBlock;
    }

    @WrapOperation(method = "playBlockFallSound()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getSoundGroup()Lnet/minecraft/sound/BlockSoundGroup;"))
    private BlockSoundGroup getBlockFallSound(
        BlockState state,
        Operation<BlockSoundGroup> original,
        @Local(ordinal = 0) int x,
        @Local(ordinal = 1) int y,
        @Local(ordinal = 2) int z
    ) {
        if (state.getBlock() instanceof SoundControlBlock block) {
            return block.getSoundGroup(getWorld(), new BlockPos(x, y, z));
        }
        return original.call(state);
    }
}
