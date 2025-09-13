package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.content.contraptions.minecart.capability.CapabilityMinecartController;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(ServerChunkLoadingManager.class)
public class ServerChunkLoadingManagerMixin {
    @Inject(method = "method_60440(Lnet/minecraft/server/world/ChunkHolder;Ljava/util/concurrent/CompletableFuture;J)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;setLoadedToWorld(Z)V", shift = At.Shift.AFTER))
    private void tryUnloadChunk(ChunkHolder chunkHolder, CompletableFuture<?> completableFuture, long l, CallbackInfo ci, @Local WorldChunk chunk) {
        CapabilityMinecartController.onChunkUnloaded(chunk);
    }
}
