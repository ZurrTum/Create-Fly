package com.zurrtum.create.client.mixin;

import com.google.common.collect.Sets;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.AllExtensions;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.levelWrappers.WrappedClientLevel;
import com.zurrtum.create.client.content.contraptions.ContraptionHandlerClient;
import com.zurrtum.create.client.flywheel.impl.visualization.VisualizationEventHandler;
import com.zurrtum.create.client.foundation.block.render.BlockDestructionProgressExtension;
import com.zurrtum.create.client.foundation.block.render.MultiPosDestructionHandler;
import com.zurrtum.create.client.ponder.Ponder;
import com.zurrtum.create.content.contraptions.minecart.capability.CapabilityMinecartController;
import com.zurrtum.create.content.equipment.armor.CardboardArmorHandler;
import com.zurrtum.create.content.equipment.armor.DivingBootsItem;
import com.zurrtum.create.foundation.block.SoundControlBlock;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.SortedSet;

import static com.zurrtum.create.Create.REDSTONE_LINK_NETWORK_HANDLER;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin extends Level {
    @Shadow
    @Final
    private Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress;

    private ClientLevelMixin(
        WritableLevelData levelData,
        ResourceKey<Level> dimension,
        RegistryAccess registryAccess,
        Holder<DimensionType> dimensionTypeRegistration,
        boolean isClientSide,
        boolean isDebug,
        long biomeZoomSeed,
        int maxChainedNeighborUpdates
    ) {
        super(
            levelData,
            dimension,
            registryAccess,
            dimensionTypeRegistration,
            isClientSide,
            isDebug,
            biomeZoomSeed,
            maxChainedNeighborUpdates
        );
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onLoadWorld(CallbackInfo ci) {
        ClientLevel level = (ClientLevel) (Object) this;
        if (!(level instanceof WrappedClientLevel)) {
            Create.invalidateRenderers();
            AnimationTickHolder.reset();
            Ponder.invalidateRenderers();
        }
        REDSTONE_LINK_NETWORK_HANDLER.onLoadWorld(level);
    }

    @Inject(method = "addEntity(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"))
    private void addEntity(CallbackInfo ci, @Local(argsOnly = true) Entity entity) {
        ClientLevel world = (ClientLevel) (Object) this;
        VisualizationEventHandler.onEntityJoinLevel(world, entity);
        ContraptionHandlerClient.addSpawnedContraptionsToCollisionList(entity, world);
        CapabilityMinecartController.attach(entity);
    }

    @Inject(method = "tickNonPassenger(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    private void tickEntity(Entity entity, CallbackInfo ci) {
        CapabilityMinecartController.entityTick(entity);
        DivingBootsItem.accelerateDescentUnderwater(entity);
        CardboardArmorHandler.mobsMayLoseTargetWhenItIsWearingCardboard(entity);
    }

    @Inject(method = "destroyBlockProgress(ILnet/minecraft/core/BlockPos;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/BlockDestructionProgress;updateTick(J)V"))
    private void onDestroyBlockProgress(
        int id,
        BlockPos pos,
        int progress,
        CallbackInfo ci,
        @Local BlockDestructionProgress progressObj
    ) {
        BlockState state = getBlockState(pos);
        MultiPosDestructionHandler handler = AllExtensions.MULTI_POS.get(state.getBlock());
        if (handler != null) {
            Set<BlockPos> extraPositions = handler.getExtraPositions((ClientLevel) (Object) this, pos, state, progress);
            if (extraPositions != null) {
                extraPositions.remove(pos);
                ((BlockDestructionProgressExtension) progressObj).create$setExtraPositions(extraPositions);
                for (BlockPos extraPos : extraPositions) {
                    destructionProgress.computeIfAbsent(extraPos.asLong(), l -> Sets.newTreeSet()).add(progressObj);
                }
            }
        }
    }

    @Inject(method = "removeProgress(Lnet/minecraft/server/level/BlockDestructionProgress;)V", at = @At("RETURN"))
    private void onRemoveProgress(BlockDestructionProgress block, CallbackInfo ci) {
        Set<BlockPos> extraPositions = ((BlockDestructionProgressExtension) block).create$getExtraPositions();
        if (extraPositions != null) {
            for (BlockPos extraPos : extraPositions) {
                long l = extraPos.asLong();
                Set<BlockDestructionProgress> set = destructionProgress.get(l);
                if (set != null) {
                    set.remove(block);
                    if (set.isEmpty()) {
                        destructionProgress.remove(l);
                    }
                }
            }
        }
    }

    @WrapOperation(method = "playBreakingSound(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"))
    private SoundType getHitSound(
        BlockState state,
        Operation<SoundType> original,
        @Local(argsOnly = true) BlockPos pos
    ) {
        if (state.getBlock() instanceof SoundControlBlock block) {
            return block.getSoundGroup(this, pos);
        }
        return original.call(state);
    }
}
