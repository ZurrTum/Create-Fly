package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.content.contraptions.minecart.capability.CapabilityMinecartController;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkMap.class)
public class ChunkMapMixin {
    @Inject(method = "lambda$scheduleUnload$12(Lnet/minecraft/server/level/ChunkHolder;Ljava/util/concurrent/CompletableFuture;J)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;setLoaded(Z)V", shift = At.Shift.AFTER))
    private void tryUnloadChunk(ChunkHolder chunkHolder, CompletableFuture<?> completableFuture, long l, CallbackInfo ci, @Local LevelChunk chunk) {
        CapabilityMinecartController.onChunkUnloaded(chunk);
    }
}
