package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.client.infrastructure.particle.CubeParticleRenderer;
import com.zurrtum.create.client.infrastructure.particle.SteamJetParticleRenderer;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleRenderer;
import net.minecraft.client.particle.ParticleTextureSheet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {
    @Inject(method = "createParticleRenderer(Lnet/minecraft/client/particle/ParticleTextureSheet;)Lnet/minecraft/client/particle/ParticleRenderer;", at = @At("TAIL"), cancellable = true)
    private void createParticleRenderer(ParticleTextureSheet textureSheet, CallbackInfoReturnable<ParticleRenderer<?>> cir) {
        if (textureSheet == SteamJetParticleRenderer.SHEET) {
            cir.setReturnValue(new SteamJetParticleRenderer((ParticleManager) (Object) this));
        } else if (textureSheet == CubeParticleRenderer.SHEET) {
            cir.setReturnValue(new CubeParticleRenderer((ParticleManager) (Object) this));
        }
    }

    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Ljava/util/List;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;", remap = false))
    private static <E> List<ParticleTextureSheet> of(E e1, E e2, E e3, Operation<List<ParticleTextureSheet>> original) {
        List<ParticleTextureSheet> list = original.call(e1, e2, e3);
        if (!(list instanceof ArrayList<ParticleTextureSheet>)) {
            list = new ArrayList<>(list);
        }
        list.add(SteamJetParticleRenderer.SHEET);
        list.add(CubeParticleRenderer.SHEET);
        return list;
    }
}
