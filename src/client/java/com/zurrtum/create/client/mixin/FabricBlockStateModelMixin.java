package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBlockStateModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(FabricBlockStateModel.class)
public interface FabricBlockStateModelMixin {
    @WrapOperation(method = "emitQuads(Lnet/fabricmc/fabric/api/renderer/v1/mesh/QuadEmitter;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/random/Random;Ljava/util/function/Predicate;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/BlockStateModel;getParts(Lnet/minecraft/util/math/random/Random;)Ljava/util/List;"))
    private List<BlockModelPart> getParts(
        BlockStateModel instance,
        Random random,
        Operation<List<BlockModelPart>> original,
        @Local(argsOnly = true) BlockRenderView world,
        @Local(argsOnly = true) BlockPos pos,
        @Local(argsOnly = true) BlockState state
    ) {
        if (instance instanceof WrapperBlockStateModel wrapper) {
            List<BlockModelPart> list = new ObjectArrayList<>();
            wrapper.addPartsWithInfo(world, pos, state, random, list);
            return list;
        }
        return original.call(instance, random);
    }
}
