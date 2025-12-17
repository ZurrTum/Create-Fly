package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import com.zurrtum.create.client.AllModels;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModelEventHandler;
import com.zurrtum.create.client.model.NormalsModelElement;
import com.zurrtum.create.client.model.UnbakedModelParser;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleUnbakedGeometry;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.zurrtum.create.Create.MOD_ID;

@Mixin(ModelManager.class)
public class ModelManagerMixin {
    @Inject(method = "lambda$loadBlockModels$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/model/BlockModel;fromStream(Ljava/io/Reader;)Lnet/minecraft/client/renderer/block/model/BlockModel;"), cancellable = true)
    private static void deserialize(CallbackInfoReturnable<Pair<Identifier, BlockModel>> cir, @Local Identifier identifier, @Local Reader input) {
        if (identifier.getNamespace().equals(MOD_ID)) {
            try {
                UnbakedModel model = GsonHelper.fromJson(UnbakedModelParser.GSON, input, UnbakedModel.class);
                if (model instanceof BlockModel jsonModel) {
                    SimpleUnbakedGeometry geometry = (SimpleUnbakedGeometry) jsonModel.geometry();
                    if (geometry != null) {
                        geometry.elements().forEach(NormalsModelElement::markFacingNormals);
                    }
                    cir.setReturnValue(Pair.of(identifier, jsonModel));
                } else {
                    UnbakedModelParser.cache(identifier, model);
                    cir.setReturnValue(null);
                }
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (Exception ignore) {
                    }
                }
            }
        }
    }

    @WrapOperation(method = "lambda$loadBlockModels$3", at = @At(value = "INVOKE", target = "Ljava/util/List;stream()Ljava/util/stream/Stream;"))
    private static Stream<Pair<Identifier, UnbakedModel>> replace(
        List<Pair<Identifier, UnbakedModel>> instance,
        Operation<Stream<Pair<Identifier, UnbakedModel>>> original
    ) {
        return Stream.concat(original.call(instance), UnbakedModelParser.getCaches());
    }

    @Inject(method = "discoverModelDependencies", at = @At(value = "NEW", target = "(Lnet/minecraft/client/resources/model/ResolvedModel;Ljava/util/Map;)Lnet/minecraft/client/resources/model/ModelManager$ResolvedModels;"))
    private static void collect(
        Map<Identifier, UnbakedModel> modelMap,
        BlockStateModelLoader.LoadedModels stateDefinition,
        ClientItemInfoLoader.LoadedClientInfos result,
        CallbackInfoReturnable<ModelManager.ResolvedModels> cir,
        @Local ModelDiscovery collector
    ) {
        Map<BlockState, BlockStateModel.UnbakedRoot> models = stateDefinition.models();
        AllModels.ALL.forEach((state, resolver) -> {
            BlockStateModel.UnbakedRoot unbaked = resolver.apply(state, models.get(state));
            unbaked.resolveDependencies(collector::getOrCreateModel);
            models.put(state, unbaked);
        });
        PartialModelEventHandler.getRegisterAdditional().keySet().forEach(collector::getOrCreateModel);
    }
}
