package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.client.infrastructure.particle.CubeParticleRenderer;
import com.zurrtum.create.client.infrastructure.particle.SteamJetParticleRenderer;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleRenderer;
import net.minecraft.client.particle.ParticleTextureSheet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {
    @Shadow
    @Final
    private Map<ParticleTextureSheet, ParticleRenderer<?>> particles;

    @Inject(method = "clearParticles()V", at = @At("TAIL"))
    private void addRenderer(CallbackInfo ci) {
        ParticleManager particleManager = (ParticleManager) (Object) this;
        particles.put(SteamJetParticleRenderer.SHEET, new SteamJetParticleRenderer(particleManager));
        particles.put(CubeParticleRenderer.SHEET, new CubeParticleRenderer(particleManager));
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
