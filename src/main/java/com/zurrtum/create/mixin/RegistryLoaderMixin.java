package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.AllDynamicRegistries;
import net.minecraft.registry.RegistryLoader;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Iterator;
import java.util.List;

@Mixin(RegistryLoader.class)
public class RegistryLoaderMixin {
    @SuppressWarnings("SuspiciousSystemArraycopy")
    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Ljava/util/List;of([Ljava/lang/Object;)Ljava/util/List;"))
    private static <E> List<RegistryLoader.Entry<?>> addEntry(@NotNull E[] list, Operation<List<RegistryLoader.Entry<?>>> original) {
        int listSize = list.length;
        AllDynamicRegistries.registerIfNeeded();
        int size = listSize + AllDynamicRegistries.ALL.size();
        RegistryLoader.Entry<?>[] replaceList = new RegistryLoader.Entry<?>[size];
        System.arraycopy(list, 0, replaceList, 0, listSize);
        Iterator<RegistryLoader.Entry<?>> iterator = AllDynamicRegistries.ALL.iterator();
        for (int i = listSize; i < size; i++) {
            replaceList[i] = iterator.next();
        }
        return original.call((Object) replaceList);
    }
}
