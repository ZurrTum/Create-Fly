package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.foundation.render.PlayerSkyhookRenderer;
import com.zurrtum.create.client.foundation.render.SkyhookRenderState;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityModel.class)
public class PlayerEntityModelMixin {
    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)V", at = @At("TAIL"))
    private void afterSetupAnim(PlayerEntityRenderState state, CallbackInfo ci) {
        SkyhookRenderState skyhookRenderState = (SkyhookRenderState) state;
        PlayerSkyhookRenderer.afterSetupAnim(
            skyhookRenderState.create$getUuid(),
            state.mainArm,
            skyhookRenderState.create$getMainStack(),
            (PlayerEntityModel) (Object) this
        );
    }
}
