package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zurrtum.create.content.equipment.symmetryWand.SymmetryPlacementContext;
import com.zurrtum.create.foundation.item.ItemPlacementSoundContext;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @WrapOperation(method = "useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "NEW", target = "(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/item/context/BlockPlaceContext;"))
    private BlockPlaceContext replaceContext(UseOnContext context, Operation<BlockPlaceContext> original) {
        if (context instanceof SymmetryPlacementContext placementContext) {
            return placementContext;
        }
        return original.call(context);
    }

    @WrapOperation(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"))
    private SoundType checkSound(
        BlockState instance,
        Operation<SoundType> original,
        @Local(argsOnly = true) BlockPlaceContext ctx,
        @Share("group") LocalRef<ItemPlacementSoundContext> group
    ) {
        if (ctx instanceof ItemPlacementSoundContext context) {
            group.set(context);
            return null;
        }
        return original.call(instance);
    }

    @WrapOperation(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BlockItem;getPlaceSound(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/sounds/SoundEvent;"))
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

    @WrapOperation(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/SoundType;getVolume()F"))
    private float getVolume(SoundType instance, Operation<Float> original, @Share("group") LocalRef<ItemPlacementSoundContext> group) {
        if (instance == null) {
            return group.get().getVolume();
        }
        return original.call(instance);
    }

    @WrapOperation(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/SoundType;getPitch()F"))
    private float getPitch(SoundType instance, Operation<Float> original, @Share("group") LocalRef<ItemPlacementSoundContext> group) {
        if (instance == null) {
            return group.get().getPitch();
        }
        return original.call(instance);
    }
}
