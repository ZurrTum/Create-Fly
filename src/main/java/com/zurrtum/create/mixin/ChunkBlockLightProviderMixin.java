package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.foundation.block.LightControlBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.light.BlockLightStorage;
import net.minecraft.world.chunk.light.ChunkBlockLightProvider;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkBlockLightProvider.class)
public abstract class ChunkBlockLightProviderMixin extends ChunkLightProvider<BlockLightStorage.Data, BlockLightStorage> {
    @Shadow
    @Final
    private BlockPos.Mutable mutablePos;

    protected ChunkBlockLightProviderMixin(ChunkProvider chunkProvider, BlockLightStorage lightStorage) {
        super(chunkProvider, lightStorage);
    }

    @WrapOperation(method = "getLightSourceLuminance(JLnet/minecraft/block/BlockState;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getLuminance()I"))
    private int getLuminance(BlockState state, Operation<Integer> original) {
        if (state.getBlock() instanceof LightControlBlock block) {
            return block.getLuminance(chunkProvider.getWorld(), mutablePos);
        }
        return original.call(state);
    }
}
