package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.AllModels;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.model.BlockStatesLoader;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@Mixin(BlockStatesLoader.class)
public class BlockStatesLoaderMixin {
    @Inject(method = "combine(Lnet/minecraft/util/Identifier;Lnet/minecraft/state/StateManager;Ljava/util/List;)Lnet/minecraft/client/render/model/BlockStatesLoader$LoadedModels;", at = @At(value = "NEW", target = "(Ljava/util/Map;)Lnet/minecraft/client/render/model/BlockStatesLoader$LoadedModels;"))
    private static void replace(
        Identifier id,
        StateManager<Block, BlockState> stateManager,
        List<BlockStatesLoader.LoadedBlockStateDefinition> definitions,
        CallbackInfoReturnable<BlockStatesLoader.LoadedModels> cir,
        @Local Map<BlockState, BlockStateModel.UnbakedGrouped> models
    ) {
        BiFunction<BlockState, BlockStateModel.UnbakedGrouped, BlockStateModel.UnbakedGrouped> factory = AllModels.ALL.get(stateManager.getOwner());
        if (factory != null) {
            models.replaceAll(factory);
        }
    }
}
