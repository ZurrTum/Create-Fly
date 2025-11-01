package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.foundation.block.SoundControlBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.world.WorldEventHandler;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WorldEventHandler.class)
public class WorldEventHandlerMixin {
    @Shadow
    @Final
    private ClientWorld world;

    @WrapOperation(method = "processWorldEvent(ILnet/minecraft/util/math/BlockPos;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getSoundGroup()Lnet/minecraft/sound/BlockSoundGroup;"))
    private BlockSoundGroup getBreakSound(BlockState state, Operation<BlockSoundGroup> original, @Local(argsOnly = true) BlockPos pos) {
        if (state.getBlock() instanceof SoundControlBlock block) {
            return block.getSoundGroup(world, pos);
        }
        return original.call(state);
    }
}
