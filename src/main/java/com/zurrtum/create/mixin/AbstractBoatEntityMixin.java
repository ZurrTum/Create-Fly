package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.foundation.block.SlipperinessControlBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractBoatEntity.class)
public abstract class AbstractBoatEntityMixin extends Entity {
    public AbstractBoatEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @WrapOperation(method = "getNearbySlipperiness()F", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getSlipperiness()F"))
    private float getSlipperiness(Block block, Operation<Float> original, @Local BlockPos.Mutable mutable) {
        if (block instanceof SlipperinessControlBlock controlBlock) {
            return controlBlock.getSlipperiness(getWorld(), mutable);
        }
        return original.call(block);
    }
}
