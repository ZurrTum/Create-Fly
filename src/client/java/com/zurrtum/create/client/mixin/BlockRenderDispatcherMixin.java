package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = BlockRenderDispatcher.class, priority = 999)
public class BlockRenderDispatcherMixin {

    @Shadow
    @Final
    private RandomSource singleThreadRandom;

    @Shadow
    @Final
    private List<BlockModelPart> singleThreadPartList;

    @Shadow
    @Final
    private ModelBlockRenderer modelRenderer;

    @Inject(method = "renderBreakingTexture(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/renderer/block/BlockModelShaper;getBlockModel(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/renderer/block/model/BlockStateModel;", shift = At.Shift.AFTER), cancellable = true)
    private void renderDamage(
        BlockState state,
        BlockPos pos,
        BlockAndTintGetter world,
        PoseStack matrices,
        VertexConsumer vertexConsumer,
        CallbackInfo ci,
        @Local BlockStateModel model
    ) {
        if (WrapperBlockStateModel.unwrapCompat(model) instanceof WrapperBlockStateModel wrapper) {
            singleThreadRandom.setSeed(state.getSeed(pos));
            singleThreadPartList.clear();
            wrapper.addPartsWithInfo(world, pos, state, singleThreadRandom, singleThreadPartList);
            if (!singleThreadPartList.isEmpty()) {
                modelRenderer.tesselateBlock(world, this.singleThreadPartList, state, pos, matrices, vertexConsumer, true, OverlayTexture.NO_OVERLAY);
            }
            ci.cancel();
        }
    }
}
