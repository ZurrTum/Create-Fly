package com.zurrtum.create.mixin;

import com.mojang.datafixers.DataFixerBuilder;
import com.zurrtum.create.datafix.schemas.V1_21_8;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.filefix.FileFixerUpper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DataFixers.class)
public class DataFixersMixin {
    @Inject(
        method = "addFixers(Lcom/mojang/datafixers/DataFixerBuilder;Lnet/minecraft/util/filefix/FileFixerUpper$Builder;)V",
        at = @At(value = "CONSTANT", args = "intValue=4531"))
    private static void init(DataFixerBuilder fixerUpper, FileFixerUpper.Builder fileFixerUpper, CallbackInfo ci) {
        V1_21_8.register(fixerUpper);
    }
}
