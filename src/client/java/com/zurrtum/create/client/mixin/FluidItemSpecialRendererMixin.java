package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.infrastructure.fluid.FluidConfig;
import com.zurrtum.create.infrastructure.fluids.FlowableFluid;
import de.crafty.eiv.common.extra.FluidItemSpecialRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(FluidItemSpecialRenderer.class)
public class FluidItemSpecialRendererMixin {
    @WrapOperation(method = "submit(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IIZI)V", at = @At(value = "NEW", target = "java/awt/Color", ordinal = 0))
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
                rgb = config.tint().apply(stack.getComponentsPatch());
            }
        }
        return original.call(rgb);
    }

    @Inject(method = "submit(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IIZI)V", at = @At(value = "INVOKE", target = "Lde/crafty/eiv/common/CommonEIVClient;resolver()Lde/crafty/eiv/common/resolver/IEivClientResolver;", remap = false))
    private void check(
        ItemStack stack,
        ItemDisplayContext itemDisplayContext,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        int light,
        int overlay,
        boolean bl,
        int k,
        CallbackInfo ci,
        @Local LocalRef<TextureAtlasSprite> sprite,
        @Share("config") LocalRef<FluidConfig> fluidConfig
    ) {
        FluidConfig config = fluidConfig.get();
        if (config != null) {
            sprite.set(config.still().get());
        }
    }
}
