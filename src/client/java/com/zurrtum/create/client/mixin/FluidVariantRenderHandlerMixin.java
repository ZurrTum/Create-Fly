package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.infrastructure.fluid.FluidConfig;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FluidVariantRenderHandler.class)
public interface FluidVariantRenderHandlerMixin {
    @ModifyReturnValue(method = "getSprites(Lnet/fabricmc/fabric/api/transfer/v1/fluid/FluidVariant;)[Lnet/minecraft/client/texture/Sprite;", at = @At(value = "RETURN", ordinal = 1))
    private Sprite[] getSprites(Sprite[] original, @Local(argsOnly = true) FluidVariant variant) {
        if (original != null) {
            return original;
        }
        Fluid fluid = variant.getFluid();
        FluidConfig config = AllFluidConfigs.get(fluid);
        if (config != null) {
            return config.toSprite();
        }
        return null;
    }

    @ModifyReturnValue(method = "getColor(Lnet/fabricmc/fabric/api/transfer/v1/fluid/FluidVariant;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/util/math/BlockPos;)I", at = @At(value = "RETURN", ordinal = 1))
    private int getColor(int original, @Local(argsOnly = true) FluidVariant variant) {
        if (original != -1) {
            return original;
        }
        Fluid fluid = variant.getFluid();
        FluidConfig config = AllFluidConfigs.get(fluid);
        if (config != null) {
            return config.tint().apply(variant.getComponents());
        }
        return -1;
    }
}
