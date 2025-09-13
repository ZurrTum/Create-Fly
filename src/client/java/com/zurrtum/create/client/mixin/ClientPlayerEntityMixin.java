package com.zurrtum.create.client.mixin;

import com.zurrtum.create.AllSynchedDatas;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @Inject(method = "isSubmergedInWater()Z", at = @At("HEAD"), cancellable = true)
    private void isSubmergedInWater(CallbackInfoReturnable<Boolean> cir) {
        if (AllSynchedDatas.HEAVY_BOOTS.get((ClientPlayerEntity) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}
