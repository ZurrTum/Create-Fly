package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.infrastructure.fluid.FluidConfig;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LiquidBlockRenderer.class)
public class FluidRendererMixin {
    @ModifyVariable(method = "tesselate(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V", at = @At(value = "STORE", ordinal = 0))
    private TextureAtlasSprite[] modSpriteArray(TextureAtlasSprite[] sprites, @Local(argsOnly = true) FluidState state, @Share("config") LocalRef<FluidConfig> ref) {
        FluidConfig config = AllFluidConfigs.ALL.get(state.getType());
        if (config != null) {
            ref.set(config);
            return config.toSprite();
        }
        return sprites;
    }

    @SuppressWarnings("DiscouragedShift")
    @ModifyVariable(method = "tesselate(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V", at = @At(value = "CONSTANT", args = "intValue=16", ordinal = 0, shift = At.Shift.BEFORE), ordinal = 0)
    private int modTintColor(int tint, @Share("config") LocalRef<FluidConfig> ref) {
        FluidConfig config = ref.get();
        if (config != null) {
            return config.tint().apply(DataComponentPatch.EMPTY);
        }
        return tint;
    }
}
