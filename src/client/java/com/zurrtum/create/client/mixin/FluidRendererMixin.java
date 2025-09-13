package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.infrastructure.fluid.FluidConfig;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FluidRenderer.class)
public class FluidRendererMixin {
    @ModifyVariable(method = "render(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/FluidState;)V", at = @At(value = "STORE", ordinal = 0))
    private Sprite[] modSpriteArray(Sprite[] sprites, @Local(argsOnly = true) FluidState state, @Share("config") LocalRef<FluidConfig> ref) {
        FluidConfig config = AllFluidConfigs.ALL.get(state.getFluid());
        if (config != null) {
            ref.set(config);
            return config.toSprite();
        }
        return sprites;
    }

    @SuppressWarnings("DiscouragedShift")
    @ModifyVariable(method = "render(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/FluidState;)V", at = @At(value = "CONSTANT", args = "intValue=16", ordinal = 0, shift = At.Shift.BEFORE), ordinal = 0)
    private int modTintColor(int tint, @Share("config") LocalRef<FluidConfig> ref) {
        FluidConfig config = ref.get();
        if (config != null) {
            return config.tint().get();
        }
        return tint;
    }
}
