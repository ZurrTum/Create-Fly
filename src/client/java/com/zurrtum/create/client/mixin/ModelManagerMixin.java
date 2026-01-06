package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModelEventHandler;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(ModelManager.class)
public class ModelManagerMixin {
    @Inject(method = "discoverModelDependencies", at = @At(value = "NEW", target = "(Lnet/minecraft/client/resources/model/ResolvedModel;Ljava/util/Map;)Lnet/minecraft/client/resources/model/ModelManager$ResolvedModels;"))
    private static void collect(
        Map<Identifier, UnbakedModel> modelMap,
        BlockStateModelLoader.LoadedModels stateDefinition,
        ClientItemInfoLoader.LoadedClientInfos result,
        CallbackInfoReturnable<ModelManager.ResolvedModels> cir,
        @Local ModelDiscovery collector
    ) {
        PartialModelEventHandler.getRegisterAdditional().keySet().forEach(collector::getOrCreateModel);
    }
}
