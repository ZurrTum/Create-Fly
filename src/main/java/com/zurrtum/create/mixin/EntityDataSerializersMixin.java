package com.zurrtum.create.mixin;

import com.zurrtum.create.AllSynchedDatas;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityDataSerializers.class)
public class EntityDataSerializersMixin {
    @Shadow
    @Final
    private static CrudeIncrementalIntIdentityHashBiMap<EntityDataSerializer<?>> SERIALIZERS;

    @Inject(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/syncher/EntityDataSerializers;registerSerializer(Lnet/minecraft/network/syncher/EntityDataSerializer;)V", ordinal = 0))
    private static void register(CallbackInfo ci) {
        AllSynchedDatas.HANDLERS.forEach(SERIALIZERS::add);
    }
}
