package com.zurrtum.create.mixin;

import com.zurrtum.create.content.contraptions.minecart.capability.CapabilityMinecartController;
import com.zurrtum.create.content.equipment.armor.CardboardArmorHandler;
import com.zurrtum.create.content.equipment.armor.DivingBootsItem;
import com.zurrtum.create.content.equipment.toolbox.ToolboxHandler;
import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.redstone.CollectingNeighborUpdater;
import net.minecraft.world.level.redstone.NeighborUpdater;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level {
    protected ServerLevelMixin(
        WritableLevelData properties,
        ResourceKey<Level> registryRef,
        RegistryAccess registryManager,
        Holder<DimensionType> dimensionEntry,
        boolean isClient,
        boolean debugWorld,
        long seed,
        int maxChainedNeighborUpdates
    ) {
        super(properties, registryRef, registryManager, dimensionEntry, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @Inject(method = "tickNonPassenger(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    private void tickEntity(Entity entity, CallbackInfo ci) {
        CapabilityMinecartController.entityTick(entity);
        DivingBootsItem.accelerateDescentUnderwater(entity);
        CardboardArmorHandler.mobsMayLoseTargetWhenItIsWearingCardboard(entity);
        ToolboxHandler.entityTick(entity, (ServerLevel) (Object) this);
    }

    @Unique
    private void updateNeighbor(BlockPos pos, Direction direction, Block sourceBlock) {
        BlockPos target = pos.relative(direction);
        BlockState state = getBlockState(target);
        if (state.getBlock() instanceof NeighborUpdateListeningBlock block) {
            block.neighborUpdate(state, (ServerLevel) (Object) this, target, sourceBlock, pos, false);
        }
    }

    @Inject(method = "updateNeighborsAt(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/redstone/Orientation;)V", at = @At("HEAD"))
    private void updateNeighborsAlways(BlockPos pos, Block sourceBlock, Orientation orientation, CallbackInfo ci) {
        if (neighborUpdater instanceof CollectingNeighborUpdater) {
            return;
        }
        for (Direction direction : NeighborUpdater.UPDATE_ORDER) {
            updateNeighbor(pos, direction, sourceBlock);
        }
    }

    @Inject(method = "updateNeighborsAtExceptFromFacing(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/redstone/Orientation;)V", at = @At("HEAD"))
    private void updateNeighborsExcept(BlockPos pos, Block sourceBlock, Direction except, Orientation orientation, CallbackInfo ci) {
        if (neighborUpdater instanceof CollectingNeighborUpdater) {
            return;
        }
        for (Direction direction : NeighborUpdater.UPDATE_ORDER) {
            if (direction == except) {
                continue;
            }
            updateNeighbor(pos, direction, sourceBlock);
        }
    }
}
