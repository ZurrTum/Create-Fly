package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.content.contraptions.actors.seat.SeatBlock;
import com.zurrtum.create.content.contraptions.minecart.CouplingHandler;
import com.zurrtum.create.content.contraptions.minecart.capability.CapabilityMinecartController;
import com.zurrtum.create.content.equipment.armor.CardboardArmorHandler;
import com.zurrtum.create.content.equipment.armor.DivingBootsItem;
import com.zurrtum.create.content.equipment.toolbox.ToolboxHandler;
import com.zurrtum.create.content.kinetics.deployer.DeployerPlayer;
import com.zurrtum.create.foundation.block.RunningEffectControlBlock;
import com.zurrtum.create.foundation.block.SoundControlBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SyncedDataHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(Entity.class)
public abstract class EntityMixin implements SyncedDataHolder {
    @Shadow
    private EntityDimensions dimensions;

    @Shadow
    private Level level;

    @Shadow
    public abstract BlockState getBlockStateOn();

    @Shadow
    public abstract BlockPos getOnPos();

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityDimensions;eyeHeight()F"))
    private float setEyeHeight(EntityDimensions dimensions, Operation<Float> original) {
        if (this instanceof DeployerPlayer) {
            this.dimensions = dimensions.withEyeHeight(0);
            return 0;
        }
        return original.call(dimensions);
    }

    @Inject(method = "rideTick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    private void tickRiding(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        CapabilityMinecartController.entityTick(entity);
        DivingBootsItem.accelerateDescentUnderwater(entity);
        CardboardArmorHandler.mobsMayLoseTargetWhenItIsWearingCardboard(entity);
        ToolboxHandler.entityTick(entity, level);
    }

    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z", at = @At(value = "HEAD"), cancellable = true)
    private void startRiding(Entity entity, boolean force, boolean emitEvent, CallbackInfoReturnable<Boolean> cir) {
        if (CouplingHandler.preventEntitiesFromMoutingOccupiedCart((Entity) (Object) this, entity)) {
            cir.setReturnValue(false);
        }
    }

    @ModifyReturnValue(method = "fireImmune()Z", at = @At("RETURN"))
    private boolean isFireImmune(boolean original) {
        if (original) {
            return true;
        }
        return ((Entity) (Object) this) instanceof Player player && AllSynchedDatas.FIRE_IMMUNE.get(player);
    }

    @Inject(method = "isUnderWater()Z", at = @At("HEAD"), cancellable = true)
    private void isSubmergedInWater(CallbackInfoReturnable<Boolean> cir) {
        if (((Entity) (Object) this) instanceof Player player && AllSynchedDatas.HEAVY_BOOTS.get(player)) {
            cir.setReturnValue(false);
        }
    }

    @WrapOperation(method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean captureDrops(ServerLevel world, Entity item, Operation<Boolean> original) {
        Entity entity = (Entity) (Object) this;
        if (AllSynchedDatas.CRUSH_DROP.get(entity)) {
            item.setDeltaMovement(Vec3.ZERO);
        } else {
            Optional<List<ItemStack>> value = AllSynchedDatas.CAPTURE_DROPS.get(entity);
            if (value.isPresent()) {
                value.get().add(((ItemEntity) item).getItem());
                return true;
            }
        }
        return original.call(world, item);
    }

    @Inject(method = "spawnSprintParticle()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getDeltaMovement()Lnet/minecraft/world/phys/Vec3;"), cancellable = true)
    private void onRunningEffect(CallbackInfo ci, @Local BlockState state, @Local BlockPos pos) {
        if (state.getBlock() instanceof RunningEffectControlBlock block) {
            if (block.addRunningEffects(state, level, pos, (Entity) (Object) this)) {
                ci.cancel();
            }
        }
    }

    @WrapOperation(method = "refreshDimensions()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getDimensions(Lnet/minecraft/world/entity/Pose;)Lnet/minecraft/world/entity/EntityDimensions;"))
    private EntityDimensions calculateDimensions(Entity entity, Pose pose, Operation<EntityDimensions> original) {
        EntityDimensions dimensions = CardboardArmorHandler.playerHitboxChangesWhenHidingAsBox(entity);
        if (dimensions != null) {
            return dimensions;
        }
        return original.call(entity, pose);
    }

    @WrapOperation(method = "playStepSound(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"))
    private SoundType getStepSound(BlockState state, Operation<SoundType> original, @Local(argsOnly = true) BlockPos pos) {
        if (state.getBlock() instanceof SoundControlBlock block) {
            return block.getSoundGroup(level, pos);
        }
        return original.call(state);
    }

    @WrapOperation(method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getOnPosLegacy()Lnet/minecraft/core/BlockPos;"))
    private BlockPos fixSeatBouncing(Entity instance, Operation<BlockPos> original) {
        return getBlockStateOn().getBlock() instanceof SeatBlock ? getOnPos() : original.call(instance);
    }
}
