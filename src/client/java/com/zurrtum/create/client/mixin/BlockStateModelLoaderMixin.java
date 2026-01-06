package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.AllModels;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@Mixin(BlockStateModelLoader.class)
public class BlockStateModelLoaderMixin {
    @Inject(method = "loadBlockStateDefinitionStack(Lnet/minecraft/resources/Identifier;Lnet/minecraft/world/level/block/state/StateDefinition;Ljava/util/List;)Lnet/minecraft/client/resources/model/BlockStateModelLoader$LoadedModels;", at = @At(value = "NEW", target = "(Ljava/util/Map;)Lnet/minecraft/client/resources/model/BlockStateModelLoader$LoadedModels;"))
    private static void replace(
        Identifier identifier,
        StateDefinition<Block, BlockState> stateDefinition,
        List<BlockStateModelLoader.LoadedBlockModelDefinition> list,
        CallbackInfoReturnable<BlockStateModelLoader.LoadedModels> cir,
        @Local Map<BlockState, BlockStateModel.UnbakedRoot> models
    ) {
        BiFunction<BlockState, BlockStateModel.UnbakedRoot, BlockStateModel.UnbakedRoot> factory = AllModels.ALL.get(stateDefinition.getOwner());
        if (factory != null) {
            models.replaceAll(factory);
        }
    }
}
