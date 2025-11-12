package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.DataFixer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DimensionDataStorage.class)
class PersistentStateManagerMixin {
    @WrapOperation(method = "readTagFromDisk(Ljava/lang/String;Lnet/minecraft/util/datafix/DataFixTypes;I)Lnet/minecraft/nbt/CompoundTag;", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/datafix/DataFixTypes;update(Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/nbt/CompoundTag;II)Lnet/minecraft/nbt/CompoundTag;"))
    private CompoundTag handleNullDataFixType(
        DataFixTypes dataFixTypes,
        DataFixer dataFixer,
        CompoundTag nbt,
        int oldVersion,
        int newVersion,
        Operation<CompoundTag> original
    ) {
        if (dataFixTypes == null) {
            return nbt;
        }

        return original.call(dataFixTypes, dataFixer, nbt, oldVersion, newVersion);
    }
}
