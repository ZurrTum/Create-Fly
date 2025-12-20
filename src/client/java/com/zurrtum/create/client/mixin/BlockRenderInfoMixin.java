package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.foundation.block.LightControlBlock;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings("UnstableApiUsage")
@Mixin(BlockRenderInfo.class)
public class BlockRenderInfoMixin {
    @Shadow
    public BlockRenderView blockView;

    @WrapOperation(method = "prepareForBlock(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getLuminance()I"))
    private int getLuminance(BlockState state, Operation<Integer> original, @Local(argsOnly = true) BlockPos pos) {
        if (state.getBlock() instanceof LightControlBlock block) {
            return block.getLuminance(blockView, pos);
        }
        return original.call(state);
    }
}
