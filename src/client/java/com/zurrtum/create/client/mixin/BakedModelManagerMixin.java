package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModelEventHandler;
import net.minecraft.client.item.ItemAssetsLoader;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.BlockStatesLoader;
import net.minecraft.client.render.model.ReferencedModelsCollector;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(BakedModelManager.class)
public class BakedModelManagerMixin {
    @Inject(method = "collect", at = @At(value = "NEW", target = "(Lnet/minecraft/client/render/model/BakedSimpleModel;Ljava/util/Map;)Lnet/minecraft/client/render/model/BakedModelManager$Models;"))
    private static void collect(
        Map<Identifier, UnbakedModel> modelMap,
        BlockStatesLoader.LoadedModels stateDefinition,
        ItemAssetsLoader.Result result,
        CallbackInfoReturnable<BakedModelManager.Models> cir,
        @Local ReferencedModelsCollector collector
    ) {
        if (modelMap.isEmpty()) {
            return;
        }
        PartialModelEventHandler.getRegisterAdditional().keySet().forEach(collector::resolve);
    }
}
