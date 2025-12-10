package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import net.caffeinemc.mods.sodium.fabric.model.FabricModelAccess;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(FabricModelAccess.class)
public class FabricModelAccessMixin {
    @WrapOperation(method = "collectPartsOf(Lnet/minecraft/client/renderer/block/model/BlockStateModel;Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/util/RandomSource;Lnet/caffeinemc/mods/sodium/client/render/helper/ListStorage;)Ljava/util/List;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/model/BlockStateModel;collectParts(Lnet/minecraft/util/RandomSource;Ljava/util/List;)V"))
    private void addParts(
        BlockStateModel model,
        RandomSource random,
        List<BlockModelPart> parts,
        Operation<Void> original,
        @Local(argsOnly = true) BlockAndTintGetter world,
        @Local(argsOnly = true) BlockPos pos,
        @Local(argsOnly = true) BlockState state
    ) {
        if (WrapperBlockStateModel.unwrapCompat(model) instanceof WrapperBlockStateModel wrapper) {
            wrapper.addPartsWithInfo(world, pos, state, random, parts);
        } else {
            original.call(model, random, parts);
        }
    }
}
