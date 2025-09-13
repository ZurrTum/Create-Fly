package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.foundation.block.SlipperinessControlBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Leashable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Leashable.class)
public interface LeashableMixin {
    @WrapOperation(method = "getSlipperiness(Lnet/minecraft/entity/Entity;)F", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getSlipperiness()F"))
    private static <E extends Entity & Leashable> float getSlipperiness(Block block, Operation<Float> original, @Local(argsOnly = true) E entity) {
        if (block instanceof SlipperinessControlBlock controlBlock) {
            return controlBlock.getSlipperiness(entity.getWorld(), entity.getVelocityAffectingPos());
        }
        return original.call(block);
    }
}
