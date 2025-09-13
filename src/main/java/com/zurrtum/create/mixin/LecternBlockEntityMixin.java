package com.zurrtum.create.mixin;

import com.zurrtum.create.api.behaviour.display.DisplayHolder;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LecternBlockEntity.class)
public class LecternBlockEntityMixin implements DisplayHolder {
    @Unique
    private NbtCompound displayLink;

    @Override
    public NbtCompound getDisplayLinkData() {
        return displayLink;
    }

    @Override
    public void setDisplayLinkData(NbtCompound data) {
        displayLink = data;
    }

    @Inject(method = "writeData(Lnet/minecraft/storage/WriteView;)V", at = @At("TAIL"))
    private void writeData(WriteView view, CallbackInfo ci) {
        writeDisplayLink(view);
    }

    @Inject(method = "readData(Lnet/minecraft/storage/ReadView;)V", at = @At("TAIL"))
    private void readData(ReadView view, CallbackInfo ci) {
        readDisplayLink(view);
    }
}
