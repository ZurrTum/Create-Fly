package com.zurrtum.create.mixin;

import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.content.contraptions.minecart.capability.MinecartController;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(AbstractMinecartEntity.class)
public class AbstractMinecartEntityMixin {
    @Inject(method = "readCustomData(Lnet/minecraft/storage/ReadView;)V", at = @At("TAIL"))
    private void readCustomData(ReadView input, CallbackInfo ci) {
        input.read("create:minecart_controller", MinecartController.CODEC).ifPresent(controller -> {
            AbstractMinecartEntity minecart = (AbstractMinecartEntity) (Object) this;
            controller.setCart(minecart);
            AllSynchedDatas.MINECART_CONTROLLER.set(minecart, Optional.of(controller));
        });
    }

    @Inject(method = "writeCustomData(Lnet/minecraft/storage/WriteView;)V", at = @At("TAIL"))
    private void writeCustomData(WriteView output, CallbackInfo ci) {
        AllSynchedDatas.MINECART_CONTROLLER.get((AbstractMinecartEntity) (Object) this).ifPresent(controller -> {
            output.put("create:minecart_controller", MinecartController.CODEC, controller);
        });
    }
}
