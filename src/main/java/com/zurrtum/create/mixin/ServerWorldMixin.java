package com.zurrtum.create.mixin;

import com.zurrtum.create.content.contraptions.minecart.capability.CapabilityMinecartController;
import com.zurrtum.create.content.equipment.armor.CardboardArmorHandler;
import com.zurrtum.create.content.equipment.armor.DivingBootsItem;
import com.zurrtum.create.content.equipment.toolbox.ToolboxHandler;
import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater;
import net.minecraft.world.block.NeighborUpdater;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {
    protected ServerWorldMixin(
        MutableWorldProperties properties,
        RegistryKey<World> registryRef,
        DynamicRegistryManager registryManager,
        RegistryEntry<DimensionType> dimensionEntry,
        boolean isClient,
        boolean debugWorld,
        long seed,
        int maxChainedNeighborUpdates
    ) {
        super(properties, registryRef, registryManager, dimensionEntry, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @Inject(method = "tickEntity(Lnet/minecraft/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tick()V"))
    private void tickEntity(Entity entity, CallbackInfo ci) {
        CapabilityMinecartController.entityTick(entity);
        DivingBootsItem.accelerateDescentUnderwater(entity);
        CardboardArmorHandler.mobsMayLoseTargetWhenItIsWearingCardboard(entity);
        ToolboxHandler.entityTick(entity, (ServerWorld) (Object) this);
    }

    @Unique
    private void updateNeighbor(BlockPos pos, Direction direction, Block sourceBlock) {
        BlockPos target = pos.offset(direction);
        BlockState state = getBlockState(target);
        if (state.getBlock() instanceof NeighborUpdateListeningBlock block) {
            block.neighborUpdate(state, (ServerWorld) (Object) this, target, sourceBlock, pos, false);
        }
    }

    @Inject(method = "updateNeighborsAlways(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/world/block/WireOrientation;)V", at = @At("HEAD"))
    private void updateNeighborsAlways(BlockPos pos, Block sourceBlock, WireOrientation orientation, CallbackInfo ci) {
        if (neighborUpdater instanceof ChainRestrictedNeighborUpdater) {
            return;
        }
        for (Direction direction : NeighborUpdater.UPDATE_ORDER) {
            updateNeighbor(pos, direction, sourceBlock);
        }
    }

    @Inject(method = "updateNeighborsExcept(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/Direction;Lnet/minecraft/world/block/WireOrientation;)V", at = @At("HEAD"))
    private void updateNeighborsExcept(BlockPos pos, Block sourceBlock, Direction except, WireOrientation orientation, CallbackInfo ci) {
        if (neighborUpdater instanceof ChainRestrictedNeighborUpdater) {
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
