package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.infrastructure.fluid.FluidConfig;
import com.zurrtum.create.infrastructure.fluids.FlowableFluid;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderHandlerRegistryImpl$WaterRenderHandler")
public class WaterRenderHandlerMixin {
    @Inject(method = "getFluidSprites(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/FluidState;)[Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;", at = @At("HEAD"), cancellable = true)
    private void getSprites(BlockAndTintGetter view, BlockPos pos, FluidState state, CallbackInfoReturnable<TextureAtlasSprite[]> cir) {
        if (state.getType() instanceof FlowableFluid fluid) {
            FluidConfig config = AllFluidConfigs.get(fluid);
            if (config != null) {
                cir.setReturnValue(config.toSprite());
            }
        }
    }

    @Inject(method = "getFluidColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/FluidState;)I", at = @At("HEAD"), cancellable = true)
    private void getTint(BlockAndTintGetter view, BlockPos pos, FluidState state, CallbackInfoReturnable<Integer> cir) {
        if (state.getType() instanceof FlowableFluid fluid) {
            FluidConfig config = AllFluidConfigs.get(fluid);
            if (config != null) {
                cir.setReturnValue(config.tint().apply(DataComponentPatch.EMPTY));
            }
        }
    }
}
