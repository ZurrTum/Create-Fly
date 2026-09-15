package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.foundation.model.SimpleModelPart;
import com.zurrtum.create.client.model.obj.ObjModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SimpleModelWrapper.class)
public class SimpleModelWrapperMixin {
    @Inject(method = "bake(Lnet/minecraft/client/resources/model/ModelBaker;Lnet/minecraft/resources/Identifier;Lnet/minecraft/client/renderer/block/dispatch/ModelState;)Lnet/minecraft/client/renderer/block/dispatch/BlockStateModelPart;", at = @At(value = "NEW", target = "(Lnet/minecraft/client/resources/model/geometry/QuadCollection;ZLnet/minecraft/client/resources/model/sprite/Material$Baked;)Lnet/minecraft/client/resources/model/SimpleModelWrapper;"), cancellable = true)
    private static void bake(
        ModelBaker modelBakery,
        Identifier location,
        ModelState state,
        CallbackInfoReturnable<BlockStateModelPart> cir,
        @Local(name = "model") ResolvedModel model,
        @Local(name = "geometry") QuadCollection geometry,
        @Local(name = "hasAmbientOcclusion") boolean hasAmbientOcclusion,
        @Local(name = "particleMaterial") Material.Baked particleMaterial
    ) {
        if (model.wrapped() instanceof ObjModel) {
            cir.setReturnValue(new SimpleModelPart(geometry, hasAmbientOcclusion, particleMaterial));
        }
    }
}
