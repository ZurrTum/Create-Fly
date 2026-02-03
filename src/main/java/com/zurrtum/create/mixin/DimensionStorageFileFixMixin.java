package com.zurrtum.create.mixin;

import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.filefix.FileFix;
import net.minecraft.util.filefix.fixes.DimensionStorageFileFix;
import net.minecraft.util.filefix.operations.FileFixOperations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DimensionStorageFileFix.class)
public abstract class DimensionStorageFileFixMixin extends FileFix {
    private DimensionStorageFileFixMixin(Schema schema) {
        super(schema);
    }

    @Inject(method = "makeFixer()V", at = @At("HEAD"))
    private void addFix(CallbackInfo ci) {
        addFileFixOperation(FileFixOperations.move("data/create_logistics.dat", "data/create/logistics.dat"));
        addFileFixOperation(FileFixOperations.move("data/create_tracks.dat", "data/create/tracks.dat"));
    }
}
