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
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
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
    public abstract @Nullable Player getLastHurtByPlayer();

    @Shadow
    public abstract ItemStack getItemInHand(InteractionHand hand);

    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @WrapOperation(method = "travelInAir(Lnet/minecraft/world/phys/Vec3;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getFriction()F"))
    private float getSlipperiness(Block block, Operation<Float> original, @Local BlockPos pos) {
        if (block instanceof SlipperinessControlBlock controlBlock) {
            return controlBlock.getSlipperiness(level(), pos);
        }
        return original.call(block);
    }

    @WrapOperation(method = "baseTick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean breatheInLava(LivingEntity entity, TagKey<Fluid> tagKey, Operation<Boolean> original, @Local ServerLevel serverWorld) {
        if (original.call(entity, tagKey)) {
            return true;
        }
        if (entity instanceof ServerPlayer serverPlayer && !serverPlayer.getAbilities().invulnerable && entity.isInLava()) {
            DivingHelmetItem.breatheInLava(serverPlayer, serverWorld);
        }
        return false;
    }

    @WrapOperation(method = "baseTick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectUtil;hasWaterBreathing(Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private boolean canBreatheInWater(LivingEntity entity, Operation<Boolean> original, @Local ServerLevel serverWorld) {
        if (original.call(entity)) {
            return true;
        }
        if (entity instanceof ServerPlayer serverPlayer && !serverPlayer.getAbilities().invulnerable) {
            return DivingHelmetItem.breatheUnderwater(serverPlayer, serverWorld);
        }
        return false;
    }

    @Inject(method = "collectEquipmentChanges()Ljava/util/Map;", at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;"))
    private void onLivingEquipmentChange(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir) {
        if (((Object) this) instanceof Player player) {
            CardboardArmorHandler.playerChangesEquipment(player);
            NetheriteDivingHandler.onEquipmentChange(player);
        }
    }

    @Inject(method = "travelInLava(Lnet/minecraft/world/phys/Vec3;DZD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"))
    private void setOnGround(
        Vec3 input,
        double baseGravity,
        boolean isFalling,
        double oldY,
        CallbackInfo ci,
        @Share("onGround") LocalBooleanRef onGround
    ) {
        if (((Object) this) instanceof Player player) {
            onGround.set(player.onGround());
        }
    }

    @Inject(method = "travelInLava(Lnet/minecraft/world/phys/Vec3;DZD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getFluidHeight(Lnet/minecraft/tags/TagKey;)D"))
    private void onTravelInFluid(
        Vec3 input,
        double baseGravity,
        boolean isFalling,
        double oldY,
        CallbackInfo ci,
        @Share("onGround") LocalBooleanRef onGround
    ) {
        if (((Object) this) instanceof Player player) {
            DivingBootsItem.onLavaTravel(player, onGround.get());
        }
    }

    @WrapOperation(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean captureDrops(Level world, Entity entity, Operation<Boolean> original) {
        if (AllSynchedDatas.CRUSH_DROP.get(this)) {
            entity.setDeltaMovement(Vec3.ZERO);
        } else if (world instanceof ServerLevel) {
            Optional<List<ItemStack>> value = AllSynchedDatas.CAPTURE_DROPS.get(this);
            if (value.isPresent()) {
                value.get().add(((ItemEntity) entity).getItem());
                return true;
            }
        }
        return original.call(world, entity);
    }

    @Inject(method = "dropExperience(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ExperienceOrb;award(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;I)V"), cancellable = true)
    private void onDropExperience(ServerLevel world, Entity attacker, CallbackInfo ci) {
        if (getLastHurtByPlayer() instanceof DeployerPlayer) {
            ci.cancel();
        }
    }

    @Inject(method = "dropAllDeathLoot(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;)V", at = @At(value = "HEAD"))
    private void onDropPre(ServerLevel world, DamageSource damageSource, CallbackInfo ci, @Share("handler") LocalIntRef handler) {
        if (damageSource.is(AllDamageTypes.CRUSH)) {
            AllSynchedDatas.CRUSH_DROP.set(this, true);
            handler.set(1);
        } else if (damageSource.getEntity() instanceof DeployerPlayer) {
            AllSynchedDatas.CAPTURE_DROPS.set(this, Optional.of(new ArrayList<>()));
            handler.set(2);
        }
    }

    @Inject(method = "dropAllDeathLoot(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;)V", at = @At(value = "TAIL"))
    private void onDropPost(ServerLevel world, DamageSource damageSource, CallbackInfo ci, @Share("handler") LocalIntRef handler) {
        switch (handler.get()) {
            case 1 -> AllSynchedDatas.CRUSH_DROP.set(this, false);
            case 2 -> AllSynchedDatas.CAPTURE_DROPS.get(this).ifPresent(drops -> {
                Inventory inventory = ((DeployerPlayer) damageSource.getEntity()).cast().getInventory();
                drops.forEach(inventory::placeItemBackInInventory);
                AllSynchedDatas.CAPTURE_DROPS.set(this, Optional.empty());
            });
        }
    }

    @WrapOperation(method = "checkFallDamage(DZLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"))
    private boolean onLandingEffect(
        BlockState state,
        Operation<Boolean> original,
        @Local(argsOnly = true) BlockPos pos,
        @Local ServerLevel world,
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

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", at = @At("HEAD"), cancellable = true)
    private void swingHand(InteractionHand hand, boolean fromServerPlayer, CallbackInfo ci) {
        ItemStack stack = getItemInHand(hand);
        if (stack.getItem() instanceof SwingControlItem item) {
            if (item.onEntitySwing(stack, (LivingEntity) (Object) this, hand)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "getVisibilityPercent(Lnet/minecraft/world/entity/Entity;)D", at = @At("HEAD"), cancellable = true)
    private void getAttackDistanceScalingFactor(Entity entity, CallbackInfoReturnable<Double> cir) {
        if (CardboardArmorHandler.testForStealth(entity)) {
            cir.setReturnValue(0d);
        }
    }

    @WrapOperation(method = "handleOnClimbable(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z"))
    private boolean isScaffolding(BlockState state, Object block, Operation<Boolean> original) {
        return original.call(state, block) || state.getBlock() instanceof ScaffoldingControlBlock;
    }

    @WrapOperation(method = "playBlockFallSound()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"))
    private SoundType getBlockFallSound(
        BlockState state,
        Operation<SoundType> original,
        @Local(ordinal = 0) int x,
        @Local(ordinal = 1) int y,
        @Local(ordinal = 2) int z
    ) {
        if (state.getBlock() instanceof SoundControlBlock block) {
            return block.getSoundGroup(level(), new BlockPos(x, y, z));
        }
        return original.call(state);
    }
}
