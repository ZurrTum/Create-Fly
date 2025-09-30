package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = BlockRenderManager.class, priority = 999)
public class BlockRenderManagerMixin {

    @Shadow
    @Final
    private Random random;

    @Shadow
    @Final
    private List<BlockModelPart> parts;

    @Shadow
    @Final
    private BlockModelRenderer blockModelRenderer;

    @Inject(method = "renderDamage(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;)V", at = @At(value = "INVOKE_ASSIGN", target = "net/minecraft/client/render/block/BlockModels.getModel(Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/render/model/BlockStateModel;", shift = At.Shift.AFTER), cancellable = true)
    private void renderDamage(
        BlockState state,
        BlockPos pos,
        BlockRenderView world,
        MatrixStack matrices,
        VertexConsumer vertexConsumer,
        CallbackInfo ci,
        @Local BlockStateModel model
    ) {
        if (WrapperBlockStateModel.unwrapCompat(model) instanceof WrapperBlockStateModel wrapper) {
            random.setSeed(state.getRenderingSeed(pos));
            parts.clear();
            wrapper.addPartsWithInfo(world, pos, state, random, parts);
            if (!parts.isEmpty()) {
                blockModelRenderer.render(world, this.parts, state, pos, matrices, vertexConsumer, true, OverlayTexture.DEFAULT_UV);
            }
            ci.cancel();
        }
    }
}
