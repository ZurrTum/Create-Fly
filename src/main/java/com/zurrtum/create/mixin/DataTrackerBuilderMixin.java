package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.AllSynchedDatas.SynchedData;
import net.minecraft.entity.data.DataTracked;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.util.collection.Class2IntMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DataTracker.Builder.class)
public class DataTrackerBuilderMixin {
    @Shadow
    @Final
    private DataTracker.Entry<?>[] entries;

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/Class2IntMap;getNext(Ljava/lang/Class;)I"))
    private int getSize(Class2IntMap map, Class<?> type, Operation<Integer> original, @Share("data") LocalRef<SynchedData> data) {
        SynchedData synchedData = AllSynchedDatas.get(type);
        data.set(synchedData);
        return synchedData.preparse(map, original::call);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void addData(DataTracked entity, CallbackInfo ci, @Share("data") LocalRef<SynchedData> data) {
        data.get().register(entries);
    }
}
