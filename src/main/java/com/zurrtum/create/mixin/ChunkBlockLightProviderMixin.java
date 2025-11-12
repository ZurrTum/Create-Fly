package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.foundation.block.LightControlBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.level.lighting.BlockLightSectionStorage;
import net.minecraft.world.level.lighting.LightEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockLightEngine.class)
public abstract class ChunkBlockLightProviderMixin extends LightEngine<BlockLightSectionStorage.BlockDataLayerStorageMap, BlockLightSectionStorage> {
    @Shadow
    @Final
    private BlockPos.MutableBlockPos mutablePos;

    protected ChunkBlockLightProviderMixin(LightChunkGetter chunkProvider, BlockLightSectionStorage lightStorage) {
        super(chunkProvider, lightStorage);
    }

    @WrapOperation(method = "getEmission(JLnet/minecraft/world/level/block/state/BlockState;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I"))
    private int getLuminance(BlockState state, Operation<Integer> original) {
        if (state.getBlock() instanceof LightControlBlock block) {
            return block.getLuminance(chunkSource.getLevel(), mutablePos);
        }
        return original.call(state);
    }
}
