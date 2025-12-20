package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.foundation.block.LightControlBlock;
import net.caffeinemc.mods.sodium.client.render.frapi.render.AbstractBlockRenderContext;
import net.caffeinemc.mods.sodium.client.services.PlatformBlockAccess;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractBlockRenderContext.class)
public class AbstractBlockRenderContextMixin {
    @WrapOperation(method = "prepareAoInfo(Z)V", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/services/PlatformBlockAccess;getLightEmission(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/util/math/BlockPos;)I"))
    private int getLightEmission(
        PlatformBlockAccess instance,
        BlockState blockState,
        BlockRenderView blockRenderView,
        BlockPos pos,
        Operation<Integer> original
    ) {
        if (blockState.getBlock() instanceof LightControlBlock block) {
            return block.getLuminance(blockRenderView, pos);
        }
        return original.call(instance, blockState, blockRenderView, pos);
    }
}
