package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.AllSynchedDatas;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import net.minecraft.network.syncher.SyncedDataHolder;
import net.minecraft.network.syncher.SynchedEntityData;

@Mixin(SynchedEntityData.class)
public class DataTrackerMixin {
    @Final
    @Shadow
    private SyncedDataHolder entity;

    @Inject(method = "assignValues(Ljava/util/List;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/syncher/SyncedDataHolder;onSyncedDataUpdated(Lnet/minecraft/network/syncher/EntityDataAccessor;)V"))
    private void onTrackedDataSet(
        List<SynchedEntityData.DataValue<?>> entries,
        CallbackInfo ci,
        @Local SynchedEntityData.DataValue<?> serializedEntry
    ) {
        AllSynchedDatas.onData(entity, serializedEntry);
    }
}
