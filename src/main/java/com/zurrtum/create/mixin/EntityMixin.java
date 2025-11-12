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
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.data.DataTracked;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(Entity.class)
public abstract class EntityMixin implements DataTracked {
    @Shadow
    private EntityDimensions dimensions;

    @Shadow
    private World world;

    @Shadow
    public abstract BlockState getSteppingBlockState();

    @Shadow
    public abstract BlockPos getSteppingPos();

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityDimensions;eyeHeight()F"))
    private float setEyeHeight(EntityDimensions dimensions, Operation<Float> original) {
        if (this instanceof DeployerPlayer) {
            this.dimensions = dimensions.withEyeHeight(0);
            return 0;
        }
        return original.call(dimensions);
    }

    @Inject(method = "tickRiding()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tick()V"))
    private void tickRiding(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        CapabilityMinecartController.entityTick(entity);
        DivingBootsItem.accelerateDescentUnderwater(entity);
        CardboardArmorHandler.mobsMayLoseTargetWhenItIsWearingCardboard(entity);
        ToolboxHandler.entityTick(entity, world);
    }

    @Inject(method = "startRiding(Lnet/minecraft/entity/Entity;ZZ)Z", at = @At(value = "HEAD"), cancellable = true)
    private void startRiding(Entity entity, boolean force, boolean emitEvent, CallbackInfoReturnable<Boolean> cir) {
        if (CouplingHandler.preventEntitiesFromMoutingOccupiedCart((Entity) (Object) this, entity)) {
            cir.setReturnValue(false);
        }
    }

    @ModifyReturnValue(method = "isFireImmune()Z", at = @At("RETURN"))
    private boolean isFireImmune(boolean original) {
        if (original) {
            return true;
        }
        return ((Entity) (Object) this) instanceof PlayerEntity player && AllSynchedDatas.FIRE_IMMUNE.get(player);
    }

    @Inject(method = "isSubmergedInWater()Z", at = @At("HEAD"), cancellable = true)
    private void isSubmergedInWater(CallbackInfoReturnable<Boolean> cir) {
        if (((Entity) (Object) this) instanceof PlayerEntity player && AllSynchedDatas.HEAVY_BOOTS.get(player)) {
            cir.setReturnValue(false);
        }
    }

    @WrapOperation(method = "dropStack(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/entity/ItemEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    private boolean captureDrops(ServerWorld world, Entity item, Operation<Boolean> original) {
        Entity entity = (Entity) (Object) this;
        if (AllSynchedDatas.CRUSH_DROP.get(entity)) {
            item.setVelocity(Vec3d.ZERO);
        } else {
            Optional<List<ItemStack>> value = AllSynchedDatas.CAPTURE_DROPS.get(entity);
            if (value.isPresent()) {
                value.get().add(((ItemEntity) item).getStack());
                return true;
            }
        }
        return original.call(world, item);
    }

    @Inject(method = "spawnSprintingParticles()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getVelocity()Lnet/minecraft/util/math/Vec3d;"), cancellable = true)
    private void onRunningEffect(CallbackInfo ci, @Local BlockState state, @Local BlockPos pos) {
        if (state.getBlock() instanceof RunningEffectControlBlock block) {
            if (block.addRunningEffects(state, world, pos, (Entity) (Object) this)) {
                ci.cancel();
            }
        }
    }

    @WrapOperation(method = "calculateDimensions()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getDimensions(Lnet/minecraft/entity/EntityPose;)Lnet/minecraft/entity/EntityDimensions;"))
    private EntityDimensions calculateDimensions(Entity entity, EntityPose pose, Operation<EntityDimensions> original) {
        EntityDimensions dimensions = CardboardArmorHandler.playerHitboxChangesWhenHidingAsBox(entity);
        if (dimensions != null) {
            return dimensions;
        }
        return original.call(entity, pose);
    }

    @WrapOperation(method = "playStepSound(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getSoundGroup()Lnet/minecraft/sound/BlockSoundGroup;"))
    private BlockSoundGroup getStepSound(BlockState state, Operation<BlockSoundGroup> original, @Local(argsOnly = true) BlockPos pos) {
        if (state.getBlock() instanceof SoundControlBlock block) {
            return block.getSoundGroup(world, pos);
        }
        return original.call(state);
    }

    @WrapOperation(method = "move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getLandingPos()Lnet/minecraft/util/math/BlockPos;"))
    private BlockPos fixSeatBouncing(Entity instance, Operation<BlockPos> original) {
        return getSteppingBlockState().getBlock() instanceof SeatBlock ? getSteppingPos() : original.call(instance);
    }
}
