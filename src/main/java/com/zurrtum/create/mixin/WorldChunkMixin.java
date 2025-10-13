package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SyncedBlockEntity;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.foundation.item.ItemHelper;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin {
    @Shadow
    public abstract World getWorld();

    @Shadow
    public abstract Map<BlockPos, BlockEntity> getBlockEntities();

    @Inject(method = "setBlockEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
    private void setBlockEntity(BlockEntity blockEntity, CallbackInfo info, @Local BlockPos pos) {
        if (getWorld() instanceof ServerWorld) {
            ItemHelper.invalidateInventoryCache(pos);
            FluidHelper.invalidateInventoryCache(pos);
        }
    }

    @WrapOperation(method = "getBlockEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/chunk/WorldChunk$CreationType;)Lnet/minecraft/block/entity/BlockEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;isRemoved()Z"))
    private boolean onRemoveBlockEntity(BlockEntity instance, Operation<Boolean> original, @Local(argsOnly = true) BlockPos pos) {
        if (original.call(instance)) {
            ItemHelper.invalidateInventoryCache(pos);
            FluidHelper.invalidateInventoryCache(pos);
            return true;
        }
        return false;
    }

    @Inject(method = "removeBlockEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;removeGameEventListener(Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/server/world/ServerWorld;)V"))
    private void onRemoveBlockEntity(BlockPos pos, CallbackInfo ci) {
        ItemHelper.invalidateInventoryCache(pos);
        FluidHelper.invalidateInventoryCache(pos);
    }

    @WrapOperation(method = "method_31716(Lnet/minecraft/util/ErrorReporter$Logging;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntityType;Lnet/minecraft/nbt/NbtCompound;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;read(Lnet/minecraft/storage/ReadView;)V"))
    private void handleUpdateTag(BlockEntity blockEntity, ReadView view, Operation<Void> original) {
        if (blockEntity instanceof SyncedBlockEntity syncedBlockEntity) {
            syncedBlockEntity.handleUpdateTag(view);
        } else {
            original.call(blockEntity, view);
        }
    }

    @Inject(method = "clear()V", at = @At("HEAD"))
    private void clear(CallbackInfo ci) {
        getBlockEntities().values().forEach(blockEntity -> {
            if (blockEntity instanceof SmartBlockEntity sbe) {
                sbe.onChunkUnloaded();
            }
        });
    }
}
