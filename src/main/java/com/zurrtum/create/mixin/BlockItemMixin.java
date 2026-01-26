package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zurrtum.create.content.contraptions.glue.SuperGlueHandler;
import com.zurrtum.create.content.equipment.symmetryWand.SymmetryHandler;
import com.zurrtum.create.content.equipment.symmetryWand.SymmetryPlacementContext;
import com.zurrtum.create.foundation.item.ItemPlacementSoundContext;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @WrapOperation(method = "useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "NEW", target = "(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/item/ItemPlacementContext;"))
    private ItemPlacementContext replaceContext(
        ItemUsageContext context,
        Operation<ItemPlacementContext> original,
        @Share("place") LocalRef<ItemPlacementContext> place
    ) {
        if (context.getWorld().isClient()) {
            return original.call(context);
        }
        if (context instanceof SymmetryPlacementContext placementContext) {
            place.set(placementContext);
            return placementContext;
        } else {
            ItemPlacementContext blockContext = original.call(context);
            place.set(blockContext);
            return blockContext;
        }
    }

    @Inject(method = "useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;", at = @At("RETURN"))
    private void useOn(
        ItemUsageContext useOnContext,
        CallbackInfoReturnable<ActionResult> cir,
        @Share("place") LocalRef<ItemPlacementContext> place
    ) {
        if (cir.getReturnValue().isAccepted()) {
            ItemPlacementContext context = place.get();
            if (context != null) {
                PlayerEntity player = context.getPlayer();
                if (player != null) {
                    ServerWorld world = (ServerWorld) context.getWorld();
                    BlockPos pos = context.getBlockPos();
                    SuperGlueHandler.glueListensForBlockPlacement(world, player, pos);
                    if (!(context instanceof SymmetryPlacementContext)) {
                        SymmetryHandler.onBlockPlaced(world, player, pos, context);
                    }
                }
            }
        }
    }

    @WrapOperation(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getSoundGroup()Lnet/minecraft/sound/BlockSoundGroup;"))
    private BlockSoundGroup checkSound(
        BlockState instance,
        Operation<BlockSoundGroup> original,
        @Local(argsOnly = true) ItemPlacementContext ctx,
        @Share("group") LocalRef<ItemPlacementSoundContext> group
    ) {
        if (ctx instanceof ItemPlacementSoundContext context) {
            group.set(context);
            return null;
        }
        return original.call(instance);
    }

    @WrapOperation(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/BlockItem;getPlaceSound(Lnet/minecraft/block/BlockState;)Lnet/minecraft/sound/SoundEvent;"))
    private SoundEvent getGroup(
        BlockItem instance,
        BlockState state,
        Operation<SoundEvent> original,
        @Share("group") LocalRef<ItemPlacementSoundContext> group
    ) {
        ItemPlacementSoundContext context = group.get();
        if (context != null) {
            SoundEvent sound = context.getSound();
            if (sound != null) {
                return sound;
            }
        }
        return original.call(instance, state);
    }

    @WrapOperation(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/sound/BlockSoundGroup;getVolume()F"))
    private float getVolume(BlockSoundGroup instance, Operation<Float> original, @Share("group") LocalRef<ItemPlacementSoundContext> group) {
        if (instance == null) {
            return group.get().getVolume();
        }
        return original.call(instance);
    }

    @WrapOperation(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/sound/BlockSoundGroup;getPitch()F"))
    private float getPitch(BlockSoundGroup instance, Operation<Float> original, @Share("group") LocalRef<ItemPlacementSoundContext> group) {
        if (instance == null) {
            return group.get().getPitch();
        }
        return original.call(instance);
    }
}
