package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import de.crafty.eiv.common.extra.FluidItemSpecialRenderer;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.awt.*;

@Mixin(FluidItemSpecialRenderer.class)
public class EivFluidItemSpecialRendererMixin {
    @WrapOperation(method = "submit(Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IIZI)V", at = @At(value = "NEW", target = "(I)Ljava/awt/Color;"))
    private Color createColor(int rgb, Operation<Color> original, @Local ItemStack stack, @Local Fluid fluid) {
        if (fluid != Fluids.WATER && fluid != Fluids.LAVA) {
            rgb = FluidVariantRendering.getColor(FluidVariant.of(fluid, stack.getComponentsPatch()));
        }
        return original.call(rgb);
    }
}
