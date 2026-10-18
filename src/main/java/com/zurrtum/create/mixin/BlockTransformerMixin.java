package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.foundation.item.CustomTransformSoundItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockTransformer.class)
public class BlockTransformerMixin {
    @WrapOperation(method = "Lnet/minecraft/core/component/BlockTransformer;transformBlock(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    private void playe(
        Level level,
        Entity except,
        BlockPos pos,
        SoundEvent sound,
        SoundSource source,
        float volume,
        float pitch,
        Operation<Void> original,
        @Local ItemStack itemInHand
    ) {
        if (itemInHand.getItem() instanceof CustomTransformSoundItem item) {
            item.playSound(level, (Player) except, pos, sound, source, volume, pitch);
        } else {
            original.call(level, except, pos, sound, source, volume, pitch);
        }
    }
}
