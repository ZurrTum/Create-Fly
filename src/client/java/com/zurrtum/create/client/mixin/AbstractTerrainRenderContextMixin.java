package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.foundation.block.SelfEmissiveLightingBlock;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.AbstractTerrainRenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.LightDataProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings("UnstableApiUsage")
@Mixin(AbstractTerrainRenderContext.class)
public class AbstractTerrainRenderContextMixin {
    @Shadow(remap = false)
    @Final
    protected BlockRenderInfo blockInfo;

    @WrapOperation(method = "flatLight(Lnet/fabricmc/fabric/impl/client/indigo/renderer/mesh/MutableQuadViewImpl;)I", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/impl/client/indigo/renderer/render/LightDataProvider;light(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)I"))
    private int light(LightDataProvider instance, BlockPos pos, BlockState blockState, Operation<Integer> original) {
        if (blockState.getBlock() instanceof SelfEmissiveLightingBlock) {
            pos = blockInfo.blockPos;
        }
        return original.call(instance, pos, blockState);
    }
}