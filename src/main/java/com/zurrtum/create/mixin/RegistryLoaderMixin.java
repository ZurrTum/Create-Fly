package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.AllDynamicRegistries;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Iterator;
import java.util.List;

import net.minecraft.resources.RegistryDataLoader;

@Mixin(RegistryDataLoader.class)
public class RegistryLoaderMixin {
    @SuppressWarnings("SuspiciousSystemArraycopy")
    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Ljava/util/List;of([Ljava/lang/Object;)Ljava/util/List;"))
    private static <E> List<RegistryDataLoader.RegistryData<?>> addEntry(
        @NotNull E[] list,
        Operation<List<RegistryDataLoader.RegistryData<?>>> original
    ) {
        int listSize = list.length;
        AllDynamicRegistries.registerIfNeeded();
        int size = listSize + AllDynamicRegistries.ALL.size();
        RegistryDataLoader.RegistryData<?>[] replaceList = new RegistryDataLoader.RegistryData<?>[size];
        System.arraycopy(list, 0, replaceList, 0, listSize);
        Iterator<RegistryDataLoader.RegistryData<?>> iterator = AllDynamicRegistries.ALL.iterator();
        for (int i = listSize; i < size; i++) {
            replaceList[i] = iterator.next();
        }
        return original.call((Object) replaceList);
    }
}
