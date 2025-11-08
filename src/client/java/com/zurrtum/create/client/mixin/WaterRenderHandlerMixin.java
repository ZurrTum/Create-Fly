package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.infrastructure.fluid.FluidConfig;
import com.zurrtum.create.infrastructure.fluids.FlowableFluid;
import net.minecraft.client.texture.Sprite;
import net.minecraft.component.ComponentChanges;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderHandlerRegistryImpl$WaterRenderHandler")
public class WaterRenderHandlerMixin {
    @Inject(method = "getFluidSprites(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/fluid/FluidState;)[Lnet/minecraft/client/texture/Sprite;", at = @At("HEAD"), cancellable = true)
    private void getSprites(BlockRenderView view, BlockPos pos, FluidState state, CallbackInfoReturnable<Sprite[]> cir) {
        if (state.getFluid() instanceof FlowableFluid fluid) {
            FluidConfig config = AllFluidConfigs.get(fluid);
            if (config != null) {
                cir.setReturnValue(config.toSprite());
            }
        }
    }

    @Inject(method = "getFluidColor(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/fluid/FluidState;)I", at = @At("HEAD"), cancellable = true)
    private void getTint(BlockRenderView view, BlockPos pos, FluidState state, CallbackInfoReturnable<Integer> cir) {
        if (state.getFluid() instanceof FlowableFluid fluid) {
            FluidConfig config = AllFluidConfigs.get(fluid);
            if (config != null) {
                cir.setReturnValue(config.tint().apply(ComponentChanges.EMPTY));
            }
        }
    }
}
