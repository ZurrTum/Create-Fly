package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.AllSynchedDatas;
import net.minecraft.entity.data.DataTracked;
import net.minecraft.entity.data.DataTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(DataTracker.class)
public class DataTrackerMixin {
    @Final
    @Shadow
    private DataTracked trackedEntity;

    @Inject(method = "writeUpdatedEntries(Ljava/util/List;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/data/DataTracked;onTrackedDataSet(Lnet/minecraft/entity/data/TrackedData;)V"))
    private void onTrackedDataSet(
        List<DataTracker.SerializedEntry<?>> entries,
        CallbackInfo ci,
        @Local DataTracker.SerializedEntry<?> serializedEntry
    ) {
        AllSynchedDatas.onData(trackedEntity, serializedEntry);
    }
}
