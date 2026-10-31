package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.client.content.trains.CameraDistanceModifier;
import com.zurrtum.create.client.flywheel.backend.engine.uniform.CameraInfoHolder;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {
    @WrapOperation(method = "alignWithEntity(F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(F)F"))
    private float getMaxZoom(Camera instance, float cameraDist, Operation<Float> original) {
        return original.call(instance, cameraDist) * CameraDistanceModifier.getMultiplier();
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lnet/minecraft/client/DeltaTracker;)V", at = @At("TAIL"))
    private void extractRenderState(CameraRenderState cameraState, DeltaTracker deltaTracker, CallbackInfo ci) {
        ((CameraInfoHolder) cameraState).flywheel$update((Camera) (Object) this);
    }
}
