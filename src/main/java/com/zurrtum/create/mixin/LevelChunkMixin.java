package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SyncedBlockEntity;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.foundation.item.ItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {
    @Shadow
    public abstract Level getLevel();

    @Shadow
    public abstract Map<BlockPos, BlockEntity> getBlockEntities();

    @Inject(method = "setBlockEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private void setBlockEntity(BlockEntity blockEntity, CallbackInfo info, @Local BlockPos pos) {
        if (getLevel() instanceof ServerLevel) {
            ItemHelper.invalidateInventoryCache(pos);
            FluidHelper.invalidateInventoryCache(pos);
        }
    }

    @WrapOperation(method = "getBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/chunk/LevelChunk$EntityCreationType;)Lnet/minecraft/world/level/block/entity/BlockEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;isRemoved()Z"))
    private boolean onRemoveBlockEntity(BlockEntity instance, Operation<Boolean> original, @Local(argsOnly = true) BlockPos pos) {
        if (original.call(instance)) {
            ItemHelper.invalidateInventoryCache(pos);
            FluidHelper.invalidateInventoryCache(pos);
            return true;
        }
        return false;
    }

    @Inject(method = "removeBlockEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;removeGameEventListener(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/server/level/ServerLevel;)V"))
    private void onRemoveBlockEntity(BlockPos pos, CallbackInfo ci) {
        ItemHelper.invalidateInventoryCache(pos);
        FluidHelper.invalidateInventoryCache(pos);
    }

    @WrapOperation(method = "method_31716(Lnet/minecraft/util/ProblemReporter$ScopedCollector;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntityType;Lnet/minecraft/nbt/CompoundTag;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;loadWithComponents(Lnet/minecraft/world/level/storage/ValueInput;)V"))
    private void handleUpdateTag(BlockEntity blockEntity, ValueInput view, Operation<Void> original) {
        if (blockEntity instanceof SyncedBlockEntity syncedBlockEntity) {
            syncedBlockEntity.handleUpdateTag(view);
        } else {
            original.call(blockEntity, view);
        }
    }

    @Inject(method = "clearAllBlockEntities()V", at = @At("HEAD"))
    private void clear(CallbackInfo ci) {
        getBlockEntities().values().forEach(blockEntity -> {
            if (blockEntity instanceof SmartBlockEntity sbe) {
                sbe.onChunkUnloaded();
            }
        });
    }
}
