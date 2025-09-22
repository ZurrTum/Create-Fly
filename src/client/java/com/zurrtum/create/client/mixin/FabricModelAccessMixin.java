package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import net.caffeinemc.mods.sodium.fabric.model.FabricModelAccess;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(FabricModelAccess.class)
public class FabricModelAccessMixin {
    @WrapOperation(method = "collectPartsOf(Lnet/minecraft/client/render/model/BlockStateModel;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/random/Random;Lnet/fabricmc/fabric/api/renderer/v1/mesh/QuadEmitter;)Ljava/util/List;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/BlockStateModel;addParts(Lnet/minecraft/util/math/random/Random;Ljava/util/List;)V"))
    private void addParts(
        BlockStateModel model,
        Random random,
        List<BlockModelPart> parts,
        Operation<Void> original,
        @Local(argsOnly = true) BlockRenderView world,
        @Local(argsOnly = true) BlockPos pos,
        @Local(argsOnly = true) BlockState state
    ) {
        if (model instanceof WrapperBlockStateModel wrapper) {
            wrapper.addPartsWithInfo(world, pos, state, random, parts);
        } else {
            original.call(model, random, parts);
        }
    }
}
