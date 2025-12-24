package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.foundation.block.LightControlBlock;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings("UnstableApiUsage")
@Mixin(BlockRenderInfo.class)
public class BlockRenderInfoMixin {
    @Shadow
    public BlockAndTintGetter blockView;

    @WrapOperation(method = "prepareForBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I"))
    private int getLuminance(BlockState state, Operation<Integer> original, @Local(argsOnly = true) BlockPos pos) {
        if (state.getBlock() instanceof LightControlBlock block) {
            return block.getLuminance(blockView, pos);
        }
        return original.call(state);
    }
}