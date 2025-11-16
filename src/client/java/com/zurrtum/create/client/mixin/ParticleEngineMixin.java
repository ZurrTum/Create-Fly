package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.client.infrastructure.particle.CubeParticleRenderer;
import com.zurrtum.create.client.infrastructure.particle.SteamJetParticleRenderer;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
    @Inject(method = "createParticleGroup(Lnet/minecraft/client/particle/ParticleRenderType;)Lnet/minecraft/client/particle/ParticleGroup;", at = @At("TAIL"), cancellable = true)
    private void createParticleRenderer(ParticleRenderType textureSheet, CallbackInfoReturnable<ParticleGroup<?>> cir) {
        if (textureSheet == SteamJetParticleRenderer.SHEET) {
            cir.setReturnValue(new SteamJetParticleRenderer((ParticleEngine) (Object) this));
        } else if (textureSheet == CubeParticleRenderer.SHEET) {
            cir.setReturnValue(new CubeParticleRenderer((ParticleEngine) (Object) this));
        }
    }

    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Ljava/util/List;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;", remap = false))
    private static <E> List<ParticleRenderType> of(E e1, E e2, E e3, Operation<List<ParticleRenderType>> original) {
        List<ParticleRenderType> list = original.call(e1, e2, e3);
        if (!(list instanceof ArrayList<ParticleRenderType>)) {
            list = new ArrayList<>(list);
        }
        list.add(SteamJetParticleRenderer.SHEET);
        list.add(CubeParticleRenderer.SHEET);
        return list;
    }
}
