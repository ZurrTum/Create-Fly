package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import com.zurrtum.create.client.AllModels;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModelEventHandler;
import com.zurrtum.create.client.model.NormalsModelElement;
import com.zurrtum.create.client.model.UnbakedModelParser;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.ItemAssetsLoader;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.thread.AsyncHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import static com.zurrtum.create.Create.MOD_ID;

@Mixin(BakedModelManager.class)
public class BakedModelManagerMixin {
    @Inject(method = "method_65750", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/json/JsonUnbakedModel;deserialize(Ljava/io/Reader;)Lnet/minecraft/client/render/model/json/JsonUnbakedModel;"), cancellable = true)
    private static void deserialize(
        CallbackInfoReturnable<Pair<Identifier, JsonUnbakedModel>> cir,
        @Local Identifier identifier,
        @Local Reader input
    ) {
        if (identifier.getNamespace().equals(MOD_ID)) {
            try {
                UnbakedModel model = JsonHelper.deserialize(UnbakedModelParser.GSON, input, UnbakedModel.class);
                if (model instanceof JsonUnbakedModel jsonModel) {
                    UnbakedGeometry geometry = (UnbakedGeometry) jsonModel.geometry();
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

    @WrapOperation(method = "method_45897", at = @At(value = "INVOKE", target = "Ljava/util/List;stream()Ljava/util/stream/Stream;"))
    private static Stream<Pair<Identifier, UnbakedModel>> replace(
        List<Pair<Identifier, UnbakedModel>> instance,
        Operation<Stream<Pair<Identifier, UnbakedModel>>> original
    ) {
        return Stream.concat(original.call(instance), UnbakedModelParser.getCaches());
    }

    @Inject(method = "collect", at = @At(value = "NEW", target = "(Lnet/minecraft/client/render/model/BakedSimpleModel;Ljava/util/Map;)Lnet/minecraft/client/render/model/BakedModelManager$Models;"))
    private static void collect(
        Map<Identifier, UnbakedModel> modelMap,
        BlockStatesLoader.LoadedModels stateDefinition,
        ItemAssetsLoader.Result result,
        CallbackInfoReturnable<BakedModelManager.Models> cir,
        @Local ReferencedModelsCollector collector
    ) {
        Map<BlockState, BlockStateModel.UnbakedGrouped> models = stateDefinition.models();
        AllModels.ALL.forEach((state, resolver) -> {
            BlockStateModel.UnbakedGrouped unbaked = resolver.apply(state, models.get(state));
            unbaked.resolve(collector::resolve);
            models.put(state, unbaked);
        });
        PartialModelEventHandler.getRegisterAdditional().keySet().forEach(collector::resolve);
    }

    @WrapOperation(method = "bake", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/ModelBaker;bake(Lnet/minecraft/client/render/model/ErrorCollectingSpriteGetter;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private static CompletableFuture<ModelBaker.BakedModels> bake(
        ModelBaker baker,
        ErrorCollectingSpriteGetter spriteGetter,
        Executor executor,
        Operation<CompletableFuture<ModelBaker.BakedModels>> original
    ) {
        ModelBaker.BakerImpl bakerImpl = baker.new BakerImpl(spriteGetter);
        CompletableFuture<ModelBaker.BakedModels> modelsCompletableFuture = original.call(baker, spriteGetter, executor);
        return AsyncHelper.mapValues(
            PartialModelEventHandler.getRegisterAdditional(), (id, model) -> {
                GeometryBakedModel bakedModel = GeometryBakedModel.create(bakerImpl, id, ModelRotation.X0_Y0);
                PartialModelEventHandler.onBakingCompleted(model, bakedModel);
                return bakedModel;
            }, executor
        ).thenAccept(PartialModelEventHandler::onBakingCompleted).thenCompose(v -> modelsCompletableFuture);
    }
}
