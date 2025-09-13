package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.foundation.block.EnchantingControlBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantingTableBlock.class)
public class EnchantingTableBlockMixin {
    @WrapOperation(method = "canAccessPowerProvider(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;", ordinal = 0))
    private static BlockState getEnchantmentPowerProvider(World world, BlockPos pos, Operation<BlockState> original) {
        BlockState state = original.call(world, pos);
        if (state.getBlock() instanceof EnchantingControlBlock block) {
            return block.getEnchantmentPowerProvider(world, pos);
        }
        return state;
    }
}
