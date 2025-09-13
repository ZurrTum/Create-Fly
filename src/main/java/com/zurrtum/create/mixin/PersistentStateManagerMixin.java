package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.DataFixer;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PersistentStateManager.class)
class PersistentStateManagerMixin {
    @WrapOperation(method = "readNbt(Ljava/lang/String;Lnet/minecraft/datafixer/DataFixTypes;I)Lnet/minecraft/nbt/NbtCompound;", at = @At(value = "INVOKE", target = "Lnet/minecraft/datafixer/DataFixTypes;update(Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/nbt/NbtCompound;II)Lnet/minecraft/nbt/NbtCompound;"))
    private NbtCompound handleNullDataFixType(
        DataFixTypes dataFixTypes,
        DataFixer dataFixer,
        NbtCompound nbt,
        int oldVersion,
        int newVersion,
        Operation<NbtCompound> original
    ) {
        if (dataFixTypes == null) {
            return nbt;
        }

        return original.call(dataFixTypes, dataFixer, nbt, oldVersion, newVersion);
    }
}
