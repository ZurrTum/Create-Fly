package com.zurrtum.create.mixin;

import com.zurrtum.create.AllSynchedDatas;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.util.collection.Int2ObjectBiMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TrackedDataHandlerRegistry.class, priority = 999)
public class TrackedDataHandlerRegistryMixin {
    @Shadow
    @Final
    private static Int2ObjectBiMap<TrackedDataHandler<?>> DATA_HANDLERS;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void register(CallbackInfo ci) {
        AllSynchedDatas.HANDLERS.forEach(DATA_HANDLERS::add);
    }
}
