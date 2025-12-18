package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.infrastructure.fluid.FluidConfig;
import com.zurrtum.create.infrastructure.fluids.FlowableFluid;
import de.crafty.eiv.common.extra.FluidItemSpecialRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(FluidItemSpecialRenderer.class)
public class FluidItemSpecialRendererMixin {
    @WrapOperation(method = "render(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IIZ)V", at = @At(value = "NEW", target = "java/awt/Color", ordinal = 0))
    private Color createColor(
        int rgb,
        Operation<Color> original,
        @Local ItemStack stack,
        @Local Fluid fluid,
        @Share("config") LocalRef<FluidConfig> fluidConfig
    ) {
        if (fluid instanceof FlowableFluid flowableFluid && flowableFluid.getEntry().block == null) {
            FluidConfig config = AllFluidConfigs.get(fluid);
            if (config != null) {
                fluidConfig.set(config);
                rgb = config.tint().apply(stack.getComponentChanges());
            }
        }
        return original.call(rgb);
    }

    @Inject(method = "render(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IIZ)V", at = @At(value = "INVOKE", target = "Lde/crafty/eiv/common/CommonEIVClient;resolver()Lde/crafty/eiv/common/resolver/IEivClientResolver;", remap = false))
    private void check(
        ItemStack stack,
        ItemDisplayContext itemDisplayContext,
        MatrixStack poseStack,
        VertexConsumerProvider multiBufferSource,
        int light,
        int overlay,
        boolean bl,
        CallbackInfo ci,
        @Local LocalRef<Sprite> sprite,
        @Share("config") LocalRef<FluidConfig> fluidConfig
    ) {
        FluidConfig config = fluidConfig.get();
        if (config != null) {
            sprite.set(config.still().get());
        }
    }
}
