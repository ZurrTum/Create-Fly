package com.zurrtum.create.client.mixin;

import cc.cassian.rrv.client.extra.FluidItemSpecialRenderer;
import cc.cassian.rrv.common.extra.FluidStack;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.AllFluids;
import com.zurrtum.create.client.AllFluidConfigs;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FluidItemSpecialRenderer.class)
public class RrvFluidItemSpecialRenderer {
    @WrapOperation(method = "submit(Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IIZI)V", at = @At(value = "INVOKE", target = "Lcc/cassian/rrv/client/extra/FluidItemSpecialRenderer;getColor(Lcc/cassian/rrv/common/extra/FluidStack;Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/client/renderer/block/FluidModel;)I"))
    private int getColor(
        FluidStack fluidStack,
        Fluid fluid,
        FluidModel fluidModel,
        Operation<Integer> original,
        @Local(argsOnly = true) ItemStack stack
    ) {
        if (fluid == AllFluids.POTION) {
            return AllFluidConfigs.TINT.get(fluid).get(fluid, stack.getComponentsPatch());
        }
        return original.call(fluidStack, fluid, fluidModel);
    }
}
