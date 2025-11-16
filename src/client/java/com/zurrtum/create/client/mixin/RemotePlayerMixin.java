package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.content.contraptions.ContraptionHandlerClient;
import net.minecraft.client.player.RemotePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RemotePlayer.class)
public class RemotePlayerMixin {
    @Inject(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/RemotePlayer;calculateEntityAnimation(Z)V"))
    private void tick(CallbackInfo ci) {
        ContraptionHandlerClient.preventRemotePlayersWalkingAnimations((RemotePlayer) (Object) this);
    }
}
