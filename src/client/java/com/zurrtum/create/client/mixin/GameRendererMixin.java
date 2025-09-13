package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.content.trains.track.TrackBlockOutline;
import com.zurrtum.create.client.foundation.block.BigOutlines;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "updateCrosshairTarget(F)V", at = @At("TAIL"))
    private void bigShapePick(CallbackInfo ci) {
        BigOutlines.pick(client);
        TrackBlockOutline.pickCurves(client);
    }
}
