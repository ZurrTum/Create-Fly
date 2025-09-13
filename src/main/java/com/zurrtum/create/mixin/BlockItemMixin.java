package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zurrtum.create.content.equipment.symmetryWand.SymmetryPlacementContext;
import com.zurrtum.create.foundation.item.ItemPlacementSoundContext;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @WrapOperation(method = "useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "NEW", target = "(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/item/ItemPlacementContext;"))
    private ItemPlacementContext replaceContext(ItemUsageContext context, Operation<ItemPlacementContext> original) {
        if (context instanceof SymmetryPlacementContext placementContext) {
            return placementContext;
        }
        return original.call(context);
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
