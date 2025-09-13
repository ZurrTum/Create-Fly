package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.foundation.block.ResistanceControlBlock;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ExplosionBehavior.class)
public class ExplosionBehaviorMixin {
    @WrapOperation(method = "getBlastResistance(Lnet/minecraft/world/explosion/Explosion;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/FluidState;)Ljava/util/Optional;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getBlastResistance()F"))
    private float getBlastResistance(
        Block block,
        Operation<Float> original,
        @Local(argsOnly = true) BlockView world,
        @Local(argsOnly = true) BlockPos pos
    ) {
        if (block instanceof ResistanceControlBlock controlBlock) {
            return controlBlock.getResistance(world, pos);
        }
        return original.call(block);
    }
}
