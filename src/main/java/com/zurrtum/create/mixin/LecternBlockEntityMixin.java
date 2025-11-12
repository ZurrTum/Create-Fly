package com.zurrtum.create.mixin;

import com.zurrtum.create.api.behaviour.display.DisplayHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LecternBlockEntity.class)
public class LecternBlockEntityMixin implements DisplayHolder {
    @Unique
    private CompoundTag displayLink;

    @Override
    public CompoundTag getDisplayLinkData() {
        return displayLink;
    }

    @Override
    public void setDisplayLinkData(CompoundTag data) {
        displayLink = data;
    }

    @Inject(method = "saveAdditional(Lnet/minecraft/world/level/storage/ValueOutput;)V", at = @At("TAIL"))
    private void writeData(ValueOutput view, CallbackInfo ci) {
        writeDisplayLink(view);
    }

    @Inject(method = "loadAdditional(Lnet/minecraft/world/level/storage/ValueInput;)V", at = @At("TAIL"))
    private void readData(ValueInput view, CallbackInfo ci) {
        readDisplayLink(view);
    }
}
