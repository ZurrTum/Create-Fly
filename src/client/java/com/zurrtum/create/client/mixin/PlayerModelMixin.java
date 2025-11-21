package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.foundation.render.PlayerSkyhookRenderer;
import com.zurrtum.create.client.foundation.render.SkyhookRenderState;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public class PlayerModelMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"))
    private void afterSetupAnim(AvatarRenderState state, CallbackInfo ci) {
        SkyhookRenderState skyhookRenderState = (SkyhookRenderState) state;
        PlayerSkyhookRenderer.afterSetupAnim(
            skyhookRenderState.create$getUuid(),
            state.mainArm,
            skyhookRenderState.create$getMainStack(),
            (PlayerModel) (Object) this
        );
    }
}
